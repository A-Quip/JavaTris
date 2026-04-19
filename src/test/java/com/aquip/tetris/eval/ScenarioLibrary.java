package com.aquip.tetris.eval;

import com.aquip.tetris.piece.PieceType;

import java.util.List;

/**
 * Ready-made scenarios covering the core challenges a Tetris AI should handle.
 *
 * <p>
 * These serve as regression tests: run them after tuning the heuristic to
 * verify nothing regressed. Add new scenarios here as you discover edge-cases.
 */
public class ScenarioLibrary {

    private ScenarioLibrary() {
    }

    // =========================================================================
    // T-Spin setups
    // =========================================================================

    /**
     * Classic T-Spin Double (TSD) setup.
     * The AI must recognise the overhang and rotate a T piece into the slot.
     * Pass condition: ≥2 lines cleared.
     */
    public static EvalScenario tSpinDouble() {
        return EvalScenario.builder("T-Spin Double")
                .board(
                        "## #######",
                        "## #######",
                        "###  #####",
                        "#### #####")
                .queue(PieceType.T)
                .maxPieces(3)
                .requireLines(2)
                .build();
    }

    /**
     * T-Spin Triple (TST) tower.
     * Harder to set up — tests whether the AI bothers with TSTs at all.
     */
    public static EvalScenario tSpinTriple() {
        return EvalScenario.builder("T-Spin Triple")
                .board(
                        "##  ######",
                        "## #######",
                        "##  ######",
                        "### ######",
                        "####  ####",
                        "##### ####")
                .queue(PieceType.T)
                .maxPieces(3)
                .requireLines(3)
                .build();
    }

    // =========================================================================
    // Tetris (4-line clear)
    // =========================================================================

    /**
     * Clean Tetris well — a deep I-piece well on the right.
     * The AI should fill the well with an I piece.
     */
    public static EvalScenario tetrisClean() {
        return EvalScenario.builder("Tetris (clean well)")
                .board(
                        "#########  ", // one column gap on the right (11 chars → board is 10)
                        "#########  ",
                        "#########  ",
                        "#########  ")
                .queue(PieceType.I)
                .maxPieces(3)
                .requireLines(4)
                .build();
    }

    /**
     * Buried Tetris — the well is there but the surface is uneven.
     * Tests whether the AI tidies the surface before the clean 4-line clear.
     */
    public static EvalScenario tetrisBuried() {
        return EvalScenario.builder("Tetris (buried well)")
                .board(
                        "##  ######",
                        "###  #####",
                        "####  ####",
                        "#####  ###",
                        "######  ##",
                        "#######  #",
                        "########  ")
                .queue(PieceType.I, PieceType.S, PieceType.Z, PieceType.L, PieceType.J)
                .maxPieces(8)
                .requireLines(4)
                .build();
    }

    // =========================================================================
    // Perfect clear (PC)
    // =========================================================================

    /**
     * Classic 4-piece perfect clear from an empty board.
     * Standard PC start with a fixed bag.
     */
    public static EvalScenario perfectClearStart() {
        return EvalScenario.builder("Perfect Clear (PC start)")
                .board() // empty board
                .queue(PieceType.L, PieceType.J, PieceType.S, PieceType.Z, PieceType.O, PieceType.T, PieceType.I)
                .maxPieces(10)
                .requireLines(4)
                .requirePerfectClear()
                .build();
    }

    // =========================================================================
    // Garbage clearing
    // =========================================================================

    /**
     * Clears {@code lines} rows of standard messy garbage (one gap per row).
     */
    public static EvalScenario garbageClear(int lines) {
        String[] pattern = buildGarbage(lines);
        return EvalScenario.builder("Garbage clear (" + lines + " lines)")
                .board(pattern)
                .queue(PieceType.I, PieceType.L, PieceType.J, PieceType.S,
                        PieceType.Z, PieceType.T, PieceType.O)
                .maxPieces(lines * 3)
                .requireLines(lines)
                .build();
    }

    // =========================================================================
    // Survival / stress
    // =========================================================================

    /**
     * High stack — board is nearly full. The AI must survive and dig out.
     */
    public static EvalScenario highStack() {
        return EvalScenario.builder("High stack survival")
                .board(
                        " #########",
                        "## #######",
                        "### ######",
                        "#### #####",
                        "####### ##",
                        "#########  ".substring(0, 10),
                        "## #######",
                        "####  ####",
                        "####### ##",
                        "####  ####",
                        "####### ##",
                        "####  ####",
                        "####### ##",
                        "####  ####",
                        "####### ##",
                        "####  ####")
                .maxPieces(20)
                .requireLines(10)
                .build();
    }

    /**
     * Completely empty board — 50 pieces.
     * Measures steady-state efficiency (lines per piece).
     */
    public static EvalScenario openField(int pieces) {
        return EvalScenario.builder("Open field (" + pieces + " pieces)")
                .board()
                .maxPieces(pieces)
                .build();
    }

    // =========================================================================
    // Full suite convenience
    // =========================================================================

    /** Returns a balanced set that covers T-spins, Tetrises, PC, and survival. */
    public static List<EvalScenario> standard() {
        return List.of(
                tSpinDouble(),
                tSpinTriple(),
                tetrisClean(),
                tetrisBuried(),
                perfectClearStart(),
                garbageClear(4),
                garbageClear(8),
                highStack(),
                openField(50));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Builds a garbage pattern — every row filled except one random-ish gap. */
    private static String[] buildGarbage(int lines) {
        String[] result = new String[lines];
        // stagger the gap across rows so they don't all share the same column
        for (int i = 0; i < lines; i++) {
            int gap = i % 10;
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < 10; x++) {
                sb.append(x == gap ? ' ' : '#');
            }
            result[lines - 1 - i] = sb.toString(); // bottom row first
        }
        return result;
    }
}