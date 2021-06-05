package dz.kalbo.emulator.model;


import java.util.Objects;

public class ScalablePoint {

    private final int originalX;
    private final int originalY;
    private final float originalZoom;

    public int x;
    public int y;
    public float zoom;

    public ScalablePoint(int x, int y, float zoom) {
        this.originalX = x;
        this.originalY = y;
        this.originalZoom = zoom;

        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public void scale(float newZoom) {
        if (Float.compare(this.zoom, newZoom) != 0) {
            this.zoom = newZoom;
            this.x = (int) (originalX / originalZoom * newZoom);
            this.y = (int) (originalY / originalZoom * newZoom);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScalablePoint that = (ScalablePoint) o;
        return originalX == that.originalX && originalY == that.originalY && Float.compare(that.originalZoom, originalZoom) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalX, originalY, originalZoom);
    }
}
