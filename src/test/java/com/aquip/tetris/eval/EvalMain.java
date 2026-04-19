package com.aquip.tetris.eval;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.config.GameConfigParser;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.ui.game.GamePanel;

import javax.swing.*;
import java.io.File;

/**
 * Entry point for the AI evaluation suite.
 *
 * A live Swing window shows each scenario as it plays out.
 * Close the window to stop early, or wait for all scenarios to finish.
 */
public class EvalMain {

    public static void main(String[] args) throws InterruptedException {

        File configFile = new File("config/config.yml");
        ConfigState config = configFile.exists()
                ? GameConfigParser.parse(configFile)
                : new ConfigState();

        AIConfig aiConfig = AIConfig.defaults();

        // ---- Build the window on the EDT ------------------------------------
        GamePanel gamePanel = new GamePanel();
        JFrame frame = new JFrame("JavaTris — AI Eval");

        SwingUtilities.invokeLater(() -> {
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(gamePanel);
            frame.setVisible(true);
        });

        // Give Swing a moment to show the window before ticks start
        Thread.sleep(2000);

        EvalSuite suite = new EvalSuite(config, aiConfig)
                .addAll(ScenarioLibrary.standard());

        suite.runAll(state -> {
            gamePanel.setState(state);
            gamePanel.repaint();
        });

        frame.setTitle("JavaTris — AI Eval (Complete)");

        frame.dispose();
        System.exit(0);
    }
}