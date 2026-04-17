package com.aquip.tetris.ui.hud;

import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class ControlsRenderer {

    public void render(Graphics2D g, Rectangle area, PlayerState state) {
        drawPanel(g, area, "Controls");

        g.setColor(Color.WHITE);
        int fontSize = Math.max(11, Math.min(14, Math.min(area.width / 10, area.height / 18)));
        int lineHeight = fontSize + 10;
        g.setFont(new Font("Monospaced", Font.PLAIN, fontSize));

        int x = area.x + 12;
        int y = area.y + 24 + lineHeight;

        if (state.player.getType() == PlayerType.AI) {
            g.drawString("Planning AI", x, y);
            y += lineHeight;
            g.drawString("Searches placements", x, y);
            y += lineHeight;
            g.drawString("Evaluates board risk", x, y);
            y += lineHeight;
            g.drawString("Uses hold when useful", x, y);
            return;
        }

        boolean playerTwo = "P2".equalsIgnoreCase(state.player.getName());

        if (playerTwo) {
            g.drawString("A      Left", x, y);
            y += lineHeight;
            g.drawString("D      Right", x, y);
            y += lineHeight;
            g.drawString("S      Soft drop", x, y);
            y += lineHeight;
            g.drawString("F      Hard drop", x, y);
            y += lineHeight;
            g.drawString("Q      Rotate ↺", x, y);
            y += lineHeight;
            g.drawString("W      Rotate ↻", x, y);
            y += lineHeight;
            g.drawString("E      Hold", x, y);
        } else {
            g.drawString("Left   Move", x, y);
            y += lineHeight;
            g.drawString("Right  Move", x, y);
            y += lineHeight;
            g.drawString("Down   Soft drop", x, y);
            y += lineHeight;
            g.drawString("Up     Hard drop", x, y);
            y += lineHeight;
            g.drawString("<      Rotate ↺", x, y);
            y += lineHeight;
            g.drawString(">      Rotate ↻", x, y);
            y += lineHeight;
            g.drawString("/      Hold", x, y);
        }

        y += lineHeight;
        g.drawString("Esc    Menu", x, y);
        y += lineHeight;
        g.drawString("R      Restart", x, y);
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
