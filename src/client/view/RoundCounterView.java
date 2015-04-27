package client.view;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * A view to display when Mr X is visible.
 */

class RoundCounterView extends JPanel {
    
    private List<RoundView> views;
    private RoundView selectedView;
    
    /**
     * Constructs a new RoundCounterView object.
     *
     * @param rounds the List of Booleans deciding when Mr X is visible.
     */
    public RoundCounterView(List<Boolean> rounds) {
        this.selectedView = null;
        this.views = new ArrayList<RoundView>();
        
        for (Boolean r : rounds) {
            RoundView roundView  = new RoundView(r);
            views.add(roundView);
        }
    }
    
    /**
     * Sets the RoundView to be selected.
     *
     * @param round the index of the RoundView to be selected.
     */
    public void setSelectedRound(int round) {
        if (selectedView != null) selectedView.setSelected(false);
        RoundView v = views.get(round);
        v.setSelected(true);
        selectedView = v;
    }
    
    // A view to display the circles to represent when Mr X is visible.
    private class RoundView extends JPanel {
        
        private boolean selected;
        private boolean filled;
        
        /**
         * Constructs a new RoundView object.
         *
         * @param filled the boolean that decides whether the circle is filled.
         */
        public RoundView(boolean filled) {
            this.selected = false;
            this.filled = filled;
            setPreferredSize(new Dimension(10, 14));
        }
        
        /**
         * Sets the circle to be selected.
         *
         * @param selected the boolean that decides whether the circle is selected.
         */
        public setSelected(boolean selected) {
            this.selected = selected;
        }
        
        /**
         * Draws the circle.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            
            g.setStroke(new basicStroke(2.0));
            
            if (filled) g.fillOval(0, 4, 10, 10);
            else g.drawOval(1, 5, 8, 8);
            
            if (selected) g.drawLine(0, 1, 10, 1);
        }
        
    }
    
}
