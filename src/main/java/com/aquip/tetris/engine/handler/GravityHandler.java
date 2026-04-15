package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.state.PlayerState;

import static java.lang.Integer.min;

public class GravityHandler implements PlayerHandler {

    @Override
    public void apply(PlayerState player, TickContext context) {

        if (!player.piece.hasPiece()) return;

        var ctx = context.get(player);

        player.gravity.gravityTicks++;

        int piecesPlaced = player.time.amount();
        int threshold = player.config.gravityThresholdForPieces(piecesPlaced);
        int softDropThreshold = player.config.softDropThresholdForPieces(piecesPlaced);

        if (player.gravity.gravityTicks >= threshold ||
                (ctx.softDrop && player.gravity.gravityTicks >= min(softDropThreshold, threshold))) {

            player.gravity.gravityTicks = 0;

            ctx.moveY += 1;
        }
    }
}
