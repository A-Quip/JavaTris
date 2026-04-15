package com.aquip.tetris.menu;

public class MenuState {

    public MenuOption screen;
    public int selectionIndex;

    public MenuState() {
        this.screen = MenuOption.PLAY;
        this.selectionIndex = 0;
    }
}
