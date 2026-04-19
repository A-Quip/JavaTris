package com.aquip.tetris.eval;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.engine.GameFactory;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.placement.PlacementResult;
import com.aquip.tetris.placement.SpinResult;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Runs a single EvalScenario against the AI and returns an EvalResult with full
 * metrics.
 */
public class EvalRunner {

    private final ConfigState config;
    private final AIConfig aiConfig;

    /**
     * Milliseconds to sleep after each tick.
     * Must be ≥ the time a BeamSearch takes / lockTick to guarantee the AI acts.
     * Default 16 ms matches the game's 60 fps timer, giving 720 ms per lock cycle.
     */
    private int tickDelayMs = 22;

    public EvalRunner(ConfigState config, AIConfig aiConfig) {
        this.config = config;
        this.aiConfig = aiConfig;
    }

    /**
     * Overrides the per-tick sleep. Use 0 for maximum speed (AI will mostly miss
     * its window).
     */
    public EvalRunner withTickDelay(int ms) {
        this.tickDelayMs = Math.max(0, ms);
        return this;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    public EvalResult run(EvalScenario scenario, Consumer<MatchState> renderCallback) {

        GameEngine engine = buildEngine(scenario);
        PlayerState ps = engine.state.players.get(0);

        injectBoard(scenario, ps);
        injectQueue(scenario, ps);

        // Briefly pause at the start so the user can see the initial board state
        if (renderCallback != null) {
            renderCallback.accept(engine.state);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        EvalResult.Builder result = new EvalResult.Builder(scenario.name);

        InputFrame emptyFrame = new InputFrame();

        int previousPiecesPlaced = 0;
        int previousScore = 0;

        // ---- main eval loop -------------------------------------------------
        for (int tick = 0; tick < maxTicks(scenario); tick++) {

            engine.tick(emptyFrame);
            result.totalTicks = tick + 1;

            // Sleep so the background AI thread has time to finish its search
            // before the lock timer expires on the current piece.
            if (tickDelayMs > 0) {
                try {
                    Thread.sleep(tickDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (renderCallback != null) {
                renderCallback.accept(engine.state);
            }

            // -- detect piece placement (list grew) ---------------------------
            int currentPieces = ps.time.piecesPlaced.size();
            if (currentPieces > previousPiecesPlaced) {

                for (int i = previousPiecesPlaced; i < currentPieces; i++) {
                    PlacementResult placement = ps.time.piecesPlaced.get(i);
                    recordPlacement(result, placement, ps);
                }

                int scoreDelta = ps.status.Score - previousScore;
                result.timeline.add(new EvalResult.PieceMoment(
                        currentPieces,
                        countLinesInRange(ps, previousPiecesPlaced, currentPieces),
                        scoreDelta,
                        ps.combo.amount(),
                        ps.b2b.amount()));

                previousPiecesPlaced = currentPieces;
                previousScore = ps.status.Score;
            }

            // -- termination checks -------------------------------------------
            if (!ps.status.alive) {
                result.termination = EvalResult.TerminationReason.GAME_OVER;
                break;
            }

            if (scenario.maxPieces > 0 && currentPieces >= scenario.maxPieces) {
                result.termination = EvalResult.TerminationReason.COMPLETED;
                break;
            }
        }

        // timeout if we exhausted ticks without another exit
        if (result.termination == EvalResult.TerminationReason.COMPLETED
                && result.totalTicks >= maxTicks(scenario)
                && scenario.maxPieces > 0
                && ps.time.piecesPlaced.size() < scenario.maxPieces) {
            result.termination = EvalResult.TerminationReason.TIMEOUT;
        }

        // -- final metrics ----------------------------------------------------
        result.totalScore = ps.status.Score;
        result.piecesPlaced = ps.time.piecesPlaced.size();
        result.totalLinesCleared = countAllLines(ps);
        result.passed = evaluatePass(scenario, result);

        return result.build();
    }

    // =========================================================================
    // Engine construction
    // =========================================================================

    private GameEngine buildEngine(EvalScenario scenario) {
        Player aiPlayer = new Player(UUID.randomUUID(), PlayerType.AI, "AI");
        return GameFactory.createGame(List.of(aiPlayer), config, aiConfig);
    }

    // =========================================================================
    // State injection
    // =========================================================================

    /**
     * Writes the scenario's board pattern into the player's live board.
     * Bottom-aligned: pattern[last] becomes the bottom row of the board.
     */
    private void injectBoard(EvalScenario scenario, PlayerState ps) {
        if (scenario.boardPattern == null || scenario.boardPattern.length == 0)
            return;

        String[] pattern = scenario.boardPattern;
        int boardH = ps.board.getHeight();
        int boardW = ps.board.getWidth();

        // Clear first
        for (int y = 0; y < boardH; y++)
            for (int x = 0; x < boardW; x++)
                ps.board.set(x, y, 0);

        // Inject bottom-aligned: pattern[0] = top of pattern, pattern[last] = bottom
        // row
        int patternH = pattern.length;
        int startY = boardH - patternH;

        for (int row = 0; row < patternH; row++) {
            String line = pattern[row];
            int boardY = startY + row;
            for (int x = 0; x < Math.min(line.length(), boardW); x++) {
                if (boardY >= 0 && boardY < boardH) {
                    ps.board.set(x, boardY, line.charAt(x) == '#' ? 1 : 0);
                }
            }
        }
    }

    /**
     * Prepends forced pieces to the front of the player's queue,
     * keeping the existing bag tail so the bag doesn't repeat.
     */
    private void injectQueue(EvalScenario scenario, PlayerState ps) {
        if (scenario.forcedQueue != null && scenario.forcedQueue.length > 0) {
            java.util.Queue<PieceType> newQueue = new java.util.LinkedList<>();
            for (PieceType t : scenario.forcedQueue)
                newQueue.add(t);
            newQueue.addAll(ps.next.next);
            ps.next.next = newQueue;
        }

        if (scenario.initialHeld != null) {
            ps.next.held = scenario.initialHeld;
            ps.next.canHold = false;
        }
    }

    // =========================================================================
    // Metric helpers
    // =========================================================================

    private void recordPlacement(EvalResult.Builder result,
            PlacementResult placement,
            PlayerState ps) {
        if (placement == null)
            return;

        result.totalLinesCleared += placement.lines;
        result.maxCombo = Math.max(result.maxCombo, ps.combo.amount());
        result.maxB2B = Math.max(result.maxB2B, ps.b2b.amount());

        boolean isSpin = placement.getPlaceKey().spin != SpinResult.NONE;

        switch (placement.lines) {
            case 1 -> result.singles++;
            case 2 -> result.doubles++;
            case 3 -> result.triples++;
            case 4 -> result.tetrises++;
        }

        if (isSpin && placement.lines > 0)
            result.tSpins++;

        if (placement.lines > 0 && isBoardEmpty(ps))
            result.perfectClears++;

        result.totalAttack += estimateAttack(placement, ps.b2b.amount() > 1);
    }

    private int estimateAttack(PlacementResult p, boolean b2b) {
        boolean isSpin = p.getPlaceKey().spin != SpinResult.NONE;
        int base = switch (p.lines) {
            case 1 -> isSpin ? 2 : 0;
            case 2 -> isSpin ? 4 : 1;
            case 3 -> isSpin ? 6 : 2;
            case 4 -> 4;
            default -> 0;
        };
        if (b2b && (p.lines == 4 || isSpin))
            base++;
        return base;
    }

    private boolean isBoardEmpty(PlayerState ps) {
        for (int y = 0; y < ps.board.getHeight(); y++)
            for (int x = 0; x < ps.board.getWidth(); x++)
                if (ps.board.get(x, y) != 0)
                    return false;
        return true;
    }

    private int countLinesInRange(PlayerState ps, int from, int to) {
        int lines = 0;
        for (int i = from; i < Math.min(to, ps.time.piecesPlaced.size()); i++) {
            PlacementResult pr = ps.time.piecesPlaced.get(i);
            if (pr != null)
                lines += pr.lines;
        }
        return lines;
    }

    private int countAllLines(PlayerState ps) {
        int lines = 0;
        for (PlacementResult pr : ps.time.piecesPlaced) {
            if (pr != null)
                lines += pr.lines;
        }
        return lines;
    }

    private boolean evaluatePass(EvalScenario scenario, EvalResult.Builder result) {
        if (result.termination == EvalResult.TerminationReason.GAME_OVER)
            return false;
        if (result.termination == EvalResult.TerminationReason.TIMEOUT)
            return false;
        if (result.totalLinesCleared < scenario.requiredLines)
            return false;
        if (scenario.requirePerfectClear && result.perfectClears == 0)
            return false;
        return true;
    }

    private int maxTicks(EvalScenario scenario) {
        return scenario.maxTicks > 0 ? scenario.maxTicks : Integer.MAX_VALUE;
    }
}