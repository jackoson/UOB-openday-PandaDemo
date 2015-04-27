package client.view;

import scotlandyard.*;
import client.application.*;

import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * A view to display the game log.
 */

public class ChatEventView extends JPanel {
  
    private static final long serialVersionUID = 6051734428889451747L;
  
    private GridBagConstraints constraints;
    private JScrollPane scrollPane;
    private JPanel scrollContainer;
    private JPanel spacer;
    private int messageCount;
    private Map<Ticket, BufferedImage> images;
    
    /**
     * Constructs a new ChatEventView object.
     *
     * @param fileAccess the FileAccess object that contains the images.
     */
    public ChatEventView(FileAccess fileAccess) {
        setLayout(new BorderLayout());
        messageCount = 0;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 20));
        
        scrollContainer = new JPanel();
        scrollContainer.setLayout(new GridBagLayout());
        scrollContainer.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        
        JPanel topSpacer = new JPanel();
        topSpacer.setOpaque(false);
        scrollContainer.add(topSpacer, constraints);
        
        scrollPane = new JScrollPane(scrollContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane. setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ScrollBarUI());
        add(scrollPane, BorderLayout.CENTER);
        
        spacer = new JPanel();
        spacer.setOpaque(false);
        
        images = fileAccess.getTicketsSmall();
    }
    
    /**
     * Draws the background of the view.
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
        g.fillRoundRect(19, 2, size.width-38, size.height+10, 10, 10);
        
        g.setColor(new Color(255, 255, 255, 250));
        g.fillRoundRect(20, 0, size.width-40, size.height+10, 10, 10);
    }
    
    /**
     * Adds a message to the view.
     *
     * @param move the Move containing the information to be displayed in the message.
     */
    public void addMessage(Move move) {
        constraints.gridy = messageCount + 1;
        remove(spacer);
        if (move instanceof MoveDouble) scrollContainer.add(new MessageView(move.colour, "Mr X played a double move:", 180), constraints);
        else if (move instanceof MoveTicket) scrollContainer.add(new MessageView((MoveTicket) move) , constraints);
        else if (move instanceof MovePass) scrollContainer.add(new MessageView(move.colour, move.colour + " Detective played a MovePass", 180), constraints);
        messageCount++;
        constraints.gridy = messageCount + 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        scrollContainer.add(spacer, constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        
        scrollContainer.revalidate();
        Dimension containerSize = scrollContainer.getSize();
        
        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        vBar.setValue(vBar.getMaximum());
    }
    
    /**
     * Clears the game log.
     */
    public void clear() {
        scrollContainer.removeAll();
    }
    
    // A view to display a message.
    private class MessageView extends AnimatablePanel {
      
        private Colour player;
        
        /**
         * Constructs a new MessageView object.
         *
         * @param player the Colour of the player to whom this message relates.
         * @param message the message to be displayed.
         * @param width the width of the message in pixels.
         */
        public MessageView(Colour player, String message, int width) {
            this.player = player;
            setup();
            setBorder(BorderFactory.createEmptyBorder(-1, 16, 1, 4));
            
            JPanel holder = new JPanel();
            holder.setOpaque(false);
            add(holder, BorderLayout.WEST);
            
            String text = String.format("<html><div WIDTH=%d>%s</div><html>", width, message);
            JLabel label = new JLabel(text);
            label.setFont(Formatter.defaultFontOfSize(14));
            label.setForeground(Formatter.greyColor());
            holder.add(label);
        }
        
        /**
         * Constructs a new MessageView object.
         *
         * @param move the MoveTicket to be displayed in the message.
         */
        public MessageView(MoveTicket move) {
            
            Integer target = move.target;
            Ticket ticket = move.ticket;
            this.player = move.colour;
            setup();
            setBorder(BorderFactory.createEmptyBorder(0, 16, 1, 4));
            
            JPanel holder = new JPanel();
            holder.setOpaque(false);
            add(holder, BorderLayout.WEST);
            
            JLabel colourLabel = new JLabel(player + " ");
            colourLabel.setFont(Formatter.defaultFontOfSize(14));
            colourLabel.setForeground(Formatter.greyColor());
            holder.add(colourLabel);
            
            TicketIndicator ticketView = new TicketIndicator(images.get(ticket));
            holder.add(ticketView);
            
            if (!player.equals(Colour.Black)) {
                JLabel locationLabel = new JLabel(" " + target);
                locationLabel.setFont(Formatter.defaultFontOfSize(14));
                locationLabel.setForeground(Formatter.greyColor());
                holder.add(locationLabel);
            }
        }
        
        // Sets up the view.
        private void setup() {
            setOpaque(false);
            setLayout(new BorderLayout());
        }
        
        /**
         * Draws the circle to the left of the message.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            
            g.setColor(Formatter.colorForPlayer(player));
            g.fillOval(2, 10, 8, 8);
        }
    }
    
    // A class to style the scroll bars.
    public class ScrollBarUI extends BasicScrollBarUI {
        
        @Override
        protected void paintTrack(Graphics g0, JComponent c, Rectangle trackBounds) {}
        
        @Override
        protected void paintThumb(Graphics g0, JComponent c, Rectangle thumbBounds) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0, 0, 0, 30));
            int width = 8;
            
            g.fillRoundRect(thumbBounds.x + thumbBounds.width/2 - width, thumbBounds.y + width, width, thumbBounds.height - width*2, width, width);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return invisibleButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return invisibleButton();
        }
        
        private JButton invisibleButton() {
            JButton jbutton = new JButton();
            jbutton.setPreferredSize(new Dimension(0, 0));
            jbutton.setMinimumSize(new Dimension(0, 0));
            jbutton.setMaximumSize(new Dimension(0, 0));
            return jbutton;
        }
    }
    
    // A view to display a ticket image
    private class TicketIndicator extends AnimatablePanel {
        
        private static final long serialVersionUID = 6013008457601185508L;
        
        private BufferedImage image;
        
        /**
         * Constructs a new TicketIndicator object.
         *
         * @param image the image to be displayed.
         */
        public TicketIndicator(BufferedImage image) {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 14));
            this.image = image;
        }
        
        /**
         * Draws the image.
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
    
}