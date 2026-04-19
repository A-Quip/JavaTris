package com.aquip.tetris.ai;

public enum Difficulty {

    // beamW look delayMs ticksPerCmd
    EASY(3, 1, 0, 1),
    MEDIUM(5, 2, 0, 1),
    HARD(10, 3, 0, 1),
    SUPERHUMAN(35, 5, 0, 1);

    // EASY(3, 1, 3000, 12),
    // MEDIUM(5, 2, 2000, 9),
    // HARD(10, 3, 1000, 7),
    // SUPERHUMAN(35, 5, 0, 1);

    public final int beamWidth;
    public final int lookaheadDepth;
    public final int decisionDelayMs;
    public final int ticksPerCommand;

    Difficulty(int beamWidth, int lookaheadDepth, int decisionDelayMs, int ticksPerCommand) {
        this.beamWidth = beamWidth;
        this.lookaheadDepth = lookaheadDepth;
        this.decisionDelayMs = decisionDelayMs;
        this.ticksPerCommand = ticksPerCommand;
    }
}
