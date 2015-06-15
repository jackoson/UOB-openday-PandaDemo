package client.view;

import java.awt.Color;
import java.util.List;

public class RouteHint {

    private List<Integer> locations;
    private Color color;

    public RouteHint(List<Integer> locations, Color color) {
        this.locations = locations;
        this.color = color;
    }

    public List<Integer> getRoute() {
        return locations;
    }

    public Color getColor() {
        return color;
    }

}
