package com.aquip.tetris.engine.event;

public class PieceFallEvent implements GameEvent {
    public final int playerIndex;

    public PieceFallEvent(int playerIndex) {
        this.playerIndex = playerIndex;
    }
}