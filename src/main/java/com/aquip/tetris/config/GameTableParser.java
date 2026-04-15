package com.aquip.tetris.config;

import com.aquip.tetris.placement.PlaceKey;
import com.aquip.tetris.placement.SpinResult;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

public class GameTableParser {

    public GameTableParser() {}

    public ScoreTable parseScore(File file) {

        Map<PlaceKey, Integer> table = new java.util.HashMap<>();

        try (FileInputStream fis = new FileInputStream(file)) {

            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(fis);

            List<Map<String, Object>> entries =
                    (List<Map<String, Object>>) root.get("clearTable");

            for (Map<String, Object> entry : entries) {

                SpinResult spin = SpinResult.valueOf((String) entry.get("spin"));
                int lines = (int) entry.get("lines");
                int score = (int) entry.get("score");

                PlaceKey key = new PlaceKey(spin, lines);
                table.put(key, score);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse score table", e);
        }

        return new ScoreTable(table);
    }

    public GarbageTable parseGarbage(File file) {

        Map<PlaceKey, Integer> table = new java.util.HashMap<>();

        try (FileInputStream fis = new FileInputStream(file)) {

            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(fis);

            List<Map<String, Object>> entries =
                    (List<Map<String, Object>>) root.get("clearTable");

            for (Map<String, Object> entry : entries) {

                SpinResult spin = SpinResult.valueOf((String) entry.get("spin"));
                int lines = (int) entry.get("lines");
                int garbage = (int) entry.get("garbage");

                PlaceKey key = new PlaceKey(spin, lines);
                table.put(key, garbage);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse garbage table", e);
        }

        return new GarbageTable(table);
    }

    public SpinTable parseSpin(File file) {

        Map<PlaceKey, Boolean> table = new java.util.HashMap<>();

        try (FileInputStream fis = new FileInputStream(file)) {

            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(fis);

            List<Map<String, Object>> entries =
                    (List<Map<String, Object>>) root.get("clearTable");

            for (Map<String, Object> entry : entries) {

                SpinResult spin = SpinResult.valueOf((String) entry.get("spin"));
                int lines = (int) entry.get("lines");

                PlaceKey key = new PlaceKey(spin, lines);

                // Example: mark whether it's a spin or not
                table.put(key, spin != SpinResult.NONE);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse spin table", e);
        }

        return new SpinTable(table);
    }
}