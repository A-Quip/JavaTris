package com.aquip.tetris.state;

public class GravityState {

    public int gravityTicks;
    public boolean softDrop;

    public GravityState() {
        this.gravityTicks = 0;
        this.softDrop = false;
    }

    public void reset() {
        gravityTicks = 0;
        softDrop = false;
    }
}