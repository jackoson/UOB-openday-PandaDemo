package client.aiview;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private boolean selected;
    private boolean partOfTree;

    public Node(Double x, Double y, Double z, Color color) {
        super(x, y, z);
        this.color = color;
        this.selected = false;
        this.partOfTree = false;
    }

    public Node(Double x, Double y, Double z, Color color, boolean selected) {
        super(x, y, z);
        this.color = color;
        this.selected = selected;
        this.partOfTree = false;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setPartOfTree(boolean partOfTree) {
        this.partOfTree = partOfTree;
    }

    public boolean isPartOfTree() {
        return partOfTree;
    }

    public Color getColor() {
        if (!selected) return Color.white;
        else return color;
    }

    public boolean equals(Object object) {
        if (object instanceof Node) {
            Node node = (Node) object;
            if (this.getX() == node.getX() &&
                this.getY() == node.getY() &&
                this.getZ() == node.getZ() &&
                this.getColor().equals(node.getColor()) &&
                this.isSelected() == node.isSelected() &&
                this.isPartOfTree() == node.isPartOfTree()) return true;
        }
        return false;
    }

}
