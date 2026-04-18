package com.aquip.tetris.ai.eval;

/**
 * Tunable weight vector for the AI's heuristic evaluation.
 * These weights determine how much the AI values or penalizes different
 * board features (e.g., holes, height, transitions).
 */
public class HeuristicWeights {

    // Common baseline weights for heuristic-based Tetris AIs
    public double aggregateHeight = -0.510066;
    public double maxHeight = -0.124312;
    public double bumpiness = -0.184483;
    public double holes = -0.35663;
    public double holeDepth = -0.152341;
    public double rowTransitions = -0.321788;
    public double colTransitions = -0.937997;
    public double linesCleared = 0.760666;

    // Setup specific weights
    public double tetrisWellDepth = 0.5;
    public double tSpinSetups = 2.0;

    public double combo = 1.0;
    public double b2b = 1.5;

    public HeuristicWeights() {
    }

    public HeuristicWeights(double aggregateHeight, double maxHeight, double bumpiness,
            double holes, double holeDepth, double rowTransitions,
            double colTransitions, double linesCleared) {
        this.aggregateHeight = aggregateHeight;
        this.maxHeight = maxHeight;
        this.bumpiness = bumpiness;
        this.holes = holes;
        this.holeDepth = holeDepth;
        this.rowTransitions = rowTransitions;
        this.colTransitions = colTransitions;
        this.linesCleared = linesCleared;
        this.tetrisWellDepth = 0.5;
        this.tSpinSetups = 2.0;
        this.combo = 1.0;
        this.b2b = 1.5;
    }
}
