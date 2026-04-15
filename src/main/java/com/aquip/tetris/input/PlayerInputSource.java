package com.aquip.tetris.input;

public interface PlayerInputSource {
    PlayerInput poll(InputFrame frame);
}
