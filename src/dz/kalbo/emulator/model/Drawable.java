package dz.kalbo.emulator.model;

public interface Drawable {

    int getId();

    Context getCurrentContext();

    void update(Context context);
}
