package com.aquip.tetris.renderer;

import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class InfoRenderer {

    public void render(Graphics2D g, PlayerState state, Rectangle area) {
        drawPanel(g, area, "Stats");

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 15));

        int x = area.x + 14;
        int y = area.y + 36;

        g.drawString(state.player.getName(), x, y);
        y += 24;

        g.drawString("Score: " + state.status.Score, x, y);
        y += 22;

        g.drawString("Pieces: " + state.time.amount(), x, y);
        y += 22;

        g.drawString("Gravity: " + state.config.gravityThresholdForPieces(state.time.amount()) + " ticks", x, y);
        y += 22;

        g.drawString("Combo: " + state.combo.amount(), x, y);
        y += 22;

        g.drawString("B2B: " + state.b2b.amount(), x, y);
        y += 22;

        g.drawString("Status: " + (state.status.alive ? "Playing" : "Game Over"), x, y);
    }

    private void drawPanel(Graphics2D g, Rectangle area, String title) {
        g.setColor(new Color(22, 26, 36, 220));
        g.fillRoundRect(area.x, area.y, area.width, area.height, 18, 18);
        g.setColor(new Color(110, 122, 150));
        g.drawRoundRect(area.x, area.y, area.width, area.height, 18, 18);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString(title, area.x + 12, area.y + 20);
    }
}
