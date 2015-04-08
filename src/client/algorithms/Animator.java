package client.algorithms;

import java.awt.*;

/**
 * A class to calculate the information required to perform a linear animation.
 */

public class Animator {
    
    private double t;
    private double tInc;
    private Point current, start, end;
    private int destination;
    
    /**
     * Constructs a new Animator object.
     *
     * @param start the start position of the animation.
     * @param end the end position of the animation.
     * @param destination the destination location (for animating counters).
     * @param tInc the amount to increment t by on each update.
     */     
    public Animator(Dimension start, Dimension end, int destination, double tInc) {
        this.start = new Point(start.width, start.height);
        this.end = new Point(end.width, end.height);
        this.destination = destination;
        this.tInc = tInc;
        current = new Point(this.start.x, this.start.y);
        t = 0.0;
    }
    
    /**
     * Updates the current position and t.
     */
    public void update() {
        t += tInc;
        current = cubicBezier(start, start, end, end, t); 
    }
    
    // Returns a Point containing the coordinates of the line 
    // for a certain Bezier curve.
    // @param p0 the P0 Point defining the curve.
    // @param p1 the P1 Point defining the curve.
    // @param p2 the P2 Point defining the curve.
    // @param p3 the P3 Point defining the curve.
    // @param t the fraction of where the Point is in relation
    // to the start and end points.
    // @return a Point containing the coordinates of the line
    // for a certain Bezier curve.
    private Point cubicBezier(Point p0, Point p1, Point p2, Point p3, double t) {
        double ti = (1 - t);
        double x = (ti * ti * ti * p0.getX()) + (3 * ti * ti * t * p1.getX())
                    + (3 * ti * t * t * p2.getX()) + (t * t * t * p3.getX());
        double y = (ti * ti * ti * p0.getY()) + (3 * ti * ti * t * p1.getY())
                    + (3 * ti * t * t * p2.getY()) + (t * t * t * p3.getY());
        return new Point((int) x, (int) y);
    }
    
    /**
     * Returns true if the animation has finished.
     *
     * @return true if the animation has finished.
     */
    public boolean isOver() {
        if (t >= 1.0) return true;
        else return false;
    }
    
    /**
     * Returns the destination (for animating counters).
     *
     * @return the destination (for animating counters).
     */
    public int getDestination() {
        return destination;
    }
    
    /**
     * Returns the current x coordinate.
     *
     * @return the current x coordinate.
     */
    public int getX() {
        return current.x;
    }
    
    /**
     * Returns the current y coordinate.
     *
     * @return the current y coordinate.
     */
    public int getY() {
        return current.y;
    }
    
}