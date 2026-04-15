package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.util.Queue;

public class TickHandler {

    public final MatchState matchState;
    private final InputHandler inputHandler;
    private final GravityHandler gravityHandler;
    private final PhysicsHandler physicsHandler;
    private final LockHandler lockHandler;
    private final QueueHandler queueHandler;
    private final PlacementHandler placementHandler;
    private final GarbageHandler garbageHandler;
    private final ScoreHandler scoreHandler;
    private final DeathHandler deathHandler;

    private final TickContext context = new TickContext();

    public TickHandler(
            MatchState matchState,
            InputHandler inputHandler,
            GravityHandler gravityHandler,
            PhysicsHandler physicsHandler,
            LockHandler lockHandler,
            QueueHandler queueHandler,
            PlacementHandler placementHandler,
            ScoreHandler scoreHandler,
            GarbageHandler garbageHandler,
            DeathHandler deathHandler
    ) {
        this.matchState = matchState;
        this.inputHandler = inputHandler;
        this.gravityHandler = gravityHandler;
        this.physicsHandler = physicsHandler;
        this.lockHandler = lockHandler;
        this.queueHandler = queueHandler;
        this.placementHandler = placementHandler;
        this.scoreHandler = scoreHandler;
        this.garbageHandler = garbageHandler;
        this.deathHandler = deathHandler;
    }

    public void tick(Queue<PlayerInput> inputs) {

        context.reset(matchState.players);

        // 1. INPUT
        inputHandler.apply(matchState, inputs, context);

        // 2. PER-PLAYER SIMULATION
        for (PlayerState player : matchState.players) {
            if (!player.status.alive) {
                continue;
            }

            player.time.tick++;

            gravityHandler.apply(player, context);
            physicsHandler.apply(player, context);
            lockHandler.apply(player, context);
            placementHandler.apply(player, context);
            scoreHandler.apply(player, context);
        }

        // 3. GLOBAL RESOLUTION
        garbageHandler.apply(matchState, context);

        // 4. SPAWN + QUEUE
        queueHandler.apply(matchState, context);

        deathHandler.apply(matchState, context);

        matchState.tick++;
    }
}
