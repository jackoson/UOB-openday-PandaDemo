package client.view;

import javax.swing.*;
import java.awt.*;

public class ChatEventView extends JPanel {
    public ChatEventView() {
        setOpaque(false);
    }
    
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        Dimension size = getSize();
        
        g.setColor(new Color(255, 255, 255, 250));
        g.fillRoundRect(20, 0, size.width-40, size.height+10, 10, 10);
    }
    
}