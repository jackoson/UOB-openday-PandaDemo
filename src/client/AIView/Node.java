package client.aiview;

import client.view.AnimatablePanel;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private boolean selected;
    private boolean tree;
    private int location;

    private AnimatablePanel.Animator xAnimator = null;
    private AnimatablePanel.Animator yAnimator = null;
    private AnimatablePanel.Animator zAnimator = null;

    public Node(Double x, Double y, Double z, Color color) {
        super(x, y, z);
        this.color = color;
        this.selected = false;
        this.tree = false;
    }

    public Node(Double x, Double y, Double z, Color color, boolean selected) {
        super(x, y, z);
        this.color = color;
        this.selected = selected;
        this.tree = false;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }

    public boolean isTree() {
        return tree;
    }

    public Color getColor() {
        if (selected) return this.color;
        else return Color.WHITE;
    }

    public void setAnimators(AnimatablePanel.Animator xAnimator, AnimatablePanel.Animator yAnimator, AnimatablePanel.Animator zAnimator) {
        this.xAnimator = xAnimator;
        this.xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.yAnimator = yAnimator;
        this.yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.zAnimator = zAnimator;
        this.zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
    }

    @Override
    public Double getX() {
        if (xAnimator == null) return super.getX();
        else return xAnimator.value();
    }

    @Override
    public Double getY() {
        if (yAnimator == null) return super.getY();
        else return yAnimator.value();
    }

    @Override
    public Double getZ() {
        if (zAnimator == null) return super.getZ();
        else return zAnimator.value();
    }

}
