package com.aquip.tetris.garbage;

import java.util.ArrayDeque;
import java.util.Queue;

public class GarbageQueue {

    private final Queue<GarbageSpike> queue;

    public GarbageQueue() {
        this.queue = new ArrayDeque<>();
    }

    // =====================
    // ADD GARBAGE
    // =====================

    public void add(int lines) {
        if (lines <= 0) return;
        queue.add(new GarbageSpike(lines, 0, 0));
    }

    public void add(int lines, int sendOnPiece, int sendAfterTick) {
        if (lines <= 0) return;
        queue.add(new GarbageSpike(lines, sendOnPiece, sendAfterTick));
    }

    // =====================
    // CANCEL GARBAGE
    // =====================

    public int poll(int amount) {

        int remaining = amount;
        int removedTotal = 0;

        while (remaining > 0 && !queue.isEmpty()) {

            GarbageSpike spike = queue.peek();

            int removed = spike.reduce(remaining);

            removedTotal += removed;
            remaining -= removed;

            if (spike.isEmpty()) {
                queue.poll();
            }
        }

        return removedTotal;
    }

    // =====================
    // APPLY GARBAGE
    // =====================

    //
    // Removes ALL garbage that is ready to be applied.
    // (Currently: everything, since delay isn't enforced yet)
    //
    public int pollAllReady() {

        int total = 0;

        while (!queue.isEmpty()) {
            GarbageSpike spike = queue.poll();
            total += spike.getLines();
        }

        return total;
    }

    // =====================
    // FUTURE (DELAY SUPPORT)
    // =====================
    public int pollReady(int currentPiece, int currentTick) {

        int total = 0;

        while (!queue.isEmpty()) {

            GarbageSpike spike = queue.peek();

            boolean ready =
                    currentPiece >= spike.getSendOnPiece() &&
                            currentTick >= spike.getSendAfterTick();

            if (!ready) break;

            total += spike.getLines();
            queue.poll();
        }

        return total;
    }

    // =====================
    // INSPECTION
    // =====================

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int totalLines() {
        int sum = 0;
        for (GarbageSpike spike : queue) {
            sum += spike.getLines();
        }
        return sum;
    }

    public Queue<GarbageSpike> getRawQueue() {
        return queue;
    }

    public String debugString(int currentPiece, int currentTick) {

        StringBuilder sb = new StringBuilder();

        for (GarbageSpike spike : queue) {

            boolean ready =
                    currentPiece >= spike.getSendOnPiece() &&
                            currentTick >= spike.getSendAfterTick();

            sb.append("[")
                    .append(ready ? "READY" : "WAIT")
                    .append("] ")
                    .append(spike.getLines())
                    .append(" lines (P:")
                    .append(spike.getSendOnPiece())
                    .append(", T:")
                    .append(spike.getSendAfterTick())
                    .append(")\n");
        }

        return sb.toString();
    }
}