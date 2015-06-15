package client.aiview;

import java.util.*;

public class Vector {

    private Double x, y, z;

    public Vector(Double x, Double y,Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector add(Vector vector) {
        Double x = this.getX() + vector.getX();
        Double y = this.getY() + vector.getY();
        Double z = this.getZ() + vector.getZ();
        return new Vector(x, y, z);
    }

    public Vector subtract(Vector vector) {
        Double x = this.getX() - vector.getX();
        Double y = this.getY() - vector.getY();
        Double z = this.getZ() - vector.getZ();
        return new Vector(x, y, z);
    }

    public Vector rotateXY(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.getX()) - (Math.sin(angle) * this.getY());
        Double y = (Math.sin(angle) * this.getX()) + (Math.cos(angle) * this.getY());
        return new Vector(x, y, z);
    }

    public Vector rotateYZ(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double y = (Math.cos(angle) * this.getY()) - (Math.sin(angle) * this.getZ());
        Double z = (Math.sin(angle) * this.getY()) + (Math.cos(angle) * this.getZ());
        return new Vector(x, y, z);
    }

    public Vector rotateXZ(double degrees) {
        Double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.getX()) + (Math.sin(angle) * this.getZ());
        Double z = (Math.cos(angle) * this.getZ()) - (Math.sin(angle) * this.getX());
        return new Vector(x, y, z);
    }

    public Vector scale(double s0, double s1, double s2) {
        Double x = this.getX() * s0;
        Double y = this.getY() * s1;
        Double z = this.getZ() * s1;
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
