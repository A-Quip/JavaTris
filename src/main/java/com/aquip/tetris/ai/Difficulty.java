package com.aquip.tetris.ai;

public enum Difficulty {

    // beamW look delayMs ticksPerCmd
    EASY(4, 1, 5000, 5),
    MEDIUM(10, 2, 2500, 3),
    HARD(20, 3, 1000, 2),
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
