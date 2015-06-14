package client.aiview;

import java.awt.*;

/**
 * Maybe add the animators in here?
 */

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
