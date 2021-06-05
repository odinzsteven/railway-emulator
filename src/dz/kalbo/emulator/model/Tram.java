package dz.kalbo.emulator.model;

import dz.kalbo.emulator.view.Kit;

import java.awt.*;
import java.awt.geom.Path2D;

public class Tram implements Drawable {
    private final int id;
    private boolean doubleLocomotive;
    private ScalableLength locomotiveLength;
    private ScalableLength wagonLength;
    private int wagonsCount;
    private int width;
    private Stroke stroke;
    private boolean directionToStart;

    private Context context;

    private Speed velocity;
    private Path2D tramShape;

    // position of tram head that point to the start of the current tranche
    private AbstractTranche headTranche;
    private float headProgress;

    // last time position calculated
    private Long lastTime;

    public Tram(int id, int locomotiveLength, int wagonLength, int wagonsCount, int width, Context context) {
        float zoom = context.getZoom();
        this.id = id;
        this.locomotiveLength = new ScalableLength(locomotiveLength, zoom);
        this.wagonLength = new ScalableLength(wagonLength, zoom);
        this.wagonsCount = wagonsCount;
        this.width = width;
        this.stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

        this.doubleLocomotive = false;
        this.directionToStart = false;

        this.context = context;
        this.velocity = new Speed(zoom);

        update(context);
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
        return width;
    }

    public void setWidth(int width) {
        if (this.width != width) {
            this.width = width;
            update(context);
        }
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        if (this.stroke != stroke) {
            this.stroke = stroke;
            update(context);
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
        if (Float.compare(newContext.getZoom(), currentContext.getZoom()) != 0) {
            float zoom = newContext.getZoom();
            this.locomotiveLength.scale(zoom);
            this.wagonLength.scale(zoom);
            this.velocity.scale(zoom);
        }

        this.context = newContext;

        AbstractTranche startingTranche = this.headTranche;
        if (startingTranche != null) {
            startingTranche.update(newContext);
            if (lastTime == null)
                lastTime = context.getTime();
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
        AbstractTranche.Iterator iterator = new AbstractTranche.Iterator(headTranche, headProgress);

        if (Kit.SHOW_TRAM_HEAD) {
            tramShape.moveTo(iterator.getX() - 5, iterator.getY());
            tramShape.lineTo(iterator.getX() + 5, iterator.getY());
        }

        tramShape.moveTo(iterator.getX(), iterator.getY());
        addTramSection(tramShape, iterator, locomotiveLength.length, directionToStart);

        for (int i = 0; i < wagonsCount; i++)
            addTramSection(tramShape, iterator, wagonLength.length, directionToStart);

        if (doubleLocomotive) {
            addTramSection(tramShape, iterator, locomotiveLength.length, directionToStart);
        }

        this.tramShape = tramShape;
    }

    private void addTramSection(Path2D.Float tramShape, AbstractTranche.Iterator iterator, int length, boolean directionToStart) {
        int headX = iterator.getX();
        int headY = iterator.getY();

        if (directionToStart)
            iterator.moveTowardStart(length);
        else
            iterator.moveTowardEnd(length);

        int tailX = iterator.getX();
        int tailY = iterator.getY();

        tramShape.lineTo(tailX, tailY);
    }

    private void advance(double speed) {
        AbstractTranche.Iterator iterator = new AbstractTranche.Iterator(headTranche, headProgress);

        float distance = (float) (Math.max(context.getTime() - lastTime, 0) * Math.abs(speed));
        iterator.move(distance, speed);

        // make sure the tram is not outside the railway
        int tramLength = getTotalTramLength();
        if (movingBackward()) {
            iterator.move(tramLength, speed);
            iterator.move(tramLength, -speed);
        } else {
            iterator.move(tramLength, -speed);
            iterator.move(tramLength, speed);
        }

        this.headTranche = iterator.getCurrentTranche();
        this.headProgress = iterator.getProgress();
        this.lastTime = context.getTime();
    }

    private boolean movingBackward() {
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
}
