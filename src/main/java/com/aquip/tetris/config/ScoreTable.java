package com.aquip.tetris.config;

import com.aquip.tetris.placement.PlaceKey;

import java.util.Map;

public class ScoreTable {

    public final Map<PlaceKey, Integer> table;

    public ScoreTable(Map<PlaceKey, Integer> table) {
        this.table = table;
    }

    public int get(PlaceKey key) {
        return table.getOrDefault(key, 0);
    }
}