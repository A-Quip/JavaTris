package com.aquip.tetris.menu;

import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.engine.GameFactory;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.ConfigState;

import java.util.UUID;

public class MenuGameFactory {

    public static GameEngine createGame(ConfigState config) {
        Player player = new Player(
                UUID.randomUUID(),
                PlayerType.HUMAN,
                "P1"
        );

        return GameFactory.createGame(java.util.List.of(player), config);
    }
}
