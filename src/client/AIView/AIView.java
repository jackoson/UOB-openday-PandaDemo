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
    private AnimatablePanel.Animator alphaAnimator = null;
    private Set<Node> nodes;
    private List<Edge<Node>> edges;
    private ThreadCommunicator threadCom;
    private Vector origin;

    private boolean onTreeView = false;
    private boolean firstPrune = true;
    private GameTree gameTree = null;
    private GraphNodeRep graphNodeRep;

    private JPanel hintPanel;
    private JButton button;
    private Integer hintState = 0;

    public AIView() {
        //Layout
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(20, 20, 20, 20);

        button = Formatter.button("Whats happening here?");
        button.setActionCommand("switch_views");
        button.addActionListener(this);
        //add(button, gbc);

        JLabel title = new JLabel("The AI is thinking", SwingConstants.CENTER);
        title.setFont(Formatter.defaultFontOfSize(30));
        title.setForeground(Color.WHITE);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        //add(title, gbc);

        addMouseListener(this);
        addMouseMotionListener(this);

        try {
            threadCom = null;
            setBackground(new Color(131, 226, 197));
            setPreferredSize(new Dimension(400, 800));
            initialVectors = new HashMap<Integer, Node>();
            nodes = new TreeSet<Node>(new Comparator<Node>() {
                public int compare(Node o1, Node o2) {
                    Double o1z = o1.getZ();
                    Double o2z = o2.getZ();
                    if (o1z < o2z) return 1;
                    else return -1;
                }
            });
            edges = new ArrayList<Edge<Node>>();
            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);
            parseJSON(json);

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
      Integer closestNode = findClosestNode(e.getPoint());
      //System.err.println("Mouse moved" + closestNode);
      if(closestNode > 0) {
        Node n = (Node)vectors.get(closestNode);
        n.setSelected(true);

        findRoutes(n);
      }
    }

    public Integer findClosestNode(Point position) {
      Double minDist = Double.POSITIVE_INFINITY;
      Integer minLoc = -1;
      for (Map.Entry<Integer, Vector> entry : vectors.entrySet()) {
        Node n = (Node)entry.getValue();
        Vector v = n.getPosInAnimation();
        if (v == null) return -1;
        v = origin.add(v);
        Double x = v.getX();
        Double y = v.getY();

        Double squareDistance = Math.pow(x - position.getX(), 2) + Math.pow(y - position.getY(), 2);
        if (squareDistance < minDist && squareDistance < 100) {
          minDist = squareDistance;
          minLoc = entry.getKey();
        }
      }
      return minLoc;
    }

    public List<List<Integer>> findRoutes(Node n, boolean mrX) {
      if (n == null){
        List<List<Integer>> list = new ArrayList<Integer>();
        list.add(new ArrayList());
        list.add(new ArrayList());
      }
      List<List<Integer>> routes = findRoutes(n.parent(), !mrX);
      if(mrX) routes.get(0).add(n.location());
      else routes.get(1).add(n.location());
      return route;
    }

    private void addHint(String message) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 10, 10, 0);

        hintPanel = new JPanel(new GridBagLayout());
        hintPanel.setPreferredSize(new Dimension(500, 140));
        hintPanel.setOpaque(true);
        hintPanel.setBackground(Color.WHITE);
        Border whiteBorder = new LineBorder(Color.WHITE, 10);
        Border blueBorder = new LineBorder(new Color(131, 226, 197), 1);
        Border compBorder = new CompoundBorder(whiteBorder, blueBorder);
        hintPanel.setBorder(compBorder);

        String firstWord = message.split(" ")[0];
        String theRest = message.replace(firstWord, "");

        JTextPane messageLabel = new JTextPane();
        messageLabel.setContentType("text/html");
        messageLabel.setText("<html><font size=+4 face='Helvetica Neue'>" + firstWord + "</font><font face='Helvetica Neue'>" + theRest + "</font></html>");
        messageLabel.setFont(Formatter.defaultFontOfSize(12));
        messageLabel.setEditable(false);
        messageLabel.setHighlighter(null);
        hintPanel.add(messageLabel, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(hintPanel, gbc);
    }

    private void removeHint() {
        this.remove(hintPanel);
    }

    private void parseJSON(Map<String, List<Map<String, Double>>> json) {
        List<Map<String, Double>> nodes = json.get("nodes");
        for (Map<String, Double> node : nodes) {
            Double ox = node.get("x");
            Double oy = node.get("y");/******SUBTRACT THE ORIGIN SO THE POINTS AREN'T OFF THE SCREEN******/
            Double oz = node.get("z");
            //Transform the points.

            Double y = 2.0 * (oy / 2000) - 1.0;
            Double phi = 2.0 * Math.PI * (ox / 2570);
            Double theta = Math.asin(y);

            Double radius = 60 + (oz * 40);

            Double x = Math.cos(theta) * Math.cos(phi) * radius;
            Double z = Math.cos(theta) * Math.sin(phi) * radius;
            y = y * radius;

            Color color = new Color(255, 113, 113);
            if (oz == 2) color = new Color(42, 154, 164);
            else if (oz == 3) color = new Color(242, 196, 109);

            Node projectedNode = new Node(ox, oy, oz, color, node.get("node").intValue());
            projectedNode.setAnimators(createAnimator(ox, x, 1.0), createAnimator(oy, y, 1.0), createAnimator(oz, z, 1.0));
            this.nodes.add(projectedNode);
        }
        List<Map<String, Double>> connections = json.get("edges");
        for (Map<String, Double> edge : connections) {
            Node node1 = getNode(edge.get("n1").intValue());
            Node node2 = getNode(edge.get("n2").intValue());
            edges.add(new Edge<Node>(node1, node2));
        }
    }

    private Node getNode(int location) {
        for (Node node : nodes) {
            if (node.location() == location) return node;
        }
        return null;
    }

    private void buildGraphNodes(GraphNodeRep graphNode, Double xStart, Double width, Double y, Node parent) {
        if (graphNode != null) {//Need to subtract origin to get proper location.
            synchronized (graphNode) {
                Double x =  xStart + (width / 2.0);
                Node vector = initialVectors.get(graphNode.location());
                Node node = new Node(vector.getX(), vector.getY(), vector.getZ(), graphNode.color(), graphNode.location());
                node.setAnimators(createDelayedAnimator(vector.getX(), x, 1.0), createDelayedAnimator(vector.getY(), y, 1.0), createDelayedAnimator(vector.getZ(), 165.0, 1.0));
                node.setTree(true);
                nodes.add(node);
                if (parent != null) {
                  treeEdges.add(new Edge<Vector>(node, parent));
                  node.setParent(parent);
                }
                width = width / graphNode.children().size();
                for (int i = 0; i < graphNode.children().size(); i++) {
                    GraphNodeRep graphNodeRep = graphNode.children().get(i);
                    buildGraphNodes(graphNodeRep, xStart + (width * i), width, y + 80, node);
                }
            }
        }
    }

    private void selectExploredNodes(GraphNodeRep graphNode) {
        if (graphNode != null) {
            synchronized (graphNode) {
                for (GraphNodeRep graphNodeRep : graphNode.children()) {
                    Node n = initialVectors.get(graphNodeRep.location());
                    n.setSelected(true);
                    selectExploredNodes(graphNodeRep);
                }
            }
        }
    }

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();
        Node origin = new Node(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0, Color.WHITE, 0);

        Double alpha = 255.0;
        if (alphaAnimator != null) alpha = alphaAnimator.value();

        drawEdges(g, edges, origin, alpha.intValue());
        drawVectors(g, nodes, origin, alpha.intValue());
    }

    private void drawVectors(Graphics2D g, Set<Node> nodes, Node origin, int alpha) {
        for (Node node : nodes) {
            Color color = node.getColor();
            Vector vector = node.rotateYZ(rotateAnimator.value());
            vector = vector.rotateXZ(rotateAnimator.value());
            vector = origin.add(vector);
            if (node.isTree()) {
              g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255 - alpha));
            } else {
              g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            }
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Node>> edges, Node origin, int alpha) {
        for (Edge<Node> edge : edges) {
            Node n1 = edge.getNode1();
            Vector node1 = n1.rotateYZ(rotateAnimator.value());
            node1 = node1.rotateXZ(rotateAnimator.value());
            node1 = origin.add(node1);

            Node n2 = edge.getNode2();
            Vector node2 = n2.rotateYZ(rotateAnimator.value());
            node2 = node2.rotateXZ(rotateAnimator.value());
            node2 = origin.add(node2);

            if (n1.isTree() || n2.isTree()) {
              g.setColor(new Color(255, 255, 255, 255 - alpha));
            } else {
              g.setColor(new Color(255, 255, 255, alpha));
            }
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(GraphNodeRep graphNode) {
        graphNodeRep = graphNode;
        //Start timer for hints
        Timer timer = new Timer(650, this);
        timer.setActionCommand("show_hint");
        timer.setRepeats(false);
        timer.start();
    }

    public void setGameTree(GameTree gameTree) {
        this.gameTree = gameTree;
    }

    public void showHint(String text) {
        if (gameTree == null) return;
        if (onTreeView) {
            System.err.println("R:" + gameTree.randomNode());
            gameTree.pause();
            addHint(text);

            Timer timer = new Timer(5000, this);
            timer.setActionCommand("hide_hint");
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void hideHint() {
        if (gameTree == null) return;
        gameTree.resume();
        removeHint();
        Timer timer = new Timer(350, this);
        timer.setActionCommand("show_hint");
        timer.setRepeats(false);
        timer.start();
    }

    public void resetFirstPrune() {
        firstPrune = true;
    }

    public void updateTree() {
        resetTree();
        buildGraphNodes(graphNodeRep, -300.0, 600.0, -180.0, null);
        selectExploredNodes(graphNodeRep);
    }

    private void resetTree() {
        Set<Node> treeVectors = new HashSet<Node>();
        for (Node node : nodes) {
            node.setSelected(false);
            if (node.isTree()) treeVectors.add(node);
        }
        nodes.removeAll(treeVectors);
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void humanPlaying() {
        if (onTreeView) threadCom.putEvent("timer_fired", true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
            updateTree();
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("switch_views")) {
            onTreeView = !onTreeView;
            humanPlaying();
            if (onTreeView) {
                hintState = 1;
            }
            else add(button);
            firstPrune = true;
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("show_hint")) {
            if (hintState < 4 && onTreeView) {
                if (hintState == 1) {
                    showHint("Hint number one.");
                } else if (hintState == 2) {
                    showHint("Hint number two.");
                } else if (hintState == 3) {
                    showHint("Hint number three.");
                }
                hintState++;
            }
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("hide_hint")) {
            hideHint();
        } else {
            super.actionPerformed(e);
        }
    }

}
