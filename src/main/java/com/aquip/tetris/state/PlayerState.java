package com.aquip.tetris.state;

import com.aquip.tetris.player.Player;
import com.aquip.tetris.ai.AIConfig;

public class PlayerState {

    public Player player;

    public ConfigState config;
    public AIConfig aiConfig;

    public BoardState board;
    public PieceState piece;
    public QueueState next;
    public LockState lock;
    public GravityState gravity;
    public TimeState time;
    public ComboState combo;
    public B2BState b2b;
    public GarbageState garbage;
    public StatusState status;

    public PlayerState() {
        this(null, new ConfigState());
    }

    public PlayerState(Player player, ConfigState config) {
        this.player = player;
        this.config = config;

        this.board = new BoardState(config.boardWidth, config.boardHeight + config.spawnBufferRows);
        this.piece = new PieceState();
        this.next = new QueueState();
        this.lock = new LockState();
        this.gravity = new GravityState();
        this.time = new TimeState();
        this.combo = new ComboState();
        this.b2b = new B2BState();
        this.garbage = new GarbageState();
        this.status = new StatusState();
    }
}
