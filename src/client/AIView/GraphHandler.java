package client.aiview;

import client.view.*;
import player.*;

import java.util.*;
import java.awt.Color;

import scotlandyard.*;

public class GraphHandler {

    private Map<Integer, Node> nodes;
    private Map<TreeNode, Node> treeMap;
    private List<Node> allNodes;
    private List<Edge<Node>> edges;
    private Map<String, List<Map<String, Double>>> json;
    private Double angle = 0.0;
    private Vector origin = null;
    private TreeNode treeNode = null;
    List<RouteHint> prev = new ArrayList<RouteHint>();

    private boolean animating;

    public GraphHandler(Map<String, List<Map<String, Double>>> json) {
        animating = false;
        nodes = new HashMap<Integer, Node>();
        treeMap = new HashMap<TreeNode, Node>();
        allNodes = new ArrayList<Node>();
        edges = new ArrayList<Edge<Node>>();
        this.json = json;
        createSphere(json);
    }

    public synchronized boolean animating() {
        return animating;
    }

    public synchronized void addNode(Node node) {
        Node n = nodes.get(node.location());
        if (n == null) {
            nodes.put(node.location(), node);
        }
        allNodes.add(node);
    }

    public synchronized void addEdge(Edge<Node> edge) {
        edges.add(edge);
    }

    public synchronized Node getNode(int location) {
        return nodes.get(location);
    }

    public synchronized Set<Node> getNodes() {
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

    public synchronized List<Edge<Node>> getEdges() {
        return edges;
    }

    public synchronized void setOrigin(Vector origin) {
        this.origin = origin;
    }

    public synchronized Vector getOrigin() {
        return origin;
    }

    public synchronized void rotateNodes(Double angle) {
        Double diff = angle - this.angle;
        for (Node node : allNodes) {
            node.rotate(diff);
        }
        this.angle = angle;
    }

    public synchronized void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public synchronized TreeNode treeNode() {
        return treeNode;
    }

    public synchronized void selectNodes(TreeNode treeNode) {
        if (treeNode != null) {
            synchronized (treeNode) {
                for (TreeNode treeNodeRep : treeNode.getChildren()) {
                    Node n = getNode(treeNodeRep.getPlayerLocation());
                    n.setSelected(true);
                    selectNodes(treeNodeRep);
                }
            }
        }
    }

    public synchronized void deselectNodes() {
        for (Node node : allNodes) {
            node.setSelected(false);
        }
    }

    private synchronized void createSphere(Map<String, List<Map<String, Double>>> json) {
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

    public synchronized void updateNodes() {
        if (!animating) selectNodes(treeNode());
    }

    public synchronized List<RouteHint> showTree(AnimatablePanel panel) {
        cleanTree();
        panel.cancelAllAnimations();
        animating = true;
        treeMap.clear();
        List<RouteHint> spider = buildTree(panel, treeNode(), -300.0, 600.0, -180.0, null, false, true);
        for (Node n : allNodes) {
            if (!n.inTree()) n.setAnimators(null, null, null);
        }
        panel.start();
        return spider;
    }

    public synchronized List<RouteHint> updateTree(AnimatablePanel panel) {
        if (!animating) {
            cleanRebuiltTree();
            List<RouteHint> spider = buildTree(panel, treeNode(), -300.0, 600.0, -180.0, null, true, true);
            for (Node n : allNodes) {
                if (!n.inTree()) {
                    n.setAnimators(null, null, null);
                    n.forwardAnimators(1.0);
                }
            }
            return spider;
        }
        return null;
    }

    private synchronized List<RouteHint> buildTree(AnimatablePanel panel, TreeNode treeNode, Double xStart, Double width, Double y, Node parent, boolean rebuilding, boolean bestChild) {
        if (treeNode == null) return new ArrayList<RouteHint>();
        synchronized (treeNode) {
            //Spiders
            List<RouteHint> allHints = new ArrayList<RouteHint>();
            List<Integer> locs = new ArrayList<Integer>();

            TreeNode treeParent = treeNode.getParent();
            if (treeParent != null) {
                Integer prevLocation = treeParent.getPlayerLocation();
                Integer location = treeNode.getMoveLocation();

                if (location != null && prevLocation != null) {
                    locs.add(prevLocation);
                    locs.add(location);

                    Color c = new Color(0,109,209,255);
                    if (treeNode.getPlayer().equals(Colour.Blue)) c = new Color(30,30,30,255);
                    RouteHint hint = new RouteHint(locs, c);
                    if (!prevContains(hint)) {
                        allHints.add(hint);
                        prev.add(hint);
                    }
                }
            }
            //Tree
            Double x =  xStart + (width / 2.0);
            Node node = treeMap.get(treeNode);
            if (node == null) {
                node = getNode(treeNode.getPlayerLocation());

                if (node.inTree()){
                    node = new Node(node.getTrueX(), node.getTrueY(), node.getTrueZ(), node.getTrueColor(), node.location());
                    allNodes.add(node);
                }
                node.setAnimators(panel.createDelayedAnimator(node.getX(), x, 1.0), panel.createDelayedAnimator(node.getY(), y, 1.0), panel.createDelayedAnimator(node.getZ(), 165.0, 1.0));
                if (rebuilding) node.forwardAnimators(1.0);
                node.setTree(true);
                node.setSelected(true);
                if (parent != null)  {
                    Edge<Node> e = new Edge<Node>(node, parent);
                    e.setSelected(true);
                    e.setInTree(true);
                    addEdge(e);
                    node.setParent(parent);
                }
            }

            if (bestChild) node.setBest(true);
            else node.setBest(false);

            TreeNode bestChildTreeNode = treeNode.getBestChild();

            int size = Math.min(treeNode.getChildren().size(), 4);
            width = width / size;
            for (int i = 0; i < size; i++) {
                TreeNode treeNodeRep = treeNode.getChildren().get(i);
                boolean best = false;
                if (treeNodeRep == bestChildTreeNode) best = true;
                allHints.addAll(buildTree(panel, treeNodeRep, xStart + (width * i), width, y + 80, node, rebuilding, (best && bestChild)));
            }

            return allHints;
        }
    }

    private synchronized boolean prevContains(RouteHint route) {
        for (RouteHint hint : prev) {
            if (hint.getRoute().size() == route.getRoute().size()) {
                boolean equal = true;
                for (Integer h : hint.getRoute()) {
                    if (!route.getRoute().contains(h)) equal = false;
                }
                if (equal) return true;
            }
        }
        return false;
    }

    public synchronized void cleanSpiders() {
        prev.clear();
    }

    public synchronized void returnFromTree(AnimatablePanel panel) {
        animating = true;

        for (Node n : allNodes) {
            n.reverseAnimation(1.0, panel);
        }
    }

    public synchronized void finishTreeBuild() {
        animating = false;
    }

    public synchronized void cleanTree() {
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

    public synchronized void cleanRebuiltTree() {
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
