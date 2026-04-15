package com.aquip.tetris.input;

import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.Set;

public class GameInputMapper {

    public Set<GameInput> map(InputFrame frame) {

        Set<GameInput> result = EnumSet.noneOf(GameInput.class);

        //System.out.println("Hld ths frm " + frame.held);

        // HOLD inputs (continuous)
        if (frame.held.contains(KeyEvent.VK_LEFT)) {
            result.add(GameInput.MOVE_LEFT);
        }

        if (frame.held.contains(KeyEvent.VK_RIGHT)) {
            result.add(GameInput.MOVE_RIGHT);
        }

        if (frame.held.contains(KeyEvent.VK_DOWN)) {
            result.add(GameInput.SOFT_DROP);
        }

        // PRESS inputs (one-time)
        if (frame.pressed.contains(KeyEvent.VK_SPACE)) {
            result.add(GameInput.HARD_DROP);
        }

        if (frame.pressed.contains(KeyEvent.VK_Z)) {
            result.add(GameInput.ROTATE_CCW);
        }

        if (frame.pressed.contains(KeyEvent.VK_X)) {
            result.add(GameInput.ROTATE_CW);
        }

        if (frame.pressed.contains(KeyEvent.VK_A)) {
            result.add(GameInput.ROTATE_180);
        }

        if (frame.pressed.contains(KeyEvent.VK_C)) {
            result.add(GameInput.HOLD_PIECE);
        }

        if (frame.pressed.contains(KeyEvent.VK_ESCAPE)) {
            result.add(GameInput.FORFEIT);
        }

        return result;
    }
}