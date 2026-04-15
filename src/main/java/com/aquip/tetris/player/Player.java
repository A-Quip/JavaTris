package com.aquip.tetris.player;

import java.util.UUID;
import java.util.Objects;

public class Player {

    private final UUID id;
    private final PlayerType type;
    private final String name;

    public Player(UUID id, PlayerType type, String name) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.name = name;
    }

    // getters
    public UUID getId() { return id; }
    public PlayerType getType() { return type; }
    public String getName() { return name; }

    //logic
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Player{id=" + id + "}";
    }
}
