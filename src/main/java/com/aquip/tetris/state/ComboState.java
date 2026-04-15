package com.aquip.tetris.state;

import com.aquip.tetris.placement.PlacementResult;
import java.util.ArrayList;
import java.util.List;

public class ComboState {

    public List<PlacementResult> currentCombo;

    public ComboState() {
        this.currentCombo = new ArrayList<>();
    }

    public int amount() {
        return currentCombo.size();
    }

    public void reset() {
        currentCombo.clear();
    }
}