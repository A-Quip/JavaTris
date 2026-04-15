package com.aquip.tetris.menu;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {

    private MenuState state;

    public void setState(MenuState state) {
        this.state = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (state == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(10, 10, 16));
        g2.fillRect(0, 0, getWidth(), getHeight());

        drawPlayMenu(g2);
    }

    private void drawPlayMenu(Graphics2D g2) {
        int panelWidth = Math.min(520, getWidth() - 80);
        int panelHeight = Math.min(320, getHeight() - 120);
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;

        g2.setColor(new Color(28, 31, 45));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g2.setColor(new Color(74, 180, 255));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2.drawString("JAVATRIS", panelX + 34, panelY + 56);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.setColor(new Color(210, 214, 223));
        g2.drawString("Single-player build", panelX + 36, panelY + 90);

        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(new Color(255, 221, 92));
        g2.drawString("> Start Game", panelX + 36, panelY + 146);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        g2.drawString("Controls", panelX + 36, panelY + 196);
        g2.drawString("Move: Left / Right", panelX + 36, panelY + 224);
        g2.drawString("Rotate: Z / X", panelX + 36, panelY + 248);
        g2.drawString("Soft Drop: Down   Hard Drop: Space", panelX + 36, panelY + 272);
        g2.drawString("Hold: C   Restart after loss: R or Enter", panelX + 36, panelY + 296);
    }
}
