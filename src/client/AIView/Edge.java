package client.aiview;

public class Edge<X> {

    private X node1;
    private X node2;

    public Edge(X node1, X node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public X getNode1() {
        return node1;
    }

    public X getNode2() {
        return node2;
    }

}
