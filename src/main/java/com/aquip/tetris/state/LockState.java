package com.aquip.tetris.state;

import com.aquip.tetris.player.Player;

public class LockState {

    public int lockTicks;
    public int slides;
    public int rotations;
    public boolean hardDrop;
    public int lowestY; // highest Y value reached (further down = larger)

    public LockState() {
        this.lockTicks = 0;
        this.slides = 0;
        this.rotations = 0;
        this.hardDrop = false;
        this.lowestY = 0;
    }

    public void reset() {
        lockTicks = 0;
        slides = 0;
        rotations = 0;
        hardDrop = false;
        lowestY = 0;
    }

    public void softReset() {
        lockTicks = 0;
        slides = 0;
        rotations = 0;
    }
}