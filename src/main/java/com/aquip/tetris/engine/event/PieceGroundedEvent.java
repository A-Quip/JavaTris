package com.aquip.tetris.engine.event;

public class PieceGroundedEvent implements GameEvent {
    public final int playerIndex;

    public PieceGroundedEvent(int playerIndex) {
        this.playerIndex = playerIndex;
    }
}