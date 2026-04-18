package com.aquip.tetris.ai.search;

import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.piece.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates a planned sequence of inputs for a single Tetris piece,
 * along with the expected positions of that piece at each command step.
 */
public class MoveSequence {

    /**
     * Sentinel value returned when no valid path is found, triggering an immediate drop.
     */
    public static final MoveSequence HARD_DROP_NOW = new MoveSequence(
            Collections.singletonList(GameInput.HARD_DROP), 
            new Piece[0], 
            -1
    );

    /**
     * The sequence of GameInput commands to be emitted to the engine.
     */
    public final List<GameInput> commands;
    
    /**
     * The absolute (x, y, rotation) states the piece should occupy BEFORE 
     * each command index is emitted. Used by the Drift Guard to detect
     * unexpected engine deviations.
     */
    public final Piece[] expectedPositions;
    
    /**
     * The engine's total pieces placed count at the moment the snapshot was taken.
     * Used by AIInputSource to detect if a result arrived after the piece has already locked.
     */
    public final int piecesPlacedWhenGenerated;

    public MoveSequence(List<GameInput> commands, Piece[] expectedPositions, int piecesPlacedWhenGenerated) {
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
        this.expectedPositions = expectedPositions;
        this.piecesPlacedWhenGenerated = piecesPlacedWhenGenerated;
    }
}
