package com.aquip.tetris.ai;

import com.aquip.tetris.ai.search.BeamSearch;
import com.aquip.tetris.input.AIInputSource;
import com.aquip.tetris.input.PlayerInputSource;
import com.aquip.tetris.player.Player;

/**
 * Public interface and lifecycle manager for the Tetris AI system.
 * Responsible for constructing and wiring the AI components.
 */
public class AIController {

    private final AIConfig config;
    private final BeamSearch beamSearch;
    private final AIThread aiThread;

    public AIController(AIConfig config, BeamSearch beamSearch) {
        this.config = config;
        this.beamSearch = beamSearch;
        this.aiThread = new AIThread(beamSearch);
    }

    /**
     * Creates a new AIInputSource for the specified player.
     *
     * @param player The player that the AI will control.
     * @return A new instance of AIInputSource.
     */
    public PlayerInputSource createInputSource(Player player) {
        return new AIInputSource(player, aiThread, config);
    }

    /**
     * Shuts down the AI system and its background threads.
     * Shuts down BeamSearch's ForkJoinPool before the ai-search executor so
     * any in-flight parallel expansion tasks drain cleanly.
     */
    public void shutdown() {
        beamSearch.shutdown();
        aiThread.shutdown();
    }

    public AIConfig getConfig() {
        return config;
    }

    public AIThread getAiThread() {
        return aiThread;
    }
}