package com.aquip.tetris.state;

import com.aquip.tetris.placement.PlacementResult;

import java.util.ArrayList;
import java.util.List;

public class TimeState {

    public int tick;
    public List<PlacementResult> piecesPlaced;

    public TimeState() {
        this.tick = 0;
        this.piecesPlaced = new ArrayList<>();
    }

    public int amount() {
        return piecesPlaced.size();
    }

    public void addPlacement(PlacementResult result) {
        piecesPlaced.add(result);
    }
}