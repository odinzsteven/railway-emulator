package dz.kalbo.emulator.tram;

import dz.kalbo.emulator.model.Updater;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmulatorEngine {

    public static final int DEFAULT_TICK = 15;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final List<Updater> updaters = new CopyOnWriteArrayList<>();
    public final int tick;

    private volatile boolean running = true;

    private volatile Long lastUpdate = null;

    public EmulatorEngine() {
        this(DEFAULT_TICK);
    }

    public EmulatorEngine(int tick) {
        this.tick = tick;
        executor.scheduleAtFixedRate(this::update, 0, tick, TimeUnit.MILLISECONDS);
    }

    private void update() {
        if (running) {
            for (Updater updater : updaters)
                try {
                    updater.update(tick);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
        }
    }

    public void addUpdater(Updater updater) {
        if (updater != null)
            updaters.add(updater);
    }

    public void setRunning(boolean run) {
        this.running = run;
    }

    public boolean isRunning() {
        return running;
    }
}
