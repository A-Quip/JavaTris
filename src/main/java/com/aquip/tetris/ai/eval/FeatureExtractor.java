package com.aquip.tetris.ai.eval;

import com.aquip.tetris.ai.sim.FastBoard;

/**
 * Extracts numerical features from a board state for heuristic evaluation.
 */
public class FeatureExtractor {

    private static final ThreadLocal<int[]> HEIGHTS_CACHE = ThreadLocal.withInitial(() -> new int[FastBoard.WIDTH]);

    /**
     * Analyzes the board, extracts features, and applies the heuristic weights in a single pass
     * without creating temporary objects. This minimizes GC pressure in the main search loop.
     * 
     * @param board The bitboard to analyze.
     * @param linesCleared Lines cleared by the most recent move.
     * @param weights The heuristic weights to apply.
     * @return A numerical score representing the value of the state.
     */
    public static double evaluateScore(FastBoard board, int linesCleared, HeuristicWeights weights) {
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
        
        // 1. Calculate Heights, Holes, Max Height, and Hole Depth in one pass
        for (int x = 0; x < FastBoard.WIDTH; x++) {
            int mask = 1 << x;
            boolean blockFound = false;
            heights[x] = 0; // Reset cache
            
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

        // 2. Bumpiness & 6. Tetris Well Detection
        for (int x = 0; x < FastBoard.WIDTH; x++) {
            if (x < FastBoard.WIDTH - 1) {
                bumpiness += Math.abs(heights[x] - heights[x + 1]);
            }
            
            int leftHeight = (x == 0) ? h : heights[x - 1];
            int rightHeight = (x == FastBoard.WIDTH - 1) ? h : heights[x + 1];
            int wellDepth = Math.min(leftHeight, rightHeight) - heights[x];
            
            if (wellDepth >= 3) {
                tetrisWellDepth = Math.max(tetrisWellDepth, wellDepth);
            }
        }

        // 4. Row Transitions
        for (int y = 0; y < h; y++) {
            int row = rows[y];
            int lastOccupied = 1; // Left wall is occupied
            for (int x = 0; x < FastBoard.WIDTH; x++) {
                int currentOccupied = (row >> x) & 1;
                if (currentOccupied != lastOccupied) rowTransitions++;
                lastOccupied = currentOccupied;
            }
            if (lastOccupied == 0) rowTransitions++; // Right wall
        }

        // 5. Column Transitions
        for (int x = 0; x < FastBoard.WIDTH; x++) {
            int mask = 1 << x;
            int lastOccupied = 0; // Buffer rows are considered empty
            for (int y = 0; y < h; y++) {
                int currentOccupied = ((rows[y] & mask) != 0) ? 1 : 0;
                if (currentOccupied != lastOccupied) colTransitions++;
                lastOccupied = currentOccupied;
            }
            if (lastOccupied == 0) colTransitions++; // Floor is considered occupied
        }

        // 7. T-Spin Setup Detection
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < FastBoard.WIDTH - 1; x++) {
                // If it's a hole floor
                if ((rows[y] & (1 << x)) == 0 && (rows[y + 1] & (1 << x)) != 0) {
                    boolean leftWall = (rows[y] & (1 << (x - 1))) != 0;
                    boolean rightWall = (rows[y] & (1 << (x + 1))) != 0;
                    boolean leftOverhang = (rows[y - 1] & (1 << (x - 1))) != 0;
                    boolean rightOverhang = (rows[y - 1] & (1 << (x + 1))) != 0;
                    
                    if ((leftWall && rightOverhang && !rightWall) || (rightWall && leftOverhang && !leftWall)) {
                        tSpinSetups++;
                    }
                }
            }
        }

        // Compute dot product
        double score = 0;
        score += aggregateHeight * weights.aggregateHeight;
        score += maxHeight * weights.maxHeight;
        score += bumpiness * weights.bumpiness;
        score += holes * weights.holes;
        score += holeDepth * weights.holeDepth;
        score += rowTransitions * weights.rowTransitions;
        score += colTransitions * weights.colTransitions;
        score += linesCleared * weights.linesCleared;
        
        score += tetrisWellDepth * weights.tetrisWellDepth;
        score += tSpinSetups * weights.tSpinSetups;
        
        return score;
    }
}
