package com.aquip.tetris.ui.menu;

import com.aquip.tetris.ai.Difficulty;

public class MenuState {

    public MenuOption screen;
    public int selectionIndex;
    public int difficultyIndex;

    public MenuState() {
        this.screen = MenuOption.PLAY;
        this.selectionIndex = 0;
        this.difficultyIndex = Difficulty.SUPERHUMAN.ordinal();
    }

    public GameMode selectedMode() {
        GameMode[] modes = GameMode.values();
        int index = Math.max(0, Math.min(selectionIndex, modes.length - 1));
        return modes[index];
    }

    public Difficulty selectedDifficulty() {
        Difficulty[] diffs = Difficulty.values();
        int index = Math.max(0, Math.min(difficultyIndex, diffs.length - 1));
        return diffs[index];
    }
}
