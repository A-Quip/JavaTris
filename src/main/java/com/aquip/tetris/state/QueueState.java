package com.aquip.tetris.state;

import com.aquip.tetris.piece.PieceType;

import java.util.LinkedList;
import java.util.Queue;

public class QueueState {

    public Queue<PieceType> next;
    public PieceType held;
    public boolean canHold;

    public QueueState() {
        this.next = new LinkedList<>();
        this.held = null;
        this.canHold = true;
    }
}