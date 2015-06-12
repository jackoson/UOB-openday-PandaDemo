package client.aiview;

import client.view.*;
import client.application.*;

import javax.swing.*;
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

    private Map<Integer, Vector> treeVectors;
    private List<Edge<Vector>> treeEdges;



    private GraphNodeRep graphNodeRep;

    public AIView() {
        try {
            threadCom = null;

            setBackground(new Color(131, 226, 197));
            setPreferredSize(new Dimension(400, 800));
            xRotate = 0.0;
            yRotate = 0.0;
            vectors = new HashMap<Integer, Vector>();
            edges = new ArrayList<Edge<Vector>>();
            treeVectors = new HashMap<Integer, Vector>();
            treeEdges = new ArrayList<Edge<Vector>>();
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
        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
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
            Double x =  xStart + (width / 2.0);
            Vector node = new Node(x, y, 0.0, graphNode.color());
            treeVectors.put(id, node);
            if (parent != null) treeEdges.add(new Edge<Vector>(node, parent));
            width = width / graphNode.children().size();
            for (int i = 0; i < graphNode.children().size(); i++) {
                GraphNodeRep graphNodeRep = graphNode.children().get(i);
                buildGraphNodes(graphNodeRep, xStart + (width * i), width, y + 40, id, node);
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

        drawEdges(g, edges, origin, true);
        drawVectors(g, vectors, origin, true);

        drawEdges(g, treeEdges, origin, false);
        drawVectors(g, treeVectors, origin, false);
    }

    private void drawVectors(Graphics2D g, Map<Integer, Vector> vectors, Vector origin, boolean rotate) {
        //A bit messy, but it gets the job done. (Orders the vectors by z value so they draw over each other properly)
        Set<Node> sortedVectors = new TreeSet<Node>(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                Double o1z = o1.getZ();
                Double o2z = o2.getZ();
                if (o1z < o2z) return 1;
                else return -1;
            }
        });
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Node n = (Node) v.getValue();
            Color color = n.getColor();
            Vector vector = n;
            if (rotate) {
                vector = n.rotateYZ(xAnimator.value());
                vector = vector.rotateXZ(yAnimator.value());
            }
            vector = origin.addVectorToVector(vector);
            Node nn = new Node(vector.getX(), vector.getY(), vector.getZ(), color, n.isSelected());
            sortedVectors.add(nn);
        }
        for (Node vector : sortedVectors) {
            g.setColor(vector.getColor());
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
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

    public void updateTree() {
        resetTree();
        buildGraphNodes(graphNodeRep, -300.0, 600.0, -180.0, 0, null);
        selectExploredNodes(graphNodeRep);//////////////////Bad
    }

    private void resetTree() {
        treeVectors = new HashMap<Integer, Vector>();
        treeEdges = new ArrayList<Edge<Vector>>();
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Node n = (Node)(v.getValue());
            n.setSelected(false);
        }
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
          updateTree();
        } else {
          super.actionPerformed(e);
        }
    }

}
