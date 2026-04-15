package com.aquip.tetris.input;

import com.aquip.tetris.ui.menu.MenuInput;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ClientInput {

    public KeyStroke pressed;

    public MenuInput toMenuInput() {
        if (pressed == null) return null;

        return switch (pressed.getKeyCode()) {
            case KeyEvent.VK_UP -> MenuInput.UP;
            case KeyEvent.VK_DOWN -> MenuInput.DOWN;
            case KeyEvent.VK_LEFT -> MenuInput.LEFT;
            case KeyEvent.VK_RIGHT -> MenuInput.RIGHT;
            case KeyEvent.VK_ENTER -> MenuInput.CONFIRM;
            case KeyEvent.VK_ESCAPE -> MenuInput.BACK;
            default -> null;
        };
    }
}
