package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.PlayerTickContext;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;
import com.aquip.tetris.placement.PlacementResult;
import com.aquip.tetris.placement.SpinResult;

public class PlacementHandler implements PlayerHandler {

    @Override
    public void apply(PlayerState player, TickContext context) {

        PlayerTickContext ctx = context.get(player);

        if (!ctx.pieceLocked) return;
        if (!player.piece.hasPiece()) return;

        player.next.canHold = true;

        Piece piece = player.piece.currentPiece;
        int[][] board = player.board.board;

        var registry = PieceRegistry.getInstance();
        int[][] shape = registry.getRotation(piece.type, piece.rotation);

        // =====================
        // 1. SPIN DETECTION
        // =====================
        SpinResult spin = detectSpin(player, ctx, piece);

        // =====================
        // 2. PLACE PIECE
        // =====================
        placePiece(board, piece, shape);

        // =====================
        // 3. CLEAR LINES
        // =====================
        int linesCleared = clearLines(board);

        // =====================
        // 4. RESULT
        // =====================
        PlacementResult result = new PlacementResult(
                piece.type,
                spin,
                piece.x,
                piece.y,
                piece.rotation,
                linesCleared
        );

        player.time.addPlacement(result);

        ctx.piecePlaced = true;

        player.piece.currentPiece = null;
        ctx.pieceSpawned = true;
    }

    // =====================
    // SPIN DETECTION
    // =====================
    private SpinResult detectSpin(PlayerState player, PlayerTickContext ctx, Piece piece) {

        var move = ctx.moveContext;

        //System.out.println("detectSpin called: rotated=" + move.rotated + " piece=" + piece.type);

        if (!move.rotated) {
            return SpinResult.NONE;
        }

        if (piece.type == com.aquip.tetris.piece.PieceType.T) {
            return detectTSpin(player, piece);
        }

        return SpinResult.NONE;
    }

    private SpinResult detectTSpin(PlayerState player, Piece piece) {

        int[][] board = player.board.board;

        int cx = piece.x + 1;
        int cy = piece.y + 1;

        boolean[] corners = new boolean[4];

        corners[0] = isOccupied(board, cx - 1, cy - 1); // TL
        corners[1] = isOccupied(board, cx + 1, cy - 1); // TR
        corners[2] = isOccupied(board, cx - 1, cy + 1); // BL
        corners[3] = isOccupied(board, cx + 1, cy + 1); // BR

        int occupied = 0;
        for (boolean c : corners) if (c) occupied++;

        //System.out.println("Corners: " +
        //        corners[0] + "," + corners[1] + "," +
        //        corners[2] + "," + corners[3] +
        //        " occupied=" + occupied);

        if (occupied < 3) return SpinResult.NONE;

        boolean front1 = false;
        boolean front2 = false;

        switch (piece.rotation) {
            case 0: // Up
                front1 = corners[0];
                front2 = corners[1];
                break;
            case 1: // Right
                front1 = corners[1];
                front2 = corners[3];
                break;
            case 2: // Down
                front1 = corners[2];
                front2 = corners[3];
                break;
            case 3: // Left
                front1 = corners[0];
                front2 = corners[2];
                break;
        }

        return (front1 && front2) ? SpinResult.T_SPIN : SpinResult.T_SPIN_MINI;
    }

    // =====================
    // PLACE PIECE
    // =====================
    private void placePiece(int[][] board, Piece piece, int[][] shape) {

        int pieceValue = piece.type.ordinal() + 1;

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {

                if (shape[y][x] == 0) continue;

                int bx = piece.x + x;
                int by = piece.y + y;

                if (by >= 0 && by < board.length &&
                        bx >= 0 && bx < board[0].length) {
                    board[by][bx] = pieceValue;
                }
            }
        }
    }

    // =====================
    // LINE CLEARING
    // =====================
    private int clearLines(int[][] board) {

        int height = board.length;
        int width = board[0].length;

        int[][] newBoard = new int[height][width];

        int writeY = height - 1;
        int linesCleared = 0;

        for (int y = height - 1; y >= 0; y--) {

            boolean full = true;

            for (int x = 0; x < width; x++) {
                if (board[y][x] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
            } else {
                newBoard[writeY] = board[y].clone();
                writeY--;
            }
        }

        while (writeY >= 0) {
            newBoard[writeY] = new int[width];
            writeY--;
        }

        for (int y = 0; y < height; y++) {
            board[y] = newBoard[y];
        }

        return linesCleared;
    }

    private boolean isOccupied(int[][] board, int x, int y) {
        if (x < 0 || x >= board[0].length || y < 0 || y >= board.length) {
            return true;
        }
        return board[y][x] != 0;
    }
}
