package dz.kalbo.emulator.view;

import dz.kalbo.emulator.model.*;
import dz.kalbo.emulator.tools.Kit;
import dz.kalbo.emulator.tram.EmulatorEngine;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Emulator extends JFrame {

    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final Dimension DEFAULT_WINDOW_DIM = new Dimension(SCREEN_SIZE.width * 3 / 4, SCREEN_SIZE.height * 3 / 4);

    private final EmulatorEngine engine;
    private final RailwayModel model;

    public Emulator(RailwayModel model) {
        super("Test");
        this.engine = new EmulatorEngine();
        this.engine.addUpdater(model);
        this.model = model;

        RailwayCanvas railwayCanvas = new RailwayCanvas(model, this);
        JScrollPane scrollPane = new JScrollPane(railwayCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(DEFAULT_WINDOW_DIM);

        railwayCanvas.setScrollPane(scrollPane);
        model.addListener(railwayCanvas);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(BorderLayout.CENTER, scrollPane);
        contentPane.add(BorderLayout.SOUTH, newToolbar(engine));
        setContentPane(contentPane);
    }

    private Component newToolbar(EmulatorEngine engine) {
        JToggleButton pauseButton = new JToggleButton("Pause", !engine.isRunning());
        pauseButton.addActionListener(e -> engine.setRunning(!pauseButton.isSelected()));

        JSlider zoomSlider = new JSlider(25, 500, 100);
        zoomSlider.addChangeListener(e -> model.setZoom(zoomSlider.getValue() / 100.0f));

        JPanel toolbar = new JPanel();
        toolbar.add(pauseButton);
        toolbar.add(zoomSlider);
        return toolbar;
    }

    private static final class RailwayCanvas extends Container {

        private final RailwayModel model;
        private final JFrame frame;
        private JScrollPane scrollPane;

        public RailwayCanvas(RailwayModel model, JFrame frame) {
            this.model = Objects.requireNonNull(model);
            this.frame = Objects.requireNonNull(frame);
        }

        public void setScrollPane(JScrollPane scrollPane) {
            this.scrollPane = scrollPane;
        }

        @Override
        public void setPreferredSize(Dimension preferredSize) {
            super.setPreferredSize(preferredSize);
            revalidate();
        }

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            Graphics2D g = (Graphics2D) graphics;

            int height = getHeight();
            int width = getWidth();
            g.setColor(ColorPalette.BACKGROUND);
            g.fillRect(0, 0, width, height);

            model.read(model -> paintFrame(g, model, width, height));
        }

        private void paintFrame(Graphics2D g, RailwayModel model, int windowWidth, int WindowHeight) {
            int screenWidth = model.getContext().getScreenWidth();
            int screenHeight = model.getContext().getScreenHeight();
            g.translate(Math.max((windowWidth - screenWidth) / 2, 0), Math.max((WindowHeight - screenHeight) / 2, 0));

            g.setColor(ColorPalette.CANVAS_BACKGROUND);
            g.fillRect(0, 0, screenWidth, screenHeight);

            g.setColor(Color.RED); // TODO remove this
            g.drawRect(0, 0, 1, 1); // TODO remove this

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
                        Iterable<AbstractTranche> nextTrances = currentTranche.getNext();
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
            canvas.pack();
            canvas.setLocationRelativeTo(null);
            canvas.setVisible(true);
        });
    }

    private static Emulator newEmulationCanvas() {
        RailwayModel model = new RailwayModel(700, 500);
        model.write(Emulator::initModel);
        return new Emulator(model);
    }

    private static void initModel(RailwayModel model) {
        Context context = model.getContext();
        float zoom = context.getZoom();
        Road road = new Road(1, context, 2, 4);
        StraightTranche tranche1 = new StraightTranche(1, new ScalablePoint(270, 140, zoom), new ScalablePoint(230, 50, zoom), context);
        road.addFirstToHead(tranche1);
        StraightTranche tranche2 = new StraightTranche(2, new ScalablePoint(270, 140, zoom), new ScalablePoint(25, 140, zoom), context);
        road.addFirstToHead(tranche2);
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

        ArcTranche loop5 = new ArcTranche(15, new ScalablePoint(560, 160, zoom), new ScalablePoint(650, 220, zoom), context);
        loop5.setSouthDirection(!inward);
        loop4.addNext(loop5);

        ArcTranche loop6 = new ArcTranche(16, new ScalablePoint(650, 220, zoom), new ScalablePoint(560, 280, zoom), context);
        loop6.setSouthDirection(inward);
        loop5.addNext(loop6);

        ArcTranche loop7 = new ArcTranche(17, new ScalablePoint(560, 280, zoom), new ScalablePoint(420, 220, zoom), context);
        loop7.setSouthDirection(inward);
        loop6.addNext(loop7);

        loop7.addNext(loop0);
        road.addFirstToHead(loop0);

        StraightTranche tranche6 = new StraightTranche(6, new ScalablePoint(420, 450, zoom), new ScalablePoint(420, 220, zoom), context);
        road.setHead(tranche6);

        model.addRoad(road);

        Tram tram = new Tram(1, 15, 20, 4, 5, context);
        tram.setDirectionToStart(false);
        Speed velocity = new Speed(50 / 1_000d, zoom); // 100px/second
        tram.updatePosition(tranche6, 0.5f, velocity);
        model.addTram(tram);
    }
}

