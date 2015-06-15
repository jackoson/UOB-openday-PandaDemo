package client.view;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class GraphNodeRep {
    private Color color;
    private Integer location;
    private List<GraphNodeRep> children;

    public GraphNodeRep(Color color, Integer location) {
        this.color = color;
        this.location = location;
        this.children = new ArrayList<GraphNodeRep>();
    }

    public void addChild(GraphNodeRep child) {
        /*boolean locOccupied = false;//?maybe use a map
        for(GraphNodeRep c : children) {
            if (child.location() == c.location()) locOccupied = true;
        }
        if (!locOccupied) */children.add(child);
    }

    public List<GraphNodeRep> children() {
        return children;
    }

    public Color color() {
        return color;
    }

    public Integer location() {
        return location;
    }
}
