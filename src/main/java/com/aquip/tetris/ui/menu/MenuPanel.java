package com.aquip.tetris.ui.menu;

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
        int panelHeight = Math.min(420, getHeight() - 120);
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;
        GameMode[] modes = GameMode.values();
        GameMode selectedMode = state.selectedMode();

        g2.setColor(new Color(28, 31, 45));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g2.setColor(new Color(74, 180, 255));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2.drawString("JAVATRIS", panelX + 34, panelY + 56);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.setColor(new Color(210, 214, 223));
        g2.drawString("Select a mode and press Enter", panelX + 36, panelY + 90);

        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        int optionY = panelY + 142;

        for (int i = 0; i < modes.length; i++) {
            GameMode mode = modes[i];
            if (i == state.selectionIndex) {
                g2.setColor(new Color(255, 221, 92));
                g2.drawString("> " + mode.label(), panelX + 36, optionY);
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString("  " + mode.label(), panelX + 36, optionY);
            }
            optionY += 34;
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        g2.drawString(selectedMode.description(), panelX + 36, panelY + 296);
        g2.drawString(selectedMode.controls(), panelX + 36, panelY + 324);
        g2.drawString("Menu: Up / Down   Start: Enter", panelX + 36, panelY + 360);
        g2.drawString("In-game: Esc menu, R or Enter restart", panelX + 36, panelY + 388);
    }
}
