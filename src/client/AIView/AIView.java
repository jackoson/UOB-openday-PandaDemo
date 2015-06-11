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
            setPreferredSize(new Dimension(600, 400));
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

            Timer time = new Timer(1000, this);
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

            Color color = Color.RED;
            if (oz == 2) color = Color.BLUE;
            else if (oz == 3) color = Color.GREEN;

            vectors.put(node.get("node").intValue(), new Node(x, y, z, color));
        }
        List<Map<String, Double>> connections = json.get("edges");
        for (Map<String, Double> edge : connections) {
            Vector node1 = vectors.get(edge.get("n1").intValue());
            Vector node2 = vectors.get(edge.get("n2").intValue());
            edges.add(new Edge<Vector>(node1, node2));
        }
    }

    private void buildGraphNodes(GraphNodeRep graphNode, Double horizontalSpace, Double y, Integer id, Vector parent) {
        if (graphNode != null) {
            Double x = horizontalSpace / 2.0;
            Vector node = new Node(x, y, 0.0, graphNode.color());
            treeVectors.put(id, node);
            if (parent != null) treeEdges.add(new Edge<Vector>(node, parent));
            for (GraphNodeRep graphNodeRep : graphNode.children()) {
                horizontalSpace = horizontalSpace / graphNode.children().size();
                buildGraphNodes(graphNodeRep, horizontalSpace, y - 40, id++, node);
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

        drawVectors(g, vectors, origin);
        drawEdges(g, edges, origin);

        drawEdges(g, treeEdges, origin);
        drawVectors(g, treeVectors, origin);
    }

    private void drawVectors(Graphics2D g, Map<Integer, Vector> vectors, Vector origin) {
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Color color = ((Node) v.getValue()).getColor();
            g.setColor(color);
            Vector vector = v.getValue().rotateYZ(xAnimator.value());
            vector = vector.rotateXZ(yAnimator.value());

            vector = origin.addVectorToVector(vector);
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Vector>> edges, Vector origin) {
        g.setColor(Color.white);
        for (Edge<Vector> edge : edges) {
            Vector node1 = edge.getNode1().rotateYZ(xAnimator.value());
            node1 = node1.rotateXZ(yAnimator.value());
            node1 = origin.addVectorToVector(node1);
            Vector node2 = edge.getNode2().rotateYZ(xAnimator.value());
            node2 = node2.rotateXZ(yAnimator.value());
            node2 = origin.addVectorToVector(node2);
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(GraphNodeRep graphNode) {
        graphNodeRep = graphNode;
    }

    public void updateTree() {
        treeVectors = new HashMap<Integer, Vector>();
        treeEdges = new ArrayList<Edge<Vector>>();
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Node n = (Node)(v.getValue());
            n.setSelected(false);
        }
        //buildGraphNodes(graphNodeRep, 600.0, 180.0, 0, null);
        selectExploredNodes(graphNodeRep);//////////////////Bad
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void actionPerformed(ActionEvent e) {
        updateTree();
    }

}
