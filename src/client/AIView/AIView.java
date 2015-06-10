import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.util.*;

public class AIView extends JPanel {

    private List<Vector> vectors;
    private Point origin;
    private double xRotate, yRotate;
    private String graph;

    public AIView() {
        setPreferredSize(new Dimension(600, 400));
        xRotate = 0.0;
        yRotate = 0.0;
        vectors = new ArrayList<Vector>();
        addVectors(vectors);
    }

    private void addVectors(List<Vector> vectors) {
        vectors.add(new Vector(0, 0, 0));
        vectors.add(new Vector(0, 100, -65));
        vectors.add(new Vector(10, 50, -90));
        vectors.add(new Vector(30, 30, 90));
        vectors.add(new Vector(50, 40, 50));
        vectors.add(new Vector(65, 8, 50));
        vectors.add(new Vector(100, -50, -100));
        vectors.add(new Vector(50, -65, 65));
        vectors.add(new Vector(-5, -100, 100));
        vectors.add(new Vector(-50, -100, -90));
        vectors.add(new Vector(-50, -50, 95));
        vectors.add(new Vector(-100, 0, -100));
        vectors.add(new Vector(-35, 35, 0));
        vectors.add(new Vector(-20, 70, -100));
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
        for (Vector vector : vectors) {
            Vector rotatedVector = vector.rotateXZ(-xRotate);
            rotatedVector.rotateYZ(-yRotate);
            Point point = origin.addVectorToPoint(rotatedVector);

            g.drawLine(start.getX(), start.getY(), point.getX(), point.getY());
            int diameter = (int) ((double) (point.getZ() + 100) / 4.0) + 5;
            if (diameter < 5) diameter = 5;
            if (vector.getZ() == 0) diameter = 15;
            int radius = (int) ((double) diameter / 2.0);
            g.fillOval(point.getX() - radius, point.getY() - radius, diameter, diameter);

            drawVectors(g, vector.getVectors(), origin, point);
        }
    }

    public void rotate() {
        xRotate = (xRotate + 0.05) % 360;
        yRotate = (yRotate + 0.001) % 360;
        repaint();
    }

}
