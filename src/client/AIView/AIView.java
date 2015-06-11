package client.aiview;

import client.view.*;
import client.application.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;

import com.google.gson.*;
import com.google.gson.stream.*;

public class AIView extends AnimatablePanel {

    private double xRotate, yRotate;
    private AnimatablePanel.Animator yAnimator, xAnimator;
    private Map<Integer, Vector> vectors;
    private List<Edge<Vector>> edges;
    private ThreadCommunicator threadCom;

    public AIView() {
        try {
            threadCom = null;

            setPreferredSize(new Dimension(600, 400));
            xRotate = 0.0;
            yRotate = 0.0;
            vectors = new HashMap<Integer, Vector>();
            edges = new ArrayList<Edge<Vector>>();
            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);
            parseJSON(json);

            yAnimator = createAnimator(0.0, 360.0, 10.0);
            xAnimator = createAnimator(0.0, 360.0, 10.0);
            //Start Polling Queue
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

            Double y = -1.0 + 2.0 * (oy / 2000);
            Double phi = 2.0 * Math.PI * (ox /2570);
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

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();
        Vector origin = new Vector(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0);

        drawVectors(g, vectors, origin);
        drawEdges(g, edges, origin);
    }

    private void drawVectors(Graphics2D g, Map<Integer, Vector> vectors, Vector origin) {
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Color color = ((Node) v.getValue()).getColor();
            g.setColor(color);
            Vector vector = v.getValue().rotateYZ(xAnimator.value());
            vector = vector.rotateXZ(yAnimator.value());

            vector = origin.addVectorToVector(vector);
            Double diameter = 11.25 - (vector.getZ() * (17.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }
    
    private void drawEdges(Graphics2D g, List<Edge<Vector>> edges, Vector origin) {
        g.setColor(Color.BLACK);
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

    public void pollQueue() {
        while (true) {
            try {
                String id = (String) threadCom.takeUpdate();
                Object object = threadCom.takeUpdate();
            } catch(Exception e) {
                System.err.println("Error taking items from the queue :" + e);
                e.printStackTrace();
            }
        }
    }

    private void decodeUpdate(String id, Object object) {
        if (id.equals("ai_dispay_tree")) {
            //GraphNodeRep graphNode = (GraphNodeRep) object;
            //Do Stuff
        }
    }

    public void animationCompleted() {
      yAnimator = createAnimator(0.0, 360.0, 10.0);
      xAnimator = createAnimator(0.0, 360.0, 10.0);
    }

}
