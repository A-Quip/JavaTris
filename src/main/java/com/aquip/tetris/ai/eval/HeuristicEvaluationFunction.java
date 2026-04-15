package com.aquip.tetris.ai.eval;

import com.aquip.tetris.ai.search.PlannedMatchState;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

public class HeuristicEvaluationFunction implements EvaluationFunction {

    private final Player player;

    public HeuristicEvaluationFunction(Player player) {
        this.player = player;
    }

    @Override
    public double evaluate(MatchState state) {
        PlayerState playerState = state.getPlayerState(player);
        if (playerState == null) {
            return Double.NEGATIVE_INFINITY;
        }

        if (!playerState.status.alive) {
            return -100_000.0;
        }

        BoardFeatures features = analyzeBoard(playerState.board.board);
        double score = 0.0;

        int linesCleared = 0;
        int garbageSent = 0;

        if (state instanceof PlannedMatchState planned && planned.plannerId().equals(player.getId())) {
            linesCleared = planned.linesCleared();
            garbageSent = planned.garbageSent();
        }

        score += linesCleared * 18.0;
        score += garbageSent * 22.0;
        score -= features.aggregateHeight * 0.48;
        score -= features.holes * 17.0;
        score -= features.bumpiness * 0.85;
        score -= features.maxHeight * 1.45;
        score -= features.coveredCells * 0.30;
        score -= features.wellDepth * 0.18;
        score -= playerState.garbage.totalLines() * 6.0;

        if (features.holes == 0) {
            score += 4.0;
        }

        if (linesCleared == 4) {
            score += 10.0;
        }

        if (features.maxHeight >= playerState.board.getHeight() - 4) {
            score -= (features.maxHeight - (playerState.board.getHeight() - 5)) * 35.0;
        }

        for (PlayerState opponent : state.players) {
            if (opponent == playerState) {
                continue;
            }

            if (!opponent.status.alive) {
                score += 5_000.0;
                continue;
            }

            BoardFeatures opponentFeatures = analyzeBoard(opponent.board.board);
            score += opponentFeatures.aggregateHeight * 0.10;
            score += opponent.garbage.totalLines() * 4.0;
            score -= opponentFeatures.holes * 0.75;
        }

        if (!state.singlePlayer() && state.isGameOver() && state.getLastAlivePlayer() == playerState) {
            score += 10_000.0;
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

    private record BoardFeatures(
            int aggregateHeight,
            int maxHeight,
            int holes,
            int coveredCells,
            int bumpiness,
            int wellDepth
    ) { }
}
