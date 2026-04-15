package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.state.PlayerState;

public class LockHandler implements PlayerHandler {

    @Override
    public void apply(PlayerState player, TickContext context) {

        if (!player.piece.hasPiece()) return;

        var ctx = context.get(player);
        var lock = player.lock;
        var config = player.config;

        //System.out.println(lock.lockTicks);

        if (ctx.hardDrop) {
            ctx.pieceLocked = true;
            return;
        }

        boolean grounded = ctx.pieceGrounded;

        // =====================
        // 1. Track lowest Y reached
        // =====================
        int currentY = player.piece.currentPiece.y;

        if (currentY > lock.lowestY) {
            lock.lowestY = currentY;

            lock.softReset();
        }

        // =====================
        // 2. Detect "attempts" (not just successful moves)
        // =====================
        boolean attemptedMove = ctx.moveX != 0;
        boolean attemptedRotate = ctx.rotation != 0;

        boolean reset = false;

        if (grounded) {

            // Slide attempts
            if (attemptedMove && lock.slides < config.maxSlides) {
                lock.slides++;
                reset = true;
            }

            // Rotation attempts
            if (attemptedRotate && lock.rotations < config.maxRotations) {
                lock.rotations++;
                reset = true;
            }
        }

        // =====================
        // 3. Lock timer behavior
        // =====================
        if (!grounded) {
            return;
        }

        if (reset) {
            lock.lockTicks = 0;
        } else {
            lock.lockTicks++;
        }

        // =====================
        // 4. Lock condition
        // =====================
        if (lock.lockTicks >= config.lockTick) {
            ctx.pieceLocked = true;
        }
    }

    public static void resetLock(com.aquip.tetris.state.LockState lock, int spawnY) {
        lock.lockTicks = 0;
        lock.slides = 0;
        lock.rotations = 0;
        lock.lowestY = spawnY;
    }
}