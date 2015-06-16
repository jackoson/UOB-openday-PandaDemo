package client.view;

import java.awt.*;
import java.util.Set;
import java.util.TreeSet;

public class GraphNodeRep {

    private Color color;
    private Integer location;
    private Set<GraphNodeRep> children;

    public GraphNodeRep(Color color, Integer location) {
        this.color = color;
        this.location = location;
        this.children = new TreeSet<GraphNodeRep>();
    }

    public void addChild(GraphNodeRep child) {
        if (children.size() < 5) children.add(child);
    }

    public Set<GraphNodeRep> children() {
        return children;
    }

    public Color color() {
        return color;
    }

    public Integer location() {
        return location;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof GraphNodeRep) {
            GraphNodeRep gnr = (GraphNodeRep) object;
            if (gnr.color().equals(this.color()) && gnr.location().equals(this.location())) return true;
        }
        return false;
    }

}
