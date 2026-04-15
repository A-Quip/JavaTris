package com.aquip.tetris.state;

import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;

public class BoardState {

    public final int[][] board;

    public BoardState() {
        this(10, 20);
    }

    public BoardState(int width, int height) {
        this.board = new int[height][width];
    }

    public int getWidth() {
        return board[0].length;
    }

    public int getHeight() {
        return board.length;
    }

    public int get(int x, int y) {
        return board[y][x];
    }

    public void set(int x, int y, int value) {
        board[y][x] = value;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < getWidth()
                && y >= 0 && y < getHeight();
    }

    public boolean isEmpty(int x, int y) {
        return inBounds(x, y) && board[y][x] == 0;
    }

    public boolean isOccupied(int x, int y) {
        return !isEmpty(x, y);
    }

    public boolean collides(Piece piece, PieceRegistry registry) {
        for (Piece.Position p : piece.getBlocks(registry)) {
            if (!inBounds(p.x, p.y) || isOccupied(p.x, p.y)) {
                return true;
            }
        }
        return false;
    }

    public void place(Piece piece, PieceRegistry registry, int value) {
        for (Piece.Position p : piece.getBlocks(registry)) {
            if (inBounds(p.x, p.y)) {
                set(p.x, p.y, value);
            }
        }
    }
}