package client.view;

import javax.swing.*;
import java.awt.*;

public class MenuBar extends JPanel {
    ChatEventView eventView;
    
    public MenuBar(ChatView chat, PlayerTicketView ticket, TimerView timer) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 200));
        setOpaque(false);
        
        eventView = new ChatEventView();
        eventView.setPreferredSize(new Dimension(300, 100));
        
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(1200, 40));
        bar.setBackground(new Color(20, 155, 247));
        
        bar.add(chat, BorderLayout.WEST);
        chat.setMenu(this);
        bar.add(ticket, BorderLayout.CENTER);
        bar.add(timer, BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
        
    }
    
    public void showChat() {
        add(eventView, BorderLayout.WEST);
    }
    
    public void hideChat() {
        remove(eventView);
    }
}