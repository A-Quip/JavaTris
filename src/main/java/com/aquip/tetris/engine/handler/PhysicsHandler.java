package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.PlayerTickContext;
import com.aquip.tetris.engine.MoveContext;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;

public class PhysicsHandler implements PlayerHandler {

    @Override
    public void apply(PlayerState player, TickContext context) {

        if (!player.piece.hasPiece()) return;

        PlayerTickContext ctx = context.get(player);
        MoveContext move = ctx.moveContext;

        Piece current = player.piece.currentPiece;

        boolean moved = false;
        boolean grounded = false;

        // =====================
        // 1. ROTATION
        // =====================
        current = applyRotation(player, ctx, current);

        // =====================
        // 2. HORIZONTAL MOVE
        // =====================
        if (ctx.moveX != 0) {
            Piece next = current.displace(ctx.moveX, 0);

            if (!collides(player, next)) {
                current = next;
                moved = true;

                move.moved = true;
                move.rotated = false;
            }
        }

        // =====================
        // 3. VERTICAL MOVE
        // =====================
        if (ctx.moveY != 0) {
            Piece next = current.displace(0, ctx.moveY);

            if (!collides(player, next)) {
                current = next;
                moved = true;

                move.moved = true;
                move.rotated = false;
            }
        }

        // =====================
        // 4. HARD DROP
        // =====================
        if (ctx.hardDrop) {
            move.hardDrop = true;

            while (true) {
                Piece next = current.displace(0, 1);
                if (collides(player, next)) break;
                current = next;
                moved = true;
            }

            grounded = true;
        }

        // =====================
        // 5. GROUNDED CHECK
        // =====================
        if (collides(player, current.displace(0, 1))) {
            grounded = true;
        }

        // =====================
        // 6. APPLY RESULT
        // =====================
        player.piece.currentPiece = current;

        if (moved) ctx.pieceMoved = true;
        if (grounded) ctx.pieceGrounded = true;
    }

    // =====================
    // ROTATION
    // =====================
    private Piece applyRotation(PlayerState player, PlayerTickContext ctx, Piece current) {

        int rot = ctx.rotation;
        MoveContext move = ctx.moveContext;

        while (rot != 0) {

            int step = (rot > 0) ? 1 : -1;

            Piece before = current;
            Piece rotated = tryRotate(player, current, step);

            if (rotated == null) break;

            current = rotated;

            move.rotated = true;
            move.rotationDelta += step;

            if (before.x != rotated.x || before.y != rotated.y) {
                move.usedKick = true;
            }

            rot -= step;
        }

        return current;
    }

    // =====================
    // ROTATION WITH SRS
    // =====================
    private Piece tryRotate(PlayerState player, Piece piece, int rotation) {

        int from = piece.rotation;
        int to = (from + rotation) & 3;

        var registry = PieceRegistry.getInstance();
        int[][] kicks = registry.getKicks(piece.type, from, to);

        for (int[] kick : kicks) {
            int dx = kick[0];
            int dy = -kick[1];

            Piece candidate = new Piece(
                    piece.type,
                    to,
                    piece.x + dx,
                    piece.y + dy
            );

            if (!collides(player, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    // =====================
    // COLLISION
    // =====================
    private boolean collides(PlayerState player, Piece piece) {

        int[][] board = player.board.board;
        var registry = PieceRegistry.getInstance();
        int[][] shape = registry.getRotation(piece.type, piece.rotation);

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {

                if (shape[y][x] == 0) continue;

                int bx = piece.x + x;
                int by = piece.y + y;

                if (bx < 0 || bx >= board[0].length || by < 0 || by >= board.length) {
                    return true;
                }

                if (board[by][bx] != 0) {
                    return true;
                }
            }
        }

        return false;
    }
}