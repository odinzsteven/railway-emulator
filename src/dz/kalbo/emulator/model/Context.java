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

    public Context(int width, int height, float zoom, long time) {
        this.time = time;
        this.width = width;
        this.height = height;
        this.zoom = zoom;
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

    public int getScreenWidth() {
        return (int) (width * zoom);
    }
    public int getScreenHeight() {
        return (int) (height * zoom);
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
