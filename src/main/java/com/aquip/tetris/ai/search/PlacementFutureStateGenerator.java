package com.aquip.tetris.ai.search;

import com.aquip.tetris.garbage.GarbageQueue;
import com.aquip.tetris.garbage.GarbageSpike;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.placement.PlaceKey;
import com.aquip.tetris.placement.PlacementResult;
import com.aquip.tetris.placement.SpinResult;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.B2BState;
import com.aquip.tetris.state.BoardState;
import com.aquip.tetris.state.ComboState;
import com.aquip.tetris.state.GarbageState;
import com.aquip.tetris.state.GravityState;
import com.aquip.tetris.state.LockState;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PieceState;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.state.QueueState;
import com.aquip.tetris.state.StatusState;
import com.aquip.tetris.state.TimeState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;

public class PlacementFutureStateGenerator implements FutureStateGenerator {

    private final Player player;
    private final PieceRegistry registry = PieceRegistry.getInstance();

    public PlacementFutureStateGenerator(Player player) {
        this.player = player;
    }

    @Override
    public List<MatchState> generate(MatchState current) {
        PlayerState playerState = current.getPlayerState(player);
        if (playerState == null || !playerState.status.alive || !playerState.piece.hasPiece()) {
            return Collections.emptyList();
        }

        List<MatchState> futures = new ArrayList<>();
        futures.addAll(generateFutures(current, playerState, playerState.piece.currentPiece, Collections.emptyList(), false));

        if (playerState.next.canHold) {
            Piece holdPiece = createHeldSpawn(playerState);
            if (holdPiece != null && !collides(playerState.board.board, holdPiece)) {
                futures.addAll(generateFutures(current, playerState, holdPiece, List.of(GameInput.HOLD_PIECE), true));
            }
        }

        return futures;
    }

    private List<MatchState> generateFutures(MatchState current,
                                             PlayerState sourceState,
                                             Piece startPiece,
                                             List<GameInput> prefix,
                                             boolean usedHold) {
        Map<String, PlacementCandidate> candidates = collectCandidates(sourceState.board.board, startPiece);
        List<MatchState> futures = new ArrayList<>(candidates.size());

        for (PlacementCandidate candidate : candidates.values()) {
            futures.add(simulateFuture(current, candidate, prefix, usedHold));
        }

        return futures;
    }

    private MatchState simulateFuture(MatchState current,
                                      PlacementCandidate candidate,
                                      List<GameInput> prefix,
                                      boolean usedHold) {
        MatchState simulatedMatch = copyMatchState(current);
        PlayerState simulatedPlayer = simulatedMatch.getPlayerState(player);
        PlayerState sourcePlayer = current.getPlayerState(player);

        PlacementResult placement = placeCandidate(simulatedPlayer, candidate.finalPiece);
        updateCombo(simulatedPlayer, placement);
        updateBackToBack(simulatedPlayer, placement);
        updateScore(simulatedPlayer, placement);
        int garbageSent = resolveGarbage(simulatedMatch, simulatedPlayer, placement);

        simulateQueueAfterPlacement(simulatedPlayer, sourcePlayer, usedHold);
        applyPendingGarbage(simulatedPlayer);
        resolveDeaths(simulatedPlayer, simulatedMatch.tick + 1);

        Queue<GameInput> plannedInputs = new ArrayDeque<>(prefix.size() + candidate.inputs.size() + 1);
        plannedInputs.addAll(prefix);
        plannedInputs.addAll(candidate.inputs);
        plannedInputs.add(GameInput.HARD_DROP);

        simulatedMatch.tick++;

        return new PlannedMatchState(
                simulatedMatch.tick,
                simulatedMatch.seed,
                simulatedMatch.players,
                player.getId(),
                plannedInputs,
                placement.lines,
                garbageSent,
                usedHold
        );
    }

    private PlacementResult placeCandidate(PlayerState playerState, Piece piece) {
        int[][] board = playerState.board.board;
        int[][] shape = registry.getRotation(piece.type, piece.rotation);
        int pieceValue = piece.type.ordinal() + 1;

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] == 0) {
                    continue;
                }

                int bx = piece.x + x;
                int by = piece.y + y;

                if (by >= 0 && by < board.length && bx >= 0 && bx < board[0].length) {
                    board[by][bx] = pieceValue;
                }
            }
        }

        int linesCleared = clearLines(board);

        PlacementResult placement = new PlacementResult(
                piece.type,
                SpinResult.NONE,
                piece.x,
                piece.y,
                piece.rotation,
                linesCleared
        );

        playerState.time.tick++;
        playerState.time.piecesPlaced.add(placement);
        playerState.piece.currentPiece = null;
        playerState.next.canHold = true;

        return placement;
    }

    private void updateCombo(PlayerState playerState, PlacementResult placement) {
        if (placement.lines > 0) {
            playerState.combo.currentCombo.add(placement);
        } else {
            playerState.combo.currentCombo.clear();
        }
    }

    private void updateBackToBack(PlayerState playerState, PlacementResult placement) {
        PlaceKey key = placement.getPlaceKey();
        if (key.spin != SpinResult.NONE || key.lines == 4) {
            playerState.b2b.currentB2B.add(placement);
        } else if (placement.lines > 0) {
            playerState.b2b.currentB2B.clear();
        }
    }

    private void updateScore(PlayerState playerState, PlacementResult placement) {
        int baseScore = playerState.config.scoreTable == null
                ? placement.lines * 100
                : playerState.config.scoreTable.get(placement.getPlaceKey());
        int comboBonus = Math.max(0, playerState.combo.amount() - 1) * 50;
        double b2bMultiplier = playerState.b2b.amount() > 1 ? 1.5 : 1.0;
        int total = (int) ((baseScore + comboBonus) * b2bMultiplier * playerState.config.scoreMult);
        playerState.status.Score += total;
    }

    private int resolveGarbage(MatchState match, PlayerState attacker, PlacementResult placement) {
        if (match.players.size() < 2) {
            return 0;
        }

        int garbage = attacker.config.garbageTable == null
                ? Math.max(0, placement.lines - 1)
                : attacker.config.garbageTable.get(placement.getPlaceKey());
        garbage += Math.max(0, attacker.combo.amount() - 1);
        if (attacker.b2b.amount() > 1) {
            garbage += 1;
        }

        if (garbage <= 0) {
            return 0;
        }

        int remaining = garbage - attacker.garbage.incoming.poll(garbage);
        if (remaining <= 0) {
            return 0;
        }

        PlayerState target = findTarget(match, attacker);
        if (target == null) {
            return 0;
        }

        target.garbage.incoming.add(remaining);
        return remaining;
    }

    private PlayerState findTarget(MatchState match, PlayerState attacker) {
        for (PlayerState candidate : match.players) {
            if (candidate == attacker || !candidate.status.alive) {
                continue;
            }
            return candidate;
        }

        return null;
    }

    private void simulateQueueAfterPlacement(PlayerState simulatedPlayer,
                                             PlayerState sourcePlayer,
                                             boolean usedHold) {
        LinkedList<PieceType> nextQueue = new LinkedList<>(simulatedPlayer.next.next);
        PieceType held = simulatedPlayer.next.held;

        if (usedHold) {
            held = sourcePlayer.piece.currentPiece.type;
            if (sourcePlayer.next.held == null && !nextQueue.isEmpty()) {
                nextQueue.poll();
            }
        }

        simulatedPlayer.next.held = held;
        simulatedPlayer.next.next.clear();

        PieceType nextType = nextQueue.poll();
        simulatedPlayer.next.next.addAll(nextQueue);
        simulatedPlayer.piece.currentPiece = nextType == null ? null : createSpawnPiece(simulatedPlayer, nextType);
        simulatedPlayer.next.canHold = true;
        simulatedPlayer.lock.reset();
        simulatedPlayer.gravity.reset();
    }

    private void applyPendingGarbage(PlayerState playerState) {
        int lines = playerState.garbage.incoming.pollAllReady();
        if (lines <= 0) {
            return;
        }

        int[][] board = playerState.board.board;
        int width = board[0].length;
        int height = board.length;

        for (int i = 0; i < lines; i++) {
            for (int y = 0; y < height - 1; y++) {
                board[y] = board[y + 1];
            }

            int[] row = new int[width];
            int hole = (playerState.time.amount() + i) % width;

            for (int x = 0; x < width; x++) {
                row[x] = x == hole ? 0 : 1;
            }

            board[height - 1] = row;
        }
    }

    private void resolveDeaths(PlayerState playerState, int tick) {
        boolean spawnCollision = playerState.piece.currentPiece != null && collides(playerState.board.board, playerState.piece.currentPiece);
        boolean lockOut = hasLockedOut(playerState);

        if (spawnCollision || lockOut) {
            playerState.status.kill(tick);
            playerState.piece.currentPiece = null;
        }
    }

    private boolean hasLockedOut(PlayerState playerState) {
        for (int y = 0; y < playerState.config.spawnBufferRows; y++) {
            for (int x = 0; x < playerState.board.getWidth(); x++) {
                if (playerState.board.get(x, y) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private Map<String, PlacementCandidate> collectCandidates(int[][] board, Piece startPiece) {
        Map<String, PlacementCandidate> candidates = new HashMap<>();
        Queue<SearchState> frontier = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        frontier.add(new SearchState(startPiece, Collections.emptyList()));
        visited.add(pieceKey(startPiece));

        while (!frontier.isEmpty()) {
            SearchState state = frontier.poll();

            Piece finalPiece = hardDrop(board, state.piece);
            String finalKey = pieceKey(finalPiece);

            PlacementCandidate existing = candidates.get(finalKey);
            if (existing == null || state.inputs.size() < existing.inputs.size()) {
                candidates.put(finalKey, new PlacementCandidate(finalPiece, state.inputs));
            }

            expand(frontier, visited, board, state, GameInput.MOVE_LEFT);
            expand(frontier, visited, board, state, GameInput.MOVE_RIGHT);
            expand(frontier, visited, board, state, GameInput.ROTATE_CW);
            expand(frontier, visited, board, state, GameInput.ROTATE_CCW);
            expand(frontier, visited, board, state, GameInput.ROTATE_180);
        }

        return candidates;
    }

    private void expand(Queue<SearchState> frontier,
                        Set<String> visited,
                        int[][] board,
                        SearchState state,
                        GameInput input) {
        Piece next = switch (input) {
            case MOVE_LEFT -> tryDisplace(board, state.piece, -1, 0);
            case MOVE_RIGHT -> tryDisplace(board, state.piece, 1, 0);
            case ROTATE_CW -> tryRotate(board, state.piece, 1);
            case ROTATE_CCW -> tryRotate(board, state.piece, -1);
            case ROTATE_180 -> tryRotate180(board, state.piece);
            default -> null;
        };

        if (next == null) {
            return;
        }

        String key = pieceKey(next);
        if (!visited.add(key)) {
            return;
        }

        List<GameInput> inputs = new ArrayList<>(state.inputs.size() + 1);
        inputs.addAll(state.inputs);
        inputs.add(input);
        frontier.add(new SearchState(next, inputs));
    }

    private Piece tryDisplace(int[][] board, Piece piece, int dx, int dy) {
        Piece moved = piece.displace(dx, dy);
        return collides(board, moved) ? null : moved;
    }

    private Piece tryRotate(int[][] board, Piece piece, int direction) {
        int from = piece.rotation;
        int to = (from + direction) & 3;
        int[][] kicks = registry.getKicks(piece.type, from, to);

        for (int[] kick : kicks) {
            Piece candidate = new Piece(
                    piece.type,
                    to,
                    piece.x + kick[0],
                    piece.y - kick[1]
            );

            if (!collides(board, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private Piece tryRotate180(int[][] board, Piece piece) {
        Piece first = tryRotate(board, piece, 1);
        if (first == null) {
            return null;
        }
        return tryRotate(board, first, 1);
    }

    private Piece hardDrop(int[][] board, Piece piece) {
        Piece current = piece;

        while (true) {
            Piece next = current.displace(0, 1);
            if (collides(board, next)) {
                return current;
            }
            current = next;
        }
    }

    private Piece createHeldSpawn(PlayerState playerState) {
        PieceType type = playerState.next.held;
        if (type == null) {
            type = playerState.next.next.peek();
        }

        if (type == null) {
            return null;
        }

        return createSpawnPiece(playerState, type);
    }

    private Piece createSpawnPiece(PlayerState playerState, PieceType type) {
        int spawnX = playerState.config.boardWidth / 2 - 1;
        return new Piece(type, 0, spawnX, 0);
    }

    private boolean collides(int[][] board, Piece piece) {
        int[][] shape = registry.getRotation(piece.type, piece.rotation);

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] == 0) {
                    continue;
                }

                int bx = piece.x + x;
                int by = piece.y + y;

                if (bx < 0 || bx >= board[0].length || by < 0 || by >= board.length) {
                    return true;
                }

                if (board[by][bx] != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private int clearLines(int[][] board) {
        int height = board.length;
        int width = board[0].length;
        int[][] rewritten = new int[height][width];

        int writeY = height - 1;
        int linesCleared = 0;

        for (int y = height - 1; y >= 0; y--) {
            boolean full = true;

            for (int x = 0; x < width; x++) {
                if (board[y][x] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
                continue;
            }

            rewritten[writeY] = board[y].clone();
            writeY--;
        }

        while (writeY >= 0) {
            rewritten[writeY] = new int[width];
            writeY--;
        }

        for (int y = 0; y < height; y++) {
            board[y] = rewritten[y];
        }

        return linesCleared;
    }

    private MatchState copyMatchState(MatchState source) {
        List<PlayerState> copiedPlayers = new ArrayList<>(source.players.size());

        for (PlayerState playerState : source.players) {
            copiedPlayers.add(copyPlayerState(playerState));
        }

        return new MatchState(source.tick, source.seed, copiedPlayers);
    }

    private PlayerState copyPlayerState(PlayerState source) {
        PlayerState copy = new PlayerState(source.player, source.config);
        copy.board = copyBoard(source.board);
        copy.piece = copyPieceState(source.piece);
        copy.next = copyQueueState(source.next);
        copy.lock = copyLockState(source.lock);
        copy.gravity = copyGravityState(source.gravity);
        copy.time = copyTimeState(source.time);
        copy.combo = copyComboState(source.combo);
        copy.b2b = copyB2BState(source.b2b);
        copy.garbage = copyGarbageState(source.garbage);
        copy.status = copyStatusState(source.status);
        return copy;
    }

    private BoardState copyBoard(BoardState source) {
        BoardState copy = new BoardState(source.getWidth(), source.getHeight());
        for (int y = 0; y < source.getHeight(); y++) {
            copy.board[y] = source.board[y].clone();
        }
        return copy;
    }

    private PieceState copyPieceState(PieceState source) {
        return new PieceState(source.currentPiece == null ? null : source.currentPiece.copy());
    }

    private QueueState copyQueueState(QueueState source) {
        QueueState copy = new QueueState();
        copy.held = source.held;
        copy.canHold = source.canHold;
        copy.next.addAll(source.next);
        return copy;
    }

    private LockState copyLockState(LockState source) {
        LockState copy = new LockState();
        copy.lockTicks = source.lockTicks;
        copy.slides = source.slides;
        copy.rotations = source.rotations;
        copy.hardDrop = source.hardDrop;
        copy.lowestY = source.lowestY;
        return copy;
    }

    private GravityState copyGravityState(GravityState source) {
        GravityState copy = new GravityState();
        copy.gravityTicks = source.gravityTicks;
        copy.softDrop = source.softDrop;
        return copy;
    }

    private TimeState copyTimeState(TimeState source) {
        TimeState copy = new TimeState();
        copy.tick = source.tick;
        copy.piecesPlaced.addAll(source.piecesPlaced);
        return copy;
    }

    private ComboState copyComboState(ComboState source) {
        ComboState copy = new ComboState();
        copy.currentCombo.addAll(source.currentCombo);
        return copy;
    }

    private B2BState copyB2BState(B2BState source) {
        B2BState copy = new B2BState();
        copy.currentB2B.addAll(source.currentB2B);
        return copy;
    }

    private GarbageState copyGarbageState(GarbageState source) {
        GarbageQueue queue = new GarbageQueue();
        for (GarbageSpike spike : source.incoming.getRawQueue()) {
            queue.add(spike.getLines(), spike.getSendOnPiece(), spike.getSendAfterTick());
        }
        return new GarbageState(source.target, queue);
    }

    private StatusState copyStatusState(StatusState source) {
        StatusState copy = new StatusState();
        copy.alive = source.alive;
        copy.deathTick = source.deathTick;
        copy.KOs = source.KOs;
        copy.Score = source.Score;
        return copy;
    }

    private String pieceKey(Piece piece) {
        int rotation = piece.type == PieceType.O ? 0 : (piece.rotation & 3);
        return piece.type + ":" + rotation + ":" + piece.x + ":" + piece.y;
    }

    private static final class SearchState {
        private final Piece piece;
        private final List<GameInput> inputs;

        private SearchState(Piece piece, List<GameInput> inputs) {
            this.piece = piece;
            this.inputs = inputs;
        }
    }

    private static final class PlacementCandidate {
        private final Piece finalPiece;
        private final List<GameInput> inputs;

        private PlacementCandidate(Piece finalPiece, List<GameInput> inputs) {
            this.finalPiece = finalPiece;
            this.inputs = inputs;
        }
    }
}
