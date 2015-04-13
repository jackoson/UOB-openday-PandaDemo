package client.view;

import client.scotlandyard.*;
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

public class PlayerTicketView extends JPanel {
  
    private static final long serialVersionUID = -6751324761131131949L;
  
    private Map<Colour, PlayerTile> tiles;
    private FileAccess fileAccess;
    private ActionListener listener = null;
    
    /**
     * Constructs a new PlayerTicketView object.
     */
    public PlayerTicketView(FileAccess fileAccess) {
        this.tiles = new HashMap<Colour, PlayerTile>();
        this.fileAccess = fileAccess;
        setLayout(new BorderLayout());
        setOpaque(false);
    }
    
    /**
     * Updates the tickets for a specified player.
     *
     * @param colour the player to be updated.
     * @param ticket the Ticket whose number is to be updated.
     * @param ticketNo the new number of Tickets for the player.
     */
    public void update(Colour colour, Ticket ticket, Integer ticketNo) {
        PlayerTile tile = tiles.get(colour);
        tile.update(ticket, ticketNo);
    }
    
    /**
     * Initialises the displayed players with the players in the List.
     * 
     * @param player the player to be added to the view.
     * @param listener the listener to receive events from the view.
     */
    public void initialise(GamePlayer player, ActionListener listener) {
        PlayerTile tile = new PlayerTile(player);
        tiles.put(player.colour(), tile);
        add(tile, BorderLayout.WEST);
        setActionListener(listener);
        validate();
    }
    
    /**
     * Forwards the specified ActionListener to each of the
     * tiles.
     * @param listener the listener to be added to the views.
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
        for (Map.Entry<Colour, PlayerTile> entry : tiles.entrySet()) {
            PlayerTile tile = entry.getValue();
            tile.setActionListener(listener);
        }
    }
    
    // A class for displaying a players tickets and current
    // player indicator.
    private class PlayerTile extends JLabel {
       
        private static final long serialVersionUID = 7573800434542888781L;
        
        private GamePlayer player;
        private MouseListener mListener;
        private Map<Ticket, TicketView> ticketViews;
        private boolean current;
        
        /**
         * Constructs a new PlayerTile that displays the player's Tickets.
         * A MouseListener listens for clicks on the Tickets.
         *
         * @param listener the MouseListener to be attached to the view.
         * @param player the player whose information is to be displayed.
         */
        public PlayerTile(GamePlayer player) {
            setPreferredSize(new Dimension(400, 40));
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            this.player = player;
            this.ticketViews = new HashMap<Ticket, TicketView>();
            
            if (player.colour().equals(Colour.Black)) {
                drawMrX();
            } else {
                drawDetective(player.colour());
            }
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
        public void update(Ticket ticket, int value) {
            TicketView view = ticketViews.get(ticket);
            if (view != null) {
                view.setValue(value);
            }
        }
        
        // Draws a detective's Tickets.
        // @param colour the colour of the detective to be drawn.
        private void drawDetective(Colour colour) {
            Map<Ticket, BufferedImage> ticketImages = fileAccess.getTicketsSmall();
            int taxiNo = player.tickets().get(Ticket.Taxi);
            int busNo = player.tickets().get(Ticket.Bus);
            int trainNo = player.tickets().get(Ticket.Underground);
            
            TicketView taxiTicket = new TicketView(ticketImages.get(Ticket.Taxi), Ticket.Taxi, colour);
            taxiTicket.setValue(taxiNo);
            add(taxiTicket);
            ticketViews.put(Ticket.Taxi, taxiTicket);
            
            TicketView busTicket = new TicketView(ticketImages.get(Ticket.Bus), Ticket.Bus, colour);
            busTicket.setValue(busNo);
            add(busTicket);
            ticketViews.put(Ticket.Bus, busTicket);
            
            TicketView trainTicket = new TicketView(ticketImages.get(Ticket.Underground), Ticket.Underground, colour);
            trainTicket.setValue(trainNo);
            add(trainTicket);
            ticketViews.put(Ticket.Underground, trainTicket);
        }
        
        // Draws Mr X's Tickets.
        private void drawMrX() {
            Map<Ticket, BufferedImage> ticketImages = fileAccess.getTicketsSmall();
            int taxiNo = player.tickets().get(Ticket.Taxi);
            int busNo = player.tickets().get(Ticket.Bus);
            int trainNo = player.tickets().get(Ticket.Underground);
            int secretNo = player.tickets().get(Ticket.SecretMove);
            int doubleNo = player.tickets().get(Ticket.DoubleMove);
            
            TicketView taxiTicket = new TicketView(ticketImages.get(Ticket.Taxi), Ticket.Taxi, Colour.Black);
            taxiTicket.setValue(taxiNo);
            add(taxiTicket);
            ticketViews.put(Ticket.Taxi, taxiTicket);
            
            TicketView busTicket = new TicketView(ticketImages.get(Ticket.Bus), Ticket.Bus, Colour.Black);
            busTicket.setValue(busNo);
            add(busTicket);
            ticketViews.put(Ticket.Bus, busTicket);
            
            TicketView trainTicket = new TicketView(ticketImages.get(Ticket.Underground), Ticket.Underground, Colour.Black);
            trainTicket.setValue(trainNo);
            add(trainTicket);
            ticketViews.put(Ticket.Underground, trainTicket);
            
            TicketView doubleTicket = new TicketView(ticketImages.get(Ticket.SecretMove), Ticket.SecretMove, Colour.Black);
            doubleTicket.setValue(secretNo);
            add(doubleTicket);
            ticketViews.put(Ticket.SecretMove, doubleTicket);
            
            TicketView secretTicket = new TicketView(ticketImages.get(Ticket.DoubleMove), Ticket.DoubleMove, Colour.Black);
            secretTicket.setValue(doubleNo);
            add(secretTicket);
            ticketViews.put(Ticket.DoubleMove, secretTicket);

        }
        
        // A class for displaying a ticket and number badge;
        private class TicketView extends JLabel implements MouseListener {
          
            private static final long serialVersionUID = 5796740871755932476L;
            
            private TicketBadge badge;
            protected ActionListener aListener = null;
            private Ticket ticket;
            private Colour colour;
            private boolean highlighted = false;
            
            /**
             * Constructs a new TicketView object.
             *
             * @param image the background image of the view.
             * @param ticket the Ticket that this view represents.
             * @param colour the Colour of the player to whom this
             * Ticket belongs.
             */
            public TicketView(BufferedImage image, Ticket ticket, Colour colour) {
                setPreferredSize(new Dimension(80, 40));
                setLayout(new GridLayout(1, 2));
                setOpaque(false);
                this.addMouseListener(this);
                this.ticket = ticket;
                this.colour = colour;
                
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
                g.setColor(new Color(255, 255, 255, 120));
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
             * Returns the Colour of the player to whom this view
             * belongs.
             *
             * @return the Colour of the player to whom this view
             * belongs.
             */
            public Colour getColor(){
                return colour;
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
    
}