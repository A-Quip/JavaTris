package com.aquip.tetris.ai;

import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.MatchState;

public class SimpleAI implements AIController {

    @Override
    public PlayerInput decide(Player player, MatchState state) {

        PlayerInput input = new PlayerInput();
        input.player = player;

        // No actions for now (AI does nothing)
        return input;
    }
}