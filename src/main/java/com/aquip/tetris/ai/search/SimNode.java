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

    /**
     * Cached heuristic score. Set by BeamSearch after parallel expansion so that
     * repeated sort/log calls never re-invoke FeatureExtractor. NaN = not yet
     * computed (guards against accidental use of a stale zero).
     */
    public double cachedScore = Double.NaN;

    public SimNode(FastBoard board, MoveSequence rootPath, int queueIndex,
            Piece currentPiece, LockState lockState, GravityState gravityState,
            boolean canHold, int totalLinesCleared) {
        this.board = board;
        this.rootPath = rootPath;
        this.queueIndex = queueIndex;
        this.currentPiece = currentPiece;
        this.lockState = lockState;
        this.gravityState = gravityState;
        this.canHold = canHold;
        this.totalLinesCleared = totalLinesCleared;
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

        // Non-linear scoring for setups
        int points = 0;
        if (cleared == 1)
            points = 1;
        else if (cleared == 2)
            points = 3;
        else if (cleared == 3)
            points = 6;
        else if (cleared == 4)
            points = 15;

        // Spin Detection
        if (p.finalPiece.type == PieceType.T) {
            boolean rotatedLast = false;
            for (int i = p.commands.size() - 1; i >= 0; i--) {
                GameInput cmd = p.commands.get(i);
                if (cmd == GameInput.HARD_DROP)
                    continue;
                if (cmd == GameInput.ROTATE_CW || cmd == GameInput.ROTATE_CCW || cmd == GameInput.ROTATE_180) {
                    rotatedLast = true;
                }
                break;
            }
            if (rotatedLast) {
                int corners = 0;
                if (board.isOccupied(p.finalPiece.x, p.finalPiece.y))
                    corners++;
                if (board.isOccupied(p.finalPiece.x + 2, p.finalPiece.y))
                    corners++;
                if (board.isOccupied(p.finalPiece.x, p.finalPiece.y + 2))
                    corners++;
                if (board.isOccupied(p.finalPiece.x + 2, p.finalPiece.y + 2))
                    corners++;
                if (corners >= 3) {
                    points += (cleared > 0) ? (cleared * 10) : 5; // T-Spin reward
                }
            }
        }

        // Advance to next piece in queue
        PieceType nextType = (queueIndex < nextQueue.length) ? nextQueue[queueIndex] : null;
        Piece nextPiece = null;
        if (nextType != null) {
            // Standard spawn: centered at x=3, top of board (matching engine's spawn logic)
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
                this.totalLinesCleared + points);
    }
}