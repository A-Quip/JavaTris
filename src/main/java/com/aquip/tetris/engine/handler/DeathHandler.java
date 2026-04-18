package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.PlayerTickContext;
import com.aquip.tetris.piece.PieceRegistry;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.state.StatusState;

public class DeathHandler {

    public void apply(MatchState match, TickContext context) {

        for (PlayerState player : match.players) {

            PlayerTickContext ctx = context.get(player);
            StatusState status = player.status;

            if (!status.alive)
                continue;

            // =====================
            // 1. APPLY GARBAGE (AFTER SPAWN)
            // =====================
            if (ctx.pieceSpawned) {
                applyPendingGarbage(player);
            }

            // =====================
            // 2. SPAWN COLLISION CHECK
            // =====================
            if (ctx.pieceSpawned) {

                if (checkSpawnCollision(player)) {
                    killPlayer(player, status, match.tick);
                }
            }

            if (ctx.piecePlaced && checkSpawnCollision(player)) {
                killPlayer(player, status, match.tick);
            }
        }
    }

    // =====================
    // APPLY GARBAGE TO BOARD
    // =====================
    private void applyPendingGarbage(PlayerState player) {

        var garbage = player.garbage.incoming;
        if (garbage == null) {
            return;
        }

        int lines = garbage.pollAllReady();

        if (lines <= 0)
            return;

        int[][] board = player.board.board;
        int width = board[0].length;
        int height = board.length;

        for (int i = 0; i < lines; i++) {

            // Shift up
            for (int y = 0; y < height - 1; y++) {
                board[y] = board[y + 1];
            }

            int[] row = new int[width];
            int hole = (int) (Math.random() * width);

            for (int x = 0; x < width; x++) {
                row[x] = (x == hole) ? 0 : 1;
            }

            board[height - 1] = row;
        }
    }

    // =====================
    // COLLISION CHECK
    // =====================
    private boolean checkSpawnCollision(PlayerState player) {

        var piece = player.piece.currentPiece;
        var registry = PieceRegistry.getInstance();
        var board = player.board;

        if (piece == null)
            return false;

        for (var block : piece.getBlocks(registry)) {
            if (board.isOccupied(block.x, block.y)) {
                return true;
            }
        }

        return false;
    }

    private void killPlayer(PlayerState player, StatusState status, int tick) {
        status.kill(tick);
        player.piece.currentPiece = null;
    }
}
