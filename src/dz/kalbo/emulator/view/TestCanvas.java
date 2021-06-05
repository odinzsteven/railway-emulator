package dz.kalbo.emulator.view;

import dz.kalbo.emulator.model.*;
import dz.kalbo.emulator.tram.EmulatorEngine;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;

public class TestCanvas extends JFrame {

    public TestCanvas(RailwayModel model) {
        super("Test");
        setContentPane(new DrawingPanel(model));
    }

    private static final class DrawingPanel extends Container {
        private final EmulatorEngine engine;
        private final RailwayModel model;

        public DrawingPanel(RailwayModel model) {
            this.model = Objects.requireNonNull(model);
            this.engine = new EmulatorEngine();
            this.engine.addUpdater(model);
            this.model.addRepaintListener(this);
        }

        @Override
        public void repaint() {
            super.repaint();
        }

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            Graphics2D g = (Graphics2D) graphics;

            // clear screen TODO move inside #paintFrame()
            g.setColor(ColorPalette.BACKGROUND);
            g.fillRect(0, 0, getWidth(), getHeight());

            // TODO remove this dot
            g.setColor(Color.RED);
            g.drawRect(0, 0, 1, 1);

            model.read(model -> paintFrame(g, model));
        }

        private void paintFrame(Graphics2D g, RailwayModel model) {
            g.setColor(Color.BLACK);

            for (Road road : model.getRoads()) {
                int radios = road.getRadios();
                int thickness = road.getThickness();
                Stroke trancheStroke = new BasicStroke(radios * 2 + thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

                LinkedList<AbstractTranche> currentTranches = new LinkedList<>(road.getHeadTranches());

                // draw a label that indicate where the road start
                if (Kit.SHOW_ROAD_START)
                    for (AbstractTranche tranche : currentTranches)
                        Kit.drawLabel(g, Kit.ROAD_START_LABEL + '#' + road.getId(), tranche.getStart().x, tranche.getStart().y);

                while (!currentTranches.isEmpty()) {
                    AbstractTranche currentTranche = currentTranches.pollFirst();
                    if (currentTranche != null) {
                        currentTranches.addAll(currentTranche.getNext());
                        currentTranche.draw(g, trancheStroke);
                    }
                }
            }

            for (Tram tram : model.getTrams())
                tram.draw(g);

//            // TEST
//            // top right
//            ScalablePoint start = new ScalablePoint(330, 50, zoom);
//            // top left
////            ScalablePoint start = new ScalablePoint(230, 50, zoom);
//            // bottom right
////            ScalablePoint start = new ScalablePoint(330, 250, zoom);
//            // bottom left
////            ScalablePoint start = new ScalablePoint(230, 250, zoom);
//
//            ScalablePoint end = new ScalablePoint(270, 140, zoom);
//            ArcTranche arcTranche = new ArcTranche(2, start, end, context);
//            arcTranche.setSouthDirection(true);
//            g.drawLine(start.x, start.y, end.x, end.y);
//
//            g.setColor(Color.RED);
//            g.setStroke(new BasicStroke(1f));
//
//            drawArchTranche(g, arcTranche);
//
//            g.setColor(Color.blue);
//            g.drawRect(start.x - 2, start.y - 2, 4, 4);
//
//            System.out.println("arc length: " + arcTranche.getLength());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TestCanvas canvas = newEmulationCanvas();
            canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            canvas.setSize(800, 600);
            canvas.setLocationRelativeTo(null);
            canvas.setVisible(true);
        });
    }

    private static TestCanvas newEmulationCanvas() {
        RailwayModel model = new RailwayModel(800, 600);
        model.write(TestCanvas::initModel);
        return new TestCanvas(model);
    }

    private static void initModel(RailwayModel model) {
        Context context = model.getContext();
        float zoom = context.getZoom();
        Road road = new Road(1, context, 2, 4);
        road.addFirstToHead(new StraightTranche(1, new ScalablePoint(270, 140, zoom), new ScalablePoint(230, 50, zoom), context));
        road.addFirstToHead(new StraightTranche(2, new ScalablePoint(270, 140, zoom), new ScalablePoint(25, 140, zoom), context));
        // top right
//        ArcTranche tranche3 = new ArcTranche(3, new ScalablePoint(420, 90, zoom), new ScalablePoint(270, 140, zoom), context);
        // bottom right
        ArcTranche tranche3 = new ArcTranche(3, new ScalablePoint(420, 220, zoom), new ScalablePoint(270, 140, zoom), context);
        // top left
//        ArcTranche tranche3 = new ArcTranche(3, new ScalablePoint(100, 90, zoom), new ScalablePoint(270, 140, zoom), context);
        // bottom left
//        ArcTranche tranche3 = new ArcTranche(3, new ScalablePoint(100, 220, zoom), new ScalablePoint(270, 140, zoom), context);

        road.setHead(tranche3);

        AbstractTranche subRoad = new ArcTranche(5, new ScalablePoint(420, 220, zoom), new ScalablePoint(560, 160, zoom), context);
        ArcTranche nextTranche = new ArcTranche(4, new ScalablePoint(560, 160, zoom), new ScalablePoint(650, 100, zoom), context);
        nextTranche.setSouthDirection(true);
        subRoad.addNext(nextTranche);
        road.addFirstToHead(subRoad);
        StraightTranche tranche6 = new StraightTranche(6, new ScalablePoint(200, 490, zoom), new ScalablePoint(420, 220, zoom), context);
        road.setHead(tranche6);

        model.addRoad(road);

        Tram tram = new Tram(1, 15, 40, 4, 5, context);
        tram.setDirectionToStart(false);
        Speed velocity = new Speed(50 / 1_000_000_000d, zoom); // 50px/second
        tram.updatePosition(tranche6, 0f, velocity);
        model.addTram(tram);
    }
}

