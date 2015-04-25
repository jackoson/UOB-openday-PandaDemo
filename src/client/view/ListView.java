package client.view;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;

/**
 * A view to display a List of RouteViews.
 */

public class ListView extends JPanel implements MouseListener {
    
    private static final long serialVersionUID = 2221775046541743370L;
  
    private ListCellView selectedCell = null;
    private ListCellView highlightedCell= null;
    private JPanel cellHolder;
    private Timer timer;
    private ActionListener listener = null;
    
    /**
     * Constructs a new ListView object.
     */
    public ListView() {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        cellHolder = new JPanel();
        cellHolder.setOpaque(false);
        cellHolder.setLayout(new GridBagLayout());
        add(cellHolder, BorderLayout.CENTER);
    }
    
    /**
     * Sets the cells to be displayed.
     *
     * @param cells the List of ListCellViews to be displayed.
     */
    public void setCells(List<ListCellView> cells) {
        cellHolder.removeAll();
        //Constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(0, 6, 0, 6);
        //Cells
        int gridy = 0;
        for (ListCellView cell : cells) {
            constraints.gridy = gridy;
            gridy++;
            
            cell.setPreferredSize(new Dimension(20, 32));
            
            cell.addMouseListener(this);
            cellHolder.add(cell, constraints);
        }
        //Spacer
        constraints.gridy = gridy + 1;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        cellHolder.add(spacer, constraints);
    }
    
    /**
     * Adds the specified ActionListener to receive when the user clicks on a cell.
     * If listener listener is null, no action is performed.
     * 
     * @param listener the listener to be added to the view.
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Returns the object from the selected cell.
     *
     * @return the object from the selected cell.
     */
    public Object selectedObject() {
        if (selectedCell != null) return selectedCell.object();
        return null;
    }
    
    /**
     * Returns the route from the highlighted cell.
     */
    public Collection highlightedRoute() {
        if (highlightedCell != null) return highlightedCell.collection();
        return null;
    }
    
    // Highlights the specified cell and sends an ActionEvent to the specified ActionListener.
    // @param cell the cell to be highlighted.
    private void highlightCell(ListCellView cell) {
        cell.setHighlighted(true);
        cell.setPreferredSize(new Dimension(20, 58), 0.2, AnimatablePanel.AnimationEase.EASE_IN_OUT);
        highlightedCell = cell;
        if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "list_cell_highlighted"));
    }
    
    // Unhighlights the specified cell and sends an ActionEvent to the specified ActionListener.
    // @param cell the cell to be unhighlighted.
    private void unhighlightCell(ListCellView cell) {
        cell.setHighlighted(false);
        cell.setPreferredSize(new Dimension(20, 32), 0.2, AnimatablePanel.AnimationEase.EASE_IN_OUT);
        highlightedCell = null;
        if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "list_cell_unhighlighted"));
    }
    
    /**
     * Called when the user clicks on a cell, it then highlights that cell.
     *
     * @param e the MouseEvent containing which cell has been clicked on.
     */
    public void mouseClicked(MouseEvent e) {
        ListCellView cell = (ListCellView) e.getSource();
        if (selectedCell != null) selectedCell.setSelected(false);
        if (cell != selectedCell) {cell.setSelected(true); selectedCell = cell;}
        else selectedCell = null;
    }
    
    /**
     * Called when the user moves their mouse into a cell, it then
     * expands that cell.
     *
     * @param e the MousEvent containing which cell the user moved into.
     */
    public void mouseEntered(MouseEvent e) {
        ListCellView cell = (ListCellView) e.getSource();
        if (highlightedCell != null) unhighlightCell(highlightedCell);
        if (cell != highlightedCell) highlightCell(cell);
    }
    
    /**
     * Called when the user moves their mouse out of a cell, it then
     * contracts that cell.
     *
     * @param e the MousEvent containing which cell the user moved out of.
     */
    public void mouseExited(MouseEvent e) {
        ListCellView cell = (ListCellView) e.getSource();
        if (cell == highlightedCell) unhighlightCell(cell);
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
    
}
