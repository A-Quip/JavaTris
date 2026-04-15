package com.aquip.tetris.renderer;

import com.aquip.tetris.state.PlayerState;

import java.awt.*;

public class PlayerRenderer {

    private final BoardRenderer board = new BoardRenderer();
    private final QueueRenderer queue = new QueueRenderer();
    private final HoldRenderer hold = new HoldRenderer();
    private final InfoRenderer info = new InfoRenderer();
    private final ControlsRenderer controls = new ControlsRenderer();

    public void render(Graphics2D g, PlayerState state, Rectangle area) {
        int padding = 24;
        int leftWidth = 180;
        int rightWidth = 180;
        int gap = 20;

        Rectangle contentArea = new Rectangle(
                area.x + padding,
                area.y + padding,
                area.width - padding * 2,
                area.height - padding * 2
        );

        Rectangle leftColumn = new Rectangle(
                contentArea.x,
                contentArea.y,
                leftWidth,
                contentArea.height
        );

        Rectangle boardArea = new Rectangle(
                leftColumn.x + leftColumn.width + gap,
                contentArea.y,
                Math.max(220, contentArea.width - leftWidth - rightWidth - gap * 2),
                contentArea.height
        );

        Rectangle rightColumn = new Rectangle(
                boardArea.x + boardArea.width + gap,
                contentArea.y,
                rightWidth,
                contentArea.height
        );

        Rectangle holdArea = new Rectangle(leftColumn.x, leftColumn.y, leftColumn.width, 120);
        Rectangle controlsArea = new Rectangle(leftColumn.x, leftColumn.y + 140, leftColumn.width, 220);
        Rectangle infoArea = new Rectangle(rightColumn.x, rightColumn.y, rightColumn.width, 190);
        Rectangle queueArea = new Rectangle(rightColumn.x, rightColumn.y + 210, rightColumn.width, contentArea.height - 210);

        board.render(g, state, boardArea);
        hold.render(g, state, holdArea);
        controls.render(g, controlsArea);
        info.render(g, state, infoArea);
        queue.render(g, state, queueArea);
    }
}
