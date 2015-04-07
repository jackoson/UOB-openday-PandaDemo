import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import javax.imageio.*;

public class ListView extends JPanel implements MouseListener {
    private BufferedImage bg;//Testing
    private ListCellView selectedObject = null;
    private ListCellView highlightedObject = null;
    private GridBagConstraints gbc;
    private JPanel gb;
    private Timer timer;
    
    public ListView() {
        ///TESTING
        try {
            bg = ImageIO.read(new File("map.jpg"));
        } catch (Exception e) {
            System.err.println(e);
        }
        //END TESTING
        setMinimumSize(new Dimension(100, 700));
        setLayout(new BorderLayout());
        
        gb = new JPanel();
        gb.setOpaque(false);
        add(gb, BorderLayout.NORTH);
        gb.setLayout(new GridBagLayout());
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 6, 0, 6);
        
    }
    
    public void setViews(java.util.List<ListCellView> cells) {
        int gridy = 0;
        for (ListCellView cell : cells) {
            gbc.gridy = gridy;
            gridy++;
            
            cell.setPreferredSize(new Dimension(160, 32));
            cell.addMouseListener(this);
            gb.add(cell, gbc);
        }
    }
    
    private void highlightObject(ListCellView object) {
        object.setHighlighted(true);
        object.setPreferredSize(new Dimension(20, 58), 0.2, AnimatablePanel.AnimationEase.EASE_IN_OUT);
        highlightedObject = object;
    }
    
    private void unhighlightObject(ListCellView object) {
        object.setHighlighted(false);
        object.setPreferredSize(new Dimension(20, 32), 0.2, AnimatablePanel.AnimationEase.EASE_IN_OUT);
        highlightedObject = null;
    }

    public void mouseClicked(MouseEvent e) {
        ListCellView object = (ListCellView)e.getSource();
        if (selectedObject != null) selectedObject.setSelected(false);
        if (object != selectedObject) {object.setSelected(true); selectedObject = object;}
        else selectedObject = null;
    }
    
    public void mouseEntered(MouseEvent e) {
        ListCellView object = (ListCellView)e.getSource();
        if (highlightedObject != null) unhighlightObject(highlightedObject);
        if (object != highlightedObject) highlightObject(object);
    }
    
    public void mouseExited(MouseEvent e) {
        ListCellView object = (ListCellView)e.getSource();
        if (object == highlightedObject) unhighlightObject(object);
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
    
    ////////TESTING
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        
        g.drawImage(bg, 0, 0, 2570, 1926, null);
    }
}
