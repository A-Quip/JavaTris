package com.aquip.tetris.input;

import com.aquip.tetris.player.Player;

import java.util.EnumSet;
import java.util.Set;

public class PlayerInputBuilder {

    private final GameInputMapper mapper = new GameInputMapper();
    private final Set<GameInput> buffer = EnumSet.noneOf(GameInput.class);

    public PlayerInput build(Player player, InputFrame frame) {

        Set<GameInput> mapped = mapper.map(frame);

        // Add to buffer
        buffer.addAll(mapped);

        PlayerInput input = new PlayerInput();
        input.player = player;

        // Send buffered inputs
        input.inputs = EnumSet.copyOf(buffer);

        // Clear buffer AFTER sending
        buffer.clear();

        return input;
    }
}