package com.aquip.tetris.ai;

import com.aquip.tetris.ai.eval.EvaluationFunction;
import com.aquip.tetris.ai.planner.InputPlanner;
import com.aquip.tetris.ai.search.FutureStateGenerator;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.state.MatchState;

import java.util.*;

public class PlanningAI implements AIController {

    private final FutureStateGenerator generator;
    private final EvaluationFunction evaluator;
    private final InputPlanner planner;

    private final Queue<GameInput> inputQueue = new ArrayDeque<>();

    public PlanningAI(FutureStateGenerator generator,
                      EvaluationFunction evaluator,
                      InputPlanner planner) {
        this.generator = generator;
        this.evaluator = evaluator;
        this.planner = planner;
    }

    @Override
    public PlayerInput decide(Player player, MatchState state) {

        PlayerInput input = new PlayerInput();
        input.player = player;

        // If we have no planned inputs, compute a new plan
        if (inputQueue.isEmpty()) {
            computeNewPlan(player, state);
        }

        // Pop next input (or do nothing)
        GameInput next = inputQueue.poll();

        if (next != null) {
            input.inputs = Set.of(next);
        } else {
            input.inputs = Collections.emptySet();
        }

        return input;
    }

    private void computeNewPlan(Player player, MatchState state) {

        MatchState current = state; // assumes this exists

        // 1. Generate future states
        List<MatchState> futures = generator.generate(current);

        if (futures.isEmpty()) return;

        // 2. Evaluate them
        MatchState bestState = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (MatchState s : futures) {
            double score = evaluator.evaluate(s);

            if (score > bestScore) {
                bestScore = score;
                bestState = s;
            }
        }

        if (bestState == null) return;

        // 3. Plan inputs to reach best state
        Queue<GameInput> plannedInputs = planner.plan(current, bestState);

        if (plannedInputs != null) {
            inputQueue.clear();
            inputQueue.addAll(plannedInputs);
        }
    }
}