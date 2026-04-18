package com.aquip.tetris.ai;

public class AIConfig {

    private final Difficulty difficulty;

    public AIConfig(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public static AIConfig defaults() {
        return new AIConfig(Difficulty.SUPERHUMAN);
    }

    /**
     * Validates that the engine's visible queue is large enough to support
     * the lookahead depth required by the current difficulty.
     * 
     * Called at search time. Throws if the visible queue is too short.
     * 
     * @param visibleQueueSize The current size of the player's next piece queue.
     * @throws IllegalStateException if nextSize < lookaheadDepth
     */
    public void validateQueueDepth(int visibleQueueSize) {
        if (visibleQueueSize < difficulty.lookaheadDepth) {
            throw new IllegalStateException(String.format(
                    "Difficulty %s requires lookahead of %d pieces but nextSize is only %d. " +
                            "Set PlayerConfig.nextSize >= %d.",
                    difficulty, difficulty.lookaheadDepth,
                    visibleQueueSize, difficulty.lookaheadDepth));
        }
    }

    public int decisionDelayMs() {
        return difficulty.decisionDelayMs;
    }

    public int ticksPerCommand() {
        return difficulty.ticksPerCommand;
    }

    public int beamWidth() {
        return difficulty.beamWidth;
    }

    public int lookaheadDepth() {
        return difficulty.lookaheadDepth;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
