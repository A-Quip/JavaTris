package com.aquip.tetris.ai.search;

import com.aquip.tetris.state.MatchState;

import java.util.List;

public interface FutureStateGenerator {

    /**
     * Given the current match state, return all possible resulting states
     * after placing the current piece (and optionally lookahead).
     */
    List<MatchState> generate(MatchState current);
}