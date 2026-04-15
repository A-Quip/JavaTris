package com.aquip.tetris.ai.search;

import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class PlannedMatchState extends MatchState {

    private final UUID plannerId;
    private final Queue<GameInput> plannedInputs;
    private final int linesCleared;
    private final int garbageSent;
    private final boolean usedHold;

    public PlannedMatchState(int tick,
                             int seed,
                             List<PlayerState> players,
                             UUID plannerId,
                             Queue<GameInput> plannedInputs,
                             int linesCleared,
                             int garbageSent,
                             boolean usedHold) {
        super(tick, seed, players);
        this.plannerId = plannerId;
        this.plannedInputs = new ArrayDeque<>(plannedInputs);
        this.linesCleared = linesCleared;
        this.garbageSent = garbageSent;
        this.usedHold = usedHold;
    }

    public UUID plannerId() {
        return plannerId;
    }

    public Queue<GameInput> plannedInputs() {
        return new ArrayDeque<>(plannedInputs);
    }

    public int linesCleared() {
        return linesCleared;
    }

    public int garbageSent() {
        return garbageSent;
    }

    public boolean usedHold() {
        return usedHold;
    }
}
