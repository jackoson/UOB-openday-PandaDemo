package client.view;

import java.util.*;

/**
 * A class to hold a cell in the ListView.
 */

abstract class ListCellView extends AnimatablePanel {
  
    private static final long serialVersionUID = 3762472219882857921L;
  
    private boolean selected = false;
    private boolean highlighted = false;
    
    /**
     * Constructs a new ListCellView object.
     */
    public ListCellView() {}
    
    /**
     * Sets whether the cell is selected.
     *
     * @param selected the boolean that decides whether the cell is selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }
    
    /**
     * Returns true if the cell is selected.
     *
     * @return true if the cell is selected.
     */
    public boolean selected() {
        return selected;
    }
    
    /**
     * Sets whether the cell is highlighted.
     * 
     * @param highlighted the boolean that decides whether the cell is highlighted.
     */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
    
    /**
     * Returns true if the cell is highlighted.
     *
     * @return true if the cell is highlighted.
     */
    public boolean highlighted() {
        return highlighted;
    }
    
    /**
     * Returns null.
     *
     * @return null.
     */
    public Object object() {
        return null;
    }
    
    /**
     * Returns null.
     *
     * @return null.
     */
    public Collection collection() {
        return null;
    }

}