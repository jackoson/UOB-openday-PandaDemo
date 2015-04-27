package client.view;

import javax.swing.*;
import javax.swing.border.*;
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
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        
        this.selectedView = null;
        this.views = new ArrayList<RoundView>();
        
        for (int i = 1; i < rounds.size(); i++) {
            RoundView roundView  = new RoundView(rounds.get(i));
            views.add(roundView);
            add(roundView);
        }
    }
    
    /**
     * Sets the RoundView to be selected.
     *
     * @param round the index of the RoundView to be selected.
     */
    public void setSelectedRound(int round) {
        if (selectedView != null) selectedView.setSelected(false);
        RoundView v = views.get(round - 1);
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
            setOpaque(false);
            this.selected = false;
            this.filled = filled;
            setPreferredSize(new Dimension(16, 40));
        }
        
        /**
         * Sets the circle to be selected.
         *
         * @param selected the boolean that decides whether the circle is selected.
         */
        public void setSelected(boolean selected) {
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
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.0f));
            
            if (filled) g.fillOval(3, 15, 10, 10);
            else g.drawOval(4, 16, 8, 8);
            
            if (selected) g.drawLine(5, 30, 11, 30);
        }
        
    }
    
}
