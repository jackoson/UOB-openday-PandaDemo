package client.aiview;

import java.awt.*;

import client.view.AnimatablePanel;

public class Edge<X> {

    private X node1;
    private X node2;
    private boolean inTree;
    private boolean selected;

    public Edge(X node1, X node2) {
        this.node1 = node1;
        this.node2 = node2;
        inTree = false;
        selected = false;
    }

    public X getNode1() {
        return node1;
    }

    public X getNode2() {
        return node2;
    }

    public void setInTree(boolean inTree) {
        this.inTree = inTree;
    }

    public boolean inTree() {
        return inTree;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Color getColor() {
        Node n = (Node)node1;
        if (selected) return n.getColor();
        else return Color.WHITE;
    }

}
