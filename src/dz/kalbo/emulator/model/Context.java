package dz.kalbo.emulator.model;

public class Context {
    private final long time;
    private final int width;
    private final int height;
    private final float zoom;

    public Context(int width, int height, float zoom) {
        this.time = System.nanoTime();
        this.width = width;
        this.height = height;
        this.zoom = zoom;
    }

    public Context(Context context) {
        this.time = System.nanoTime();
        this.width = context.width;
        this.height = context.height;
        this.zoom = context.zoom;
    }

    public long getTime() {
        return time;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getZoom() {
        return zoom;
    }

    @Override
    public String toString() {
        return "Context{" +
                "width=" + width +
                ", height=" + height +
                ", zoom=" + zoom +
                '}';
    }
}
