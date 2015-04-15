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
    private MenuBar menu = null;
    
    /**
     * Constructs a new ChatView object.
     */
    public ChatView() {
        setPreferredSize(new Dimension(300, 40));
        setLayout(new GridBagLayout());
        text = getStyledTextField();
        add(text);
    }
    
    // Returns a styled text field.
    // @return a styled text field.
    private JTextField getStyledTextField() {
        JTextField textField = new JTextField(hint);
        textField.setPreferredSize(new Dimension(260, 20));
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.GRAY);
        textField.setBorder(new RoundedBorder());
        textField.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        textField.addKeyListener(this);
        textField.addFocusListener(this);
        return textField;
    }
    
    public void setMenu(MenuBar menu) {
        this.menu = menu;
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
        if (menu != null) menu.showChat();
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
        if (menu != null) menu.hideChat();
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
    
    // A class to give the JTextField rounded ends.
    // Copied from: http://java-swing-tips.blogspot.com.ar/2012/03/rounded-border-for-jtextfield.html
    private class RoundedBorder extends AbstractBorder {
        
        /**
         * Draws the border on the JTextField.
         *
         * @param c the component to draw the Border on.
         * @param g0 the Graphics object to draw to.
         * @param x the x coordinate of the Border.
         * @param y the y coordinate of the Border.
         * @param width the width of the Border.
         * @param height the height of the Border.
         */
        @Override
        public void paintBorder(Component c, Graphics g0, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width - 1, height - 1, height - 1, height - 1);
            Container parent = c.getParent();
            if (parent != null) {
                g.setColor(parent.getBackground());
                Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
                corner.subtract(new Area(rect));
                g.fill(corner);
            }
            g.setColor(Color.WHITE);
            g.draw(rect);
        }
        
        /**
         * Returns the Insets of the Border.
         *
         * @param c the component to get the Insets for.
         * @return the Insets of the Border.
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 8, 2, 8);
        }
        
        /**
         * Returns the Insets of the Border.
         *
         * @param c the component to get the Insets for.
         * @param insets the Insets to be changed.
         * @return the Insets of the Border.
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 2;
            return insets;
        }
        
    }
    
}