package com.aquip.tetris.eval;

import com.aquip.tetris.piece.PieceType;

/**
 * Describes a board scenario for the AI to solve.
 */
public class EvalScenario {

    public final String name;

    /**
     * Bottom-aligned board pattern rows (top → bottom). '#' = filled, ' ' = empty.
     */
    public final String[] boardPattern;

    /** Pieces to prepend to the queue. The AI will see these first. */
    public final PieceType[] forcedQueue;

    /** Optional held piece at scenario start. Null = no hold. */
    public final PieceType initialHeld;

    /** Stop evaluating after this many pieces are placed (0 = unlimited). */
    public final int maxPieces;

    /**
     * Hard cap on game ticks before the scenario is marked timed-out (0 =
     * unlimited).
     */
    public final int maxTicks;

    /** Minimum lines the AI must clear to be considered a pass. */
    public final int requiredLines;

    /**
     * If true the scenario passes only if the board is fully empty after
     * completion.
     */
    public final boolean requirePerfectClear;

    // -------------------------------------------------------------------------

    private EvalScenario(Builder b) {
        this.name = b.name;
        this.boardPattern = b.boardPattern;
        this.forcedQueue = b.forcedQueue;
        this.initialHeld = b.initialHeld;
        this.maxPieces = b.maxPieces;
        this.maxTicks = b.maxTicks;
        this.requiredLines = b.requiredLines;
        this.requirePerfectClear = b.requirePerfectClear;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {

        private final String name;
        private String[] boardPattern = new String[0];
        private PieceType[] forcedQueue = new PieceType[0];
        private PieceType initialHeld = null;
        private int maxPieces = 10;
        private int maxTicks = 10_000;
        private int requiredLines = 0;
        private boolean requirePerfectClear = false;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Sets the board pattern. Rows should all be the same width as the
         * board (default 10). The pattern will be bottom-aligned.
         */
        public Builder board(String... rows) {
            this.boardPattern = rows;
            return this;
        }

        /**
         * Pieces that will be placed at the front of the player's queue before the
         * scenario starts. You can pass as many as you like; the normal bag
         * randomiser fills in the rest.
         */
        public Builder queue(PieceType... pieces) {
            this.forcedQueue = pieces;
            return this;
        }

        public Builder held(PieceType piece) {
            this.initialHeld = piece;
            return this;
        }

        /** Scenario ends after N pieces are placed. */
        public Builder maxPieces(int n) {
            this.maxPieces = n;
            return this;
        }

        /** Hard tick cap — prevents infinite loops on unsolvable scenarios. */
        public Builder maxTicks(int n) {
            this.maxTicks = n;
            return this;
        }

        /** The AI must clear at least this many lines for the scenario to pass. */
        public Builder requireLines(int n) {
            this.requiredLines = n;
            return this;
        }

        /** The AI must achieve a perfect clear (fully empty board) to pass. */
        public Builder requirePerfectClear() {
            this.requirePerfectClear = true;
            return this;
        }

        public EvalScenario build() {
            return new EvalScenario(this);
        }
    }
}