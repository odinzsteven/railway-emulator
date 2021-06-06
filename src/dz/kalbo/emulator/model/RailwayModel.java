package dz.kalbo.emulator.model;

import dz.kalbo.emulator.tools.Kit;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class RailwayModel implements Updater {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
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
        Context updateContext = new Context(context.getWidth(), context.getHeight(),
                context.getZoom(), context.getTime() + timeSinceLastUpdate);

        update(updateContext);
    }

    public void update(Context newContext) {
        if (SwingUtilities.isEventDispatchThread())
            throw new IllegalThreadStateException("dont update from UI thread");

        long lockTime = System.nanoTime();
        try {
            Lock lock = this.lock.writeLock();
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                long acquireTime = System.nanoTime();
                Context oldContext = this.context;
                try {
                    this.context = newContext;
                    for (Road road : this.roads)
                        road.update(newContext);
                    for (Tram tram : this.trams)
                        tram.update(newContext);
                } finally {
                    long now = System.nanoTime();
                    lock.unlock();

                    repaint();
                    if (newContext.getScreenWidth() != oldContext.getScreenWidth()
                            || newContext.getScreenHeight() != oldContext.getScreenHeight())
                        resize();

                    if (Kit.SHOW_UPDATE_TIME) {
                        System.out.println("update done in: " + Kit.printAsMillis(now - acquireTime)
                                + " (+" + Kit.printAsMillis(acquireTime - lockTime) + ")");
                    }
                }
            } else
                System.out.println("write skipped!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void repaint() {
        if (SwingUtilities.isEventDispatchThread())
            doRepaint();
        else
            SwingUtilities.invokeLater(this::doRepaint);
    }

    private void doRepaint() {
        for (Component listener : listeners)
            listener.repaint();
    }

    private void resize() {
        if (SwingUtilities.isEventDispatchThread())
            doResize();
        else
            SwingUtilities.invokeLater(this::doResize);
    }

    private void doResize() {
        Dimension preferredSize = new Dimension(context.getScreenWidth(), context.getScreenHeight());
        for (Component listener : listeners)
            listener.setPreferredSize(preferredSize);
    }

    public void read(Consumer<RailwayModel> reader) {
        try {
            Lock lock = this.lock.readLock();
            if (lock.tryLock(100, TimeUnit.MILLISECONDS))
                try {
                    reader.accept(this);
                } finally {
                    lock.unlock();
                }
            else
                System.out.println("read skipped!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void write(Consumer<RailwayModel> writer) {
        Runnable job = () -> {
            Lock lock = this.lock.writeLock();
            try {
                if (lock.tryLock(100, TimeUnit.MILLISECONDS))
                    try {
                        writer.accept(this);
                    } finally {
                        lock.unlock();
                    }
                else
                    System.out.println("skip write");
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        };

        if (SwingUtilities.isEventDispatchThread())
            executor.execute(job);
        else
            job.run();
    }

    public void addListener(Component listener) {
        listeners.add(listener);
        resize();
        repaint();
    }

    public void setZoom(float newZoom) {
        write(model -> update(new Context(context.getWidth(), context.getHeight(), newZoom, context.getTime())));

    }

    public void setWidth(int width) {
        write(model -> update(new Context(width, context.getHeight(), context.getZoom(), context.getTime())));
    }

    public void setHeight(int height) {
        write(model -> update(new Context(context.getWidth(), height, context.getZoom(), context.getTime())));
    }
}
