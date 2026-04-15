package com.aquip.tetris.ui.menu;

public class MenuRenderer {

    public void render(MenuState state) {

        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("==== TETRIS ====");
        System.out.println();

        if (state.screen == MenuOption.PLAY) {
            renderPlayScreen(state);
        }

        System.out.println();
        System.out.println("Press Enter to start");
    }

    private void renderPlayScreen(MenuState state) {
        for (GameMode mode : GameMode.values()) {
            String prefix = mode == state.selectedMode() ? "> " : "  ";
            System.out.println(prefix + mode.label());
        }

        System.out.println();
        System.out.println(state.selectedMode().description());
        System.out.println(state.selectedMode().controls());
    }
}
