package com.aquip.tetris.input;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.ai.AIGameSnapshot;
import com.aquip.tetris.ai.AIThread;
import com.aquip.tetris.ai.search.MoveSequence;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.PlayerState;

import java.util.HashSet;
import java.util.concurrent.Future;

/**
 * Async state machine consumer that implements PlayerInputSource.
 * It periodically polls the game state, triggers searches via AIThread,
 * and feeds the resulting input sequence back to the engine.
 */
public class AIInputSource implements PlayerInputSource {

    private enum Phase {
        IDLE, SEARCHING, CONSUMING
    }

    private final Player player;
    private final AIThread aiThread;
    private final AIConfig config;

    // State
    private Phase phase = Phase.IDLE;
    private int lastKnownPiecesPlaced = -1;
    private Future<MoveSequence> pendingResult = null;
    private MoveSequence activeSequence = null;
    private int sequenceIndex = 0;
    private boolean holdPending = false;
    private int ticksSinceLastCommand = 0;
    private int consecutiveStaleResults = 0;
    private long searchStartedAtMs = 0;

    // Expected piece state at each command index — for Drift Guard.
    // sequenceExpected[i] = the (x, y, rotation) the piece should be at
    // BEFORE command i is emitted. Populated when a sequence is loaded.
    private Piece[] sequenceExpected = null;

    public AIInputSource(Player player, AIThread aiThread, AIConfig config) {
        this.player = player;
        this.aiThread = aiThread;
        this.config = config;
    }

    @Override
    public PlayerInput poll(InputFrame frame) {

        // Resolve PlayerState from MatchState
        PlayerState state = frame.state.getPlayerState(player);

        if (state == null || !state.status.alive)
            return emptyInput(player);

        int currentPieces = state.time.piecesPlaced.size();
        Piece currentPiece = state.piece.currentPiece;
        ticksSinceLastCommand++;

        // 1. HOLD PENDING — piece just changed due to a hold we issued.
        // Skip all other checks; start a fresh search from new piece.
        if (holdPending) {
            triggerNewSearch(state);
            holdPending = false;
            return emptyInput(player);
        }

        // 2. STALE CHECK — piece count changed; a new piece has spawned.
        // Primary replanning trigger.
        if (currentPieces != lastKnownPiecesPlaced && currentPiece != null) {
            lastKnownPiecesPlaced = currentPieces;

            if (phase == Phase.SEARCHING) {
                consecutiveStaleResults++;
                if (consecutiveStaleResults >= 2) {
                    consecutiveStaleResults = 0;
                    phase = Phase.IDLE;
                    return singleInput(player, GameInput.HARD_DROP); // Emergency drop
                }
            } else {
                consecutiveStaleResults = 0;
            }

            triggerNewSearch(state);
            return emptyInput(player);
        }

        // 3. COLLECT SEARCH RESULT (non-blocking)
        if (phase == Phase.SEARCHING && pendingResult != null && pendingResult.isDone()) {
            try {
                MoveSequence result = pendingResult.get();

                if (result != null && (result.piecesPlacedWhenGenerated == currentPieces
                        || result.piecesPlacedWhenGenerated == -1)) {
                    // Fresh result or emergency sentinel — load it
                    consecutiveStaleResults = 0;
                    activeSequence = result;
                    sequenceExpected = result.expectedPositions;
                    sequenceIndex = 0;
                    phase = Phase.CONSUMING;

                } else {
                    // Arrived stale
                    consecutiveStaleResults++;
                    if (consecutiveStaleResults >= 2) {
                        // Emergency: piece is being lost to gravity; hard drop immediately
                        consecutiveStaleResults = 0;
                        phase = Phase.IDLE;
                        return singleInput(player, GameInput.HARD_DROP);
                    }
                    triggerNewSearch(state);
                }
            } catch (Exception e) {
                System.err.println("[AI] Error in search thread:");
                e.printStackTrace();
                triggerNewSearch(state);
            }
            pendingResult = null;
        }

        // 4. DECISION DELAY GATE (artificial lag for difficulty)
        if (phase == Phase.CONSUMING) {
            long elapsed = System.currentTimeMillis() - searchStartedAtMs;
            if (elapsed < config.decisionDelayMs())
                return emptyInput(player);
        }

        // 5. DRIFT GUARD
        // Fires only if the engine piece is in a state that does NOT
        // exist in the current planned sequence at the current index.
        if (phase == Phase.CONSUMING && currentPiece != null
                && sequenceExpected != null
                && sequenceIndex < sequenceExpected.length) {

            Piece expected = sequenceExpected[sequenceIndex];
            if (!positionMatches(currentPiece, expected)) {
                triggerNewSearch(state);
                return emptyInput(player);
            }
        }

        // 6. PACING GATE
        if (phase != Phase.CONSUMING)
            return emptyInput(player);
        if (ticksSinceLastCommand < config.ticksPerCommand())
            return emptyInput(player);

        // 7. EMIT NEXT COMMAND(S)
        if (activeSequence == null || sequenceIndex >= activeSequence.commands.size()) {
            phase = Phase.IDLE;
            return emptyInput(player);
        }

        GameInput next = activeSequence.commands.get(sequenceIndex);

        // HOLD EXCEPTION: emit hold alone; set flag to skip drift on next tick
        if (next == GameInput.HOLD_PIECE) {
            sequenceIndex++;
            holdPending = true;
            ticksSinceLastCommand = 0;
            return singleInput(player, GameInput.HOLD_PIECE);
        }

        // ATOMIC HARD DROP EXCEPTION: if a rotation is immediately followed by
        // HARD_DROP, emit both in one PlayerInput on the same tick
        if (isRotation(next)
                && sequenceIndex + 1 < activeSequence.commands.size()
                && activeSequence.commands.get(sequenceIndex + 1) == GameInput.HARD_DROP) {

            GameInput hardDrop = activeSequence.commands.get(sequenceIndex + 1);
            sequenceIndex += 2;
            ticksSinceLastCommand = 0;
            return batchInput(player, next, hardDrop);
        }

        if (next == GameInput.NONE) {
            sequenceIndex++;
            ticksSinceLastCommand = 0;
            return emptyInput(player);
        }

        sequenceIndex++;
        ticksSinceLastCommand = 0;
        return singleInput(player, next);
    }

    private void triggerNewSearch(PlayerState state) {
        phase = Phase.SEARCHING;
        activeSequence = null;
        sequenceExpected = null;
        sequenceIndex = 0;
        searchStartedAtMs = System.currentTimeMillis();

        AIGameSnapshot snapshot = AIGameSnapshot.from(state);
        pendingResult = aiThread.submit(snapshot);
    }

    private boolean positionMatches(Piece actual, Piece expected) {
        return actual.x == expected.x
                && actual.y >= expected.y
                && actual.rotation == expected.rotation;
    }

    private boolean isRotation(GameInput input) {
        return input == GameInput.ROTATE_CW
                || input == GameInput.ROTATE_CCW
                || input == GameInput.ROTATE_180;
    }

    private PlayerInput emptyInput(Player player) {
        PlayerInput input = new PlayerInput();
        input.player = player;
        input.inputs = new HashSet<>();
        return input;
    }

    private PlayerInput singleInput(Player player, GameInput command) {
        PlayerInput input = new PlayerInput();
        input.player = player;
        input.inputs = new HashSet<>();
        input.inputs.add(command);
        return input;
    }

    private PlayerInput batchInput(Player player, GameInput... commands) {
        PlayerInput input = new PlayerInput();
        input.player = player;
        input.inputs = new HashSet<>();
        if (commands != null) {
            for (GameInput command : commands) {
                input.inputs.add(command);
            }
        }
        return input;
    }
}
