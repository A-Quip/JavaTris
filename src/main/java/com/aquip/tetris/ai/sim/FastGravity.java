package com.aquip.tetris.ai.sim;

import com.aquip.tetris.state.ConfigState;

/**
 * Fast gravity simulation for AI search.
 * Mirrors GravityHandler, using the exact formulas from ConfigState.
 */
public class FastGravity {

    /**
     * Checks if gravity should trigger a vertical move on this tick.
     * 
     * @param gravityTicks The current accumulated gravity ticks.
     * @param isSoftDrop   Whether soft drop is currently being applied.
     * @param config       The game configuration for thresholds.
     * @param piecesPlaced Total pieces placed (for difficulty scaling).
     * @return true if gravity triggers a move.
     */
    public static boolean shouldFall(int gravityTicks, boolean isSoftDrop, 
                                     ConfigState config, int piecesPlaced) {
        
        int threshold = config.gravityThresholdForPieces(piecesPlaced);
        
        if (isSoftDrop) {
            int softDropThreshold = config.softDropThresholdForPieces(piecesPlaced);
            // Mirrors GravityHandler decision logic
            return gravityTicks >= Math.min(softDropThreshold, threshold);
        }

        return gravityTicks >= threshold;
    }
}
