package com.aquip.tetris.state;

public class StatusState {

    public boolean alive;
    public int deathTick;
    public int KOs;
    public int Score;

    public StatusState() {
        this.alive = true;
        this.deathTick = -1;
        this.KOs = 0;
        this.Score = 0;
    }

    public void kill(int tick) {
        this.alive = false;
        this.deathTick = tick;
    }
}