package dz.kalbo.emulator.view;

import dz.kalbo.emulator.model.*;
import dz.kalbo.emulator.tram.EmulatorEngine;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Emulator extends JFrame {

    private final EmulatorEngine engine;
    private final RailwayModel model;

    public Emulator(RailwayModel model) {
        super("Test");
        this.engine = new EmulatorEngine();
        this.engine.addUpdater(model);
        this.model = model;
        RailwayCanvas railwayCanvas = new RailwayCanvas(model);
        model.addRepaintListener(railwayCanvas);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(BorderLayout.CENTER, railwayCanvas);
        contentPane.add(BorderLayout.SOUTH, newToolbar(engine));
        setContentPane(contentPane);
    }

    private Component newToolbar(EmulatorEngine engine) {
        JPanel toolbar = new JPanel();
        JToggleButton pauseButton = new JToggleButton("Pause", !engine.isRunning());
        pauseButton.addActionListener(e -> engine.setRunning(!pauseButton.isSelected()));
        toolbar.add(pauseButton);
        return toolbar;
    }

    private static final class RailwayCanvas extends Container {

        private final RailwayModel model;

        public RailwayCanvas(RailwayModel model) {
            this.model = Objects.requireNonNull(model);
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

                LinkedList<AbstractTranche> tranchesNeedDrawing = new LinkedList<>(road.getHeadTranches());
                HashSet<AbstractTranche> alreadyDrawn = new HashSet<>();

                // draw a label that indicate where the road start
                if (Kit.SHOW_ROAD_START)
                    for (AbstractTranche tranche : tranchesNeedDrawing)
                        Kit.drawLabel(g, Kit.ROAD_START_LABEL + '#' + road.getId(), tranche.getStart().x, tranche.getStart().y);

                while (!tranchesNeedDrawing.isEmpty()) {
                    AbstractTranche currentTranche = tranchesNeedDrawing.pollFirst();
                    if (currentTranche != null) {
                        List<AbstractTranche> nextTrances = currentTranche.getNext();
                        for (AbstractTranche nextTrance : nextTrances)
                            if (!alreadyDrawn.contains(nextTrance))
                                tranchesNeedDrawing.add(nextTrance);

                        currentTranche.draw(g, trancheStroke);
                        alreadyDrawn.add(currentTranche);
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
            Emulator canvas = newEmulationCanvas();
            canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            canvas.setSize(800, 600);
            canvas.setLocationRelativeTo(null);
            canvas.setVisible(true);
        });
    }

    private static Emulator newEmulationCanvas() {
        RailwayModel model = new RailwayModel(800, 600);
        model.write(Emulator::initModel);
        return new Emulator(model);
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

        boolean inward = true;

        ArcTranche loop0 = new ArcTranche(10, new ScalablePoint(420, 220, zoom), new ScalablePoint(560, 160, zoom), context);
        loop0.setSouthDirection(!inward);

        ArcTranche loop1 = new ArcTranche(11, new ScalablePoint(560, 160, zoom), new ScalablePoint(650, 100, zoom), context);
        loop0.addNext(loop1);
        loop1.setSouthDirection(inward);

        ArcTranche loop2 = new ArcTranche(12, new ScalablePoint(650, 100, zoom), new ScalablePoint(560, 40, zoom), context);
        loop2.setSouthDirection(!inward);
        loop1.addNext(loop2);

        ArcTranche loop3 = new ArcTranche(13, new ScalablePoint(560, 40, zoom), new ScalablePoint(450, 100, zoom), context);
        loop2.addNext(loop3);
        loop3.setSouthDirection(!inward);

        ArcTranche loop4 = new ArcTranche(14, new ScalablePoint(450, 100, zoom), new ScalablePoint(560, 160, zoom), context);
        loop4.setSouthDirection(inward);
        loop3.addNext(loop4);

        ArcTranche loop5 = new ArcTranche(15, new ScalablePoint(560, 160, zoom), new ScalablePoint(700, 220, zoom), context);
        loop5.setSouthDirection(!inward);
        loop4.addNext(loop5);

        ArcTranche loop6 = new ArcTranche(16, new ScalablePoint(700, 220, zoom), new ScalablePoint(560, 280, zoom), context);
        loop6.setSouthDirection(inward);
        loop5.addNext(loop6);

        ArcTranche loop7 = new ArcTranche(17, new ScalablePoint(560, 280, zoom), new ScalablePoint(420, 220, zoom), context);
        loop7.setSouthDirection(inward);
        loop6.addNext(loop7);

        loop7.addNext(loop0);
        road.addFirstToHead(loop0);

        StraightTranche tranche6 = new StraightTranche(6, new ScalablePoint(420, 500, zoom), new ScalablePoint(420, 220, zoom), context);
        road.setHead(tranche6);

        model.addRoad(road);

        Tram tram = new Tram(1, 15, 40, 4, 5, context);
        tram.setDirectionToStart(false);
        Speed velocity = new Speed(100 / 1_000d, zoom); // 100px/second
        tram.updatePosition(tranche6, 0f, velocity);
        model.addTram(tram);
    }
}

