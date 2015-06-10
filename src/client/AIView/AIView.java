package client.aiview;

import client.view.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;

import com.google.gson.*;
import com.google.gson.stream.*;

public class AIView extends AnimatablePanel {

    private Point origin;
    private double xRotate, yRotate;
    private AnimatablePanel.Animator yAnimator, xAnimator;
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
            
            yAnimator = createAnimator(0.0, 360.0, 10.0);
            xAnimator = createAnimator(0.0, 360.0, 10.0);
        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void parseJSON(Map<String, List<Map<String, Double>>> json) {
        List<Map<String, Double>> nodes = json.get("nodes");
        for (Map<String, Double> node : nodes) {
            Double x = node.get("x");
            Double y = node.get("y");
            Double z = node.get("z");
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
        origin = new Point(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0);
        //origin = new Point(0, 0, 0);

        drawVectors(g, vectors, origin, origin);
        //drawEdges(g, edges);
    }

    private void drawVectors(Graphics2D g, Map<Integer, Vector> vectors, Point origin, Point start) {
        for (Map.Entry<Integer, Vector> v : vectors.entrySet()) {
            Double z = -1.0 + 2.0 * (v.getValue().getX() / 2570);
            Double phi = 2.0 * Math.PI * (v.getValue().getY() /2000);
            Double theta = Math.asin(z);
            
            Double x = Math.cos(theta) * Math.cos(phi) * 50;
            Double y = Math.cos(theta) * Math.sin(phi) * 50;
            z = z*50;
            Vector vector = new Vector(x, z, y);
            //System.err.println(vector.getX() + " : " + vector.getY() + " : " + vector.getZ() + ":" + phi + ":" + theta);
            
            vector = vector.rotateYZ(xAnimator.value());
            vector = vector.rotateXZ(yAnimator.value());
            //System.err.println("R:" + x + " : " + y + " : " + z + ":" + phi + ":" + theta);
            
            Point point = origin.addVectorToPoint(vector);
            
            Double diameter = 5.0;
            Double radius = diameter/2;
            g.fillOval((int)(point.getX() - radius), (int)(point.getY() - radius), diameter.intValue(), diameter.intValue());

        }
        
    }

    private void drawEdges(Graphics2D g, List<Edge<Vector>> edges) {
        for (Edge<Vector> edge : edges) {
            Vector node1 = edge.getNode1();
            Vector node2 = edge.getNode2();
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void rotate() {
        xRotate = (xRotate + 0.05) % 360;
        yRotate = (yRotate + 0.001) % 360;
        //repaint();
    }

}
