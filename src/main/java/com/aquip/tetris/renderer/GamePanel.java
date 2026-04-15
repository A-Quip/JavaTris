package com.aquip.tetris.renderer;

import com.aquip.tetris.state.MatchState;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private MatchState state;

    private final BoardRenderer boardRenderer;

    public GamePanel() {
        this.boardRenderer = new BoardRenderer();
        setBackground(Color.BLACK);
    }

    public void setState(MatchState state) {
        this.state = state;
        repaint();
    }

    private final PlayerRenderer renderer = new PlayerRenderer();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (state == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int count = state.players.size();

        if (count == 1) {
            renderer.render(g2, state.players.get(0), new Rectangle(0, 0, getWidth(), getHeight()));

            if (state.isGameOver()) {
                drawGameOver(g2);
            }
            return;
        }

        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);

        int cellW = getWidth() / cols;
        int cellH = getHeight() / rows;

        for (int i = 0; i < count; i++) {
            int col = i % cols;
            int row = i / cols;

            Rectangle area = new Rectangle(
                    col * cellW,
                    row * cellH,
                    cellW,
                    cellH
            );

            renderer.render(g2, state.players.get(i), area);
        }

        if (state.isGameOver()) {
            drawGameOver(g2);
        }
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 30));
        FontMetrics titleMetrics = g2.getFontMetrics();
        String title = "GAME OVER";
        int titleX = (getWidth() - titleMetrics.stringWidth(title)) / 2;
        int titleY = getHeight() / 2 - 20;
        g2.drawString(title, titleX, titleY);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        FontMetrics bodyMetrics = g2.getFontMetrics();
        String restart = "Press R or Enter to restart";
        String menu = "Press Esc for menu";
        int restartX = (getWidth() - bodyMetrics.stringWidth(restart)) / 2;
        int menuX = (getWidth() - bodyMetrics.stringWidth(menu)) / 2;
        g2.drawString(restart, restartX, titleY + 40);
        g2.drawString(menu, menuX, titleY + 70);
    }
}
