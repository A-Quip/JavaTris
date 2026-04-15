package com.aquip.tetris.ai.eval;

import com.aquip.tetris.state.MatchState;

public interface EvaluationFunction {

    /**
     * Score a match state. Higher = better.
     */
    double evaluate(MatchState state);
}