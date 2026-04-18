package com.aquip.tetris.ui.menu;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.config.GameConfigParser;
import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.state.ConfigState;

import java.io.File;

public class MenuEngine {

    public final File gameConfigFile;
    public final MenuState state;
    private final MenuInputMapper mapper;

    public MenuEngine(File gameConfigFile,
            MenuInputMapper mapper) {

        this.gameConfigFile = gameConfigFile;
        this.state = new MenuState();
        this.mapper = mapper;
    }

    public GameEngine update(InputFrame frame) {

        for (MenuInput input : mapper.map(frame)) {
            if (input != null) {
                handleInput(input);
            }
        }

        return evaluateState();
    }

    private void handleInput(MenuInput input) {
        int maxIndex = GameMode.values().length; // +1 for Difficulty row

        switch (input) {

            case UP -> state.selectionIndex = Math.max(0, state.selectionIndex - 1);
            case DOWN -> state.selectionIndex = Math.min(maxIndex, state.selectionIndex + 1);

            case LEFT -> {
                if (state.selectionIndex == maxIndex) {
                    int diffs = com.aquip.tetris.ai.Difficulty.values().length;
                    state.difficultyIndex = (state.difficultyIndex - 1 + diffs) % diffs;
                }
            }
            case RIGHT -> {
                if (state.selectionIndex == maxIndex) {
                    int diffs = com.aquip.tetris.ai.Difficulty.values().length;
                    state.difficultyIndex = (state.difficultyIndex + 1) % diffs;
                }
            }

            case CONFIRM -> {
                if (state.screen == MenuOption.PLAY) {
                    state.screen = null; // start game
                }
            }

            case BACK -> {
                state.screen = MenuOption.PLAY;
            }
        }
    }

    private GameEngine evaluateState() {

        if (state.screen != null)
            return null;

        return createGame();
    }

    public GameEngine createGame() {
        ConfigState config = GameConfigParser.parse(gameConfigFile);
        AIConfig aiConfig = new AIConfig(state.selectedDifficulty());
        return MenuGameFactory.createGame(state.selectedMode(), config, aiConfig);
    }

    public void showPlayMenu() {
        state.screen = MenuOption.PLAY;
        state.selectionIndex = 0;
    }
}
