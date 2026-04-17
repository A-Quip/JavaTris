package com.aquip.tetris.input;

import com.aquip.tetris.player.Player;

import java.awt.event.KeyEvent;
import java.util.Set;

public class HumanInputSource implements PlayerInputSource {

    private final Player player;
    private final ControlScheme controlScheme;

    // --- DAS/ARR config ---
    private static final int DAS_DELAY = 10;
    private static final int ARR = 2;

    // --- DAS state ---
    private int dasTimer = 0;
    private int arrTimer = 0;
    private int lastDirection = 0;

    public HumanInputSource(Player player) {
        this(player, ControlScheme.PLAYER_ONE);
    }

    public HumanInputSource(Player player, ControlScheme controlScheme) {
        this.player = player;
        this.controlScheme = controlScheme;
    }

    @Override
    public PlayerInput poll(InputFrame frame) {

        Set<GameInput> inputs = new java.util.HashSet<>();

        int move = handleHorizontal(frame);

        // Horizontal movement (DAS-controlled)
        if (move == -1) {
            inputs.add(GameInput.MOVE_LEFT);
        } else if (move == 1) {
            inputs.add(GameInput.MOVE_RIGHT);
        }

        // Edge-triggered actions
        if (isPressed(frame, getRotateCWKeys())) {
            inputs.add(GameInput.ROTATE_CW);
        }

        if (isPressed(frame, getRotateCCWKeys())) {
            inputs.add(GameInput.ROTATE_CCW);
        }

        if (isPressed(frame, getHardDropKeys())) {
            inputs.add(GameInput.HARD_DROP);
        }

        if (isPressed(frame, getHoldKeys())) {
            inputs.add(GameInput.HOLD_PIECE);
        }

        // Continuous action
        if (isHeld(frame, getSoftDropKeys())) {
            inputs.add(GameInput.SOFT_DROP);
        }

        PlayerInput input = new PlayerInput();
        input.player = player;
        input.inputs = inputs;

        return input;
    }

    // ============================================
    // DAS / ARR
    // ============================================
    private int handleHorizontal(InputFrame frame) {

        boolean leftHeld = isHeld(frame, getLeftKeys());
        boolean rightHeld = isHeld(frame, getRightKeys());

        boolean leftPressed = isPressed(frame, getLeftKeys());
        boolean rightPressed = isPressed(frame, getRightKeys());

        int direction = 0;
        if (leftHeld && !rightHeld)
            direction = -1;
        else if (rightHeld && !leftHeld)
            direction = 1;

        // no input → reset
        if (direction == 0) {
            dasTimer = 0;
            arrTimer = 0;
            lastDirection = 0;
            return 0;
        }

        if (leftPressed || rightPressed) {
            dasTimer = 0;
            arrTimer = 0;
            lastDirection = direction;
            return direction;
        }

        // direction changed while holding
        if (direction != lastDirection) {
            dasTimer = 0;
            arrTimer = 0;
            lastDirection = direction;
            return direction;
        }

        dasTimer++;

        if (dasTimer < DAS_DELAY) {
            return 0;
        }

        arrTimer++;

        if (arrTimer >= ARR) {
            arrTimer = 0;
            return direction;
        }

        return 0;
    }

    // ============================================
    // KEY MAPPING
    // ============================================

    private int[] getLeftKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_LEFT }
                : new int[] { KeyEvent.VK_A };
    }

    private int[] getRightKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_RIGHT }
                : new int[] { KeyEvent.VK_D };
    }

    private int[] getSoftDropKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_DOWN }
                : new int[] { KeyEvent.VK_S };
    }

    private int[] getRotateCWKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_PERIOD }
                : new int[] { KeyEvent.VK_W };
    }

    private int[] getRotateCCWKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_COMMA }
                : new int[] { KeyEvent.VK_Q };
    }

    private int[] getHardDropKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_UP }
                : new int[] { KeyEvent.VK_F };
    }

    private int[] getHoldKeys() {
        return controlScheme == ControlScheme.PLAYER_ONE
                ? new int[] { KeyEvent.VK_SLASH }
                : new int[] { KeyEvent.VK_E };
    }

    private boolean isPressed(InputFrame frame, int[] keys) {
        for (int key : keys) {
            if (frame.pressed.contains(key)) {
                return true;
            }
        }

        return false;
    }

    private boolean isHeld(InputFrame frame, int[] keys) {
        for (int key : keys) {
            if (frame.held.contains(key)) {
                return true;
            }
        }

        return false;
    }
}