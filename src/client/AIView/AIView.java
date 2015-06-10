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

    public AIView() {
        try {
            setPreferredSize(new Dimension(600, 400));
            xRotate = 0.0;
            yRotate = 0.0;
            vectors = new HashMap<Integer, Vector>();
            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);
            createVectors(json);
        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createVectors(Map<String, List<Map<String, Double>>> json) {
        List<Map<String, Double>> nodes = json.get("nodes");
        for (Map<String, Double> node : nodes) {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            int z = node.get("z").intValue();
            vectors.put(node.get("node").intValue(), new Vector(x, y, z));
        }
    }

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();
        origin = new Point((int) (size.getWidth() / 2.0), (int) (size.getHeight() / 2.0), 0);

        drawVectors(g, vectors, origin, origin);
    }

    private void drawVectors(Graphics2D g, Map<Integer, Vector> vectors, Point origin, Point start) {
        for (Map.Entry<Integer, Vector> vector : vectors.entrySet()) {
            Vector rotatedVector = vector.getValue().rotateXZ(-xRotate);
            rotatedVector.rotateYZ(-yRotate);
            Point point = origin.addVectorToPoint(rotatedVector);

            g.drawLine(start.getX(), start.getY(), point.getX(), point.getY());
            int diameter = (int) ((double) (point.getZ() + 100) / 4.0) + 5;
            if (diameter < 5) diameter = 5;
            if (vector.getValue().getZ() == 0) diameter = 15;
            int radius = (int) ((double) diameter / 2.0);
            g.fillOval(point.getX() - radius, point.getY() - radius, diameter, diameter);
        }
    }

    public void rotate() {
        xRotate = (xRotate + 0.05) % 360;
        yRotate = (yRotate + 0.001) % 360;
        repaint();
    }

}
