package com.aquip.tetris.state;

import com.aquip.tetris.player.Player;

import java.util.ArrayList;
import java.util.List;

public class MatchState {

    public int tick;
    public int seed;
    public List<PlayerState> players;

    // Default constructor
    public MatchState() {
        this(0, 0, new ArrayList<>());
    }

    public MatchState(int tick, int seed, List<PlayerState> players) {
        this.tick = tick;
        this.seed = seed;
        this.players = players != null ? players : new ArrayList<>();
    }

    public boolean singlePlayer() {
        return players.size() == 1;
    }

    public void addPlayer(PlayerState player) {
        players.add(player);
    }

    public int aliveCount() {
        int alive = 0;
        for (PlayerState player : players) {
            if (player.status.alive) {
                alive++;
            }
        }
        return alive;
    }

    public PlayerState getLastAlivePlayer() {
        PlayerState remaining = null;
        for (PlayerState player : players) {
            if (!player.status.alive) {
                continue;
            }

            if (remaining != null) {
                return null;
            }

            remaining = player;
        }
        return remaining;
    }

    public boolean isGameOver() {
        if (players.isEmpty()) {
            return false;
        }

        int alive = aliveCount();
        return singlePlayer() ? alive == 0 : alive <= 1;
    }

    public PlayerState getPlayerState(Player player) {
        for (PlayerState ps : players) {
            if (ps.player.equals(player)) {
                return ps;
            }
        }
        return null;
    }

    public MatchState copy() {
        return new MatchState(tick, seed, players);
    }
}
