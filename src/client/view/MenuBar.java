package client.view;

import javax.swing.*;
import java.awt.*;

public class MenuBar extends JPanel {
    
    private ChatEventView eventView;
    private JPanel bar;
    
    public MenuBar(ChatView chat, ChatEventView eventView, PlayerTicketView ticket, TimerView timer) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 200));
        setOpaque(false);
        
        this.eventView = eventView;
        eventView.setPreferredSize(new Dimension(360, 100));
        add(eventView, BorderLayout.WEST);
        eventView.setVisible(false);
        
        bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(1200, 40));
        
        bar.add(chat, BorderLayout.WEST);
        bar.add(ticket, BorderLayout.CENTER);
        bar.add(timer, BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
    }
    
    public void setBackgroundColor(Color color) {
        bar.setBackground(color);
    }
    
    public void showChat() {
        eventView.setVisible(true);
    }
    
    public void hideChat() {
        eventView.setVisible(false);
    }
}