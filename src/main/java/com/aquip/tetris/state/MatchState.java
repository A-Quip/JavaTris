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