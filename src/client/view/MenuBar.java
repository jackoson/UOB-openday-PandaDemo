package client.view;

import javax.swing.*;
import java.awt.*;

/**
 * A view to hold the views that make up the menu bar.
 */

public class MenuBar extends JPanel {
  
    private static final long serialVersionUID = -8366257729974330405L;
    
    private ChatEventView eventView;
    private JPanel bar;
    
    /**
     * Constructs a new MenuBar object.
     *
     * @param chat the ChatView to be displayed in the MenuBar.
     * @param eventView the ChatEventView to be displayed above the MenuBar.
     * @param ticket the PlayerTicketView to be displayed in the MenuBar.
     * @param timer the TimerView to be displayed in the MenuBar.
     */
    public MenuBar(ChatView chat, ChatEventView eventView, PlayerTicketView ticket, TimerView timer) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 200));
        setOpaque(false);
        
        this.eventView = eventView;
        eventView.setPreferredSize(new Dimension(280, 100));
        add(eventView, BorderLayout.WEST);
        eventView.setVisible(false);
        
        bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(1200, 40));
        
        bar.add(chat, BorderLayout.WEST);
        bar.add(ticket, BorderLayout.CENTER);
        bar.add(timer, BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
    }
    
    /**
     * Sets the background Color of the MenuBar.
     *
     * @param color the new Color of the MenuBar.
     */
    public void setBackgroundColor(Color color) {
        bar.setBackground(color);
    }
    
    /**
     * Shows the game log (ChatEventView);
     */
    public void showChat() {
        eventView.setVisible(true);
    }
    
    /**
     * Hides the game log (ChatEventView);
     */
    public void hideChat() {
        eventView.setVisible(false);
    }
    
}