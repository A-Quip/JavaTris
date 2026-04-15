package com.aquip.tetris.state;

import com.aquip.tetris.config.GarbageTable;
import com.aquip.tetris.config.ScoreTable;
import com.aquip.tetris.config.SpinTable;
import com.aquip.tetris.piece.PieceType;

import java.util.HashMap;
import java.util.Map;

public class ConfigState {

    public int boardWidth;
    public int boardHeight;
    public int spawnBufferRows;

    public Map<PieceType, Integer> minoSet;
    public boolean useBag;

    public boolean allowHold;
    public int nextSize;

    public int gravityTick;
    public int minimumGravityTick;
    public int gravityStepEveryPieces;
    public int gravityStepAmount;

    public int lockTick;
    public int maxSlides;
    public int maxRotations;

    public ScoreTable scoreTable;
    public double scoreMult;

    public SpinTable spinTable;

    public GarbageTable garbageTable;
    public boolean godMode;
    public boolean pacifist;

    public ConfigState() {
        this.boardWidth = 10;
        this.boardHeight = 20;
        this.spawnBufferRows = 4;

        this.minoSet = new HashMap<>();
        this.useBag = true;

        this.allowHold = true;
        this.nextSize = 5;

        this.gravityTick = 60;
        this.minimumGravityTick = 1;
        this.gravityStepEveryPieces = 10;
        this.gravityStepAmount = 1;

        this.lockTick = 45;
        this.maxSlides = 15;
        this.maxRotations = 15;

        this.scoreMult = 1.0;

        this.godMode = false;
        this.pacifist = false;
    }

    public int gravityThresholdForPieces(int piecesPlaced) {
        int steps = gravityStepEveryPieces <= 0 ? 0 : piecesPlaced / gravityStepEveryPieces;
        int threshold = gravityTick - (steps * gravityStepAmount);
        return Math.max(1, Math.max(minimumGravityTick, threshold));
    }

    public int softDropThresholdForPieces(int piecesPlaced) {
        return Math.max(1, gravityThresholdForPieces(piecesPlaced) / 8);
    }
}
