package com.aquip.tetris.engine;

import com.aquip.tetris.engine.handler.TickHandler;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.input.PlayerInputSource;
import com.aquip.tetris.state.MatchState;

import java.util.List;
import java.util.Queue;

public class GameEngine {

    public final MatchState state;
    public final Queue<PlayerInput> inputs;
    public final TickHandler tickHandler;

    private final List<PlayerInputSource> inputSources;

    public GameEngine(MatchState state,
                      Queue<PlayerInput> inputs,
                      TickHandler tickHandler,
                      List<PlayerInputSource> inputSources) {

        this.state = state;
        this.inputs = inputs;
        this.tickHandler = tickHandler;
        this.inputSources = inputSources;
    }

    public void tick(InputFrame frame) {

        frame.state = this.state.copy();
        inputs.clear();

        for (PlayerInputSource source : inputSources) {
            PlayerInput input = source.poll(frame);
            if (input != null) {
                inputs.add(input);
            }
        }

        tickHandler.tick(inputs);
    }

    public MatchState getMatchState() {
        return state;
    }
}