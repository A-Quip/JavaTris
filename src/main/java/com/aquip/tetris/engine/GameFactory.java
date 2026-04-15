package com.aquip.tetris.engine;

import com.aquip.tetris.ai.HeuristicAI;
import com.aquip.tetris.engine.handler.*;
import com.aquip.tetris.input.*;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameFactory {

    public static GameEngine createGame(List<Player> players,
                                        ConfigState config) {

        MatchState matchState = new MatchState();
        matchState.seed = (int) System.currentTimeMillis();

        List<PlayerInputSource> inputSources = new ArrayList<>();

        for (Player player : players) {

            PlayerInputSource source;

            if (player.getType() == PlayerType.HUMAN) {
                source = new HumanInputSource(player);
            } else {
                source = new AIInputSource(player, new HeuristicAI());
            }

            inputSources.add(source);

            PlayerState playerState = new PlayerState(player, config);

            matchState.addPlayer(playerState);
        }

        Queue<PlayerInput> inputQueue = new LinkedList<>();

        TickHandler tickHandler = new TickHandler(
                matchState,
                new InputHandler(),
                new GravityHandler(),
                new PhysicsHandler(),
                new LockHandler(),
                new QueueHandler(),
                new PlacementHandler(),
                new ScoreHandler(),
                new GarbageHandler(),
                new DeathHandler()
        );

        return new GameEngine(
                matchState,
                inputQueue,
                tickHandler,
                inputSources
        );
    }
}
