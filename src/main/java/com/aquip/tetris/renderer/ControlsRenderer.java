package com.aquip.tetris.renderer;

import java.awt.*;

public class ControlsRenderer {

    public void render(Graphics2D g, Rectangle area) {
        drawPanel(g, area, "Controls");

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));

        int x = area.x + 12;
        int y = area.y + 38;

        g.drawString("Left / Right  Move", x, y);
        y += 24;
        g.drawString("Down          Soft drop", x, y);
        y += 24;
        g.drawString("Space         Hard drop", x, y);
        y += 24;
        g.drawString("Z / X         Rotate", x, y);
        y += 24;
        g.drawString("C             Hold", x, y);
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
