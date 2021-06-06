package dz.kalbo.emulator.model;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public class Road implements Drawable {

    private final int id;
    private final Deque<AbstractTranche> headTranches = new LinkedList<>();

    protected Context currentContext;
    private ScalableLength radios;
    private ScalableLength thickness;

    public Road(int id, Context context, int radios, int thickness) {
        this.id = id;
        this.currentContext = context;
        float zoom = context.getZoom();
        this.radios = new ScalableLength(radios, zoom);
        this.thickness = new ScalableLength(thickness, zoom);
    }

    @Override
    public int getId() {
        return id;
    }

    public Deque<AbstractTranche> getHeadTranches() {
        return headTranches;
    }

    public int getRadios() {
        return radios.length;
    }

    public int getThickness() {
        return thickness.length;
    }

    public void setRadios(int radios) {
        this.radios = new ScalableLength(radios, currentContext.getZoom());
    }

    public void setThickness(int thickness) {
        this.thickness = new ScalableLength(thickness, currentContext.getZoom());
    }

    public void setHead(AbstractTranche tranche) {
        Objects.requireNonNull(tranche);
        if (!headTranches.isEmpty()) {
            tranche.addNext(headTranches);
            headTranches.clear();
        }

        addFirstToHead(tranche);
    }

    public void addFirstToHead(AbstractTranche tranche) {
        headTranches.addFirst(Objects.requireNonNull(tranche));
    }

    public void addLastToHead(AbstractTranche tranche) {
        headTranches.addLast(Objects.requireNonNull(tranche));
    }

    @Override
    public Context getCurrentContext() {
        return currentContext;
    }

    @Override
    public final void update(Context newContext) {
        Context currentContext = this.currentContext;
        if (Float.compare(newContext.getZoom(), currentContext.getZoom()) != 0) {
            updateNow(newContext);
        }
    }

    private void updateNow(Context newContext) {
        this.currentContext = newContext;
        this.radios.scale(newContext.getZoom());
        this.thickness.scale(newContext.getZoom());

        LinkedList<AbstractTranche> tranchesNeedUpdate = new LinkedList<>(headTranches);
        while (!tranchesNeedUpdate.isEmpty()) {
            AbstractTranche currentTranche = tranchesNeedUpdate.pollFirst();
            if (currentTranche != null) {
                for (AbstractTranche tranche : currentTranche.getNext())
                    if (tranche.needUpdate(newContext))
                        tranchesNeedUpdate.add(tranche);

                currentTranche.update(newContext);
            }
        }
    }
}
