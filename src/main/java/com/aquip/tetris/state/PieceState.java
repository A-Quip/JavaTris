package com.aquip.tetris.state;

import com.aquip.tetris.piece.Piece;

public class PieceState {

    public Piece currentPiece;

    public PieceState() {
        this(null);
    }

    public PieceState(Piece piece) {
        this.currentPiece = piece;
    }

    public boolean hasPiece() {
        return currentPiece != null;
    }

    public void clear() {
        currentPiece = null;
    }
}