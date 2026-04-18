package com.aquip.tetris.config;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.ai.Difficulty;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/**
 * Parser for the AI section of the game configuration.
 */
public class AIConfigParser {

    /**
     * Parses the AI configuration from a YAML file.
     * Expects an 'ai' section with an optional 'difficulty' key.
     * 
     * @param file The YAML configuration file.
     * @return A configured AIConfig instance.
     */
    public static AIConfig parse(File file) {
        try {
            if (!file.exists())
                return AIConfig.defaults();

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new FileInputStream(file));

            if (data == null)
                return AIConfig.defaults();

            @SuppressWarnings("unchecked")
            Map<String, Object> ai = (Map<String, Object>) data.get("ai");

            if (ai == null)
                return AIConfig.defaults();

            // Support both 'difficulty' string and legacy 'tickDelay' (ignored)
            String difficultyStr = (String) ai.getOrDefault("difficulty", "NORMAL");

            Difficulty difficulty;
            try {
                difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
            } catch (Exception e) {
                difficulty = Difficulty.HARD;
            }

            return new AIConfig(difficulty);

        } catch (Exception e) {
            // Log error if logging was available, but fallback to defaults for robustness
            return AIConfig.defaults();
        }
    }
}