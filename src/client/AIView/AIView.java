package client.aiview;

import client.view.*;
import client.view.Formatter;
import client.application.*;
import client.model.*;
import player.*;

import scotlandyard.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;

import com.google.gson.*;
import com.google.gson.stream.*;

public class AIView extends AnimatablePanel implements ActionListener {

    private AnimatablePanel.Animator rotateAnimator;
    private ThreadCommunicator threadCom;
    private GraphHandler graphHandler;

    private boolean onTreeView = false;
    private boolean running = false;
    private GameTree gameTree = null;

    private JPanel hintPanel;
    private JButton button;

    private RatingView ratingView;
    private HintsView hintsView;

    private String TUTORIAL = "TUTORIAL";
    private String RATING = "RATING";
    private String HINTS = "HINTS";
    private String BUTTON = "BUTTON";

    /*
    ratingView.update(true, MoveDouble.instance(Colour.Black, Ticket.Taxi, 12, Ticket.Underground, 46), "this location has more transport links than the one you chose");
    ratingView.update(false, MoveDouble.instance(Colour.Black, Ticket.Taxi, 12, Ticket.Underground, 46), "this location has more transport links than the one you chose");
    */

    public AIView(FileAccess fileAccess) {
        try {
            threadCom = null;
            setBackground(Formatter.aiBackgroundColor());
            setPreferredSize(new Dimension(400, 800));

            setLayout(new CardLayout());
            add(new TutorialView(), "TUTORIAL");
            ratingView = new RatingView(fileAccess);
            add(ratingView, "RATING");
            hintsView = new HintsView(fileAccess);
            add(hintsView, "HINTS");
            add(new ButtonView(this), "BUTTON");
            switchToView(BUTTON);

            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);

            graphHandler = new GraphHandler(json);

            rotateAnimator = createAnimator(0.0, 360.0, 10.0);
            rotateAnimator.setLoops(true);

            Timer time = new Timer(50, this);
            time.setActionCommand("rep");
            time.start();

        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean onTreeView() {
        return onTreeView;
    }

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        graphHandler.rotateNodes(rotateAnimator.value());

        Dimension size = getSize();
        graphHandler.setOrigin(new Vector(size.getWidth() / 2.0, size.getHeight() / 2.0 - 100, 0.0));

        drawEdges(g, graphHandler.getEdges(), graphHandler.getOrigin());
        drawVectors(g, graphHandler.getNodes(), graphHandler.getOrigin());
    }

    private void drawVectors(Graphics2D g, Set<Node> nodes, Vector origin) {
        for (Node node : nodes) {
            if (node.getColor().getAlpha() == 0) continue;
            g.setColor(node.getColor());
            Vector vector = origin.offsetAdd(node);
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Node>> edges, Vector origin) {
        for (Edge<Node> edge : edges) {
            Node n1 = edge.getNode1();
            Node n2 = edge.getNode2();
            if (edge.getAlpha() == 0.0) continue;
            Vector node1 = origin.offsetAdd(n1);
            Vector node2 = origin.offsetAdd(n2);
            g.setColor(edge.getColor());
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(TreeNode treeNode) {
        graphHandler.setTreeNode(treeNode);
        setRepaints(false);
        running = true;
        switchToView(BUTTON);
        showTree();
    }

    public void stop() {
        setRepaints(true);
        running = false;
        humanPlaying(onTreeView);
        switchToView(TUTORIAL);
        showSphere();

    }

    public void setGameTree(GameTree gameTree) {
        this.gameTree = gameTree;
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void humanPlaying(boolean human) {
        System.err.println("Sending" + human);
        if (human) threadCom.putEvent("human_playing", true);
        else threadCom.putEvent("ai_playing", true);
    }

    private void switchToView(String view) {
        CardLayout layout = (CardLayout) getLayout();
        layout.show(this, view);
    }

    public void showTree() {
        if (onTreeView) return;
        humanPlaying(true);
        onTreeView = true;
        graphHandler.showTree(this);
        System.err.println("Switch");
    }

    public void showSphere() {
        if (!onTreeView) return;
        threadCom.putUpdate("show_route", new ArrayList<RouteHint>());
        graphHandler.returnFromTree(this);
        onTreeView = false;
        humanPlaying(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
            if (running) {
                if (onTreeView) {
                    if (!graphHandler.animating()) {
                        graphHandler.cleanSpiders();
                        threadCom.putUpdate("show_route", graphHandler.updateTree(this));
                    }
                } else {
                    graphHandler.updateNodes();
                }
                repaint();
            }
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("switch_views")) {
            if (onTreeView) {
                threadCom.putUpdate("show_route", new ArrayList<RouteHint>());
                graphHandler.returnFromTree(this);
                onTreeView = false;
                humanPlaying(false);
            } else {
                humanPlaying(true);
                onTreeView = true;
                graphHandler.showTree(this);
                System.err.println("Switch");
            }
        }else {
            super.actionPerformed(e);
        }
    }

    @Override
    public void animationCompleted() {
        if (!onTreeView) {
            graphHandler.cleanTree();
            Double rotateValue = rotateAnimator.value();
            removeAnimator(rotateAnimator);
            rotateAnimator = createAnimator(rotateValue, rotateValue + 360.0, 10.0);
            rotateAnimator.setLoops(true);
        } else {
            graphHandler.finishTreeBuild();
        }
    }

}
