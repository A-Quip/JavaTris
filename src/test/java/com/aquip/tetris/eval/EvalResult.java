package com.aquip.tetris.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of everything the evaluator recorded while running one
 * {@link EvalScenario}.
 */
public class EvalResult {

    // ---- Identity -----------------------------------------------------------

    public final String scenarioName;

    // ---- Termination --------------------------------------------------------

    public enum TerminationReason {
        /** The AI placed all pieces up to maxPieces without dying. */
        COMPLETED,
        /** The AI topped out before placing all pieces. */
        GAME_OVER,
        /** The tick cap was reached before the scenario completed. */
        TIMEOUT
    }

    public final TerminationReason termination;

    // ---- Core metrics -------------------------------------------------------

    public final int totalLinesCleared;
    public final int totalScore;
    public final int piecesPlaced;
    public final int totalTicks;

    // ---- Line-clear breakdown -----------------------------------------------

    public final int singles;
    public final int doubles;
    public final int triples;
    public final int tetrises;
    public final int tSpins; // any T-spin clear (mini or full)
    public final int perfectClears;

    // ---- Combo / B2B --------------------------------------------------------

    public final int maxCombo;
    public final int maxB2B;

    // ---- Efficiency ---------------------------------------------------------

    /** Lines per piece — a rough measure of how well the AI packed the board. */
    public final double linesPerPiece;

    /** Attack sent per piece (using standard attack table). */
    public final double attackPerPiece;

    // ---- Per-piece timeline -------------------------------------------------

    /**
     * One entry per piece placed.
     * Useful for charting how metrics evolved over the course of the scenario.
     */
    public final List<PieceMoment> timeline;

    // ---- Pass / Fail --------------------------------------------------------

    public final boolean passed;

    // =========================================================================

    private EvalResult(Builder b) {
        this.scenarioName = b.scenarioName;
        this.termination = b.termination;
        this.totalLinesCleared = b.totalLinesCleared;
        this.totalScore = b.totalScore;
        this.piecesPlaced = b.piecesPlaced;
        this.totalTicks = b.totalTicks;
        this.singles = b.singles;
        this.doubles = b.doubles;
        this.triples = b.triples;
        this.tetrises = b.tetrises;
        this.tSpins = b.tSpins;
        this.perfectClears = b.perfectClears;
        this.maxCombo = b.maxCombo;
        this.maxB2B = b.maxB2B;
        this.linesPerPiece = piecesPlaced > 0 ? (double) totalLinesCleared / piecesPlaced : 0;
        this.attackPerPiece = piecesPlaced > 0 ? (double) b.totalAttack / piecesPlaced : 0;
        this.timeline = Collections.unmodifiableList(b.timeline);
        this.passed = b.passed;
    }

    // =========================================================================
    // Pretty-print
    // =========================================================================

    @Override
    public String toString() {
        String statusIcon = passed ? "PASS" : "FAIL";
        return """
                +---------------------------------------------------+
                       | STATUS:  %-39s  |
                       | Lines:  %5d  |  Pieces: %5d  |  L/P: %8.2f |
                       | Score:  %5d  |  Ticks:  %5d  |  A/P: %8.2f |
                       +---------------------------------------------------+
                       """.formatted(
                statusIcon,
                totalLinesCleared, piecesPlaced, linesPerPiece,
                totalScore, totalTicks, attackPerPiece);
    }

    // =========================================================================
    // Per-piece snapshot
    // =========================================================================

    public static class PieceMoment {
        public final int pieceIndex;
        public final int linesThisPiece;
        public final int scoreThisPiece;
        public final int comboAtPlacement;
        public final int b2bAtPlacement;

        public PieceMoment(int pieceIndex, int lines, int score, int combo, int b2b) {
            this.pieceIndex = pieceIndex;
            this.linesThisPiece = lines;
            this.scoreThisPiece = score;
            this.comboAtPlacement = combo;
            this.b2bAtPlacement = b2b;
        }
    }

    // =========================================================================
    // Builder — used by EvalRunner
    // =========================================================================

    public static class Builder {

        String scenarioName;
        TerminationReason termination = TerminationReason.COMPLETED;

        int totalLinesCleared;
        int totalScore;
        int piecesPlaced;
        int totalTicks;
        int totalAttack;

        int singles, doubles, triples, tetrises, tSpins, perfectClears;
        int maxCombo, maxB2B;

        boolean passed;

        final List<PieceMoment> timeline = new ArrayList<>();

        public Builder(String scenarioName) {
            this.scenarioName = scenarioName;
        }

        public EvalResult build() {
            return new EvalResult(this);
        }
    }
}