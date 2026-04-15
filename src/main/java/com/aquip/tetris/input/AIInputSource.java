package com.aquip.tetris.input;

import com.aquip.tetris.ai.AIController;
import com.aquip.tetris.player.Player;

public class AIInputSource implements PlayerInputSource {

    private final Player player;
    private final AIController ai;

    public AIInputSource(Player player, AIController ai) {
        this.player = player;
        this.ai = ai;
    }

    @Override
    public PlayerInput poll(InputFrame frame) {
        return ai.decide(player, frame.state);
    }
}
