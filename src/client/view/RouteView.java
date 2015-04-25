package client.view;

import scotlandyard.*;
import client.application.*;

import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A view to display a sequence of Moves.
 */

public class RouteView extends ListCellView {
    
    private MainIndicator indicator;
    private List<MoveIndicator> moveIndicators;
    private List<ArrowHolder> arrows;
    private DotsHolder dots = null;
    private Move firstMove = null;
    private List<Integer> locations;
    
    /**
     * Constructs a new RouteView.
     *
     * @param moves the List of Moves to the displayed.
     * @param fileAccess the FileAccess object containing all images.
     */
    public RouteView(List<Move> moves, FileAccess fileAccess) {
        firstMove = moves.get(0);
        locations = new ArrayList<Integer>();
        Map<Ticket, BufferedImage> images = fileAccess.getTicketsSmall();
        moveIndicators = new ArrayList<MoveIndicator>();
        arrows = new ArrayList<ArrowHolder>();
        
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(9, 7, 9, 7));
        //Constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weighty = 1;
        constraints.weightx = 1;
        constraints.gridy = 0;
        constraints.gridx = 0;
        
        int count = 0;
        ArrowHolder arrow = null;
        for (Move move : moves) {
            count++;
            if (count > 3) break;
            //Extract move data
            if (!(move instanceof MoveTicket)) continue; //Need to support double and pass moves
            MoveTicket mTicket = (MoveTicket)move;
            //Add MainIndicator
            if (count == 1) {
                indicator = new MainIndicator(new TicketIndicator(images.get(mTicket.ticket)));
                add(indicator, constraints);
            }
            //Add MoveIndicator
            constraints.gridx = (count * 2) - 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            BufferedImage image = images.get(mTicket.ticket);
            MoveIndicator m = new MoveIndicator(image, mTicket.target);
            moveIndicators.add(m);
            add(m, constraints);
            //
            locations.add(mTicket.target);
            //Add arrow separator
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = count * 2;
            arrow = new ArrowHolder();
            arrows.add(arrow);
            add(arrow, constraints);
        }
        if (arrow != null) {remove(arrow); arrows.remove(arrow);}
        if (count > 3) {dots = new DotsHolder(); add(dots, constraints);}
    }
    
    /**
     * Returns the first Move as a ListCell object.
     *
     * @return the first Move as a ListCell object.
     */
    @Override
    public Object object() {
        return firstMove;
    }
    
    /**
     * Returns the first Move as a ListCell object.
     *
     * @return the first Move as a ListCell object.
     */
    @Override
    public Collection collection() {
        return locations;
    }
    
    /**
     * Updates the UI when the expand animation begins.
     */
    @Override
    public void animationBegun() {
        if (!highlighted()) {
            for (MoveIndicator m : moveIndicators) {
                m.setHighlighted(false);
            }
        } else {
            indicator.setHighlighted(true);
        }
    }
    
    /**
     * Updates the UI when the expand animation finishes.
     */
    @Override
    public void animationCompleted() {
        if (highlighted()) {
            for (MoveIndicator m : moveIndicators) {
                m.setHighlighted(true);
            }
        } else {
            indicator.setHighlighted(false);
        }
    }
    
    /**
     * Updates the UI when the cell is seleced or deselected.
     */
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        for (MoveIndicator m : moveIndicators) {
            m.setSelected(selected);
        }
        for (ArrowHolder a : arrows) {
            a.setSelected(selected);
        }
        if (dots != null) dots.setSelected(selected);
        indicator.setSelected(selected);
    }
    
    /**
     * Draws the background and shadow.
     *
     * @param g0 the Graphics object to draw to.
     */
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        Dimension size = getSize();
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRoundRect(0, 6, size.width, size.height - 6, 10, 10);
        
        if (selected()) g.setColor(Formatter.primaryColor());
        else g.setColor(new Color(255, 255, 255, 250));
        g.fillRoundRect(1, 3, size.width-2, size.height - 6, 10, 10);
    }
    
    // A view to display the leftmost indicator, holding an image and a divider
    private class MainIndicator extends JPanel {
      
        private JPanel divider;
        private boolean selected = false;
        private boolean highlighted = false;
        
        /**
         * Constructs a MainIndicator object.
         *
         * @param ticket the TicketIndicator to display.
         */
        public MainIndicator(TicketIndicator ticket) {
            setOpaque(false);
            setLayout(new GridBagLayout());
            //Ticket
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.NORTHWEST;
            add(ticket, constraints);
            //Spacer
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            spacer.setPreferredSize(new Dimension(6, 6));
            add(spacer, constraints);
            //Divider
            divider = new JPanel();
            divider.setPreferredSize(new Dimension(1,1));
            divider.setBackground(new Color(255, 255, 255, 0));
            add(divider, constraints);
        }
        
        /**
         * Makes the divider visible (when the cell is expanded).
         *
         * @param visible the boolean that decides whether the divider is visible.
         */
        public void setDividerVisible(boolean visible) {
            if (visible) divider.setBackground(Formatter.greyColor());
            else divider.setBackground(new Color(255, 255, 255, 0));
        }
        
        /**
         * Sets the view as selected.
         *
         * @param selected the boolean that decides whether the view is selected.
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
            setHighlighted(highlighted);
        }
        
        /**
         * Sets the view as highlighted.
         *
         * @param highlighted the boolean that decides whether the view is highlighted.
         */
        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
            
            if (highlighted) {
                if (selected) divider.setBackground(Color.WHITE);
                else divider.setBackground(Formatter.greyColor());
            } else divider.setBackground(new Color(255, 255, 255, 0));
        }
        
    }
    
    // A view to hold a TicketIndicator and LocationIndicator, representing a Move.
    private class MoveIndicator extends JPanel {
      
        private GridBagConstraints constraints;
        private TicketIndicator ticket;
        private JPanel spacer;
        private LocationIndicator location;
        
        /**
         * Constructs a new MoveIndicator object.
         *
         * @param image the image of the Ticket to be displayed.
         * @param loc the integer to be displayed alongside the Ticket.
         */
        public MoveIndicator(BufferedImage image, Integer loc) {
            setOpaque(false);
            setMinimumSize(new Dimension(28, 44));
            setLayout(new GridBagLayout());
            //Constraints
            constraints = new GridBagConstraints();
            constraints.weighty = 1;
            constraints.anchor = GridBagConstraints.SOUTH;
            //Set up panels
            ticket = new TicketIndicator(image);
            spacer = new JPanel();
            spacer.setOpaque(false);
            spacer.setPreferredSize(new Dimension(20, 10));
            location = new LocationIndicator(loc);
            add(location, constraints);
        }
        
        /**
         * Re-adds panels depending on the highlight state.
         *
         * @param highlighted the boolean that decides whether to re-add panels.
         */
        public void setHighlighted(boolean highlighted) {
            removeAll();
            if (highlighted) {
                constraints.gridy = 0;
                add(ticket, constraints);
                constraints.gridy = 1;
                add(spacer, constraints);
            }
            constraints.gridy = 2;
            add(location, constraints);
        }
        
        /**
         * Sets the view as selected.
         *
         * @param selected the boolean that decides whether the view is selected.
         */ 
        public void setSelected(boolean selected) {
            location.setSelected(selected);
        }
        
    }
    
    // A view to display a ticket image.
    private class TicketIndicator extends AnimatablePanel {
      
        private BufferedImage image;
        
        /**
         * Constructs a new TicketIndicator object.
         *
         * @param image the image of the Ticket to be displayed.
         */
        public TicketIndicator(BufferedImage image) {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 14));
            this.image = image;
        }
        
        /**
         * Draws the Ticket image.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            Dimension size = getSize();
            if (image != null) g.drawImage(image, null, 0, 0);
        }
        
    }
    
    // A view to display number that represents a location.
    private class LocationIndicator extends JPanel {
      
        private JLabel label;
        
        /**
         * Constructs a new LocationIndicator object.
         *
         * @param number the number to be displayed.
         */
        public LocationIndicator(int number) {
            setPreferredSize(new Dimension(28, 14));
            setOpaque(false);
            setLayout(new BorderLayout());
            
            label = new JLabel(Integer.toString(number), SwingConstants.CENTER);
            label.setOpaque(false);
            label.setForeground(Formatter.greyColor());
            label.setFont(Formatter.defaultFontOfSize(12));
            add(label, BorderLayout.NORTH);
        }
        
        /**
         * Sets the view as selected.
         *
         * @param selected the boolean that decides whether the view is selected.
         */
        public void setSelected(boolean selected) {
            if (selected) label.setForeground(Color.WHITE);
            else label.setForeground(new Color(100, 100, 100));
        }
        
    }
    
    // A view that contains an indicator that sits between Moves.
    private class ArrowHolder extends JPanel {
      
        private Polygon triangle;
        private boolean selected = false;
        
        /**
         * Constructs a new ArrowHolder object.
         */
        public ArrowHolder() {
            setOpaque(false);
            setMinimumSize(new Dimension(8, 10));
            
            int xPoly[] = {1,1,7};
            int yPoly[] = {1,9,5};
            
            triangle = new Polygon(xPoly, yPoly, xPoly.length);
        }
        
        /**
         * Sets the view as selected.
         *
         * @param selected the boolean that decides whether the view is selected.
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        /**
         * Draws the arrow in the view.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (selected) g.setColor(Color.WHITE);
            else g.setColor(Formatter.greyColor());
            g.fillPolygon(triangle);
            
        }
        
    }
    
    // A view that contains an indicator if there are extra items that cannot be displayed.
    private class DotsHolder extends JPanel {
      
        private boolean selected = false;
        
        /**
         * Constructs a new DotsHolder object.
         */
        public DotsHolder() {
            setOpaque(false);
            setMinimumSize(new Dimension(10, 10));
        }
        
        /**
         * Sets the view as selected.
         *
         * @param selected the boolean that decides whether the view is selected.
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        /**
         * Draws the dots to the view.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (selected) g.setColor(Color.WHITE);
            else g.setColor(Formatter.greyColor());
            g.fillOval(0, 4, 2, 2);
            g.fillOval(4, 4, 2, 2);
            g.fillOval(8, 4, 2, 2);
            
        }
        
    }
    
}