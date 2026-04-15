package com.aquip.tetris.engine;

public class MoveContext {

    public boolean rotated;
    public int rotationDelta; // +1 or -1
    public boolean lastWasRotation;

    public boolean usedKick;
    public boolean moved;

    public boolean hardDrop;
    public boolean softDrop;

    public void reset() {
        rotated = false;
        rotationDelta = 0;
        lastWasRotation = false;
        usedKick = false;
        moved = false;
        hardDrop = false;
        softDrop = false;
    }
}