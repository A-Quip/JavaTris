package com.aquip.tetris.placement;

import com.aquip.tetris.piece.PieceType;

public class PlacementResult {

    public final PieceType piece;
    public final SpinResult spin;
    public final int offsetX;
    public final int offsetY;
    public final int rotation;
    public final int lines;

    public PlacementResult(PieceType piece, SpinResult spin, int offsetX, int offsetY, int rotation, int lines) {
        this.piece = piece;
        this.spin = spin;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.rotation = rotation;
        this.lines = lines;
    }

    public PlaceKey getPlaceKey() {
        return new PlaceKey(spin, lines);
    }

    @Override
    public String toString() {
        return "PlacementResult{" +
                "piece=" + piece +
                ", spin=" + spin +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", rotation=" + rotation +
                ", lines=" + lines +
                '}';
    }
}