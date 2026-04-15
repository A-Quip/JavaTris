package com.aquip.tetris.piece;

public class KickTable {
    private final int[][][][] kicks; // [from][to][test][x,y]

    public KickTable(int[][][][] kicks) {
        this.kicks = kicks;
    }

    public int[][] getKicks(int from, int to) {
        return kicks[from][to];
    }
}
