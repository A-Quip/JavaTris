package com.aquip.tetris.ai;

public enum Difficulty {

    // beamW look delayMs ticksPerCmd
    EASY(10, 1, 0, 1),
    MEDIUM(20, 2, 0, 1),
    HARD(30, 3, 0, 1),
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
