package com.aquip.tetris.ui.menu;

public enum GameMode {
    SOLO(
            "Solo",
            "Single-player survival.",
            "P1: Arrows, Z/X, C, Space"
    ),
    VS_AI(
            "Vs AI",
            "Human versus planning bot with garbage.",
            "P1: Arrows, Z/X, C, Space"
    ),
    TWO_PLAYER(
            "2 Players",
            "Two humans on one keyboard with garbage.",
            "P1: Arrows/Z/X/C/Space | P2: A/D/Q/W/E/Shift"
    ),
    AI_DEMO(
            "AI Demo",
            "Watch the bot play by itself.",
            "No human input during play"
    );

    private final String label;
    private final String description;
    private final String controls;

    GameMode(String label, String description, String controls) {
        this.label = label;
        this.description = description;
        this.controls = controls;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public String controls() {
        return controls;
    }
}
