package com.aquip.tetris.ui.hud;

import com.aquip.tetris.piece.*;
import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class QueueRenderer {

    public void render(Graphics2D g, PlayerState state, Rectangle area) {
        drawPanel(g, area, "Next");

        int i = 0;
        int cardHeight = 56;

        for (PieceType type : state.next.next) {
            if (i >= state.config.nextSize) break;

            drawMiniPiece(g, type, new Rectangle(
                    area.x + 10,
                    area.y + 30 + i * cardHeight,
                    area.width - 20,
                    cardHeight - 6
            ));
            i++;
        }
    }

    private void drawMiniPiece(Graphics2D g, PieceType type, Rectangle area) {

        var registry = PieceRegistry.getInstance();
        int blockSize = Math.max(10, Math.min((area.width - 16) / 4, (area.height - 12) / 2));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Piece.Position p : registry.getBlocks(type, 0)) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        int piecePixelWidth = (maxX - minX + 1) * blockSize;
        int piecePixelHeight = (maxY - minY + 1) * blockSize;
        int originX = area.x + (area.width - piecePixelWidth) / 2;
        int originY = area.y + (area.height - piecePixelHeight) / 2;

        g.setColor(getColor(type));

        for (Piece.Position p : registry.getBlocks(type, 0)) {
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
