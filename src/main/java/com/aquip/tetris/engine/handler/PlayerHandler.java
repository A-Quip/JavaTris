package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.state.PlayerState;

public interface PlayerHandler {
    void apply(PlayerState player, TickContext context);
}
