package com.aquip.tetris.ai;

import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.state.PlayerState;

/**
 * Immutable thread-safe state clone used by the AI search process.
 * Contains all necessary game state information to simulate future moves.
 */
public final class AIGameSnapshot {

    public final int piecesPlacedCount;  // Stale-move sentinel
    public final int currentTick;

    public final Piece currentPiece;     // Immutable — direct reference copy
    public final PieceType heldPiece;    // Nullable enum — direct copy
    public final PieceType[] nextQueue;  // Full visible queue snapshot

    public final int[][] board;          // Deep copy: each row independently cloned

    // Lock/gravity state — required for accurate BFS simulation
    public final int lockTicks;
    public final int lockSlides;
    public final int lockRotations;
    public final int lowestY;
    public final int gravityTicks;
    public final boolean canHold;
    public final int comboCount;
    public final int b2bCount;

    public final boolean lockHardDrop;
    public final boolean gravitySoftDrop;

    public final ConfigState config;    // Immutable — reference copy

    public AIGameSnapshot(int piecesPlacedCount, int currentTick, Piece currentPiece, 
                          PieceType heldPiece, PieceType[] nextQueue, int[][] board, 
                          int lockTicks, int lockSlides, int lockRotations, int lowestY, 
                          int gravityTicks, boolean canHold, int comboCount, int b2bCount,
                          boolean lockHardDrop, boolean gravitySoftDrop, ConfigState config) {
        this.piecesPlacedCount = piecesPlacedCount;
        this.currentTick = currentTick;
        this.currentPiece = currentPiece;
        this.heldPiece = heldPiece;
        this.nextQueue = nextQueue;
        this.board = board;
        this.lockTicks = lockTicks;
        this.lockSlides = lockSlides;
        this.lockRotations = lockRotations;
        this.lowestY = lowestY;
        this.gravityTicks = gravityTicks;
        this.canHold = canHold;
        this.comboCount = comboCount;
        this.b2bCount = b2bCount;
        this.lockHardDrop = lockHardDrop;
        this.gravitySoftDrop = gravitySoftDrop;
        this.config = config;
    }

    /**
     * Creates a deep-cloned snapshot from the current PlayerState.
     * Must be called on the game thread.
     *
     * @param player The current player state to snapshot.
     * @return A thread-safe, immutable snapshot of the game state.
     */
    public static AIGameSnapshot from(PlayerState player) {
        // Deep copy the board — the only field mutated in-place by the engine
        int h = player.board.board.length;
        int[][] boardCopy = new int[h][];
        for (int y = 0; y < h; y++) {
            boardCopy[y] = player.board.board[y].clone();
        }

        return new AIGameSnapshot(
            player.time.piecesPlaced.size(),
            player.time.tick,
            player.piece.currentPiece,
            player.next.held,
            player.next.next.toArray(new PieceType[0]),
            boardCopy,
            player.lock.lockTicks,
            player.lock.slides,
            player.lock.rotations,
            player.lock.lowestY,
            player.gravity.gravityTicks,
            player.next.canHold,
            player.combo.amount(),
            player.b2b.amount(),
            player.lock.hardDrop,
            player.gravity.softDrop,
            player.config
        );
    }
}
