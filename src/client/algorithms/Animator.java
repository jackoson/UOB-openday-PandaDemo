package client.algorithms;

import client.scotlandyard.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * A class to calculate the information required to perform a linear animation.
 */

public class Animator implements ActionListener {
    
    private int count = 0;
    private double t = 0.0;
    private double increment;
    private Point start, end;
    private Map<Colour, Point> locations;
    private Colour counter;
    private JPanel panel;
    private boolean animatingCounter;
    
    private Timer timer = new Timer(20, this);
    
    /**
     * Constructs a new Animator object for animating counters.
     *
     * @param start the start position of the animation.
     * @param end the end position of the animation.
     */     
    public Animator(Point start, Point end, Map<Colour, Point> locations, Colour counter, JPanel panel) {
        this.start = start;
        this.end = end;
        this.locations = locations;
        this.counter = counter;
        this.panel = panel;
        increment = 1.0 / 50.0;
        animatingCounter = true;
    }
    
    /**
     * Constructs a new Animator object for animating the board.
     *
     * @param scaleFactor the current scaleFactor of the board.
     * @param increment the amount to increment the scale factor by.
     */
    public Animator(double scaleFactor, double increment) {
        this.t = scaleFactor;
        this.increment = increment;
        animatingCounter = false;
    }
    
    public void start() {
        timer.start();
    }
    
    private void updateCounter() {
        count++;
        t += increment;
        locations.remove(counter);
        locations.put(counter, linearBezier(start, end, t));
        panel.repaint();
    }
    
    private void updateBoard() {
        //TODO:
    }
    
    // Returns a Point containing the coordinates a certain 
    // way through the line.
    // @param p0 the P0 Point defining the curve.
    // @param p1 the P1 Point defining the curve.
    // @param t the fraction of where the Point is in relation
    // to the start and end points.
    // @return a Point containing the coordinates a certain
    // way through the line.
    private Point linearBezier(Point p0, Point p1, double t) {
        double ti = (1 - t);
        double x = (ti * p0.getX()) + (t * p1.getX());
        double y = (ti * p0.getY()) + (t * p1.getY());
        return new Point((int) x, (int) y);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (count == 50) {
            timer.stop();
        } else if(animatingCounter) {
            updateCounter();
        } else {
            updateBoard();
        }
    }
    
}