package client.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * A view for displaying a text field to the user and getting it's contents when they press enter.
 */

public class ChatView extends JPanel implements KeyListener, FocusListener {
  
    private static final long serialVersionUID = 3644963183305191230L;
    
    private String hint = "Chat";
    private boolean showHint = true;
    private JTextField text;
    private ActionListener listener;
    
    /**
     * Constructs a new ChatView object.
     */
    public ChatView() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 40));
        setLayout(new GridBagLayout());
        text = getStyledTextField();
        add(text);
    }
    
    // Returns a styled text field.
    // @return a styled text field.
    private JTextField getStyledTextField() {
        RoundTextField textField = new RoundTextField(hint);
        textField.setPreferredSize(new Dimension(320, 20));
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.GRAY);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        textField.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        textField.addKeyListener(this);
        textField.addFocusListener(this);
        return textField;
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
     * Called when a key is pressed whilst in a text field.
     * sends an ActionEvent to the specified listener when the enter key is pressed.
     *
     * @param e the KeyEvent that contains which key has been pressed.
     */
    public void keyPressed(KeyEvent e) {
        if (listener != null && e.getKeyCode() == KeyEvent.VK_ENTER && !showHint) {
            String message = text.getText();
            if (!message.isEmpty()) {
              listener.actionPerformed(new ActionEvent(message, 0, "message"));
            }
            text.setText("");
        }
    }
    
    /**
     * Called when the text field gets focus.
     * Removes the hint.
     *
     * @param e the FocusEvent containing information about 
     * this focus change.
     */
    public void focusGained(FocusEvent e) {
        if (text.getText().equals(hint)) {
            text.setText("");
            showHint = false;
        }
        if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "show_chat"));
    }

    /**
     * Called when the text field loses focus.
     * Sets the hint if no text has been entered.
     *
     * @param e the FocusEvent containing information about 
     * this focus change.
     */
    public void focusLost(FocusEvent e) {
        if (text.getText().isEmpty()) {
            text.setText(hint);
            showHint = true;
        }
        if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "hide_chat"));
    }
    
    /**
     * Unused method from the KeyListener interface.
     *
     * @param e the KeyEvent containing information about
     * which key has been released.
     */
    public void keyReleased(KeyEvent e) {}
    
    /**
     * Unused method from the KeyListener interface.
     *
     * @param e the KeyEvent containing information about
     * which key has been typed.
     */
    public void keyTyped(KeyEvent e) {}
    
    private class RoundTextField extends JTextField {
        
        public RoundTextField(String text) {
            super(text);
            setOpaque(false);
        }
        
        @Override
        public void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            
            Dimension size = getSize();
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRoundRect(0, 0, size.width, size.height, size.height, size.height);
            
            g.setColor(new Color(255, 255, 255, 250));
            g.fillRoundRect(1, 1, size.width - 2, size.height - 2, size.height - 2, size.height - 2);
            g.translate(10, 0);
            super.paintComponent(g0);
        }
    }
}