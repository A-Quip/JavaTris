package com.aquip.tetris.engine.event;

public class GravityTickEvent implements GameEvent {
    public final int playerIndex;

    public GravityTickEvent(int playerIndex) {
        this.playerIndex = playerIndex;
    }
}