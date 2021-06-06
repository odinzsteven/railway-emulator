package dz.kalbo.emulator.model;

import dz.kalbo.emulator.tools.FinalList;
import dz.kalbo.emulator.tools.Kit;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.LinkedList;

public class Tram implements Drawable {
    private static final LinkedList<AbstractTranche> NO_TRANCHES = new FinalList<>();
    private final int id;
    private boolean doubleLocomotive;
    private ScalableLength locomotiveLength;
    private ScalableLength wagonLength;
    private int wagonsCount;
    private ScalableLength width;
    private Stroke stroke;
    private boolean directionToStart;

    private Context context;

    private Speed velocity;
    private Path2D tramShape;

    // position of tram head that point to the start of the current tranche
    private AbstractTranche headTranche;
    private float headProgress;

    private LinkedList<AbstractTranche> tranchesUnderTram = NO_TRANCHES;

    // last time position calculated
    private Long lastTime;

    public Tram(int id, int locomotiveLength, int wagonLength, int wagonsCount, int width, Context context) {
        float zoom = context.getZoom();
        this.id = id;
        this.locomotiveLength = new ScalableLength(locomotiveLength, zoom);
        this.wagonLength = new ScalableLength(wagonLength, zoom);
        this.wagonsCount = wagonsCount;
        this.width = new ScalableLength(width, zoom);

        this.doubleLocomotive = false;
        this.directionToStart = false;

        this.velocity = new Speed(zoom);

        update(context);
    }

    private void updateDrawParams() {
        this.stroke = new BasicStroke(this.width.length, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    }

    @Override
    public int getId() {
        return id;
    }

    public double getCurrentSpeed() {
        if (velocity != null)
            return directionToStart ? -velocity.speed : velocity.speed;
        else
            return 0d;
    }

    public boolean isDoubleLocomotive() {
        return doubleLocomotive;
    }

    public void setDoubleLocomotive(boolean doubleLocomotive) {
        if (this.doubleLocomotive != doubleLocomotive) {
            this.doubleLocomotive = doubleLocomotive;
            update(context);
        }
    }

    public ScalableLength getLocomotiveLength() {
        return locomotiveLength;
    }

    public void setLocomotiveLength(ScalableLength locomotiveLength) {
        if (this.locomotiveLength != locomotiveLength) {
            this.locomotiveLength = locomotiveLength;
            locomotiveLength.scale(context.getZoom());
            update(context);
        }
    }

    public ScalableLength getWagonLength() {
        return wagonLength;
    }

    public void setWagonLength(ScalableLength wagonLength) {
        if (this.wagonLength != wagonLength) {
            this.wagonLength = wagonLength;
            wagonLength.scale(context.getZoom());
            update(context);
        }
    }

    public int getWagonsCount() {
        return wagonsCount;
    }

    public void setWagonsCount(int wagonsCount) {
        if (this.wagonsCount != wagonsCount) {
            this.wagonsCount = wagonsCount;
            update(context);
        }
    }

    public int getWidth() {
        return width.length;
    }

    public void setWidth(ScalableLength width) {
        if (this.width != width) {
            width.scale(context.getZoom());
            this.width = width;
            updateDrawParams();
        }
    }

    public boolean isDirectionToStart() {
        return directionToStart;
    }

    public void setDirectionToStart(boolean directionToStart) {
        if (this.directionToStart != directionToStart) {
            this.directionToStart = directionToStart;
            update(context);
        }
    }

    @Override
    public Context getCurrentContext() {
        return context;
    }

    public void draw(Graphics2D g) {
        if (tramShape != null) {
            g.setStroke(stroke);
            g.draw(tramShape);
        }
    }

    @Override
    public final void update(Context newContext) {
        Context currentContext = this.context;
        if (currentContext == null ||
                Float.compare(newContext.getZoom(), currentContext.getZoom()) != 0) {
            float zoom = newContext.getZoom();
            this.locomotiveLength.scale(zoom);
            this.wagonLength.scale(zoom);
            this.velocity.scale(zoom);
            this.width.scale(zoom);
            updateDrawParams();
        }

        this.context = newContext;

        AbstractTranche startingTranche = this.headTranche;
        if (startingTranche != null) {
            startingTranche.update(newContext);
            if (lastTime == null)
                lastTime = this.context.getTime();
            else {
                double currentSpeed = getCurrentSpeed();
                if (Double.compare(currentSpeed, 0d) != 0)
                    advance(currentSpeed);

                updateShape();
            }
        } else {
            this.lastTime = null;
            this.tramShape = null;
        }
    }

    private void updateShape() {
        Path2D.Float tramShape = new Path2D.Float();
        AbstractTranche.Traverser traverser = new AbstractTranche.StaticTraverser(tranchesUnderTram, headProgress);
//        System.out.println(AbstractTranche.absProgress(traverser.getProgress(), direction));

        if (Kit.SHOW_TRAM_HEAD) {
            tramShape.moveTo(traverser.getX() - 5, traverser.getY());
            tramShape.lineTo(traverser.getX() + 5, traverser.getY());
        }

        tramShape.moveTo(traverser.getX(), traverser.getY());
        addTramSection(tramShape, traverser, locomotiveLength.length, directionToStart);

        for (int i = 0; i < wagonsCount; i++)
            addTramSection(tramShape, traverser, wagonLength.length, directionToStart);

        if (doubleLocomotive) {
            addTramSection(tramShape, traverser, locomotiveLength.length, directionToStart);
        }

        this.tramShape = tramShape;
    }

    private void addTramSection(Path2D.Float tramShape, AbstractTranche.Traverser traverser, int length, boolean directionToStart) {
        int headX = traverser.getX();
        int headY = traverser.getY();

        if (directionToStart)
            traverser.moveTowardStart(length);
        else
            traverser.moveTowardEnd(length);

        int tailX = traverser.getX();
        int tailY = traverser.getY();

        tramShape.lineTo(tailX, tailY);
    }

    private void advance(double speed) {
        float distance = (float) (Math.max(context.getTime() - lastTime, 0) * Math.abs(speed));
        int tramLength = getTotalTramLength();

        AbstractTranche.Traverser traverser = new AbstractTranche.Traverser(headTranche, headProgress);
        traverser.move(distance, speed);

        headTranche = traverser.getCurrentTranche();
        headProgress = traverser.getProgress();
        lastTime = context.getTime();

        updateTranchesUnderTram(tramLength);
    }

    private AbstractTranche traverse(double speed, float distance) {
        return new AbstractTranche.Traverser(headTranche, headProgress).move(distance, speed).getCurrentTranche();
    }

    private void updateTranchesUnderTram(int tramLength) {
        if (headTranche != null) {
            LinkedList<AbstractTranche> currentTranchesUnderTram = new LinkedList<>();
            currentTranchesUnderTram.add(headTranche);
            float tranchesLength = headTranche.getLength() * AbstractTranche.absProgress(headProgress, directionToStart ? -1d : 1);

            Iterator<AbstractTranche> iterator = tranchesUnderTram.iterator();
            while (iterator.hasNext() && Float.compare(tranchesLength, tramLength) < 0) {
                tranchesLength += addUnderTram(currentTranchesUnderTram, iterator.next());
            }

            if (isMovingBackward()) {
                while (currentTranchesUnderTram.peekLast().getFirstNext() != null
                        && Float.compare(tranchesLength, tramLength) < 0)
                    //noinspection ConstantConditions
                    tranchesLength += addUnderTram(currentTranchesUnderTram, currentTranchesUnderTram.peekLast().getFirstNext());
            } else {
                //noinspection ConstantConditions
                while (currentTranchesUnderTram.peekLast().getFirsPreviews() != null
                        && Float.compare(tranchesLength, tramLength) < 0)
                    //noinspection ConstantConditions
                    tranchesLength += addUnderTram(currentTranchesUnderTram, currentTranchesUnderTram.peekLast().getFirsPreviews());
            }

            this.tranchesUnderTram = currentTranchesUnderTram;
        } else
            tranchesUnderTram = NO_TRANCHES;
    }

    private float addUnderTram(LinkedList<AbstractTranche> currentTranchesUnderTram, AbstractTranche tranche) {
        if (tranche != null) {
            if (!tranche.equals(currentTranchesUnderTram.peekLast())) {
                currentTranchesUnderTram.addLast(tranche);
                return tranche.getLength();
            }
        }
        return 0f;
    }

    private boolean isMovingBackward() {
        return velocity != null && velocity.speed < 0;
    }

    private int getTotalTramLength() {
        return wagonsCount * wagonLength.length + locomotiveLength.length * (doubleLocomotive ? 2 : 1);
    }

    public void updatePosition(AbstractTranche headTranche, float headProgress, Speed velocity) {
        this.headTranche = headTranche;
        this.headProgress = headProgress;
        this.velocity = velocity;
        update(context);
    }

    @Override
    public String toString() {
        return "Tram#" + id + "{head=#" + (headTranche != null ? headTranche.id : null) + "," +
                "over: " + tranchesUnderTram + ", " + velocity + ", " + (directionToStart ? "toStart" : "toEnd") +
                '}';
    }
}
