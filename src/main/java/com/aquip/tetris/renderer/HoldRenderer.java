package com.aquip.tetris.renderer;

import com.aquip.tetris.piece.*;
import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class HoldRenderer {

    public void render(Graphics2D g, PlayerState state, Rectangle area) {
        drawPanel(g, area, "Hold");

        if (state.next.held == null) {
            g.setColor(new Color(160, 170, 186));
            g.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g.drawString("Empty", area.x + 14, area.y + 46);
            return;
        }

        var registry = PieceRegistry.getInstance();
        int blockSize = Math.max(12, Math.min((area.width - 48) / 4, (area.height - 54) / 3));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Piece.Position p : registry.getBlocks(state.next.held, 0)) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        int piecePixelWidth = (maxX - minX + 1) * blockSize;
        int piecePixelHeight = (maxY - minY + 1) * blockSize;
        int originX = area.x + (area.width - piecePixelWidth) / 2;
        int originY = area.y + 34 + (area.height - 44 - piecePixelHeight) / 2;

        g.setColor(state.next.canHold ? getColor(state.next.held) : Color.GRAY);

        for (Piece.Position p : registry.getBlocks(state.next.held, 0)) {
            g.fillRect(
                    originX + (p.x - minX) * blockSize,
                    originY + (p.y - minY) * blockSize,
                    blockSize,
                    blockSize
            );
        }
    }

    private Color getColor(PieceType type) {
        return switch (type) {
            case I -> Color.CYAN;
            case O -> Color.YELLOW;
            case T -> Color.MAGENTA;
            case S -> Color.GREEN;
            case Z -> Color.RED;
            case J -> Color.BLUE;
            case L -> Color.ORANGE;
        };
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
