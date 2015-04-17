package client.view;

import scotlandyard.*;
import client.application.*;
import client.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 * A view to display the players tickets.
 */

public class PlayerTicketView extends JLabel {
    
    private static final long serialVersionUID = 7573800434542888781L;
    
    private FileAccess fileAccess;
    private Map<Ticket, TicketView> ticketViews;
    
    /**
     * Constructs a new PlayerTicketView that displays the player's Tickets.
     * A MouseListener listens for clicks on the Tickets.
     *
     * @param fileAccess the FileAccess object.
     */
    public PlayerTicketView(FileAccess fileAccess) {
        this.fileAccess = fileAccess;
        setPreferredSize(new Dimension(400, 40));
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        this.ticketViews = new HashMap<Ticket, TicketView>();
        
        addTicketViews();
    }
    
    /**
     * Forwards the specified ActionListener to each of the
     * ticket views.
     *
     * @param listener the listener to be added to the views.
     */
    public void setActionListener(ActionListener listener) {
        for (Map.Entry<Ticket, TicketView> entry : ticketViews.entrySet()) {
            TicketView view = entry.getValue();
            view.setActionListener(listener);
        }
    }
    
    /**
     * Updates a ticketview with a new value
     *
     * @param ticket the ticket that represents the ticketview
     * @param value the newm value to be updated
     */
    public void update(Map<Ticket, Integer> tickets) {
        for (Map.Entry<Ticket, Integer> entry : tickets.entrySet()) {
            TicketView view = ticketViews.get(entry.getKey());
            view.setValue(entry.getValue());
        }
    }
    
    // Add the TicketViews
    private void addTicketViews() {
        Map<Ticket, BufferedImage> ticketImages = fileAccess.getTicketsSmall();
        
        TicketView taxiTicket = new TicketView(ticketImages.get(Ticket.Taxi), Ticket.Taxi);
        taxiTicket.setValue(0);
        add(taxiTicket);
        ticketViews.put(Ticket.Taxi, taxiTicket);
        
        TicketView busTicket = new TicketView(ticketImages.get(Ticket.Bus), Ticket.Bus);
        busTicket.setValue(0);
        add(busTicket);
        ticketViews.put(Ticket.Bus, busTicket);
        
        TicketView trainTicket = new TicketView(ticketImages.get(Ticket.Underground), Ticket.Underground);
        trainTicket.setValue(0);
        add(trainTicket);
        ticketViews.put(Ticket.Underground, trainTicket);
        
        TicketView doubleTicket = new TicketView(ticketImages.get(Ticket.Secret), Ticket.Secret);
        doubleTicket.setValue(0);
        add(doubleTicket);
        ticketViews.put(Ticket.Secret, doubleTicket);
        
        TicketView secretTicket = new TicketView(ticketImages.get(Ticket.Double), Ticket.Double);
        secretTicket.setValue(0);
        add(secretTicket);
        ticketViews.put(Ticket.Double, secretTicket);
        
    }
    
    // A class for displaying a ticket and number badge;
    private class TicketView extends JLabel implements MouseListener {
        
        private static final long serialVersionUID = 5796740871755932476L;
        
        private TicketBadge badge;
        protected ActionListener aListener = null;
        private Ticket ticket;
        private boolean highlighted = false;
        
        /**
         * Constructs a new TicketView object.
         *
         * @param image the background image of the view.
         * @param ticket the Ticket that this view represents.
         * @param colour the Colour of the player to whom this
         * Ticket belongs.
         */
        public TicketView(BufferedImage image, Ticket ticket) {
            setPreferredSize(new Dimension(80, 40));
            setLayout(new GridLayout(1, 2));
            setOpaque(false);
            this.addMouseListener(this);
            this.ticket = ticket;
            
            JLabel icon = new JLabel(new ImageIcon(image.getScaledInstance(22, 16, Image.SCALE_SMOOTH)));
            add(icon);
            badge = new TicketBadge();
            add(badge);
        }
        
        /**
         * Draws the view in the JLabel.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setColor(new Color(255, 255, 255, 60));
            if (highlighted) g.fillRect(0, 0, 80, 40);
        }
        
        /**
         * Sets the badge value.
         *
         * @param value the value to be displayed
         */
        public void setValue(int value) {
            badge.setValue(value);
        }
        
        /**
         * Sets the specified ActionListener to recieve click events.
         *
         * @param listener the listener to be added.
         */
        public void setActionListener(ActionListener listener) {
            aListener = listener;
        }
        
        /**
         * Returns the Ticket associated with this view.
         *
         * @return the Ticket associated with this view.
         */
        public Ticket getTicket(){
            return ticket;
        }
        
        /**
         * Is called when a Move is clicked on. Sends an ActionEvent to the
         * added ActionListener.
         *
         * @param e the MouseEvent containing the JLabel of the Move clicked on.
         */
        public void mouseClicked(MouseEvent e) {
            if (aListener != null) {
                aListener.actionPerformed(new ActionEvent(ticket, 0, "ticket"));
            }
        }
        
        /**
         * Draws a transparent white background when the cursor
         * enters the view.
         * @param e the MouseEvent containing the cursor location.
         */
        public void mouseEntered(MouseEvent e) {
            highlighted = true;
            repaint();
        }
        
        /**
         * Draws a transparent background when the cursor
         * enters the view.
         * @param e the MouseEvent containing the cursor location.
         */
        public void mouseExited(MouseEvent e) {
            highlighted = false;
            repaint();
        }
        
        /**
         * Unused method from the MouseListener interface.
         * @param e the MouseEvent containing the cursor location.
         */
        public void mouseReleased(MouseEvent e) {}
        /**
         * Unused method from the MouseListener interface.
         * @param e the MouseEvent containing the cursor location.
         */
        public void mousePressed(MouseEvent e) {}
        /**
         * Unused method from the MouseListener interface.
         * @param e the MouseEvent containing the cursor location.
         */
        public void mouseDragged(MouseEvent e) {}
        /**
         * Unused method from the MouseListener interface.
         * @param e the MouseEvent containing the cursor location.
         */
        public void mouseMoved(MouseEvent e) {}
        
        // Class for displaying the number of tickets in
        // a badge.
        private class TicketBadge extends JLabel {
            
            private static final long serialVersionUID = 6607860351406820475L;
            
            private JLabel label;
            
            /**
             * Constructs a new TicketBadge object.
             */
            public TicketBadge() {
                setPreferredSize(new Dimension(40, 40));
                setLayout(new GridBagLayout());
                label = new JLabel("0", SwingConstants.CENTER);
                label.setForeground(Color.white);
                label.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
                add(label);
            }
            
            /**
             * Sets the number in the view.
             *
             * @param value the number for the view to display.
             */
            public void setValue(int value) {
                label.setText("" + value);
            }
            
        }
        
    }
    
}
