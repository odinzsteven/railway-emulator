package dz.kalbo.emulator.model;

import dz.kalbo.emulator.view.ColorPalette;
import dz.kalbo.emulator.tools.Kit;

import java.awt.*;

public class StraightTranche extends AbstractTranche {

    private double angle;

    public StraightTranche(int id, ScalablePoint start, ScalablePoint end, Context context) {
        super(id, start, end, context);
    }

    @Override
    protected float calcLength(int width, int height) {
        return (float) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
    }

    void onUpdate() {
        this.length = calcLength(distance(start.x, end.x), distance(start.y, end.y));
        this.angle = Kit.calcAngle(end.y - start.y, end.x - start.x);
    }

    @Override
    public void draw(Graphics2D g, Stroke stroke) {
        g.setColor(ColorPalette.TRANCHE);
        g.setStroke(stroke);

        g.drawLine(start.x, start.y, end.x, end.y);

        // draw debug labels
        super.draw(g, stroke);
    }

    @Override
    public int getX(float progress) {
        return getCords(progress, end.x, start.x);
    }

    @Override
    public int getY(float progress) {
        return getCords(progress, end.y, start.y);
    }

    @Override
    public double getAngle(float progress1, float progress2) {
        return angle;
    }

    private int getCords(float progress, int end, int start) {
        if (Float.compare(progress, 1f) < 0) {
            if (Float.compare(progress, 0f) > 0) {
                return (int) (start + (end - start) * progress);
            } else
                return start;
        } else
            return end;
    }
}
