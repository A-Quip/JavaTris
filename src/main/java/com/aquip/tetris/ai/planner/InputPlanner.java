package com.aquip.tetris.ai.planner;

import java.util.Queue;

import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.state.MatchState;

public interface InputPlanner {

    /**
     * Generate the sequence of inputs needed to reach the target state.
     */
    Queue<GameInput> plan(MatchState from, MatchState to);
}