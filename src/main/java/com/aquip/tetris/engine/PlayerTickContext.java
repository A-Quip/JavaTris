package com.aquip.tetris.engine;

import com.aquip.tetris.input.GameInput;

import java.util.ArrayList;
import java.util.List;

public class PlayerTickContext {

    public final List<GameInput> inputs = new ArrayList<>();

    // =====================
    // INTENT
    // =====================
    public int moveX;
    public int moveY;
    public int rotation;

    public boolean hold;
    public boolean hardDrop;
    public boolean softDrop;

    // =====================
    // RESULTS
    // =====================
    public boolean pieceMoved;
    public boolean pieceLocked;
    public boolean pieceSpawned;
    public boolean pieceGrounded;
    public boolean piecePlaced;

    // =====================
    // MOVE CONTEXT (NEW)
    // =====================
    public final MoveContext moveContext = new MoveContext();

    public void reset() {
        inputs.clear();

        moveX = 0;
        moveY = 0;
        rotation = 0;

        hold = false;
        hardDrop = false;
        softDrop = false;

        pieceMoved = false;
        pieceLocked = false;
        pieceSpawned = false;
        pieceGrounded = false;
        piecePlaced = false;

    }

    public void resetPiece() {
        moveContext.reset();
    }
}
