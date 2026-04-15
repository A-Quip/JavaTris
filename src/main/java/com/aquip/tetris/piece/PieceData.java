package com.aquip.tetris.piece;

public class PieceData {
    final int[][][] rotations;
    private final KickTable kickTable;

    public PieceData(int[][][] rotations, KickTable kickTable) {
        this.rotations = rotations;
        this.kickTable = kickTable;
    }

    public int[][] getRotation(int rotation) {
        return rotations[rotation % rotations.length];
    }

    public int[][][] getAllRotations() {
        return rotations;
    }

    public int[][] getKicks(int from, int to) {
        return kickTable.getKicks(from, to);
    }
}
