package com.aquip.tetris.input;

import com.aquip.tetris.player.Player;

import java.awt.event.KeyEvent;
import java.util.Set;

public class HumanInputSource implements PlayerInputSource {

    private final Player player;

    // --- DAS/ARR config ---
    private static final int DAS_DELAY = 10;
    private static final int ARR = 2;

    // --- DAS state ---
    private int dasTimer = 0;
    private int arrTimer = 0;
    private int lastDirection = 0;

    public HumanInputSource(Player player) {
        this.player = player;
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
        if (frame.pressed.contains(getKeyRotateCW())) {
            inputs.add(GameInput.ROTATE_CW);
        }

        if (frame.pressed.contains(getKeyRotateCCW())) {
            inputs.add(GameInput.ROTATE_CCW);
        }

        if (frame.pressed.contains(getKeyHardDrop())) {
            inputs.add(GameInput.HARD_DROP);
        }

        if (frame.pressed.contains(getKeyHold())) {
            inputs.add(GameInput.HOLD_PIECE);
        }

        // Continuous action
        if (frame.held.contains(getKeySoftDrop())) {
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

        boolean leftHeld = frame.held.contains(getKeyLeft());
        boolean rightHeld = frame.held.contains(getKeyRight());

        boolean leftPressed = frame.pressed.contains(getKeyLeft());
        boolean rightPressed = frame.pressed.contains(getKeyRight());

        int direction = 0;
        if (leftHeld && !rightHeld) direction = -1;
        else if (rightHeld && !leftHeld) direction = 1;

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

    private int getKeyLeft() {
        return playerIndex() == 0 ? KeyEvent.VK_LEFT : KeyEvent.VK_A;
    }

    private int getKeyRight() {
        return playerIndex() == 0 ? KeyEvent.VK_RIGHT : KeyEvent.VK_D;
    }

    private int getKeySoftDrop() {
        return playerIndex() == 0 ? KeyEvent.VK_DOWN : KeyEvent.VK_S;
    }

    private int getKeyRotateCW() {
        return playerIndex() == 0 ? KeyEvent.VK_X : KeyEvent.VK_W;
    }

    private int getKeyRotateCCW() {
        return playerIndex() == 0 ? KeyEvent.VK_Z : KeyEvent.VK_Q;
    }

    private int getKeyHardDrop() {
        return playerIndex() == 0 ? KeyEvent.VK_SPACE : KeyEvent.VK_SHIFT;
    }

    private int getKeyHold() {
        return playerIndex() == 0 ? KeyEvent.VK_C : KeyEvent.VK_E;
    }

    private int playerIndex() {
        try {
            return Integer.parseInt(player.getName().substring(1)) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}