package client.aiview;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private boolean selected;

    public Node(Double x, Double y, Double z, Color color) {
        super(x, y, z);
        this.color = color;
        this.selected = true;
    }

    public void setSelected(boolean selected) {
        this.selected = !selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Color getColor() {
        if (selected) return Color.GRAY;
        else return color;
    }

}
