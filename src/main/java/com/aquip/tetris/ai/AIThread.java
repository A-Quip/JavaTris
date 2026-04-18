package com.aquip.tetris.ai;

import com.aquip.tetris.ai.search.BeamSearch;
import com.aquip.tetris.ai.search.MoveSequence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Background worker thread for AI search.
 * Wraps an ExecutorService to perform beam searches asynchronously
 * without blocking the main game thread.
 */
public class AIThread {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ai-search");
        t.setDaemon(true);
        return t;
    });

    private final BeamSearch search;

    public AIThread(BeamSearch search) {
        this.search = search;
    }

    /**
     * Submits a new search task to the background thread.
     * Called from the game thread.
     * 
     * @param snapshot An immutable snapshot of the current game state.
     * @return A Future that will contain the best discovered MoveSequence.
     */
    public Future<MoveSequence> submit(AIGameSnapshot snapshot) {
        return executor.submit(() -> search.findBestPath(snapshot));
    }

    /**
     * Shuts down the background executor immediately.
     */
    public void shutdown() {
        executor.shutdownNow();
    }
}
