package client.aiview;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;

import com.google.gson.*;
import com.google.gson.stream.*;

public class AIView extends JPanel {

    private Point origin;
    private double xRotate, yRotate;
    private Map<Integer, Vector> vectors;
    private List<Edge<Vector>> edges;

    public AIView() {
        try {
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
        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void parseJSON(Map<String, List<Map<String, Double>>> json) {
        List<Map<String, Double>> nodes = json.get("nodes");
        for (Map<String, Double> node : nodes) {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            int z = node.get("z").intValue();
            vectors.put(node.get("node").intValue(), new Vector(x, y, z));
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
        //origin = new Point((int) (size.getWidth() / 2.0), (int) (size.getHeight() / 2.0), 0);
        origin = new Point(0, 0, 0);

        drawVectors(g, vectors, origin, origin);
        drawEdges(g, edges);
    }

    private void drawVectors(Graphics2D g, Map<Integer, Vector> vectors, Point origin, Point start) {
        for (Map.Entry<Integer, Vector> vector : vectors.entrySet()) {
            Vector rotatedVector = vector.getValue().rotateXZ(-xRotate);
            rotatedVector.rotateYZ(-yRotate);
            Point point = origin.addVectorToPoint(rotatedVector);
            int diameter = (int) ((double) (point.getZ() + 100) / 4.0) + 5;
            if (diameter < 5) diameter = 5;
            if (vector.getValue().getZ() == 0) diameter = 15;
            int radius = (int) ((double) diameter / 2.0);
            g.fillOval((int)(point.getX() * 0.75) - radius, (int)(point.getY() * 0.75) - radius, diameter, diameter);
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Vector>> edges) {
        for (Edge<Vector> edge : edges) {
            Vector node1 = edge.getNode1();
            Vector node2 = edge.getNode2();
            g.drawLine((int)(node1.getX() * 0.75), (int)(node1.getY() * 0.75), (int)(node2.getX() * 0.75), (int)(node2.getY() * 0.75));
        }
    }

    public void rotate() {
        xRotate = (xRotate + 0.05) % 360;
        yRotate = (yRotate + 0.001) % 360;
        repaint();
    }

}
