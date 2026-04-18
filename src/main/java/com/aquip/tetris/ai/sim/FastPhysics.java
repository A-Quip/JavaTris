package com.aquip.tetris.ai.sim;

import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;

/**
 * Fast movement and collision logic for AI simulation.
 * Mirrors PhysicsHandler but operates on FastBoard for speed.
 */
public class FastPhysics {

    /**
     * Checks if a piece collides with the board at its current position.
     */
    public static boolean collides(FastBoard board, Piece piece) {
        var registry = PieceRegistry.getInstance();
        int[][] shape = registry.getRotation(piece.type, piece.rotation);

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] == 0) continue;

                int bx = piece.x + x;
                int by = piece.y + y;

                // board.isOccupied handles out-of-bounds checks correctly
                if (board.isOccupied(bx, by)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Attempts to rotate a piece using SRS kick tables.
     * 
     * @return The new piece state if rotation was successful, null otherwise.
     */
    public static Piece tryRotate(FastBoard board, Piece piece, int rotationStep) {
        int from = piece.rotation;
        int to = (from + rotationStep) & 3;

        var registry = PieceRegistry.getInstance();
        int[][] kicks = registry.getKicks(piece.type, from, to);

        for (int[] kick : kicks) {
            int dx = kick[0];
            int dy = -kick[1]; // Engine uses inverted Y for SRS kicks

            Piece candidate = new Piece(
                    piece.type,
                    to,
                    piece.x + dx,
                    piece.y + dy);

            if (!collides(board, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Attempts to move a piece by the specified displacement.
     * 
     * @return The new piece state if move was successful, null otherwise.
     */
    public static Piece applyMove(FastBoard board, Piece piece, int dx, int dy) {
        Piece candidate = piece.displace(dx, dy);
        if (!collides(board, candidate)) {
            return candidate;
        }
        return null;
    }

    /**
     * Drops the piece to the lowest non-colliding position.
     * 
     * @return The final piece position after the drop.
     */
    public static Piece hardDrop(FastBoard board, Piece piece) {
        Piece current = piece;
        while (true) {
            Piece next = current.displace(0, 1);
            if (collides(board, next)) {
                break;
            }
            current = next;
        }
        return current;
    }
}
