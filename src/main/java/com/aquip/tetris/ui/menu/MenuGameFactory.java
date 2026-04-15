package com.aquip.tetris.ui.menu;

import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.engine.GameFactory;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.ConfigState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuGameFactory {

    public static GameEngine createGame(GameMode mode, ConfigState config) {
        List<Player> players = new ArrayList<>();

        switch (mode) {
            case SOLO -> players.add(createPlayer(PlayerType.HUMAN, "P1"));
            case VS_AI -> {
                players.add(createPlayer(PlayerType.HUMAN, "P1"));
                players.add(createPlayer(PlayerType.AI, "AI1"));
            }
            case TWO_PLAYER -> {
                players.add(createPlayer(PlayerType.HUMAN, "P1"));
                players.add(createPlayer(PlayerType.HUMAN, "P2"));
            }
            case AI_DEMO -> players.add(createPlayer(PlayerType.AI, "AI1"));
        }

        return GameFactory.createGame(players, config);
    }

    private static Player createPlayer(PlayerType type, String name) {
        return new Player(
                UUID.randomUUID(),
                type,
                name
        );
    }
}
