package client.aiview;

import client.view.*;
import player.*;

import java.util.*;
import java.awt.Color;

public class GraphHandler {

    private Map<Integer, Node> nodes;
    private List<Node> allNodes;
    private List<Edge<Node>> edges;
    private Map<String, List<Map<String, Double>>> json;
    private Double angle = 0.0;
    private Vector origin = null;
    private TreeNode treeNode = null;

    private boolean animating;

    public GraphHandler(Map<String, List<Map<String, Double>>> json) {
        animating = false;
        nodes = new HashMap<Integer, Node>();
        allNodes = new ArrayList<Node>();
        edges = new ArrayList<Edge<Node>>();
        this.json = json;
        createSphere(json);
    }

    public boolean animating() {
        return animating;
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
        Set<Node> orderedNodes = new TreeSet<Node>(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                Double o1z = o1.getZ();
                Double o2z = o2.getZ();
                if (o1z < o2z) return 1;
                else return -1;
            }
        });
        for (Node node : allNodes) {
            orderedNodes.add(node);
        }
        return orderedNodes;
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

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public TreeNode treeNode() {
        return treeNode;
    }

    public void selectNodes(TreeNode treeNode) {
        if (treeNode != null) {
            synchronized (treeNode) {
                for (TreeNode treeNodeRep : treeNode.getChildren()) {
                    Node n = getNode(treeNodeRep.getTrueLocation());
                    n.setSelected(true);
                    selectNodes(treeNodeRep);
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

    public void updateNodes() {
        if (!animating) selectNodes(treeNode());
    }

    public void showTree(AnimatablePanel panel) {
        cleanTree();
        panel.cancelAllAnimations();
        animating = true;
        buildTree(panel, treeNode(), -300.0, 600.0, -180.0, null, false);
        for (Node n : allNodes) {
            if (!n.inTree()) n.setAnimators(null, null, null, panel.createDelayedAnimator(1.0, 0.0, 1.0));
        }
        for (Edge e : edges) {
            if (!e.inTree()) e.setAnimator(panel.createDelayedAnimator(1.0, 0.0, 1.0));
        }
        panel.start();
    }

    public void updateTree(AnimatablePanel panel) {
        if (!animating) {
            cleanRebuiltTree();
            buildTree(panel, treeNode(), -300.0, 600.0, -180.0, null, true);
            for (Node n : allNodes) {
                if (!n.inTree()) {
                    n.setAnimators(null, null, null, panel.createDelayedAnimator(1.0, 0.0, 1.0));
                    n.forwardAnimators(1.0);
                }
            }
        }
    }

    private void buildTree(AnimatablePanel panel, TreeNode treeNode, Double xStart, Double width, Double y, Node parent, boolean rebuilding) {
        if (treeNode == null) return;
        synchronized (treeNode) {
            Double x =  xStart + (width / 2.0);
            Node node = getNode(treeNode.getTrueLocation());
            if (node == null) {
                System.err.println("Null node when creating tree.");
                return;
            }
            if (node.inTree()){
                node = new Node(node.getTrueX(), node.getTrueY(), node.getTrueZ(), node.getColor(), node.location());
                allNodes.add(node);
            }
            node.setAnimators(panel.createDelayedAnimator(node.getX(), x, 1.0), panel.createDelayedAnimator(node.getY(), y, 1.0), panel.createDelayedAnimator(node.getZ(), 165.0, 1.0), null);
            if (rebuilding) node.forwardAnimators(1.0);
            node.setTree(true);
            node.setSelected(true);
            if (parent != null)  {
                Edge<Node> e = new Edge<Node>(node, parent);
                e.setInTree(true);
                e.setAnimator(panel.createDelayedAnimator(0.0, 1.0, 1.0));
                if (rebuilding) e.forwardAnimators(1.0);
                addEdge(e);
                node.setParent(parent);
            }
            int size = Math.min(treeNode.getChildren().size(), 4);
            width = width / size;
            for (int i = 0; i < size; i++) {
                TreeNode treeNodeRep = treeNode.getChildren().get(i);
                buildTree(panel, treeNodeRep, xStart + (width * i), width, y + 80, node, rebuilding);
            }
        }
    }

    public void returnFromTree(AnimatablePanel panel) {
        animating = true;
        for (Node n : allNodes) {
            n.reverseAnimation(1.0, panel);
        }
        for (Edge e : edges) {
            e.reverseAnimation(1.0, panel);
        }
    }

    public void finishTreeBuild() {
        animating = false;
    }

    public void cleanTree() {
        List<Node> newAllNodes = new ArrayList<Node>();
        for (Node n : allNodes) {
            if(nodes.containsValue(n)) newAllNodes.add(n);
            n.resetAnimators();
            n.setTree(false);
        }
        allNodes = newAllNodes;
        List<Edge<Node>> newEdges = new ArrayList<Edge<Node>>();
        for (Edge<Node> e : edges) {
            if (!e.inTree()) {
                newEdges.add(e);
            }
        }
        edges = newEdges;
        animating = false;
    }

    public void cleanRebuiltTree() {
        List<Node> newAllNodes = new ArrayList<Node>();
        for (Node n : allNodes) {
            if(nodes.containsValue(n)) newAllNodes.add(n);
            n.setTree(false);
        }
        allNodes = newAllNodes;
        List<Edge<Node>> newEdges = new ArrayList<Edge<Node>>();
        for (Edge<Node> e : edges) {
            if (!e.inTree()) {
                newEdges.add(e);
            }
        }
        edges = newEdges;
    }
}
