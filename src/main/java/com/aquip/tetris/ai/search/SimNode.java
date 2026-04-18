package com.aquip.tetris.ai.search;

import com.aquip.tetris.ai.sim.FastBoard;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.state.LockState;
import com.aquip.tetris.state.GravityState;

/**
 * Represents a candidate state in the beam search.
 * Contains a board state, current piece parameters, and the original path
 * required to reach this branch of the search.
 */
public class SimNode {

    public final FastBoard board;
    public final MoveSequence rootPath;
    public final int queueIndex;

    public final Piece currentPiece;
    public final LockState lockState;
    public final GravityState gravityState;
    public final boolean canHold;

    public final int totalLinesCleared;
    public final int comboCount;
    public final boolean isBackToBack;

    /**
     * Cached heuristic score. Set by BeamSearch after parallel expansion so that
     * repeated sort/log calls never re-invoke FeatureExtractor. NaN = not yet
     * computed (guards against accidental use of a stale zero).
     */
    public double cachedScore = Double.NaN;

    public SimNode(FastBoard board, MoveSequence rootPath, int queueIndex,
            Piece currentPiece, LockState lockState, GravityState gravityState,
            boolean canHold, int totalLinesCleared, int comboCount, boolean isBackToBack) {
        this.board = board;
        this.rootPath = rootPath;
        this.queueIndex = queueIndex;
        this.currentPiece = currentPiece;
        this.lockState = lockState;
        this.gravityState = gravityState;
        this.canHold = canHold;
        this.totalLinesCleared = totalLinesCleared;
        this.comboCount = comboCount;
        this.isBackToBack = isBackToBack;
    }

    /**
     * Applies a placement to this node, returning a new node for the next piece in
     * the queue.
     *
     * @param p         The placement to apply.
     * @param nextQueue The full visible piece queue snapshot.
     * @return A new SimNode representing the state after placement and new piece
     *         spawn.
     */
    public SimNode applyPlacement(BFSPathFinder.Placement p, PieceType[] nextQueue) {
        FastBoard nextBoard = board.copy();

        // Get pre-computed masks and shift them for fast placement
        int[] masks = PieceRegistry.getInstance().getRotationMasks(p.finalPiece.type, p.finalPiece.rotation);
        int[] shiftedMasks = new int[masks.length];
        for (int i = 0; i < masks.length; i++) {
            if (p.finalPiece.x < 0) {
                shiftedMasks[i] = masks[i] >> (-p.finalPiece.x);
            } else {
                shiftedMasks[i] = masks[i] << p.finalPiece.x;
            }
        }

        int cleared = nextBoard.place(shiftedMasks, p.finalPiece.y);

        // --- SCORE & STREAK UPDATES ---
        boolean isSpin = false;
        if (p.finalPiece.type == PieceType.T) {
            boolean rotatedLast = false;
            for (int i = p.commands.size() - 1; i >= 0; i--) {
                GameInput cmd = p.commands.get(i);
                if (cmd == GameInput.HARD_DROP || cmd == GameInput.NONE) continue;
                if (cmd == GameInput.ROTATE_CW || cmd == GameInput.ROTATE_CCW || cmd == GameInput.ROTATE_180) {
                    rotatedLast = true;
                }
                break;
            }
            if (rotatedLast) {
                int cx = p.finalPiece.x + 1;
                int cy = p.finalPiece.y + 1;
                int corners = 0;
                if (board.isOccupied(cx - 1, cy - 1)) corners++;
                if (board.isOccupied(cx + 1, cy - 1)) corners++;
                if (board.isOccupied(cx - 1, cy + 1)) corners++;
                if (board.isOccupied(cx + 1, cy + 1)) corners++;
                if (corners >= 3) isSpin = true;
            }
        }

        int nextCombo = (cleared > 0) ? (comboCount + 1) : 0;
        boolean nextB2B = isBackToBack;
        if (cleared > 0) {
            boolean b2bAction = (cleared == 4 || isSpin);
            if (b2bAction) {
                // Keep B2B alive or start it if already active? (Following standard rules)
                // In many modern systems, B2B is active if the LAST clear was also B2B.
            } else {
                nextB2B = false;
            }
            if (b2bAction) nextB2B = true; 
        }

        int points = cleared;
        if (isSpin) points += 5; // T-Spin bonus
        if (nextCombo > 1) points += (nextCombo / 2); // Combo bonus
        if (isBackToBack && (cleared == 4 || isSpin)) points += 2; // B2B bonus

        // Advance to next piece in queue
        PieceType nextType = (queueIndex < nextQueue.length) ? nextQueue[queueIndex] : null;
        Piece nextPiece = null;
        if (nextType != null) {
            nextPiece = new Piece(nextType, 0, 3, 3);
        }

        // New state always gets fresh lock/gravity reset
        return new SimNode(
                nextBoard,
                this.rootPath, // Continue tracking the original move that led to this branch
                this.queueIndex + 1,
                nextPiece,
                new LockState(),
                new GravityState(),
                true, // Can hold again after a placement locks
                this.totalLinesCleared + points,
                nextCombo,
                nextB2B);
    }
}