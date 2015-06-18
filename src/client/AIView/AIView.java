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
    private Animator alphaAnimator;
    private ThreadCommunicator threadCom;
    private GraphHandler graphHandler;

    private boolean onTreeView = false;
    private boolean running = false;
    private GameTree gameTree = null;
    private Timer time;

    private JPanel hintPanel;
    private JButton button;

    private MessageView hintsView;
    private MessageView tutorialView;

    private String TUTORIAL = "TUTORIAL";
    private String HINTS = "HINTS";

    private String[] aiHints = {
        "<html><font size=+2 face='Helvetica Neue'>The</font><font face='Helvetica Neue'> AI works by exploring every possible move for each player in turn, generating the above structure called a game tree. Using this tree, it can determine the best possible move for a player, employing a minimax algorithm to do so.</font></html>",
        "<html><font size=+2 face='Helvetica Neue'>You</font><font face='Helvetica Neue'> can see how this tree relates to the game on the left, as each move that the AI considers is drawn over the board.</font></html>",
        "<html><font size=+2 face='Helvetica Neue'>The</font><font face='Helvetica Neue'> red sequence of moves above represents the path that leads to the best outcome for the player. You can see it changing as the AI explores further into the game.</font></html>"
    };

    private String[] tutorialHints = {
        "<font size=+2>The aim:</font> The Detective (Blue counter) must land on the same location as Mr X (Black counter) to win.",
        "<font size=+2>To move:</font> Click on the location you would like to move to, then click on the ticket you wish to use. You can only move to a location to which you can travel using your available tickets.",
        "<font size=+2>Tips:</font> Mr X's location only updates on certain rounds (full circles), so try using his previous moves to predict where he will be next. It is best to go to the locations with more transport options when Mr X is not visible or when you're not sure where he is."
    };

    public AIView(FileAccess fileAccess) {
        try {
            threadCom = null;
            setBackground(Formatter.aiBackgroundColor());
            setPreferredSize(new Dimension(400, 800));

            setLayout(new CardLayout());
            hintsView = new MessageView("The AI is thinking", aiHints, false);
            add(hintsView, "HINTS");
            tutorialView = new MessageView("How to play:", tutorialHints, true);
            add(tutorialView, "TUTORIAL");

            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);

            graphHandler = new GraphHandler(json);

            rotateAnimator = createAnimator(0.0, 360.0, 10.0, true);
            alphaAnimator = null;

            time = new Timer(500, this);
            time.setActionCommand("rep");
            time.start();

            hintsView.start(null);
            switchToView(HINTS);
            running = true;
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
            if (onTreeView && !node.inTree() && alphaAnimator == null) continue;
            Color c = node.getColor();
            if (alphaAnimator != null && !node.inTree()) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alphaAnimator.value() * 255));
            g.setColor(c);

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
            if (onTreeView && !edge.inTree() && alphaAnimator == null) continue;
            Vector node1 = origin.offsetAdd(n1);
            Vector node2 = origin.offsetAdd(n2);

            Color c = edge.getColor();
            if (alphaAnimator != null && !edge.inTree()) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alphaAnimator.value() * 255));
            g.setColor(c);
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(TreeNode treeNode) {
        running = true;
        graphHandler.setTreeNode(treeNode);
        tutorialView.stop();
        hintsView.start(gameTree);
        setTitle("The AI is thinking");
        switchToView(HINTS);
        showTree();
    }

    public void stop() {
        running = false;
        tutorialView.start(null);
        switchToView(TUTORIAL);
        showSphere();
    }

    public void setGameTree(GameTree gameTree) {
        this.gameTree = gameTree;
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }


    private void switchToView(String view) {
        CardLayout layout = (CardLayout) getLayout();
        layout.show(this, view);
    }

    public void showTree() {
        if (onTreeView) return;
        time = new Timer(500, this);
        time.setActionCommand("rep");
        time.start();
        if (gameTree != null) gameTree.pause();
        graphHandler.showTree(this);
        Double rotateValue = rotateAnimator.value();
        removeAnimator(rotateAnimator);
        rotateAnimator = createAnimator(rotateValue, rotateValue + 360.0, 2.0, false);
        alphaAnimator = createAnimator(1.0, 0.0, 1.0, false);
        onTreeView = true;
    }

    public void setTitle(String title) {
        hintsView.setTitle(title);
    }

    public void showSphere() {
        if (!onTreeView) return;
        time.stop();
        graphHandler.returnFromTree(this);
        alphaAnimator = createAnimator(0.0, 1.0, 1.0, false);
        Double rotateValue = rotateAnimator.value();
        removeAnimator(rotateAnimator);
        rotateAnimator = createAnimator(rotateValue, rotateValue + 360.0, 10.0, true);
        onTreeView = false;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
            if (running) {
                if (onTreeView) {
                    if (!graphHandler.animating()) {
                        graphHandler.cleanSpiders();
                        GraphHandler.TreeWorker worker = graphHandler.new TreeWorker(this, threadCom);
                        worker.execute();
                    }
                } else {
                    graphHandler.updateNodes();
                }
                repaint();
            }
        } else {
            super.actionPerformed(e);
        }
    }

    @Override
    public void animationCompleted() {
        if (!onTreeView) {
            threadCom.putUpdate("show_route", new ArrayList<RouteHint>());
            graphHandler.cleanTree();
        } else {
            graphHandler.finishTreeBuild();
            if (gameTree != null) gameTree.resume();
        }
    }

}
