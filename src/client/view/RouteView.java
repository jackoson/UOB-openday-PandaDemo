package client.view;

import scotlandyard.*;
import client.application.*;

import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class RouteView extends ListCellView {
    MainIndicator indicator;
    java.util.List<MoveIndicator> moveIndicators;
    java.util.List<ArrowHolder> arrows;
    DotsHolder dots = null;
    Move firstMove = null;
    
    public RouteView(java.util.List<Move> moves, FileAccess fileAccess) {
        firstMove = moves.get(0);
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
    //Return the first move as the ListCell object
    @Override
    public Object object() {
        return firstMove;
    }
    //Update the UI as the expand animation begins
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
    //Update the UI as the expand animation finishes
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
    //Update the Ui as the cell is seleced or deselected
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
    //Draw background and shadow
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        Dimension size = getSize();
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRoundRect(0, 6, size.width, size.height - 6, 10, 10);
        
        if (selected()) g.setColor(new Color(20, 155, 247, 250));
        else g.setColor(new Color(255, 255, 255, 250));
        g.fillRoundRect(1, 3, size.width-2, size.height - 6, 10, 10);
    }
    //The leftmost indicator, holding an image and a divider
    private class MainIndicator extends JPanel {
        private JPanel divider;
        boolean selected = false;
        boolean highlighted = false;
        
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
        
        public void setDividerVisible(boolean visible) {
            if (visible) divider.setBackground(new Color(160, 160, 160));
            else divider.setBackground(new Color(255, 255, 255, 0));
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
            setHighlighted(highlighted);
        }
        
        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
            
            if (highlighted) {
                if (selected) divider.setBackground(new Color(255, 255, 255));
                else divider.setBackground(new Color(180, 180, 180));
            } else divider.setBackground(new Color(255, 255, 255, 0));
        }
    }
    //Panel to hold a TicketIndicator and LocationIndicator, representing a move
    private class MoveIndicator extends JPanel {
        GridBagConstraints constraints;
        TicketIndicator ticket;
        JPanel spacer;
        LocationIndicator location;
        
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
        //Function for re-add panels depending on the highlight state
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
        
        public void setSelected(boolean selected) {
            location.setSelected(selected);
        }
    }
    //Panel to display a ticket image
    private class TicketIndicator extends AnimatablePanel {
        BufferedImage image;
        
        public TicketIndicator(BufferedImage image) {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 14));
            this.image = image;
        }
        //Draw ticket image
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            Dimension size = getSize();
            if (image != null) g.drawImage(image, null, 0, 0);
        }
    }
    //Panel to display number that represents a location
    private class LocationIndicator extends JPanel {
        JLabel label;
        public LocationIndicator(int number) {
            setPreferredSize(new Dimension(28, 14));
            setOpaque(false);
            setLayout(new BorderLayout());
            
            label = new JLabel(Integer.toString(number), SwingConstants.CENTER);
            label.setOpaque(false);
            label.setForeground(new Color(100, 100, 100));
            label.setFont(new Font("Helvetica Neue", 0, 12));
            add(label, BorderLayout.NORTH);
        }
        
        public void setSelected(boolean selected) {
            if (selected) label.setForeground(new Color(255, 255, 255));
            else label.setForeground(new Color(100, 100, 100));
        }
    }
    //Holder for an indicator that sits between moves
    private class ArrowHolder extends JPanel {
        Polygon triangle;
        boolean selected = false;
        
        public ArrowHolder() {
            setOpaque(false);
            setMinimumSize(new Dimension(8, 10));
            
            int xPoly[] = {1,1,7};
            int yPoly[] = {1,9,5};
            
            triangle = new Polygon(xPoly, yPoly, xPoly.length);
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (selected) g.setColor(new Color(255, 255, 255));
            else g.setColor(new Color(180, 180, 180));
            g.fillPolygon(triangle);
            
        }
    }
    //Holder for an indicator if there are extra items that cannot be displayed
    private class DotsHolder extends JPanel {
        boolean selected = false;
        
        public DotsHolder() {
            setOpaque(false);
            setMinimumSize(new Dimension(10, 10));
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (selected) g.setColor(new Color(255, 255, 255));
            else g.setColor(new Color(180, 180, 180));
            g.fillOval(0, 4, 2, 2);
            g.fillOval(4, 4, 2, 2);
            g.fillOval(8, 4, 2, 2);
            
        }
    }
}