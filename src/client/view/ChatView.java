package client.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * A view for displaying a text field to the user and getting it's contents when they press enter.
 */

public class ChatView extends JLabel implements MouseListener {
  
    private static final long serialVersionUID = 3644963183305191230L;
    
    private boolean logVisible = false;
    private boolean highlighted = false;
    private ActionListener listener;
    
    /**
     * Constructs a new ChatView object.
     */
    public ChatView() {
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setPreferredSize(new Dimension(150, 40));
        setLayout(new GridBagLayout());
        setForeground(Color.WHITE);
        setFont(Formatter.boldFontOfSize(14));
        setText("Show game log");
        addMouseListener(this);
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
        if (highlighted) g.fillRect(0, 0, 280, 40);
    }
    
    /**
     * Adds the specified ActionListener to receive events from all sub views.
     * If listener listener is null, no action is performed.
     * 
     * @param listener the listener to be added to the view.
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Is called when the view is clicked on. Sends an ActionEvent to the
     * added ActionListener.
     *
     * @param e the MouseEvent containing the view of the Move clicked on.
     */
    public void mouseClicked(MouseEvent e) {
        if (listener != null) {
            if (!logVisible) {
                listener.actionPerformed(new ActionEvent(this, 0, "show_chat"));
                setText("Hide game log");
            } else {
                listener.actionPerformed(new ActionEvent(this, 0, "hide_chat"));
                setText("Show game log");
            }
            logVisible = !logVisible;
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
    
}