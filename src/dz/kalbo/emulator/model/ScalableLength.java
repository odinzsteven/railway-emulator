package dz.kalbo.emulator.model;

import java.util.Objects;

public class ScalableLength {
    private final int originalLength;
    private final float originalZoom;

    public int length;
    public float zoom;

    public ScalableLength(int length, float zoom) {
        this.originalLength = length;
        this.originalZoom = zoom;
        this.length = length;
        this.zoom = zoom;
    }

    public void scale(float newZoom) {
        if (Float.compare(this.zoom, newZoom) != 0) {
            this.zoom = newZoom;
            this.length = (int) (originalLength / originalZoom * newZoom);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScalableLength)) return false;
        ScalableLength that = (ScalableLength) o;
        return originalLength == that.originalLength &&
                Float.compare(that.originalZoom, originalZoom) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalLength, originalZoom);
    }
}
