package dz.kalbo.emulator.view;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Kit {

    // global config
    public static final Color INFO_COLOR = Color.RED;
    public static final Stroke INFO_STROKE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public static final boolean SHOW_ROAD_START = true;
    public static final String ROAD_START_LABEL = "road start";

    public static final boolean SHOW_TRANCHE_START = false;
    public static final String TRANCHE_START_LABEL = "start";

    public static final boolean SHOW_TRANCHE_END = true;
    public static final String TRANCHE_END_LABEL = "end";

    public static final boolean SHOW_ARC_RECT = true;
    public static final boolean SHOW_ARC_CIRCLE = true;
    public static final boolean SHOW_UPDATE_TIME = false;
    public static final boolean SHOW_TRAM_HEAD = true;

    // math constants
    public static final float PI_DIV_2 = (float) (Math.PI / 2);
    public static final double PI_x_2 = Math.PI * 2;

    public static final double TOP_RIGHT_ANGLE = Math.toRadians(45);
    public static final double TOP_LEFT_ANGLE = Math.toRadians(135);
    public static final double BOTTOM_LEFT_ANGLE = Math.toRadians(225);
    public static final double BOTTOM_RIGHT_ANGLE = Math.toRadians(315);

    public static final double RIGHT_ANGLE = Math.toRadians(0);
    public static final double TOP_ANGLE = Math.toRadians(90);
    public static final double LEFT_ANGLE = Math.toRadians(180);
    public static final double BOTTOM_ANGLE = Math.toRadians(270);

    // helpers methods
    public static void drawLabel(Graphics2D g, String label, int x, int y) {
        g.setColor(INFO_COLOR);
        g.setStroke(INFO_STROKE);
        g.drawString(label, x + randomOffset(), y + randomOffset());
    }

    private static int randomOffset() {
//        return ThreadLocalRandom.current().nextInt(-5, 5);
        return 0;
    }

    public static void drawAngledLine(Graphics2D g, int x, int y, int length, double angle) {
        double actualAngle = PI_x_2 - angle;
        int x2 = (int) (x + length * Math.cos(actualAngle));
        int y2 = (int) (y + length * Math.sin(actualAngle));
        g.drawLine(x, y, x2, y2);
    }

    public static float bound(float value) {
        return bound(value, 0f, 1f);
    }

    public static float bound(float value, float min, float max) {
        if (Float.compare(value, min) < 0)
            return min;
        else if (Float.compare(value, max) > -1)
            return max;
        else
            return value;
    }

    public static double calcAngle(int y, int x) {
        return PI_x_2 - Math.atan2(y, x);
    }

    public static String printAsMillis(long nanos) {
        return (nanos / 1_000_000) + " ms";
    }
}
