package com.aquip.tetris.ui.game;

import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.ui.hud.ControlsRenderer;
import com.aquip.tetris.ui.hud.HoldRenderer;
import com.aquip.tetris.ui.hud.InfoRenderer;
import com.aquip.tetris.ui.hud.QueueRenderer;

import java.awt.*;

public class PlayerRenderer {

    private final BoardRenderer board = new BoardRenderer();
    private final QueueRenderer queue = new QueueRenderer();
    private final HoldRenderer hold = new HoldRenderer();
    private final InfoRenderer info = new InfoRenderer();
    private final ControlsRenderer controls = new ControlsRenderer();

    public void render(Graphics2D g, PlayerState state, Rectangle area) {
        int padding = area.width < 760 ? 16 : 24;
        int gap = area.width < 760 ? 12 : 20;

        Rectangle contentArea = new Rectangle(
                area.x + padding,
                area.y + padding,
                area.width - padding * 2,
                area.height - padding * 2
        );

        int leftWidth = Math.max(92, Math.min(160, contentArea.width / 5));
        int rightWidth = Math.max(120, Math.min(180, contentArea.width / 4));
        int boardWidth = contentArea.width - leftWidth - rightWidth - gap * 2;

        if (boardWidth < 180) {
            int shortage = 180 - boardWidth;
            int leftTrim = Math.min(shortage / 2 + shortage % 2, Math.max(0, leftWidth - 84));
            leftWidth -= leftTrim;
            shortage -= leftTrim;

            int rightTrim = Math.min(shortage, Math.max(0, rightWidth - 108));
            rightWidth -= rightTrim;
            boardWidth = contentArea.width - leftWidth - rightWidth - gap * 2;
        }

        Rectangle leftColumn = new Rectangle(
                contentArea.x,
                contentArea.y,
                leftWidth,
                contentArea.height
        );

        Rectangle boardArea = new Rectangle(
                leftColumn.x + leftColumn.width + gap,
                contentArea.y,
                Math.max(180, boardWidth),
                contentArea.height
        );

        Rectangle rightColumn = new Rectangle(
                boardArea.x + boardArea.width + gap,
                contentArea.y,
                rightWidth,
                contentArea.height
        );

        int holdHeight = area.width < 760 ? 104 : 120;
        int controlsTop = holdHeight + 16;
        int infoHeight = area.width < 760 ? 212 : 214;
        int queueTop = infoHeight + 16;

        Rectangle holdArea = new Rectangle(leftColumn.x, leftColumn.y, leftColumn.width, holdHeight);
        Rectangle controlsArea = new Rectangle(
                leftColumn.x,
                leftColumn.y + controlsTop,
                leftColumn.width,
                Math.max(176, contentArea.height - controlsTop)
        );
        Rectangle infoArea = new Rectangle(rightColumn.x, rightColumn.y, rightColumn.width, infoHeight);
        Rectangle queueArea = new Rectangle(
                rightColumn.x,
                rightColumn.y + queueTop,
                rightColumn.width,
                Math.max(120, contentArea.height - queueTop)
        );

        board.render(g, state, boardArea);
        hold.render(g, state, holdArea);
        controls.render(g, controlsArea, state);
        info.render(g, state, infoArea);
        queue.render(g, state, queueArea);
    }
}
