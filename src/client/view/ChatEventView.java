package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ChatEventView extends JPanel implements MouseListener {
    private GridBagConstraints constraints;
    private JScrollPane scrollPane;
    private JPanel scrollContainer;
    private JPanel spacer;
    private int messageCount;
    
    public ChatEventView() {
        setLayout(new BorderLayout());
        messageCount = 0;
        setOpaque(false);
        addMouseListener(this);
        setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 20));
        
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
    
    public void addMessage() {
        constraints.gridy = messageCount + 1;
        remove(spacer);
        scrollContainer.add(new MessageView("This is an example of a message.", "Green Player", scrollContainer.getSize().width - 24) , constraints);
        messageCount++;
        constraints.gridy = messageCount + 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        scrollContainer.add(spacer, constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        
        scrollPane.revalidate();
        Dimension containerSize = scrollContainer.getSize();
        scrollPane.getVerticalScrollBar().setValue(containerSize.height);
    }
    
    /**
     * Is called when a Move is clicked on. Sends an ActionEvent to the
     * added ActionListener.
     *
     * @param e the MouseEvent containing the JLabel of the Move clicked on.
     */
    public void mouseClicked(MouseEvent e) {
        addMessage();
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
    public void mouseEntered(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseExited(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseMoved(MouseEvent e) {}
    
    private class MessageView extends AnimatablePanel {
        
        public MessageView(String message, String player, int width) {
            setBackground(Color.GREEN);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            setOpaque(false);
            
            String text = String.format("<html><div WIDTH=%d>%s</div><html>", width, player + ": " + message);
            JLabel label = new JLabel(text);
            label.setFont(Formatter.defaultFontOfSize(14));
            label.setForeground(Formatter.greyColor());
            add(label, BorderLayout.WEST);
        }
        
        public MessageView(int move) {
            //setBackground(Color.GREEN);
            setOpaque(false);
            setPreferredSize(new Dimension(100, 30));
            
            JLabel label = new JLabel("Message");
            label.setFont(Formatter.defaultFontOfSize(12));
            label.setForeground(Color.RED);
            add(label);
        }
    }
    
    public class ScrollBarUI extends BasicScrollBarUI {
        
        @Override
        protected void paintTrack(Graphics g0, JComponent c, Rectangle trackBounds) {
            
            
        }
        
        @Override
        protected void paintThumb(Graphics g0, JComponent c, Rectangle thumbBounds) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0, 0, 0, 30));
            int width = 6;
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
    
}