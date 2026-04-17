package com.aquip.tetris.ui.menu;

public enum GameMode {
    SOLO(
            "Solo",
            "Single-player survival."),
    VS_AI(
            "Vs AI",
            "Human versus planning bot with garbage."),
    TWO_PLAYER(
            "2 Players",
            "Two humans on one keyboard with garbage."),
    AI_DEMO(
            "AI Demo",
            "Watch the bot play by itself.");

    private final String label;
    private final String description;

    GameMode(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }
}
