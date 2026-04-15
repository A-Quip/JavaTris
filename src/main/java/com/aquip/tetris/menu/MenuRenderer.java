package com.aquip.tetris.menu;

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
        System.out.println("> Start Game");
        System.out.println();
        System.out.println("Single-player build");
        System.out.println("Move: Left / Right");
        System.out.println("Rotate: Z / X");
        System.out.println("Soft Drop: Down | Hard Drop: Space | Hold: C");
    }
}
