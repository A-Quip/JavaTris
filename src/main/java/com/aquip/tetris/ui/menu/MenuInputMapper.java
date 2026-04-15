package com.aquip.tetris.ui.menu;

import com.aquip.tetris.input.InputFrame;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MenuInputMapper {

    public List<MenuInput> map(InputFrame frame) {
        List<MenuInput> inputs = new ArrayList<>();

        for (int key : frame.pressed) {
            switch (key) {
                case KeyEvent.VK_UP -> inputs.add(MenuInput.UP);
                case KeyEvent.VK_DOWN -> inputs.add(MenuInput.DOWN);
                case KeyEvent.VK_LEFT -> inputs.add(MenuInput.LEFT);
                case KeyEvent.VK_RIGHT -> inputs.add(MenuInput.RIGHT);
                case KeyEvent.VK_ENTER -> inputs.add(MenuInput.CONFIRM);
                case KeyEvent.VK_ESCAPE -> inputs.add(MenuInput.BACK);
            }
        }

        return inputs;
    }
}
