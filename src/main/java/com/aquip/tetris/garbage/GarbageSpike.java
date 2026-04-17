package com.aquip.tetris.garbage;

public class GarbageSpike {

    private int lines;
    private final int sendOnPiece;
    private final int sendAfterTick;

    public GarbageSpike(int lines, int sendOnPiece, int sendAfterTick) {
        this.lines = lines;
        this.sendOnPiece = sendOnPiece;
        this.sendAfterTick = sendAfterTick;
    }

    public int getLines() {
        return lines;
    }

    public int getSendOnPiece() {
        return sendOnPiece;
    }

    public int getSendAfterTick() {
        return sendAfterTick;
    }

    public boolean isEmpty() {
        return lines <= 0;
    }

    /**
     * Reduces this spike and returns how much was actually removed
     */
    public int reduce(int amount) {
        int removed = Math.min(lines, amount);
        lines -= removed;
        return removed;
    }
}