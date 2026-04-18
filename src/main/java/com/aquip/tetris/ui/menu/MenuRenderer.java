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
        GameMode[] modes = GameMode.values();
        for (int i = 0; i < modes.length; i++) {
            GameMode mode = modes[i];
            String prefix = i == state.selectionIndex ? "> " : "  ";
            System.out.println(prefix + mode.label());
        }

        // Add Difficulty row
        String diffPrefix = state.selectionIndex == modes.length ? "> " : "  ";
        System.out.println(diffPrefix + "Difficulty: < " + state.selectedDifficulty().name() + " >");

        System.out.println();
        if (state.selectionIndex < modes.length) {
            System.out.println(state.selectedMode().description());
        } else {
            System.out.println("Adjust AI difficulty level (affects search depth and speed).");
        }
    }
}
