package com.aquip.tetris.input;

public enum GameInput {
    MOVE_LEFT,
    MOVE_RIGHT,
    SOFT_DROP,
    HARD_DROP,
    HOLD_PIECE,
    ROTATE_CW,
    ROTATE_CCW,
    ROTATE_180, // unused
    NONE, // Used for tick waiting (gravity simulation)

    FORFEIT
}
