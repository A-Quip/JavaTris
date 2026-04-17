package com.aquip.tetris.input;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;

public class SwingInputSource implements InputSource {
    // =====================
    // INTERNAL KEY STATE
    // =====================
    private static class KeyState {
        boolean held = false;

        boolean pressedThisFrame = false;
        boolean releasedThisFrame = false;
    }

    private final Map<Integer, KeyState> keys = new HashMap<>();

    public SwingInputSource(JComponent component) {

        component.setFocusable(true);

        bind(component, KeyEvent.VK_UP);
        bind(component, KeyEvent.VK_LEFT);
        bind(component, KeyEvent.VK_RIGHT);
        bind(component, KeyEvent.VK_DOWN);
        bind(component, KeyEvent.VK_SPACE);
        bind(component, KeyEvent.VK_ENTER);
        bind(component, KeyEvent.VK_A);
        bind(component, KeyEvent.VK_D);
        bind(component, KeyEvent.VK_S);
        bind(component, KeyEvent.VK_W);
        bind(component, KeyEvent.VK_Q);
        bind(component, KeyEvent.VK_E);
        bind(component, KeyEvent.VK_F);
        bind(component, KeyEvent.VK_PERIOD);
        bind(component, KeyEvent.VK_COMMA);
        bind(component, KeyEvent.VK_SLASH);
        bind(component, KeyEvent.VK_R);
        bind(component, KeyEvent.VK_ESCAPE);
    }

    // =====================
    // KEY BINDING
    // =====================
    private void bind(JComponent c, int keyCode) {

        keys.putIfAbsent(keyCode, new KeyState());

        String press = "press_" + keyCode;
        String release = "release_" + keyCode;

        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, false), press);

        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, true), release);

        c.getActionMap().put(press, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyState k = keys.get(keyCode);

                if (!k.held) {
                    k.pressedThisFrame = true;
                }

                k.held = true;
            }
        });

        c.getActionMap().put(release, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyState k = keys.get(keyCode);

                k.held = false;
                k.releasedThisFrame = true;
            }
        });
    }

    // =====================
    // POLL (MAIN ENTRY)
    // =====================
    @Override
    public InputFrame poll() {

        InputFrame frame = new InputFrame();

        for (Map.Entry<Integer, KeyState> entry : keys.entrySet()) {
            int key = entry.getKey();
            KeyState k = entry.getValue();

            // =====================
            // BUILD FRAME OUTPUT
            // =====================
            if (k.held) {
                frame.held.add(key);
            }

            if (k.pressedThisFrame) {
                frame.pressed.add(key);
            }

            if (k.releasedThisFrame) {
                frame.released.add(key);
            }
        }

        // =====================
        // CLEAR FRAME FLAGS
        // =====================
        for (KeyState k : keys.values()) {
            k.pressedThisFrame = false;
            k.releasedThisFrame = false;
        }

        return frame;
    }
}
