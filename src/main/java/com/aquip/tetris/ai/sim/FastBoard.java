package com.aquip.tetris.ai.sim;

import java.util.Arrays;

/**
 * High-performance bitboard representation of the Tetris board.
 * Each row is represented as a 10-bit integer mask.
 */
public class FastBoard {

    public static final int WIDTH = 10;

    // Height must match the engine's total board height (visible + buffer rows).
    // Usually 24 (20 visible + 4 spawn buffer rows).
    public final int height;
    public final int[] rows; // rows[0] = topmost row, matching engine board[0]

    // --- Cached hash (#6) ---
    // Recomputed lazily on the first hashCode() call after any mutation.
    // Avoids iterating all 24 rows on every HashMap lookup in BeamSearch.
    private int cachedHash;
    private boolean hashDirty = true;

    // --- ThreadLocal line-clear scratch buffer (#5) ---
    // clearLines() is called on every applyPlacement(). Allocating a new
    // int[height] each time generates thousands of short-lived arrays per
    // search. A per-thread buffer is allocated once and reused forever.
    private static final int MAX_HEIGHT = 32; // safe upper bound
    private static final ThreadLocal<int[]> CLEAR_BUFFER = ThreadLocal.withInitial(() -> new int[MAX_HEIGHT]);

    public FastBoard(int height) {
        this.height = height;
        this.rows = new int[height];
    }

    /**
     * Creates a FastBoard from an engine-style 2D array board.
     *
     * @param board The 2D array board from AIGameSnapshot or PlayerState.
     * @return A bit-masked FastBoard instance.
     */
    public static FastBoard fromSnapshot(int[][] board) {
        FastBoard fb = new FastBoard(board.length);
        for (int y = 0; y < board.length; y++) {
            int mask = 0;
            for (int x = 0; x < WIDTH; x++) {
                if (board[y][x] != 0) {
                    mask |= (1 << x);
                }
            }
            fb.rows[y] = mask;
        }
        return fb;
    }

    /**
     * Creates a deep copy of this board, propagating the cached hash state so
     * that an unmodified copy does not recompute on its first lookup.
     */
    public FastBoard copy() {
        FastBoard b = new FastBoard(height);
        System.arraycopy(rows, 0, b.rows, 0, height);
        b.cachedHash = cachedHash;
        b.hashDirty = hashDirty;
        return b;
    }

    /**
     * Checks if a specific cell is occupied.
     */
    public boolean isOccupied(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= height) {
            return true; // out of bounds is considered occupied
        }
        return (rows[y] & (1 << x)) != 0;
    }

    /**
     * Places a piece on the board and clears any full lines.
     *
     * @param pieceRowMasks Pre-computed row bitmasks for the piece rotation,
     *                      already shifted by piece.x.
     * @param pieceY        Top-left anchor Y coordinate of the piece.
     * @return The number of lines cleared.
     */
    public int place(int[] pieceRowMasks, int pieceY) {
        for (int dy = 0; dy < pieceRowMasks.length; dy++) {
            int targetY = pieceY + dy;
            if (targetY >= 0 && targetY < height) {
                rows[targetY] |= pieceRowMasks[dy];
            }
        }
        return clearLines();
    }

    /**
     * Standard Tetris line-clearing logic using a reusable ThreadLocal buffer.
     *
     * <p>
     * The buffer is a plain int[] that is reused across calls on the same
     * thread. After compacting non-full rows from the bottom up, the top
     * {@code cleared} slots (which were never written in this call) are zeroed
     * before copying back, matching the behaviour of the original fresh-array
     * allocation.
     * </p>
     *
     * @return Number of lines cleared.
     */
    private int clearLines() {
        int fullMask = (1 << WIDTH) - 1;
        int write = height - 1;
        int cleared = 0;
        int[] newRows = CLEAR_BUFFER.get();

        for (int y = height - 1; y >= 0; y--) {
            if (rows[y] == fullMask) {
                cleared++;
            } else {
                newRows[write--] = rows[y];
            }
        }

        // After the loop, write == cleared - 1. Positions 0..cleared-1 in the
        // buffer were never written in this call and may hold stale data from a
        // previous use. Zero them before copying so the board's top rows are
        // correctly empty.
        Arrays.fill(newRows, 0, cleared, 0);
        System.arraycopy(newRows, 0, rows, 0, height);

        hashDirty = true; // board state changed — invalidate cached hash
        return cleared;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FastBoard other = (FastBoard) o;
        return Arrays.equals(rows, other.rows);
    }

    /**
     * Returns the hash of this board's row array, recomputing it only when the
     * board has been mutated since the last call. This avoids iterating all rows
     * on every HashMap lookup inside BeamSearch's deduplication map.
     */
    @Override
    public int hashCode() {
        if (hashDirty) {
            cachedHash = Arrays.hashCode(rows);
            hashDirty = false;
        }
        return cachedHash;
    }
}