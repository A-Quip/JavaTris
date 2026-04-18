package com.aquip.tetris.ai.eval;

import com.aquip.tetris.ai.sim.FastBoard;

/**
 * Extracts numerical features from a board state for heuristic evaluation.
 */
public class FeatureExtractor {

    private static final ThreadLocal<int[]> HEIGHTS_CACHE = ThreadLocal.withInitial(() -> new int[FastBoard.WIDTH]);

    /**
     * Analyzes the board, extracts features, and applies the heuristic weights in a
     * single pass
     * without creating temporary objects. This minimizes GC pressure in the main
     * search loop.
     * 
     * @param board        The bitboard to analyze.
     * @param linesCleared Lines cleared by the most recent move.
     * @param weights      The heuristic weights to apply.
     * @return A numerical score representing the value of the state.
     */
    public static double evaluateScore(FastBoard board, int totalPoints, int combo, boolean b2b,
            HeuristicWeights weights) {
        int[] heights = HEIGHTS_CACHE.get();
        int h = board.height;
        int[] rows = board.rows;

        int aggregateHeight = 0;
        int maxHeight = 0;
        int bumpiness = 0;
        int holes = 0;
        int holeDepth = 0;
        int rowTransitions = 0;
        int colTransitions = 0;
        int tetrisWellDepth = 0;
        int tSpinSetups = 0;

        // Calculate Heights, Holes, Max Height, and Hole Depth
        for (int x = 0; x < FastBoard.WIDTH; x++) {
            int mask = 1 << x;
            boolean blockFound = false;
            heights[x] = 0;

            for (int y = 0; y < h; y++) {
                if ((rows[y] & mask) != 0) {
                    if (!blockFound) {
                        heights[x] = h - y;
                        blockFound = true;
                    }
                } else if (blockFound) {
                    holes++;
                    holeDepth += (heights[x] - (h - y));
                }
            }

            aggregateHeight += heights[x];
            if (heights[x] > maxHeight) {
                maxHeight = heights[x];
            }
        }

        // Bumpiness & Well Detection (9-0 stacking preference)
        for (int x = 0; x < FastBoard.WIDTH; x++) {
            if (x < FastBoard.WIDTH - 1) {
                bumpiness += Math.abs(heights[x] - heights[x + 1]);
            }

            int leftHeight = (x == 0) ? h : heights[x - 1];
            int rightHeight = (x == FastBoard.WIDTH - 1) ? h : heights[x + 1];
            int wellDepth = Math.min(leftHeight, rightHeight) - heights[x];

            if (wellDepth >= 3) {
                // Penalize wells that aren't on the far left or far right (standard stacking)
                if (x > 0 && x < FastBoard.WIDTH - 1) {
                    tetrisWellDepth -= wellDepth;
                } else {
                    tetrisWellDepth += wellDepth;
                }
            }
        }

        // Transitions
        for (int y = 0; y < h; y++) {
            int row = rows[y];
            int lastOccupied = 1;
            for (int x = 0; x < FastBoard.WIDTH; x++) {
                int currentOccupied = (row >> x) & 1;
                if (currentOccupied != lastOccupied)
                    rowTransitions++;
                lastOccupied = currentOccupied;
            }
            if (lastOccupied == 0)
                rowTransitions++;
        }

        for (int x = 0; x < FastBoard.WIDTH; x++) {
            int mask = 1 << x;
            int lastOccupied = 0;
            for (int y = 0; y < h; y++) {
                int currentOccupied = ((rows[y] & mask) != 0) ? 1 : 0;
                if (currentOccupied != lastOccupied)
                    colTransitions++;
                lastOccupied = currentOccupied;
            }
            if (lastOccupied == 0)
                colTransitions++;
        }

        // T-Slot Detection (3-Corner Rule)
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < FastBoard.WIDTH - 1; x++) {
                // Potential T-center must be empty
                if ((rows[y] & (1 << x)) == 0) {
                    int corners = 0;
                    if ((rows[y - 1] & (1 << (x - 1))) != 0)
                        corners++; // Top-left
                    if ((rows[y - 1] & (1 << (x + 1))) != 0)
                        corners++; // Top-right
                    if ((rows[y + 1] & (1 << (x - 1))) != 0)
                        corners++; // Bottom-left
                    if ((rows[y + 1] & (1 << (x + 1))) != 0)
                        corners++; // Bottom-right

                    if (corners >= 3) {
                        // Must also have an overhang to allow kicking in
                        boolean hasOverhang = ((rows[y - 1] & (1 << x)) != 0);
                        if (hasOverhang)
                            tSpinSetups++;
                    }
                }
            }
        }

        // Final Score Calculation
        double score = 0;
        score += aggregateHeight * weights.aggregateHeight;
        score += maxHeight * weights.maxHeight;
        score += bumpiness * weights.bumpiness;
        score += holes * weights.holes;
        score += holeDepth * weights.holeDepth;
        score += rowTransitions * weights.rowTransitions;
        score += colTransitions * weights.colTransitions;

        score += totalPoints * weights.linesCleared;
        score += tetrisWellDepth * weights.tetrisWellDepth;
        score += tSpinSetups * weights.tSpinSetups;

        score += combo * weights.combo;
        if (b2b)
            score += weights.b2b;

        return score;
    }
}
