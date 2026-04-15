package com.aquip.tetris.state;

import com.aquip.tetris.placement.PlacementResult;
import java.util.ArrayList;
import java.util.List;

public class B2BState {

    public List<PlacementResult> currentB2B;

    public B2BState() {
        this.currentB2B = new ArrayList<>();
    }

    public int amount() {
        return currentB2B.size();
    }

    public void reset() {
        currentB2B.clear();
    }
}