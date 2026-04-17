package com.aquip.tetris.config;

import com.aquip.tetris.placement.PlaceKey;

import java.util.Map;

public class SpinTable {

    public final Map<PlaceKey, Boolean> table;

    public SpinTable(Map<PlaceKey, Boolean> table) {
        this.table = table;
    }
}
