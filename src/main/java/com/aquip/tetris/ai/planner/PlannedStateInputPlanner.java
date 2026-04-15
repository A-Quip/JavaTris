package com.aquip.tetris.ai.planner;

import com.aquip.tetris.ai.search.PlannedMatchState;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.state.MatchState;

import java.util.ArrayDeque;
import java.util.Queue;

public class PlannedStateInputPlanner implements InputPlanner {

    @Override
    public Queue<GameInput> plan(MatchState from, MatchState to) {
        if (to instanceof PlannedMatchState plannedState) {
            return plannedState.plannedInputs();
        }

        return new ArrayDeque<>();
    }
}
