package client.view;

import javax.swing.*;
import java.awt.*;

public class MenuBar extends JPanel {
  
    private static final long serialVersionUID = -8366257729974330405L;
  
    private ChatEventView eventView;
    
    public MenuBar(ChatView chat, ChatEventView eventView, PlayerTicketView ticket, TimerView timer) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 200));
        setOpaque(false);
        
        this.eventView = eventView;
        eventView.setPreferredSize(new Dimension(360, 100));
        add(eventView, BorderLayout.WEST);
        eventView.setVisible(false);
        
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(1200, 40));
        bar.setBackground(new Color(20, 155, 247));
        
        bar.add(chat, BorderLayout.WEST);
        bar.add(ticket, BorderLayout.CENTER);
        bar.add(timer, BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
        
    }
    
    public void showChat() {
        eventView.setVisible(true);
    }
    
    public void hideChat() {
        eventView.setVisible(false);
    }
    
}