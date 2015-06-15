package client.aiview;

import java.util.*;
import client.view.AnimatablePanel.Animator;

public class Vector {

    private Double x, y, z;

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

    public void add(Vector vector) {
        x = x + vector.getX();
        y = y + vector.getY();
        z = z + vector.getZ();
    }

    public void subtract(Vector vector) {
        x = x - vector.getX();
        y = y - vector.getY();
        z = z - vector.getZ();
    }

    public void rotate(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double yy = (Math.cos(angle) * y) - (Math.sin(angle) * z);
        z = (Math.sin(angle) * y) + (Math.cos(angle) * z);
        y=yy;

        Double zz = (Math.cos(angle) * z) - (Math.sin(angle) * x);
        x = (Math.cos(angle) * x) + (Math.sin(angle) * z);
        z = zz;
    }

    public Vector scale(double s0, double s1, double s2) {
        Double x = this.getX() * s0;
        Double y = this.getY() * s1;
        Double z = this.getZ() * s1;
        return new Vector(x, y, z);
    }

    public Vector offsetAdd(Vector vector) {
        Double x = this.getX() + vector.getX();
        Double y = this.getY() + vector.getY();
        Double z = this.getZ() + vector.getZ();
        return new Vector(x, y, z);
    }

    public Vector offsetSubtract(Vector vector) {
        Double x = this.getX() - vector.getX();
        Double y = this.getY() - vector.getY();
        Double z = this.getZ() - vector.getZ();
        return new Vector(x, y, z);
    }

    public Vector offsetRotate(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.getX()) - (Math.sin(angle) * this.getY());
        Double y = (Math.sin(angle) * this.getX()) + (Math.cos(angle) * this.getY());

        x = (Math.cos(angle) * this.getX()) - (Math.sin(angle) * this.getY());
        y = (Math.sin(angle) * this.getX()) + (Math.cos(angle) * this.getY());
        return new Vector(x, y, z);
    }

    public Double getX() {
        return this.x;
    }

    public Double getY() {
        return this.y;
    }

    public Double getZ() {
        return this.z;
    }

    public String stringValue() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
