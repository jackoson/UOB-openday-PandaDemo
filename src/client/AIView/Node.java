package client.aiview;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private boolean selected;
    

    public Node(Double x, Double y, Double z, Color color) {
        super(x, y, z);
        this.color = color;
        this.selected = false;
    }

    public Node(Double x, Double y, Double z, Color color, boolean selected) {
        super(x, y, z);
        this.color = color;
        this.selected = selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Color getColor() {
        if (!selected) return Color.white;
        else return color;
    }

}
