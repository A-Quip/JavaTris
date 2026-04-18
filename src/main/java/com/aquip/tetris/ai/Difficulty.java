package com.aquip.tetris.ai;

public enum Difficulty {

    // beamW look delayMs ticksPerCmd
    EASY(10, 1, 4000, 8),
    MEDIUM(20, 2, 3000, 6),
    HARD(30, 3, 1500, 4),
    SUPERHUMAN(60, 5, 0, 1);

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
