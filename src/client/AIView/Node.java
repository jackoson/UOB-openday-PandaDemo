package client.aiview;

import client.view.AnimatablePanel;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private Integer location;
    private Node parent;
    private boolean selected;
    private boolean tree;
    private boolean best;

    private AnimatablePanel.Animator xAnimator = null;
    private AnimatablePanel.Animator yAnimator = null;
    private AnimatablePanel.Animator zAnimator = null;

    public Node(Double x, Double y, Double z, Color color, int location) {
        super(x, y, z);
        this.color = color;
        this.location = location;
        this.parent = null;
        this.selected = false;
        this.tree = false;
        this.best = false;
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

    public boolean inTree() {
        return tree;
    }

    public Color getTrueColor() {
        return this.color;
    }

    public Color getColor() {
        if (best && tree) return new Color(255, 0, 0);
        else if (selected) return color;
        else return Color.WHITE;
    }

    public void setBest(boolean best) {
        this.best = best;
    }

    public boolean isBest() {
        return best;
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
        if (this.xAnimator != null) this.xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.yAnimator = yAnimator;
        if (this.xAnimator != null) this.yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.zAnimator = zAnimator;
        if (this.xAnimator != null) this.zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
    }

    public void reverseAnimation(Double duration, AnimatablePanel panel) {
        if(this.xAnimator != null) {
            xAnimator = panel.createAnimator(getX(), super.getX(), duration, false);
            this.xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        }
        if(this.yAnimator != null) {
            yAnimator = panel.createAnimator(getY(), super.getY(), duration, false);
            this.yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        }
        if(this.zAnimator != null) {
            zAnimator = panel.createAnimator(getZ(), super.getZ(), duration, false);
            this.zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        }
    }

    public void resetAnimators() {
        xAnimator = null;
        yAnimator = null;
        zAnimator = null;
    }

    public void forwardAnimators(Double time) {
        if(this.xAnimator != null) xAnimator.setTime(time);
        if(this.yAnimator != null) yAnimator.setTime(time);
        if(this.zAnimator != null) zAnimator.setTime(time);
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

    public Double getTrueX() {
        return super.getX();
    }

    public Double getTrueY() {
        return super.getY();
    }

    public Double getTrueZ() {
        return super.getZ();
    }

}
