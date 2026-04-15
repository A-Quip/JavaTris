package com.aquip.tetris.ui.menu;

public class MenuState {

    public MenuOption screen;
    public int selectionIndex;

    public MenuState() {
        this.screen = MenuOption.PLAY;
        this.selectionIndex = 0;
    }

    public GameMode selectedMode() {
        GameMode[] modes = GameMode.values();
        int index = Math.max(0, Math.min(selectionIndex, modes.length - 1));
        return modes[index];
    }
}
