package com.aquip.tetris.util;

import com.aquip.tetris.config.GameTableParser;
import com.aquip.tetris.state.ConfigState;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class GameConfigParser {

    public static ConfigState parse(File file) {

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new FileInputStream(file));

            ConfigState config = new ConfigState();

            if (data == null) return config;

            // =====================
            // BASIC SETTINGS
            // =====================
            config.boardWidth = ((Number) data.getOrDefault("boardWidth", 10)).intValue();
            config.boardHeight = ((Number) data.getOrDefault("boardHeight", 20)).intValue();
            config.spawnBufferRows = ((Number) data.getOrDefault("spawnBufferRows", 4)).intValue();

            config.gravityTick = ((Number) data.getOrDefault("gravityTick", 60)).intValue();
            config.minimumGravityTick = ((Number) data.getOrDefault("minimumGravityTick", 8)).intValue();
            config.gravityStepEveryPieces = ((Number) data.getOrDefault("gravityStepEveryPieces", 10)).intValue();
            config.gravityStepAmount = ((Number) data.getOrDefault("gravityStepAmount", 4)).intValue();
            config.lockTick = ((Number) data.getOrDefault("lockTick", 30)).intValue();

            config.useBag = (boolean) data.getOrDefault("useBag", true);

            // =====================
            // TABLES (NEW)
            // =====================
            GameTableParser tableParser = new GameTableParser();

            config.scoreTable   = tableParser.parseScore(file);
            config.garbageTable = tableParser.parseGarbage(file);
            config.spinTable    = tableParser.parseSpin(file);

            return config;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse game config", e);
        }
    }
}
