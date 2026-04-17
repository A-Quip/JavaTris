package com.aquip.tetris.ai;

import com.aquip.tetris.ai.eval.HeuristicEvaluationFunction;
import com.aquip.tetris.ai.planner.PlannedStateInputPlanner;
import com.aquip.tetris.ai.search.PlacementFutureStateGenerator;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.MatchState;

public class HeuristicAI implements AIController {

    private final PlanningAI delegate;

    public HeuristicAI(Player player, AIConfig aiConfig) {
        this.delegate = new PlanningAI(
                new PlacementFutureStateGenerator(player),
                new HeuristicEvaluationFunction(player),
                new PlannedStateInputPlanner(),
                aiConfig.tickDelay);
    }

    @Override
    public PlayerInput decide(Player player, MatchState state) {
        return delegate.decide(player, state);
    }
}
