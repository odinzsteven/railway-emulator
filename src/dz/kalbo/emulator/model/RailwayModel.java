package dz.kalbo.emulator.model;

import dz.kalbo.emulator.tools.Kit;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

public class RailwayModel implements Updater {

    private final StampedLock lock = new StampedLock();
    private Context context;
    private final List<Component> listeners = new CopyOnWriteArrayList<>();
    private final List<Road> roads = new CopyOnWriteArrayList<>();
    private final List<Tram> trams = new CopyOnWriteArrayList<>();

    public RailwayModel(int width, int height) {
        this(width, height, 1f);
    }

    public RailwayModel(int width, int height, float zoom) {
        this.context = new Context(width, height, zoom);
    }

    public Context getContext() {
        return context;
    }

    public List<Tram> getTrams() {
        return trams;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public void addRoad(Road road) {
        roads.add(road);
    }

    public void addTram(Tram tram) {
        trams.add(tram);
    }

    public void update(long timeSinceLastUpdate) {
        long lockTime = System.nanoTime();
        long stamp = lock.writeLock();
        long acquireTime = System.nanoTime();
        Context updateContext = new Context(this.context, timeSinceLastUpdate);
        try {
            update(updateContext);
        } finally {
            long now = System.nanoTime();
            lock.unlock(stamp);
            for (Component listener : listeners)
                listener.repaint();

            if (Kit.SHOW_UPDATE_TIME) {
                System.out.println("update done in: " + Kit.printAsMillis(now - acquireTime)
                        + " (+" + Kit.printAsMillis(acquireTime - lockTime) + ")");
            }
        }
    }

    private void update(Context updateContext) {
        this.context = updateContext;
        for (Road road : this.roads)
            road.update(updateContext);
        for (Tram tram : this.trams)
            tram.update(updateContext);
    }

    public void read(Consumer<RailwayModel> reader) {
        long stamp = lock.tryOptimisticRead();
        reader.accept(this);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                reader.accept(this);
            } finally {
                lock.unlockRead(stamp);
            }
        }
    }

    public void write(Consumer<RailwayModel> writer) {
        long stamp = lock.writeLock();
        try {
            writer.accept(this);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void addRepaintListener(Component listener) {
        listeners.add(listener);
    }
}
