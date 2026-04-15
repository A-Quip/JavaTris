package com.aquip.tetris.placement;

import java.util.Objects;

public class PlaceKey {

    public final SpinResult spin;
    public final int lines;

    public PlaceKey(SpinResult spin, int lines) {
        this.spin = spin;
        this.lines = lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceKey)) return false;
        PlaceKey that = (PlaceKey) o;
        return lines == that.lines && spin == that.spin;
    }

    @Override
    public String toString() {
        return "spin: " + spin + ", lines: " + lines;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spin, lines);
    }
}