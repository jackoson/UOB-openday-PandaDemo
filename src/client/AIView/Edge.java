package client.aiview;

public class Edge<X> {

    private X node1;
    private X node2;
    private boolean inTree;

    public Edge(X node1, X node2) {
        this.node1 = node1;
        this.node2 = node2;
        inTree = false;
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

}
