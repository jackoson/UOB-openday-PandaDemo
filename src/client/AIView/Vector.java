package client.aiview;

import java.util.*;
import client.view.AnimatablePanel.Animator;

public class Vector {

    private Double x, y, z;
    private Animator xAnimator = null, yAnimator = null, zAnimator = null;

    public Vector(Double x, Double y,Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void set(Double x, Double y,Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void setAnimators(Animator xAnimator, Animator yAnimator, Animator zAnimator) {
        this.xAnimator = xAnimator;
        this.yAnimator = yAnimator;
        this.zAnimator = zAnimator;
    }

    public Vector addVectorToVector(Vector vector) {
        Double x = this.x + vector.getX();
        Double y = this.y + vector.getY();
        Double z = this.z + vector.getZ();
        return new Vector(x, y, z);
    }

    public Vector subtractVectorFromVector(Vector vector) {
        Double x = this.x - vector.getX();
        Double y = this.y - vector.getY();
        Double z = this.z - vector.getZ();
        return new Vector(x, y, z);
    }

    public Vector rotateXY(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.x) - (Math.sin(angle) * this.y);
        Double y = (Math.sin(angle) * this.x) + (Math.cos(angle) * this.y);
        return new Vector(x, y, z);
    }

    public Vector rotateYZ(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double y = (Math.cos(angle) * this.y) - (Math.sin(angle) * this.z);
        Double z = (Math.sin(angle) * this.y) + (Math.cos(angle) * this.z);
        return new Vector(x, y, z);
    }

    public Vector rotateXZ(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.x) + (Math.sin(angle) * this.z);
        Double z = (Math.cos(angle) * this.z) - (Math.sin(angle) * this.x);
        return new Vector(x, y, z);
    }

    public Vector scale(double s0, double s1, double s2) {
        Double x = this.x * s0;
        Double y = this.y * s1;
        Double z = this.z * s1;
        return new Vector(x, y, z);
    }

    public Double getX() {
        if(xAnimator != null) return xAnimator.value();
        return x;
    }

    public Double getY() {
        if(yAnimator != null) return yAnimator.value();
        return y;
    }

    public Double getZ() {
        if(zAnimator != null) return zAnimator.value();
        return z;
    }

    public String stringValue() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
