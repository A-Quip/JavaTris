package com.aquip.tetris.config;

import com.aquip.tetris.placement.PlaceKey;

import java.util.Map;

public class GarbageTable {

    public final Map<PlaceKey, Integer> table;

    public GarbageTable(Map<PlaceKey, Integer> table) {
        this.table = table;
    }

    public int get(PlaceKey key) {
        return table.getOrDefault(key, 0);
    }
}