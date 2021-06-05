package dz.kalbo.emulator.tram;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

public class EmulatorEngine {

    public static final int DEFAULT_TICK = 15;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final List<Runnable> updaters = new CopyOnWriteArrayList<>();
    public final int tick;

    public EmulatorEngine() {
        this(DEFAULT_TICK);
    }

    public EmulatorEngine(int tick) {
        this.tick = tick;
        executor.scheduleAtFixedRate(this::calc, 0, tick, TimeUnit.MILLISECONDS);
    }

    private void calc() {
        for (Runnable updater : updaters)
            try {
                updater.run();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
    }

    public void addUpdater(Runnable updater) {
        if (updater != null)
            updaters.add(updater);
    }

}
