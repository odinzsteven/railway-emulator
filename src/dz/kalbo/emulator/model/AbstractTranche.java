package dz.kalbo.emulator.model;

import dz.kalbo.emulator.tools.Kit;

import java.awt.*;
import java.util.*;
import java.util.function.DoubleFunction;

public abstract class AbstractTranche implements Drawable, Comparable<AbstractTranche> {

    protected final NavigableSet<AbstractTranche> next = new TreeSet<>();
    protected final NavigableSet<AbstractTranche> previews = new TreeSet<>();

    protected final int id;
    protected ScalablePoint start;
    protected ScalablePoint end;
    protected Context currentContext;
    protected float length;
    protected int priority;

    public AbstractTranche(int id, ScalablePoint start, ScalablePoint end, Context currentContext) {
        this.id = id;
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        this.currentContext = Objects.requireNonNull(currentContext);
        this.priority = id;

        if (start.x == end.x && start.y == end.y)
            throw new IllegalArgumentException("start and end point must be different");

        onUpdate();
    }

    @Override
    public int compareTo(AbstractTranche o) {
        return -Integer.compare(this.priority, o.priority);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Context getCurrentContext() {
        return currentContext;
    }

    @Override
    public final void update(Context newContext) {
        if (needUpdate(newContext)) {
            float zoom = newContext.getZoom();
            start.scale(zoom);
            end.scale(zoom);
        }
        this.currentContext = newContext;
        onUpdate();
    }

    public boolean needUpdate(Context newContext) {
        return Float.compare(newContext.getZoom(), currentContext.getZoom()) != 0;
    }

    abstract void onUpdate();

    public ScalablePoint getStart() {
        return start;
    }

    public void setStart(ScalablePoint start) {
        this.start = start;
        update(currentContext);
    }

    public ScalablePoint getEnd() {
        return end;
    }

    public void setEnd(ScalablePoint end) {
        this.end = end;
        update(currentContext);
    }

    public float getLength() {
        return length;
    }

    protected abstract float calcLength(int width, int height);

    public void addNext(AbstractTranche nextTranche) {
        Objects.requireNonNull(nextTranche);
        if (!next.contains(nextTranche))
            next.add(nextTranche);
        if (!nextTranche.previews.contains(this))
            nextTranche.previews.add(this);
    }

    public void addNext(Collection<AbstractTranche> tranches) {
        if (tranches != null)
            for (AbstractTranche tranche : tranches)
                addNext(tranche);
    }

    public AbstractTranche getFirstNext() {
        if (next.isEmpty())
            return null;
        else
            return next.first();
    }

    public AbstractTranche getFirsPreviews() {
        if (previews.isEmpty())
            return null;
        else
            return previews.first();
    }

    public NavigableSet<AbstractTranche> getNext() {
        return next;
    }

    public NavigableSet<AbstractTranche> getPreviews() {
        return previews;
    }

    static int distance(int first, int second) {
        return Math.max(Math.abs(first - second), 1);
    }

    @Override
    public String toString() {
        return "Tranche#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTranche)) return false;
        AbstractTranche that = (AbstractTranche) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public abstract int getX(float progress);

    public abstract int getY(float progress);

    public abstract double getAngle(float progress1, float progress2);

    public void draw(Graphics2D g, Stroke stroke) {
        if (Kit.SHOW_TRANCHE_START)
            Kit.drawLabel(g, Kit.TRANCHE_START_LABEL + '#' + getId(), start.x, start.y);

        if (Kit.SHOW_TRANCHE_END)
            Kit.drawLabel(g, Kit.TRANCHE_END_LABEL + '#' + getId(), end.x, end.y);
    }

    public static float absProgress(float progressFromStart, double direction) {
        float progress = Kit.bound(progressFromStart);
        return direction > 0 ? progress : 1f - progress;
    }

    public static class Traverser {

        private AbstractTranche tranche;
        private float progress;

        private Integer currentX;
        private Integer currentY;

        public Traverser(AbstractTranche tranche, float progress) {
            this.tranche = Objects.requireNonNull(tranche);
            this.progress = progress;
        }

        public Traverser moveTowardStart(float length) {
            return move(length, 1);
        }

        public Traverser moveTowardEnd(float length) {
            return move(length, -1);
        }

        public Traverser move(float length, double direction) {
            float coveredDistance = AbstractTranche.absProgress(progress, direction) * tranche.getLength();
            float distance = coveredDistance + length;

            while (Float.compare(distance, tranche.getLength()) > 0) {
                distance -= tranche.getLength();
                AbstractTranche nextTranche = getNextTranche(direction);
                if (nextTranche == null) {
                    distance = tranche.getLength();
                    break;
                } else {
                    tranche = nextTranche;
                }
            }

            progress = AbstractTranche.absProgress(distance / tranche.getLength(), direction);
            currentX = null;
            currentY = null;
            return this;
        }

        public Traverser moveToEnd(double speed) {
            return move(Float.MAX_VALUE, speed);
        }

        public AbstractTranche getNextTranche(double direction) {
            return Double.compare(direction, 0d) > 0 ? tranche.getFirstNext() : tranche.getFirsPreviews();
        }

        public AbstractTranche getCurrentTranche() {
            return tranche;
        }

        public float getProgress() {
            return progress;
        }

        public int getX() {
            if (currentX == null)
                currentX = tranche.getX(progress);
            return currentX;
        }

        public int getY() {
            if (currentY == null)
                currentY = tranche.getY(progress);
            return currentY;
        }

        public boolean canMove(double direction) {
            return getNextTranche(direction) != null || Float.compare(AbstractTranche.absProgress(progress, direction), 1f) < 0;
        }

        @Override
        public String toString() {
            return "Iterator{" +
                    "tranche=" + tranche.id +
                    ", progress=" + progress +
                    ", x=" + getX() +
                    ", y=" + getY() +
                    '}';
        }
    }

    public static class StaticTraverser extends Traverser {

        private final Iterator<? extends AbstractTranche> iterator;

        public StaticTraverser(Deque<? extends AbstractTranche> tranches, float progress) {
            super(tranches.peekFirst(), progress);
            this.iterator = tranches.iterator();
            this.iterator.next();
        }

        @Override
        public AbstractTranche getNextTranche(double direction) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }
}
