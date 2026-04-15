package com.aquip.tetris.ai;

import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class HeuristicAI implements AIController {

    private final Queue<GameInput> plannedInputs = new ArrayDeque<>();
    private final PieceRegistry registry = PieceRegistry.getInstance();

    @Override
    public PlayerInput decide(Player player, MatchState state) {

        PlayerInput input = new PlayerInput();
        input.player = player;
        input.inputs = Collections.emptySet();

        if (state == null) {
            plannedInputs.clear();
            return input;
        }

        PlayerState playerState = state.getPlayerState(player);
        if (playerState == null || !playerState.status.alive) {
            plannedInputs.clear();
            return input;
        }

        if (!playerState.piece.hasPiece()) {
            plannedInputs.clear();
            return input;
        }

        if (plannedInputs.isEmpty()) {
            Plan plan = chooseBestPlan(playerState);
            if (plan != null) {
                plannedInputs.addAll(plan.inputs);
            }
        }

        GameInput next = plannedInputs.poll();
        if (next != null) {
            input.inputs = EnumSet.of(next);
        }

        return input;
    }

    private Plan chooseBestPlan(PlayerState player) {

        Piece currentPiece = player.piece.currentPiece;
        Plan best = buildPlan(player, currentPiece, Collections.emptyList());

        if (!player.next.canHold) {
            return best;
        }

        Piece holdPiece = createHeldSpawn(player);
        if (holdPiece == null || collides(player.board.board, holdPiece)) {
            return best;
        }

        Plan holdPlan = buildPlan(player, holdPiece, List.of(GameInput.HOLD_PIECE));

        if (best == null) {
            return holdPlan;
        }

        if (holdPlan != null && holdPlan.score > best.score) {
            return holdPlan;
        }

        return best;
    }

    private Piece createHeldSpawn(PlayerState player) {

        Piece current = player.piece.currentPiece;
        if (current == null) {
            return null;
        }

        PieceType type = player.next.held;
        if (type == null) {
            type = player.next.next.peek();
        }

        if (type == null) {
            return null;
        }

        return createSpawnPiece(player, type);
    }

    private Piece createSpawnPiece(PlayerState player, PieceType type) {
        int spawnX = player.config.boardWidth / 2 - 1;
        return new Piece(type, 0, spawnX, 0);
    }

    private Plan buildPlan(PlayerState player, Piece startPiece, List<GameInput> prefix) {

        if (startPiece == null || collides(player.board.board, startPiece)) {
            return null;
        }

        Map<String, PlacementCandidate> candidates = collectCandidates(player.board.board, startPiece);

        PlacementCandidate bestCandidate = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (PlacementCandidate candidate : candidates.values()) {
            SimulatedBoard simulated = simulatePlacement(player.board.board, candidate.finalPiece);
            double score = evaluateBoard(simulated.board, simulated.linesCleared);

            if (score > bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }
        }

        if (bestCandidate == null) {
            return null;
        }

        List<GameInput> inputs = new ArrayList<>(prefix.size() + bestCandidate.inputs.size() + 1);
        inputs.addAll(prefix);
        inputs.addAll(bestCandidate.inputs);
        inputs.add(GameInput.HARD_DROP);

        return new Plan(inputs, bestScore);
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

    private SimulatedBoard simulatePlacement(int[][] board, Piece piece) {

        int[][] copy = copyBoard(board);
        int[][] shape = registry.getRotation(piece.type, piece.rotation);
        int pieceValue = piece.type.ordinal() + 1;

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] == 0) {
                    continue;
                }

                int bx = piece.x + x;
                int by = piece.y + y;

                if (by >= 0 && by < copy.length && bx >= 0 && bx < copy[0].length) {
                    copy[by][bx] = pieceValue;
                }
            }
        }

        int linesCleared = clearLines(copy);
        return new SimulatedBoard(copy, linesCleared);
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

    private double evaluateBoard(int[][] board, int linesCleared) {

        BoardFeatures features = analyzeBoard(board);
        int height = board.length;

        double score = 0.0;
        score += linesCleared * 12.0;
        score -= features.aggregateHeight * 0.45;
        score -= features.holes * 14.0;
        score -= features.bumpiness * 0.75;
        score -= features.wellDepth * 0.30;
        score -= features.maxHeight * 1.25;
        score -= features.coveredCells * 0.40;

        if (features.holes == 0) {
            score += 3.0;
        }

        if (linesCleared == 4) {
            score += 6.0;
        }

        if (features.maxHeight >= height - 4) {
            score -= (features.maxHeight - (height - 5)) * 25.0;
        }

        return score;
    }

    private BoardFeatures analyzeBoard(int[][] board) {

        int width = board[0].length;
        int height = board.length;
        int[] heights = new int[width];

        int aggregateHeight = 0;
        int maxHeight = 0;
        int holes = 0;
        int coveredCells = 0;

        for (int x = 0; x < width; x++) {
            boolean seenBlock = false;

            for (int y = 0; y < height; y++) {
                if (board[y][x] != 0) {
                    if (!seenBlock) {
                        heights[x] = height - y;
                        aggregateHeight += heights[x];
                        maxHeight = Math.max(maxHeight, heights[x]);
                        seenBlock = true;
                    }
                } else if (seenBlock) {
                    holes++;
                    coveredCells += height - y;
                }
            }
        }

        int bumpiness = 0;
        for (int x = 0; x < width - 1; x++) {
            bumpiness += Math.abs(heights[x] - heights[x + 1]);
        }

        int wellDepth = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (board[y][x] != 0) {
                    continue;
                }

                boolean leftWall = x == 0 || board[y][x - 1] != 0;
                boolean rightWall = x == width - 1 || board[y][x + 1] != 0;

                if (leftWall && rightWall) {
                    wellDepth++;
                }
            }
        }

        return new BoardFeatures(aggregateHeight, maxHeight, holes, coveredCells, bumpiness, wellDepth);
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

    private int[][] copyBoard(int[][] board) {

        int[][] copy = new int[board.length][];

        for (int i = 0; i < board.length; i++) {
            copy[i] = board[i].clone();
        }

        return copy;
    }

    private String pieceKey(Piece piece) {
        int rotation = (piece.type == PieceType.O) ? 0 : (piece.rotation & 3);
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

    private static final class SimulatedBoard {
        private final int[][] board;
        private final int linesCleared;

        private SimulatedBoard(int[][] board, int linesCleared) {
            this.board = board;
            this.linesCleared = linesCleared;
        }
    }

    private static final class BoardFeatures {
        private final int aggregateHeight;
        private final int maxHeight;
        private final int holes;
        private final int coveredCells;
        private final int bumpiness;
        private final int wellDepth;

        private BoardFeatures(int aggregateHeight,
                              int maxHeight,
                              int holes,
                              int coveredCells,
                              int bumpiness,
                              int wellDepth) {
            this.aggregateHeight = aggregateHeight;
            this.maxHeight = maxHeight;
            this.holes = holes;
            this.coveredCells = coveredCells;
            this.bumpiness = bumpiness;
            this.wellDepth = wellDepth;
        }
    }

    private static final class Plan {
        private final List<GameInput> inputs;
        private final double score;

        private Plan(List<GameInput> inputs, double score) {
            this.inputs = inputs;
            this.score = score;
        }
    }
}
