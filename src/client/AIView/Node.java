package client.aiview;

import client.view.AnimatablePanel;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private boolean selected;
    private Node parent;
    private Integer location;

    private AnimatablePanel.Animator xAnimator = null;
    private AnimatablePanel.Animator yAnimator = null;
    private AnimatablePanel.Animator zAnimator = null;

    public Node(Double x, Double y, Double z, Color color) {
        super(x, y, z);
        this.color = color;
        this.selected = false;
    }

    public Node(Double x, Double y, Double z, Color color, boolean selected) {
        super(x, y, z);
        this.color = color;
        this.selected = selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Color getColor() {
        if (selected) return this.color;
        else return Color.WHITE;
    }

    public void setParent(Node p) {
      parent = p;
    }

    public Node parent() {
      return parent;
    }

    public void setLocation(Integer l) {
      location = l;
    }

    public Integer location() {
      return location;
    }

    public void setAnimators(AnimatablePanel.Animator xAnimator, AnimatablePanel.Animator yAnimator, AnimatablePanel.Animator zAnimator) {
        this.xAnimator = xAnimator;
        this.xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.yAnimator = yAnimator;
        this.yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.zAnimator = zAnimator;
        this.zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
    }

    public Node getPosInAnimation() {
        if (xAnimator == null && yAnimator == null && zAnimator == null) return null;
        else return new Node(xAnimator.value(), yAnimator.value(), zAnimator.value(), this.color, this.selected);
    }

}
