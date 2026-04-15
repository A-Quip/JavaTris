package com.aquip.tetris.state;

import com.aquip.tetris.garbage.GarbageQueue;

import java.util.UUID;

public class GarbageState {

    public UUID target;
    public GarbageQueue incoming;

    public GarbageState() {
        this(null, new GarbageQueue());
    }

    public GarbageState(UUID target, GarbageQueue incoming) {
        this.target = target;
        this.incoming = incoming != null ? incoming : new GarbageQueue();
    }

    public int totalLines() {
        return incoming.totalLines();
    }

    public String debugString(int currentPiece, int currentTick) {
        return incoming.debugString(currentPiece, currentTick);
    }
}