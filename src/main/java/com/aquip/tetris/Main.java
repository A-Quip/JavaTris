package com.aquip.tetris;

import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.input.InputSource;
import com.aquip.tetris.input.SwingInputSource;
import com.aquip.tetris.ui.game.GamePanel;
import com.aquip.tetris.ui.menu.MenuEngine;
import com.aquip.tetris.ui.menu.MenuInputMapper;
import com.aquip.tetris.ui.menu.MenuPanel;

import javax.swing.*;
import java.io.File;
import java.awt.event.KeyEvent;

public class Main {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Tetris");
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MenuPanel menuPanel = new MenuPanel();
        GamePanel gamePanel = new GamePanel();

        frame.setContentPane(menuPanel);
        frame.setVisible(true);
        requestFocus(frame);

        // ONE input source
        InputSource inputSource = new SwingInputSource(frame.getRootPane());

        MenuEngine menu = new MenuEngine(
                new File("config/config.yml"),
                new MenuInputMapper()
        );
        menuPanel.setState(menu.state);

        GameEngine game = null;

        while (true) {

            InputFrame inputFrame = inputSource.poll();

            if (game == null) {

                game = menu.update(inputFrame);

                menuPanel.setState(menu.state);
                menuPanel.repaint();

                if (game != null) {
                    gamePanel.setState(game.getMatchState());
                    frame.setContentPane(gamePanel);
                    frame.revalidate();
                    frame.repaint();
                    requestFocus(frame);
                }

            } else {

                boolean gameOver = game.getMatchState().isGameOver();

                if (gameOver &&
                        (inputFrame.pressed.contains(KeyEvent.VK_R)
                                || inputFrame.pressed.contains(KeyEvent.VK_ENTER))) {
                    game = menu.createGame();
                    gamePanel.setState(game.getMatchState());
                    frame.setContentPane(gamePanel);
                    frame.revalidate();
                    frame.repaint();
                    requestFocus(frame);
                } else if (inputFrame.pressed.contains(KeyEvent.VK_ESCAPE)) {
                    game = null;
                    menu.showPlayMenu();
                    frame.setContentPane(menuPanel);
                    frame.revalidate();
                    frame.repaint();
                    requestFocus(frame);
                } else if (gameOver) {
                    gamePanel.setState(game.getMatchState());
                    gamePanel.repaint();
                } else {
                    game.tick(inputFrame);

                    gamePanel.setState(game.getMatchState());
                    gamePanel.repaint();
                }
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void requestFocus(JFrame frame) {
        SwingUtilities.invokeLater(() -> frame.getRootPane().requestFocusInWindow());
    }
}
