package dz.kalbo.emulator.model;

import java.util.Objects;

public class Speed {
    private final double originalSpeed;
    private final float originalZoom;

    public double speed;
    public float zoom;

    public Speed(float zoom) {
        this(0d, zoom);
    }

    public Speed(double speed, float zoom) {
        this.originalSpeed = speed;
        this.originalZoom = zoom;
        this.speed = speed;
        this.zoom = zoom;
    }

    public void scale(float newZoom) {
        if (Float.compare(this.zoom, newZoom) != 0) {
            this.zoom = newZoom;
            this.speed = originalSpeed / originalZoom * newZoom;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Speed)) return false;
        Speed speed = (Speed) o;
        return originalSpeed == speed.originalSpeed &&
                Float.compare(speed.originalZoom, originalZoom) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalSpeed, originalZoom);
    }

    @Override
    public String toString() {
        return "Speed(" + speed + ')';
    }
}
