package client.aiview;

import client.view.*;
import client.view.Formatter;
import client.application.*;
import player.GameTree;

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

public class AIView extends AnimatablePanel implements ActionListener, MouseListener, MouseMotionListener {

    private AnimatablePanel.Animator rotateAnimator;
    private ThreadCommunicator threadCom;
    private GraphView graphView;

    private boolean onTreeView = false;
    private GameTree gameTree = null;

    private JPanel hintPanel;
    private JButton button;
    private Integer hintState = 0;

    public AIView() {
        //Layout
        setLayout(new CardLayout());
        add(new TutorialView());

        addMouseListener(this);
        addMouseMotionListener(this);

        try {
            threadCom = null;
            setBackground(new Color(131, 226, 197));
            setPreferredSize(new Dimension(400, 800));

            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);

            graphView = new GraphView(json);

            rotateAnimator = createAnimator(0.0, 360.0, 10.0);
            rotateAnimator.setLoops(true);

            Timer time = new Timer(300, this);
            time.setActionCommand("rep");
            time.start();

        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {
      /*Integer closestNode = findClosestNode(e.getPoint());
      //System.err.println("Mouse moved" + closestNode);
      if(closestNode > 0) {
        Node n = getNode(closestNode);
        n.setSelected(true);

        List<List<Integer>> routes = findRoutes(n, true);
        System.err.println("ROUTE: " + routes);
      }*/
    }

    /*public Integer findClosestNode(Point position) {
      Double minDist = Double.POSITIVE_INFINITY;
      Integer minLoc = -1;
      for (Node node : nodes) {
        Vector v = origin.add(node);
        Double x = v.getX();
        Double y = v.getY();

        Double squareDistance = Math.pow(x - position.getX(), 2) + Math.pow(y - position.getY(), 2);
        if (squareDistance < minDist && squareDistance < 100) {
          minDist = squareDistance;
          minLoc = node.location();
        }
      }
      return minLoc;
    }*/

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

    /*private void buildGraphNodes(GraphNodeRep graphNode, Double xStart, Double width, Double y, Node parent) {
        if (graphNode != null) {//Need to subtract origin to get proper location.
            synchronized (graphNode) {
                Double x =  xStart + (width / 2.0);
                Node vector = getNode(graphNode.location());
                Node node = new Node(vector.getX(), vector.getY(), vector.getZ(), graphNode.color(), graphNode.location());
                node.setAnimators(createDelayedAnimator(vector.getX(), x, 1.0), createDelayedAnimator(vector.getY(), y, 1.0), createDelayedAnimator(vector.getZ(), 165.0, 1.0));
                node.setTree(true);
                nodes.add(node);
                if (parent != null) {
                  edges.add(new Edge<Node>(node, parent));
                  node.setParent(parent);
                }
                width = width / graphNode.children().size();
                for (int i = 0; i < graphNode.children().size(); i++) {
                    GraphNodeRep graphNodeRep = graphNode.children().get(i);
                    buildGraphNodes(graphNodeRep, xStart + (width * i), width, y + 80, node);
                }
            }
        }
    }*/

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();
        graphView.setOrigin(new Vector(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0));

        drawEdges(g, graphView.getEdges(), graphView.getOrigin());
        drawVectors(g, graphView.getNodes(), graphView.getOrigin());
    }

    private void drawVectors(Graphics2D g, Set<Node> nodes, Vector origin) {
        for (Node node : nodes) {
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
            Vector node1 = origin.offsetAdd(n1);
            Node n2 = edge.getNode2();
            Vector node2 = origin.offsetAdd(n2);
            g.setColor(new Color(255, 255, 255, Math.min(n1.getColor().getAlpha(), n2.getColor().getAlpha())));
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(GraphNodeRep graphNode) {
        graphView.setGraphNode(graphNode);
        //Start timer for hints
        Timer timer = new Timer(650, this);
        timer.setActionCommand("show_hint");
        timer.setRepeats(false);
        timer.start();
    }

    public void setGameTree(GameTree gameTree) {
        this.gameTree = gameTree;
    }

    /*public void updateTree() {
        resetTree();
        buildGraphNodes(graphNodeRep, -300.0, 600.0, -180.0, null);
        selectExploredNodes(graphNodeRep);
    }*/

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void humanPlaying() {
        if (onTreeView) threadCom.putEvent("timer_fired", true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
            //updateTree();
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("switch_views")) {
            onTreeView = !onTreeView;
            humanPlaying();
            if (onTreeView) {
                hintState = 1;
            }
            //else add(button);
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("show_hint")) {
            if (hintState < 4 && onTreeView) {
                if (hintState == 1) {
                //    showHint("Hint number one.");
                } else if (hintState == 2) {
                //    showHint("Hint number two.");
                } else if (hintState == 3) {
              //      showHint("Hint number three.");
                }
                hintState++;
            }
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("hide_hint")) {
          //  hideHint();
        } else {
            super.actionPerformed(e);
        }
    }

}
