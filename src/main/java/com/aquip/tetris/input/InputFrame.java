package com.aquip.tetris.input;

import com.aquip.tetris.state.MatchState;

import java.util.HashSet;
import java.util.Set;

public class InputFrame {

    public final Set<Integer> pressed = new HashSet<>();
    public final Set<Integer> released = new HashSet<>();
    public final Set<Integer> held = new HashSet<>();
    public MatchState state;
}