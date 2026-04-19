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
 *
 * <h3>Spin detection</h3>
 * Spin bonuses are only applied when {@code cleared > 0}. A T-spin position
 * with no line clear has no scoring value, so rewarding it in the heuristic
 * would cause the AI to waste pieces "setting up" spins that never pay off.
 *
 * Full T-Spin vs Mini T-Spin distinction follows the standard SRS front-corner
 * rule (matching {@link com.aquip.tetris.placement.SpinDetector}):
 * <ul>
 * <li>Full T-Spin: both front corners are occupied → higher bonus</li>
 * <li>T-Spin Mini: only one front corner occupied → smaller bonus</li>
 * </ul>
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
     * computed.
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
     * @return A new SimNode representing the state after placement.
     */
    public SimNode applyPlacement(BFSPathFinder.Placement p, PieceType[] nextQueue) {
        FastBoard nextBoard = board.copy();

        // Shift pre-computed rotation masks by piece.x for fast bitboard placement
        int[] masks = PieceRegistry.getInstance().getRotationMasks(p.finalPiece.type, p.finalPiece.rotation);
        int[] shiftedMasks = new int[masks.length];
        for (int i = 0; i < masks.length; i++) {
            shiftedMasks[i] = (p.finalPiece.x < 0)
                    ? masks[i] >> (-p.finalPiece.x)
                    : masks[i] << p.finalPiece.x;
        }

        int cleared = nextBoard.place(shiftedMasks, p.finalPiece.y);

        // ---- Spin detection -------------------------------------------------
        // Only evaluate when lines were actually cleared. A T-spin with 0 lines
        // has no scoring value; awarding it encourages wasteful setups.
        SpinKind spin = detectSpin(p, cleared);

        // ---- Streak updates -------------------------------------------------
        int nextCombo = (cleared > 0) ? (comboCount + 1) : 0;

        boolean b2bAction = (cleared == 4 || spin == SpinKind.FULL);
        boolean nextB2B;
        if (cleared == 0) {
            nextB2B = isBackToBack; // no clear → streaks unchanged
        } else if (b2bAction) {
            nextB2B = true; // tetris or full T-spin keeps / starts B2B
        } else {
            nextB2B = false; // non-B2B clear breaks the streak
        }

        // ---- Heuristic point tally ------------------------------------------
        // This is used only by the beam search scoring, not the real score system.
        int points = cleared;

        switch (spin) {
            case FULL -> points += 5; // Full T-Spin: high reward (TSD ≈ 1200 pts real)
            case MINI -> points += 2; // T-Spin Mini: smaller reward
            case NONE -> {
            }
        }

        if (nextCombo > 1)
            points += (nextCombo / 2);

        // B2B bonus only for actions that qualify (tetris or full T-spin)
        if (isBackToBack && b2bAction)
            points += 2;

        // ---- Advance to next piece ------------------------------------------
        PieceType nextType = (queueIndex < nextQueue.length) ? nextQueue[queueIndex] : null;
        Piece nextPiece = (nextType != null) ? new Piece(nextType, 0, 3, 3) : null;

        return new SimNode(
                nextBoard,
                this.rootPath,
                this.queueIndex + 1,
                nextPiece,
                new LockState(),
                new GravityState(),
                true,
                this.totalLinesCleared + points,
                nextCombo,
                nextB2B);
    }

    // =========================================================================
    // Spin detection
    // =========================================================================

    private enum SpinKind {
        NONE, MINI, FULL
    }

    /**
     * Detects whether the placement was a T-spin (full or mini).
     *
     * <p>
     * Matches the logic in {@link com.aquip.tetris.placement.SpinDetector}:
     * <ol>
     * <li>Piece must be T.</li>
     * <li>Placement must have ended with a rotation command.</li>
     * <li>At least 3 of the 4 corner cells around the T-centre must be
     * occupied.</li>
     * <li>If both <em>front</em> corners are occupied → Full T-Spin;
     * otherwise → Mini.</li>
     * </ol>
     *
     * <p>
     * Uses {@code board} (state before placement) for the corner check,
     * which is correct: the surrounding cells that define the slot are not the
     * ones being cleared.
     *
     * @param p       The placement being evaluated.
     * @param cleared Lines cleared by this placement (0 → always
     *                {@link SpinKind#NONE}).
     */
    private SpinKind detectSpin(BFSPathFinder.Placement p, int cleared) {
        if (cleared == 0)
            return SpinKind.NONE;
        if (p.finalPiece.type != PieceType.T)
            return SpinKind.NONE;

        // Was the last meaningful command a rotation?
        boolean rotatedLast = false;
        for (int i = p.commands.size() - 1; i >= 0; i--) {
            GameInput cmd = p.commands.get(i);
            if (cmd == GameInput.HARD_DROP || cmd == GameInput.NONE)
                continue;
            rotatedLast = (cmd == GameInput.ROTATE_CW
                    || cmd == GameInput.ROTATE_CCW
                    || cmd == GameInput.ROTATE_180);
            break;
        }
        if (!rotatedLast)
            return SpinKind.NONE;

        // Corner occupancy check around T-centre (cx, cy)
        int cx = p.finalPiece.x + 1;
        int cy = p.finalPiece.y + 1;

        boolean tl = board.isOccupied(cx - 1, cy - 1); // top-left
        boolean tr = board.isOccupied(cx + 1, cy - 1); // top-right
        boolean bl = board.isOccupied(cx - 1, cy + 1); // bottom-left
        boolean br = board.isOccupied(cx + 1, cy + 1); // bottom-right

        int occupied = (tl ? 1 : 0) + (tr ? 1 : 0) + (bl ? 1 : 0) + (br ? 1 : 0);
        if (occupied < 3)
            return SpinKind.NONE;

        // Front corners depend on the T facing direction (rotation & 3)
        boolean front1, front2;
        switch (p.finalPiece.rotation & 3) {
            case 0 -> {
                front1 = tl;
                front2 = tr;
            } // facing up → top corners are front
            case 1 -> {
                front1 = tr;
                front2 = br;
            } // facing right → right corners are front
            case 2 -> {
                front1 = bl;
                front2 = br;
            } // facing down → bottom corners are front
            default -> {
                front1 = tl;
                front2 = bl;
            } // facing left → left corners are front
        }

        // Both front corners occupied → Full T-Spin; one → Mini
        return (front1 && front2) ? SpinKind.FULL : SpinKind.MINI;
    }
}