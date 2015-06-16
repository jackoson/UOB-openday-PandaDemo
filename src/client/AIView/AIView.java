package client.aiview;

import client.view.*;
import client.view.Formatter;
import client.application.*;
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

public class AIView extends AnimatablePanel implements ActionListener, MouseMotionListener {

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

    public AIView(FileAccess fileAccess) {
        try {
            threadCom = null;
            setBackground(new Color(131, 226, 197));
            setPreferredSize(new Dimension(400, 800));

            //setLayout(new CardLayout());
            //add(new TutorialView(), "TUTORIAL");
            ratingView = new RatingView(fileAccess);
            //add(ratingView, "RATING");
            ratingView.update(true, MoveDouble.instance(Colour.Black, Ticket.Taxi, 12, Ticket.Underground, 46), "this location has more transport links than the one you chose");
            ratingView.update(false, MoveDouble.instance(Colour.Black, Ticket.Taxi, 12, Ticket.Underground, 46), "this location has more transport links than the one you chose");
            hintsView = new HintsView();
            //add(hintsView, "HINTS");

            JButton button = Formatter.button("Switch");
            button.addActionListener(this);
            button.setActionCommand("switch_views");
            add(button);

            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);

            graphHandler = new GraphHandler(json);

            rotateAnimator = createAnimator(0.0, 360.0, 10.0);
            rotateAnimator.setLoops(true);

            Timer time = new Timer(20, this);
            time.setActionCommand("rep");
            time.start();

            addMouseMotionListener(this);

        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {
        /*
        if (!onTreeView) return;
        Node closestNode = findClosestNode(e.getPoint());
        if(closestNode != null) {
            closestNode.setSelected(true);

            List<List<Integer>> routes = findRoutes(closestNode, true);
            System.err.println("ROUTE: " + routes);
        }
        repaint();
        */
    }
/*
    public Node findClosestNode(Point position) {
      Double minDist = Double.POSITIVE_INFINITY;
      Node minNode = null;
      for (Node node : graphHandler.getNodes()) {
          if (!node.inTree()) continue;
        Vector v = graphHandler.getOrigin().offsetAdd(node);
        Double x = v.getX();
        Double y = v.getY();

        Double squareDistance = Math.pow(x - position.getX(), 2) + Math.pow(y - position.getY(), 2);
        if (squareDistance < minDist && squareDistance < 100) {
          minDist = squareDistance;
          minNode = node;
        }
      }
      return minNode;
    }

    public List<List<Integer>> findRoutes(Node n, boolean mrX) {
      if (n == null){
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        list.add(new ArrayList<Integer>());
        list.add(new ArrayList<Integer>());
        return list;
      }
      List<List<Integer>> routes = findRoutes(n.parent(), !mrX);
      if(mrX) routes.get(0).add(n.location());
      else routes.get(1).add(n.location());
      return routes;
    }
*/
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        graphHandler.rotateNodes(rotateAnimator.value());

        Dimension size = getSize();
        graphHandler.setOrigin(new Vector(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0));

        drawEdges(g, graphHandler.getEdges(), graphHandler.getOrigin());
        drawVectors(g, graphHandler.getNodes(), graphHandler.getOrigin());
    }

    private void drawVectors(Graphics2D g, Set<Node> nodes, Vector origin) {
        for (Node node : nodes) {
            if (onTreeView && !node.inTree()) continue;
            g.setColor(node.getColor());
            Vector vector = origin.offsetAdd(node);
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Node>> edges, Vector origin) {
        //System.err.println("Count: " + edges.size());
        for (Edge<Node> edge : edges) {
            Node n1 = edge.getNode1();
            Node n2 = edge.getNode2();
            if (!edge.inTree() && onTreeView) continue;
            Vector node1 = origin.offsetAdd(n1);
            Vector node2 = origin.offsetAdd(n2);
            g.setColor(new Color(255, 255, 255, Math.min(n1.getColor().getAlpha(), n2.getColor().getAlpha())));
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(TreeNode graphNode) {
        graphHandler.setGraphNode(graphNode);
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void setGameTree(GameTree gameTree) {
        this.gameTree = gameTree;
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void humanPlaying() {
        if (onTreeView) threadCom.putEvent("timer_fired", true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
            if (running){
                if (onTreeView) graphHandler.updateTree(this);
                if(!onTreeView) graphHandler.updateNodes();
                repaint();
            }
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("switch_views")) {
            humanPlaying();
            if (onTreeView) {
                graphHandler.returnFromTree(this);
                onTreeView = false;
            } else {
                onTreeView = true;
                graphHandler.showTree(this);
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
