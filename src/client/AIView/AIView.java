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

public class AIView extends AnimatablePanel implements ActionListener {

    private double xRotate, yRotate;
    private AnimatablePanel.Animator yAnimator, xAnimator;
    private Map<Integer, Vector> vectors;
    private List<Edge<Vector>> edges;
    private ThreadCommunicator threadCom;

    private Set<Vector> treeVectors;
    private List<Edge<Vector>> treeEdges;

    private boolean onTreeView = false;
    private boolean showPrune = false;
    private boolean firstPrune = true;
    private GameTree gameTree = null;
    private GraphNodeRep graphNodeRep;

    private Map<Vector, NodeAnimator> nodeAnimators;
    private boolean nodeAnimationStarted = false;
    private AnimatablePanel.Animator alphaAnimator = null;

    public AIView() {
        //Layout
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(20, 20, 20, 20);

        JButton button = Formatter.button("Whats happening?");
        button.setActionCommand("switch_views");
        button.addActionListener(this);
        add(button, gbc);

        JLabel title = new JLabel("The AI is thinking", SwingConstants.CENTER);
        title.setFont(Formatter.defaultFontOfSize(30));
        title.setForeground(Color.WHITE);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //add(title, gbc);

        try {
            threadCom = null;

            setBackground(new Color(131, 226, 197));
            setPreferredSize(new Dimension(400, 800));
            xRotate = 0.0;
            yRotate = 0.0;
            vectors = new HashMap<Integer, Vector>();
            edges = new ArrayList<Edge<Vector>>();
            treeVectors = new HashSet<Vector>();
            treeEdges = new ArrayList<Edge<Vector>>();
            nodeAnimators = new HashMap<Vector, NodeAnimator>();
            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);
            parseJSON(json);

            yAnimator = createAnimator(0.0, 360.0, 10.0);
            yAnimator.setLoops(true);
            xAnimator = createAnimator(0.0, 360.0, 10.0);
            xAnimator.setLoops(true);

            Timer time = new Timer(300, this);
            time.setActionCommand("rep");
            time.start();

            showHint("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nisl felis, accumsan sed sapien eget, faucibus egestas lectus. Cras eu auctor metus, at aliquet augue. Donec semper facilisis porta.");
        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void showHint(String message) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 10, 10, 0);

        JPanel hintPanel = new JPanel(new GridBagLayout());
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

    private void parseJSON(Map<String, List<Map<String, Double>>> json) {
        List<Map<String, Double>> nodes = json.get("nodes");
        for (Map<String, Double> node : nodes) {
            Double ox = node.get("x");
            Double oy = node.get("y");
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

            vectors.put(node.get("node").intValue(), new Node(x, y, z, color));
        }
        List<Map<String, Double>> connections = json.get("edges");
        for (Map<String, Double> edge : connections) {
            Vector node1 = vectors.get(edge.get("n1").intValue());
            Vector node2 = vectors.get(edge.get("n2").intValue());
            edges.add(new Edge<Vector>(node1, node2));
        }
    }

    private void buildGraphNodes(GraphNodeRep graphNode, Double xStart, Double width, Double y, Integer id, Vector parent) {
        if (graphNode != null) {
            Integer location = graphNode.location();

            Node node = (Node) vectors.get(location);

            Double x =  xStart + (width / 2.0);

            if (!node.isPartOfTree()) {
                //Animate it.
                AnimatablePanel.Animator xAnimator = createDelayedAnimator(node.getX(), x, 10.0);
                xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
                AnimatablePanel.Animator yAnimator = createDelayedAnimator(node.getY(), y, 10.0);
                yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
                AnimatablePanel.Animator zAnimator = createDelayedAnimator(node.getZ(), 165.0, 10.0);
                zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
                NodeAnimator animator = new NodeAnimator(xAnimator, yAnimator, zAnimator);
                nodeAnimators.put(node, animator);
                node.setPartOfTree(true);
            } else {
                node = new Node(x, y, 165.0, graphNode.color(), true);
                node.setPartOfTree(true);
                treeVectors.add(node);
            }

            if (parent != null) treeEdges.add(new Edge<Vector>(node, parent));
            width = width / graphNode.children().size();
            for (int i = 0; i < graphNode.children().size(); i++) {
                GraphNodeRep graphNodeRep = graphNode.children().get(i);
                buildGraphNodes(graphNodeRep, xStart + (width * i), width, y + 80, id * width.intValue() * i, node);
            }
        }
    }

    private void selectExploredNodes(GraphNodeRep graphNode) {
        if (graphNode != null) {
            for (GraphNodeRep graphNodeRep : graphNode.children()) {
                Node n = (Node)(vectors.get(graphNodeRep.location()));
                n.setSelected(true);
                selectExploredNodes(graphNodeRep);
            }
        }
    }

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();
        Vector origin = new Vector(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0);

        boolean rotate = true;
        if (xAnimator == null && yAnimator == null) rotate = false;

        drawEdges(g, edges, origin, rotate);
        Set<Vector> valueSet = new HashSet<Vector>(vectors.values());
        drawVectors(g, valueSet, origin, rotate);

        if (alphaAnimator != null) {
            drawEdges(g, treeEdges, origin, false);
            drawVectors(g, treeVectors, origin, false);
        }
    }

    private void drawVectors(Graphics2D g, Set<Vector> vectors, Vector origin, boolean rotate) {
        //A bit messy, but it gets the job done. (Orders the vectors by z value so they draw over each other properly)
        Set<Node> sortedVectors = new TreeSet<Node>(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                Double o1z = o1.getZ();
                Double o2z = o2.getZ();
                if (o1z < o2z) return 1;
                else return -1;
            }
        });
        for (Vector vector : vectors) {
            Node n = (Node)vector;
            Color color = n.getColor();
            if (rotate) {
                vector = n.rotateYZ(xAnimator.value());
                vector = vector.rotateXZ(yAnimator.value());
            }
            vector = updateNode(vector, origin);
            Node nn = new Node(vector.getX(), vector.getY(), vector.getZ(), color, n.isSelected());
            nn.setPartOfTree(n.isPartOfTree());
            sortedVectors.add(nn);
        }
        for (Node vector : sortedVectors) {
            g.setColor(vector.getColor());
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }

    private Vector updateNode(Vector vector, Vector origin) {
        if (nodeAnimators.containsKey(vector) && nodeAnimationStarted) {
            NodeAnimator animator = nodeAnimators.get(vector);
            Double x = animator.xAnimator.value();
            Double y = animator.yAnimator.value();
            Double z = animator.zAnimator.value();
            return new Vector(x, y, z);
        } else {
          return origin.addVectorToVector(vector);
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Vector>> edges, Vector origin, boolean rotate) {
        g.setColor(Color.white);
        for (Edge<Vector> edge : edges) {
            Vector node1 = edge.getNode1();
            if (rotate) {
                node1 = node1.rotateYZ(xAnimator.value());
                node1 = node1.rotateXZ(yAnimator.value());
              }
            node1 = origin.addVectorToVector(node1);
            Vector node2 = edge.getNode2();
            if (rotate) {
                node2 = node2.rotateYZ(xAnimator.value());
                node2 = node2.rotateXZ(yAnimator.value());
            }
            node2 = origin.addVectorToVector(node2);
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(GraphNodeRep graphNode) {
        graphNodeRep = graphNode;
    }

    public void showPrune(GameTree gameTree) {
        if (onTreeView && showPrune && !firstPrune) {
            this.gameTree = gameTree;
            gameTree.pause();
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                System.err.println(e);
            }
            showPrune = false;
            gameTree.resume();
        } else if (onTreeView && firstPrune) {
            Timer timer = new Timer(650, this);
            timer.setActionCommand("show_prune");
            timer.setRepeats(false);
            firstPrune = false;
            timer.start();
        }
    }

    public void resetFirstPrune() {
        firstPrune = true;
    }

    public void updateTree() {
        resetTree();
        buildGraphNodes(graphNodeRep, -300.0, 600.0, -180.0, 0, null);
        selectExploredNodes(graphNodeRep);//////////////////Bad
    }

    private void resetTree() {
        nodeAnimators.clear();
        treeVectors.clear();
        treeEdges.clear();
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Node n = (Node)(v.getValue());
            n.setSelected(false);
            n.setPartOfTree(false);
        }
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
          updateTree();
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("switch_views")) {
            onTreeView = !onTreeView;
            firstPrune = true;
            cancelAllAnimations();
            xAnimator = null;
            yAnimator = null;
            nodeAnimationStarted = true;
            alphaAnimator = createDelayedAnimator(1.0, 0.0, 1.0);
            start();
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("show_prune")) {
            showPrune = true;
        } else {
          super.actionPerformed(e);
        }
    }

    public void animationCompleted() {
        nodeAnimators.clear();
        alphaAnimator = null;
        nodeAnimationStarted = false;
    }

    private class NodeAnimator {

        /**
         * The AnimatablePanel.Animator for the x coordinate.
         */
        public AnimatablePanel.Animator xAnimator;

        /**
         * The AnimatablePanel.Animator for the y coordinate.
         */
        public AnimatablePanel.Animator yAnimator;

        /**
         * The AnimatablePanel.Animator for the z coordinate.
         */
        public AnimatablePanel.Animator zAnimator;

        /**
         * Constructs a new BoardAnimator object.
         *
         * @param scaleAnimator the AnimatablePanel.Animator for the scale Factor.
         * @param xAnimator the AnimatablePanel.Animator for the x coordinate.
         * @param yAnimator the AnimatablePanel.Animator for the y coordinate.
         */
        public NodeAnimator(AnimatablePanel.Animator xAnimator, AnimatablePanel.Animator yAnimator,
                               AnimatablePanel.Animator zAnimator) {
            this.xAnimator = xAnimator;
            this.yAnimator = yAnimator;
            this.zAnimator = zAnimator;
        }

    }

}
