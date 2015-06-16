package client.aiview;

import client.view.AnimatablePanel;

public class Edge<X> {

    private X node1;
    private X node2;
    private boolean inTree;
    private Double alpha;
    private AnimatablePanel.Animator alphaAnimator;

    public Edge(X node1, X node2) {
        this.node1 = node1;
        this.node2 = node2;
        inTree = false;
        alpha = 1.0;
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

    public void setAnimator(AnimatablePanel.Animator alphaAnimator) {
        if (alphaAnimator != null) alpha = alphaAnimator.value();
        this.alphaAnimator = alphaAnimator;
    }

    public void reverseAnimation(Double duration, AnimatablePanel panel) {
        if(this.alphaAnimator != null) {
            alphaAnimator = panel.createAnimator(alphaAnimator.value(), alpha, duration);
        }
    }

    public void forwardAnimators(Double time) {
        if(this.alphaAnimator != null) alphaAnimator.setTime(time);
    }

    public Double getAlpha() {
        Double alpha = this.alpha;
        if (alphaAnimator != null) alpha = alphaAnimator.value();
        return alpha;
    }

}
