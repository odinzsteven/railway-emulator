package dz.kalbo.emulator.model;

import dz.kalbo.emulator.view.ColorPalette;
import dz.kalbo.emulator.view.Kit;

import java.awt.*;

public class ArcTranche extends AbstractTranche {

    private static final int RIGHT_DRAWING_ANGLE = 0;
    private static final int TOP_DRAWING_ANGLE = 90;
    private static final int LEFT_DRAWING_ANGLE = 180;
    private static final int BOTTOM_DRAWING_ANGLE = 270;

    public static final int DRAWING_ARC_ANGLE = 90;
    public static final double DRAWING_ARC_ANGLE_IN_RADIAN = Math.toRadians(DRAWING_ARC_ANGLE);

    private Rectangle rectangle;
    private int drawingAngle;
    private double startPointAngle;
    private double endPointAngle;
    private boolean southDirection;

    public ArcTranche(int id, ScalablePoint start, ScalablePoint end, Context context) {
        super(id, start, end, context);
    }

    @Override
    public void draw(Graphics2D g, Stroke stroke) {
        g.setColor(ColorPalette.TRANCHE);
        g.setStroke(stroke);

        Rectangle rect = rectangle;
        g.drawArc(rect.x, rect.y, rect.width, rect.height, drawingAngle, DRAWING_ARC_ANGLE);

        if (Kit.SHOW_ARC_RECT) {
            g.setStroke(Kit.INFO_STROKE);
            g.setColor(Kit.INFO_COLOR);
            g.draw(rect);
        }

        if (Kit.SHOW_ARC_CIRCLE) {
            g.setStroke(Kit.INFO_STROKE);
            g.setColor(Kit.INFO_COLOR);
            g.drawOval(rect.x, rect.y, rect.width, rect.height);
        }

        // draw debug labels
        super.draw(g, stroke);
    }

    @Override
    public int getX(float progress) {
        if (Float.compare(progress, 1f) < 0) {
            if (Float.compare(progress, 0f) > 0) {
                int a = end.x - start.x;
                int b = end.y - start.y;
                double tan = Math.tan(getProgressAngle(progress));
                double delta = (a * b) / Math.sqrt(b * b + a * a * tan * tan);
                return (int) (rectangle.x + rectangle.width / 2 + delta);
            } else
                return start.x;
        } else
            return end.x;
    }

    public double getProgressAngle(float progress) {
        float d = getLength() * progress;
        int a = end.x - start.x;
        int b = end.y - start.y;
        // improve implementation TODO
        // https://mysite.du.edu/~jcalvert/math/ellarc.htm#:~:text=Finding%20the%20lengths%20of%20circular,d%20%3D%202r%20is%20the%20diameter
        // https://math.stackexchange.com/questions/433094/how-to-determine-the-arc-length-of-ellipse#comment3969027_433908
        return startPointAngle + (endPointAngle - startPointAngle) * progress;
    }

    @Override
    public int getY(float progress) {
        if (Float.compare(progress, 1f) < 0) {
            if (Float.compare(progress, 0f) > 0) {
                int a = end.x - start.x;
                int b = end.y - start.y;
                double tan = Math.tan(getProgressAngle(progress));
                double delta = (a * b * tan) / Math.sqrt(b * b + a * a * tan * tan);
                return (int) (rectangle.y + rectangle.height / 2 - delta);
            } else
                return start.y;
        } else
            return end.y;
    }

    @Override
    public double getAngle(float progress1, float progress2) {
        return Kit.calcAngle(getY(progress2) - getY(progress1), getX(progress2) - getX(progress1));
    }

    public boolean isSouthDirection() {
        return southDirection;
    }

    public void setSouthDirection(boolean southDirection) {
        if (this.southDirection != southDirection) {
            this.southDirection = southDirection;
            update(currentContext);
        }
    }

    @Override
    protected float calcLength(int width, int height) {
        int smallSide = Math.min(width, height);
        int extra = Math.max(width, height) - smallSide;
        return Math.abs(Kit.PI_DIV_2 * smallSide + extra);
    }

    @Override
    void onUpdate() {
        int width = distance(start.x, end.x);
        int height = distance(start.y, end.y);
        this.length = calcLength(width, height);
        if (southDirection) {
            calcSouthRectangle(width, height);
            calcSouthStartAngle();
        } else {
            calcNorthRectangle(width, height);
            calcNorthStartAngle();
        }
    }

    public void calcNorthRectangle(int width, int height) {
        Rectangle rectangle = new Rectangle();
        rectangle.width = width * 2;
        rectangle.height = height * 2;

        rectangle.x = Math.min(start.x, end.x);
        rectangle.y = Math.min(start.y, end.y);

        boolean startFromLeft = start.x < end.x;
        boolean startFromTop = start.y < end.y;
        if (startFromTop) {
            if (startFromLeft)
                rectangle.x = rectangle.x - width;
        } else if (!startFromLeft)
            rectangle.x = rectangle.x - width;

        this.rectangle = rectangle;
    }

    public void calcSouthRectangle(int width, int height) {
        Rectangle rectangle = new Rectangle();
        rectangle.width = width * 2;
        rectangle.height = height * 2;

        rectangle.x = Math.min(start.x, end.x);
        rectangle.y = Math.min(start.y, end.y) - height;

        boolean startFromLeft = start.x < end.x;
        boolean startFromTop = start.y < end.y;

        if (startFromTop) {
            if (!startFromLeft)
                rectangle.x = rectangle.x - width;
        } else {
            if (startFromLeft)
                rectangle.x = rectangle.x - width;
        }

        this.rectangle = rectangle;
    }

    private void calcSouthStartAngle() {
        boolean startFromLeft = start.x < end.x;
        boolean startFromTop = start.y < end.y;

        if (startFromTop) {
            if (startFromLeft) {
                drawingAngle = LEFT_DRAWING_ANGLE /* -h */;
                startPointAngle = Math.toRadians(drawingAngle);
                endPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
            } else {
                drawingAngle = BOTTOM_DRAWING_ANGLE /* -w -y */;
                startPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
                endPointAngle = Math.toRadians(drawingAngle);
            }
        } else {
            if (startFromLeft) {
                drawingAngle = BOTTOM_DRAWING_ANGLE; /* -w -y */
                startPointAngle = Math.toRadians(drawingAngle);
                endPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
            } else {
                drawingAngle = LEFT_DRAWING_ANGLE /* -h */;
                startPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
                endPointAngle = Math.toRadians(drawingAngle);
            }
        }
    }

    private void calcNorthStartAngle() {
        boolean startFromLeft = start.x < end.x;
        boolean startFromTop = start.y < end.y;

        if (startFromTop) {
            if (startFromLeft) {
                drawingAngle = RIGHT_DRAWING_ANGLE;/* -w */
                startPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
                endPointAngle = Math.toRadians(drawingAngle);
            } else {
                /* -w */
                drawingAngle = TOP_DRAWING_ANGLE;/* -w */
                startPointAngle = Math.toRadians(drawingAngle);
                endPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
            }
        } else {
            if (startFromLeft) {
                drawingAngle = TOP_DRAWING_ANGLE;
                startPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
                endPointAngle = Math.toRadians(drawingAngle);
            } else {
                drawingAngle = RIGHT_DRAWING_ANGLE;/* -w */
                startPointAngle = Math.toRadians(drawingAngle);
                endPointAngle = Math.toRadians(drawingAngle + DRAWING_ARC_ANGLE);
            }
        }
    }

    private static int doubleDistance(int first, int second) {
        return distance(first, second) * 2;
    }
}