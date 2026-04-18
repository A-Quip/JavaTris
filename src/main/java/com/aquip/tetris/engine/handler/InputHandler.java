package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.util.Queue;

public class InputHandler {

    public void apply(MatchState match, Queue<PlayerInput> inputs, TickContext context) {

        while (!inputs.isEmpty()) {
            PlayerInput input = inputs.poll();

            PlayerState player = match.getPlayerState(input.player);

            for (GameInput action : input.inputs) {
                applyInput(player, match, action, context);
            }
        }
    }

    private void applyInput(PlayerState player,
                            MatchState match,
                            GameInput input,
                            TickContext context) {

        if (player == null || !player.status.alive) {
            return;
        }

        var ctx = context.get(player);

        //System.out.println(input);

        switch (input) {
            case MOVE_LEFT -> ctx.moveX -= 1;
            case MOVE_RIGHT -> ctx.moveX += 1;
            case HARD_DROP -> ctx.hardDrop = true;
            case SOFT_DROP -> ctx.softDrop = true;
            case HOLD_PIECE -> ctx.hold = true;

            case ROTATE_CW -> ctx.rotation += 1;
            case ROTATE_CCW -> ctx.rotation -= 1;
            case ROTATE_180 -> ctx.rotation += 2;
            case NONE -> {}

            case FORFEIT -> player.status.alive = false;
        }
    }
}
