package com.aquip.tetris.menu;

import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.player.PlayerType;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MenuEngineTest {

    @Test
    void confirmStartsSinglePlayerGame() {
        MenuEngine menu = new MenuEngine(
                new File("config/config.yml"),
                new MenuInputMapper()
        );

        InputFrame frame = new InputFrame();
        frame.pressed.add(KeyEvent.VK_ENTER);

        GameEngine game = menu.update(frame);

        assertNotNull(game);
        assertEquals(1, game.getMatchState().players.size());
        assertEquals(PlayerType.HUMAN, game.getMatchState().players.get(0).player.getType());
        assertEquals("P1", game.getMatchState().players.get(0).player.getName());
    }
}
