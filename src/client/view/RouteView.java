import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class RouteView extends ListCellView {
    MainIndicator indicator;
    java.util.List<MoveIndicator> moves;
    
    public RouteView() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(9, 7, 9, 7));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1;
        gbc.weightx = 1;
        
        indicator = new MainIndicator(new TicketIndicator());
        add(indicator, gbc);
        
        moves = new ArrayList<MoveIndicator>();
        for (int i = 0; i < 3; i++) {
            MoveIndicator m = new MoveIndicator();
            moves.add(m);
            add(m, gbc);
        }
    }
    
    @Override
    public void animationBegun() {
        if (!highlighted()) {
            for (MoveIndicator m : moves) {
                m.setHighlighted(false);
            }
        } else {
            indicator.setHighlighted(true);
        }
    }
    
    @Override
    public void animationCompleted() {
        if (highlighted()) {
            for (MoveIndicator m : moves) {
                m.setHighlighted(true);
            }
        } else {
            indicator.setHighlighted(false);
        }
    }
    
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        for (MoveIndicator m : moves) {
            m.setSelected(selected);
        }
        indicator.setSelected(selected);
    }
    

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
    
    private class MainIndicator extends JPanel {
        private JPanel divider;
        boolean selected = false;
        boolean highlighted = false;
        
        public MainIndicator(TicketIndicator ticket) {
            setOpaque(false);
            setLayout(new GridBagLayout());
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(ticket, gbc);
            
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.VERTICAL;

            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            spacer.setPreferredSize(new Dimension(6, 6));
            add(spacer, gbc);
            
            divider = new JPanel();
            divider.setPreferredSize(new Dimension(1,1));
            divider.setBackground(new Color(255, 255, 255, 0));
            add(divider, gbc);
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
    
    private class MoveIndicator extends JPanel {
        GridBagConstraints gbc;
        TicketIndicator ticket;
        JPanel spacer;
        LocationIndicator location;
        
        public MoveIndicator() {
            setOpaque(false);
            setMinimumSize(new Dimension(28, 44));
            gbc = new GridBagConstraints();
            gbc.weighty = 1;
            gbc.anchor = GridBagConstraints.SOUTH;
            setLayout(new GridBagLayout());
            ticket = new TicketIndicator();
            spacer = new JPanel();
            spacer.setOpaque(false);
            spacer.setPreferredSize(new Dimension(20, 10));
            location = new LocationIndicator(128);
            add(location, gbc);
        }
        
        public void setHighlighted(boolean highlighted) {
            removeAll();
            if (highlighted) {
                gbc.gridy = 0;
                add(ticket, gbc);
                ticket.fadeIn();
                gbc.gridy = 1;
                add(spacer, gbc);
            }
            gbc.gridy = 2;
            add(location, gbc);
        }
        
        public void setSelected(boolean selected) {
            location.setSelected(selected);
        }
    }
    
    private class TicketIndicator extends AnimatablePanel {
        AnimatablePanel.Animator opacityAnimator;
        
        public TicketIndicator() {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 14));
        }
        
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            Dimension size = getSize();
            int alpha = 255;
            if (opacityAnimator != null) alpha = (int)(opacityAnimator.value()*255);
            g.setColor(new Color(251, 68, 60, alpha));
            g.fillRoundRect(0, 0, size.width, size.height, 6, 6);
        }
        
        public void fadeIn() {
            opacityAnimator = createAnimator(0.0, 0.2, 1.0);
        }
        
        @Override
        public void animationCompleted() {
            opacityAnimator = null;
        }
    }
    
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
}