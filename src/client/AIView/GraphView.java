package client.aiview;

import client.view.*;

import java.util.*;
import java.awt.Color;

public class GraphView {

    private Map<Integer, Node> nodes;
    private Set<Node> allNodes;
    private List<Edge<Node>> edges;
    private Map<String, List<Map<String, Double>>> json;
    private Double angle = 0.0;
    private Vector origin = null;
    private GraphNodeRep graphNode = null;

    public GraphView(Map<String, List<Map<String, Double>>> json) {
        nodes = new HashMap<Integer, Node>();
        allNodes = new TreeSet<Node>(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                Double o1z = o1.getZ();
                Double o2z = o2.getZ();
                if (o1z < o2z) return 1;
                else return -1;
            }
        });
        edges = new ArrayList<Edge<Node>>();
        this.json = json;
        createSphere(json);
    }

    public void addNode(Node node) {
        Node n = nodes.get(node.location());
        if (n == null) {
            nodes.put(node.location(), node);
        }
        allNodes.add(node);
    }

    public void addEdge(Edge<Node> edge) {
        edges.add(edge);
    }

    public Node getNode(int location) {
        return nodes.get(location);
    }

    public Set<Node> getNodes() {
        return allNodes;
    }

    public List<Edge<Node>> getEdges() {
        return edges;
    }

    public void setOrigin(Vector origin) {
        this.origin = origin;
    }

    public Vector getOrigin() {
        return origin;
    }

    public void rotateNodes(Double angle) {
        Double diff = angle - this.angle;
        for (Node node : allNodes) {
            node.rotate(diff);
        }
        this.angle = angle;
    }

    public void setGraphNode(GraphNodeRep graphNode) {
        this.graphNode = graphNode;
    }

    public void selectNodes(GraphNodeRep graphNode) {
        if (graphNode != null) {
            synchronized (graphNode) {
                for (GraphNodeRep graphNodeRep : graphNode.children()) {
                    Node n = getNode(graphNodeRep.location());
                    n.setSelected(true);
                    selectNodes(graphNodeRep);
                }
            }
        }
    }

    public void deselectNodes() {
        for (Node node : allNodes) {
            node.setSelected(false);
        }
    }

    private void createSphere(Map<String, List<Map<String, Double>>> json) {
        List<Map<String, Double>> items = json.get("nodes");
        for (Map<String, Double> item : items) {
            Double ox = item.get("x");
            Double oy = item.get("y");
            Double oz = item.get("z");

            Double y = 2.0 * (oy / 2000) - 1.0;
            Double phi = 2.0 * Math.PI * (ox / 2570);
            Double theta = Math.asin(y);
            Double radius = 60 + (oz * 40);
            Double x = Math.cos(theta) * Math.cos(phi) * radius;
            Double z = Math.cos(theta) * Math.sin(phi) * radius;
            y = y * radius;

            Color color = new Color(255, 113, 113);
            if (oz == 2) color = new Color(42, 154, 164);
            else if (oz == 3) color = new Color(242, 196, 109);

            addNode(new Node(x, y, z, color, item.get("node").intValue()));
        }
        List<Map<String, Double>> connections = json.get("edges");
        for (Map<String, Double> edge : connections) {
            Node node1 = getNode(edge.get("n1").intValue());
            Node node2 = getNode(edge.get("n2").intValue());
            addEdge(new Edge<Node>(node1, node2));
        }
    }

}
