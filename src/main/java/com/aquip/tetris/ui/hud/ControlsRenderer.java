package com.aquip.tetris.ui.hud;

import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class ControlsRenderer {

    public void render(Graphics2D g, Rectangle area, PlayerState state) {
        drawPanel(g, area, "Controls");

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));

        int x = area.x + 12;
        int y = area.y + 38;

        if (state.player.getType() == PlayerType.AI) {
            g.drawString("Planning AI", x, y);
            y += 24;
            g.drawString("Searches placements", x, y);
            y += 24;
            g.drawString("Evaluates board risk", x, y);
            y += 24;
            g.drawString("Uses hold when useful", x, y);
            return;
        }

        boolean playerTwo = "P2".equalsIgnoreCase(state.player.getName());

        if (playerTwo) {
            g.drawString("A / D         Move", x, y);
            y += 24;
            g.drawString("S             Soft drop", x, y);
            y += 24;
            g.drawString("Shift         Hard drop", x, y);
            y += 24;
            g.drawString("Q / W         Rotate", x, y);
            y += 24;
            g.drawString("E             Hold", x, y);
        } else {
            g.drawString("Left / Right  Move", x, y);
            y += 24;
            g.drawString("Down          Soft drop", x, y);
            y += 24;
            g.drawString("Space         Hard drop", x, y);
            y += 24;
            g.drawString("Z / X         Rotate", x, y);
            y += 24;
            g.drawString("C             Hold", x, y);
        }

        y += 24;
        g.drawString("Esc           Menu", x, y);
        y += 24;
        g.drawString("R / Enter     Restart", x, y);
    }

    private void drawPanel(Graphics2D g, Rectangle area, String title) {
        g.setColor(new Color(22, 26, 36, 220));
        g.fillRoundRect(area.x, area.y, area.width, area.height, 18, 18);
        g.setColor(new Color(110, 122, 150));
        g.drawRoundRect(area.x, area.y, area.width, area.height, 18, 18);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString(title, area.x + 12, area.y + 20);
    }
}
