package client.view;

abstract class ListCellView extends AnimatablePanel {
    private boolean selected = false;
    private boolean highlighted = false;
    
    public ListCellView() {
        
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }
    
    public boolean selected() {
        return selected;
    }
    
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
    
    public boolean highlighted() {
        return highlighted;
    }
    
    public Object object() {
        return null;
    }

}