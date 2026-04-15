package com.aquip.tetris.ui.game;

import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceRegistry;
import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class BoardRenderer {

    public void render(Graphics2D g, PlayerState state, Rectangle area) {
        int boardWidth = state.board.getWidth();
        int boardHeight = state.config.boardHeight;
        int blockSize = Math.max(12, Math.min(area.width / boardWidth, area.height / boardHeight));
        int boardPixelWidth = boardWidth * blockSize;
        int boardPixelHeight = boardHeight * blockSize;

        int offsetX = area.x + (area.width - boardPixelWidth) / 2;
        int offsetY = area.y + (area.height - boardPixelHeight) / 2;

        g.setColor(new Color(16, 18, 26));
        g.fillRoundRect(offsetX - 8, offsetY - 8, boardPixelWidth + 16, boardPixelHeight + 16, 18, 18);

        drawBoard(g, state, offsetX, offsetY, blockSize);
        drawGhostPiece(g, state, offsetX, offsetY, blockSize);
        drawCurrentPiece(g, state, offsetX, offsetY, blockSize);
    }

    private void drawBoard(Graphics2D g, PlayerState state, int offsetX, int offsetY, int blockSize) {
        var board = state.board;
        int hiddenRows = state.config.spawnBufferRows;

        for (int y = hiddenRows; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {

                int value = board.get(x, y);

                if (value != 0) {
                    g.setColor(getColor(value));
                    fillCell(g, x, y - hiddenRows, offsetX, offsetY, blockSize);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    drawCell(g, x, y - hiddenRows, offsetX, offsetY, blockSize);
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g, PlayerState state, int offsetX, int offsetY, int blockSize) {
        if (!state.piece.hasPiece()) return;

        Piece piece = state.piece.currentPiece;
        int hiddenRows = state.config.spawnBufferRows;

        g.setColor(getColor(piece.type.ordinal() + 1));

        for (Piece.Position p : piece.getBlocks(PieceRegistry.getInstance())) {
            fillCell(g, p.x, p.y - hiddenRows, offsetX, offsetY, blockSize);
        }
    }

    private void drawGhostPiece(Graphics2D g, PlayerState state, int offsetX, int offsetY, int blockSize) {
        if (!state.piece.hasPiece()) return;

        Piece ghost = computeGhost(state);
        int hiddenRows = state.config.spawnBufferRows;

        g.setColor(new Color(255, 255, 255, 80));

        for (Piece.Position p : ghost.getBlocks(PieceRegistry.getInstance())) {
            fillCell(g, p.x, p.y - hiddenRows, offsetX, offsetY, blockSize);
        }
    }

    private Piece computeGhost(PlayerState state) {

        Piece piece = state.piece.currentPiece;
        Piece ghost = piece;

        while (!state.board.collides(ghost, PieceRegistry.getInstance())) {
            ghost = ghost.displace(0, 1);
        }

        // last valid position
        return ghost.displace(0, -1);
    }

    private void fillCell(Graphics2D g, int x, int y, int offsetX, int offsetY, int blockSize) {
        g.fillRect(offsetX + x * blockSize, offsetY + y * blockSize, blockSize, blockSize);
    }

    private void drawCell(Graphics2D g, int x, int y, int offsetX, int offsetY, int blockSize) {
        g.drawRect(offsetX + x * blockSize, offsetY + y * blockSize, blockSize, blockSize);
    }

    private Color getColor(int value) {
        return switch (value) {
            case 1 -> Color.CYAN;
            case 2 -> Color.YELLOW;
            case 3 -> Color.MAGENTA;
            case 4 -> Color.GREEN;
            case 5 -> Color.RED;
            case 6 -> Color.BLUE;
            case 7 -> Color.ORANGE;
            default -> Color.GRAY;
        };
    }
}
