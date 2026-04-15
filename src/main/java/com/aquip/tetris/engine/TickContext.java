package com.aquip.tetris.engine;

import com.aquip.tetris.engine.event.GameEvent;
import com.aquip.tetris.state.PlayerState;

import java.util.*;

public class TickContext {

    private final List<GameEvent> events = new ArrayList<>();
    private final Map<PlayerState, PlayerTickContext> playerContexts = new HashMap<>();

    public void emit(GameEvent event) {
        events.add(event);
    }

    public List<GameEvent> getEvents() {
        return events;
    }

    public PlayerTickContext get(PlayerState player) {
        return playerContexts.computeIfAbsent(player, p -> new PlayerTickContext());
    }

    public void reset(Collection<PlayerState> players) {
        events.clear();

        for (PlayerState player : players) {
            get(player).reset();
        }
    }
}