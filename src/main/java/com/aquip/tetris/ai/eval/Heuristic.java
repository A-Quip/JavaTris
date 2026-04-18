package com.aquip.tetris.ai.eval;

import com.aquip.tetris.ai.search.SimNode;

/**
 * Evaluates a board state and piece placement to produce a score.
 * Used by the BeamSearch to rank candidate moves.
 */
public class Heuristic {

    private final HeuristicWeights weights;

    public Heuristic() {
        this(new HeuristicWeights());
    }

    public Heuristic(HeuristicWeights weights) {
        this.weights = weights;
    }

    /**
     * Evaluates the quality of a given SimNode state by extracting features
     * and applying the weight vector.
     * 
     * @param node The node to evaluate.
     * @return A numerical score representing the value of the state.
     */
    public double evaluate(SimNode node) {
        return FeatureExtractor.evaluateScore(node.board, node.totalLinesCleared, 
                node.comboCount, node.isBackToBack, weights);
    }
}
