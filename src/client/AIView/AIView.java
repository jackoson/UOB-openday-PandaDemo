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

    private List<Vector> vectors;
    private Point origin;
    private double xRotate, yRotate;
    private AnimatablePanel.Animator yAnimator, xAnimator;

    public AIView() {
        try {
            setPreferredSize(new Dimension(600, 400));
            xRotate = 45.0;
            yRotate = 45.0;
            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, Integer> json = gson.fromJson(reader, Map.class);
            //System.out.println("Nodes:" + json.get("nodes"));
            vectors = new ArrayList<Vector>();
            vectors.add(new Vector(100, 0, 0));
            vectors.add(new Vector(100, 100, 0));
            vectors.add(new Vector(100, 50, 0));
            vectors.add(new Vector(100, 150, 0));
            vectors.add(new Vector(0, 50, 0));
            vectors.add(new Vector(200, 50, 0));
            
            yAnimator = createAnimator(0.0, 360.0, 10.0);
            xAnimator = createAnimator(0.0, 360.0, 10.0);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
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

    private void drawVectors(Graphics2D g, List<Vector> vectors, Point origin, Point start) {
        for (Vector v : vectors) {
            Double z = -1.0 + 2.0 * ((double)v.getX() / 200);
            Double phi = 2.0 * Math.PI * ((double)v.getY() /200);
            Double theta = Math.asin(z);
            
            Double x = Math.cos(theta) * Math.cos(phi) * 50;
            Double y = Math.cos(theta) * Math.sin(phi) * 50;
            Vector vector = new Vector(x.intValue(), z.intValue()*50, y.intValue());
            System.err.println(x + " : " + y + " : " + z + ":" + phi + ":" + theta);
            
            vector = vector.rotateYZ(xAnimator.value());
            vector = vector.rotateXZ(yAnimator.value());
            System.err.println("R:" + x + " : " + y + " : " + z + ":" + phi + ":" + theta);
            
            Point point = origin.addVectorToPoint(vector);
            
            int diameter = 5;
            int radius = diameter/2;
            g.fillOval(point.getX() - radius, point.getY() - radius, diameter, diameter);

            //drawVectors(g, vector.getVectors(), origin, point);
        }
    }

    public void rotate() {
        xRotate = (xRotate + 0.05) % 360;
        yRotate = (yRotate + 0.001) % 360;
        //repaint();
    }

}
