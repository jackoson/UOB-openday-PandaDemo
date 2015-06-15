package client.aiview;

import client.view.AnimatablePanel;

import java.awt.*;

public class Node extends Vector {

    private Color color;
    private Double alpha;
    private Integer location;
    private Node parent;
    private boolean selected;
    private boolean tree;

    private AnimatablePanel.Animator xAnimator = null;
    private AnimatablePanel.Animator yAnimator = null;
    private AnimatablePanel.Animator zAnimator = null;
    private AnimatablePanel.Animator alphaAnimator = null;

    public Node(Double x, Double y, Double z, Color color, int location) {
        super(x, y, z);
        this.color = color;
        this.alpha = 1.0;
        this.location = location;
        this.parent = null;
        this.selected = false;
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

    public boolean inTree() {
        return tree;
    }

    public Color getColor() {
        Double alpha = this.alpha;
        if(alphaAnimator != null) alpha = alphaAnimator.value();
        if (selected) return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        else return new Color(255, 255, 255, (int)(alpha * 255));
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

    public void setAnimators(AnimatablePanel.Animator xAnimator, AnimatablePanel.Animator yAnimator, AnimatablePanel.Animator zAnimator, AnimatablePanel.Animator alphaAnimator) {
        this.xAnimator = xAnimator;
        if (this.xAnimator != null) this.xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.yAnimator = yAnimator;
        if (this.xAnimator != null) this.yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.zAnimator = zAnimator;
        if (this.xAnimator != null) this.zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        this.alphaAnimator = alphaAnimator;
        if (this.alphaAnimator != null) this.alphaAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
    }

    public void reverseAnimation(Double duration, AnimatablePanel panel) {
        if(this.xAnimator != null) {
            xAnimator = panel.createAnimator(getX(), super.getX(), duration);
            this.xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        }
        if(this.yAnimator != null) {
            yAnimator = panel.createAnimator(getY(), super.getY(), duration);
            this.yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        }
        if(this.zAnimator != null) {
            zAnimator = panel.createAnimator(getZ(), super.getZ(), duration);
            this.zAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        }
        if(this.alphaAnimator != null) {
            alphaAnimator = panel.createAnimator(alphaAnimator.value(), alpha, duration);
        }
    }

    public void resetAnimators() {
        xAnimator = null;
        yAnimator = null;
        zAnimator = null;
        alphaAnimator = null;
    }

    public void forwardAnimators(Double time) {
        if(this.xAnimator != null) xAnimator.setTime(time);
        if(this.yAnimator != null) yAnimator.setTime(time);
        if(this.zAnimator != null) zAnimator.setTime(time);
        if(this.alphaAnimator != null) alphaAnimator.setTime(time);
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
