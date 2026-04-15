package com.aquip.tetris.piece;

import java.util.ArrayList;
import java.util.List;

public final class Piece {

    public final PieceType type;
    public final int rotation; // 0–3
    public final int x;
    public final int y;

    public Piece(PieceType type, int rotation, int x, int y) {
        this.type = type;
        this.rotation = rotation & 3;
        this.x = x;
        this.y = y;
    }

    public Piece displace(int dx, int dy) {
        return new Piece(type, rotation, x + dx, y + dy);
    }

    public Piece rotate(int direction) {
        return new Piece(type, (rotation + direction) & 3, x, y);
    }

    /**
     * Returns absolute block positions in the board.
     */
    public List<Position> getBlocks(PieceRegistry registry) {
        int[][] shape = registry.getRotation(type, rotation);

        List<Position> blocks = new ArrayList<>();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    blocks.add(new Position(x + col, y + row));
                }
            }
        }

        return blocks;
    }

    public static final class Position {
        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public Piece copy() { return new Piece(type, rotation, x, y);}
}