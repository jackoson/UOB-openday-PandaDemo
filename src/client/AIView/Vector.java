package client.aiview;

import java.util.*;

public class Vector {
    
    private Double x, y, z;

    public Vector(Double x, Double y,Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector addVectorToVector(Vector vector) {
        x = x + vector.getX();
        y = y + vector.getY();
        z = z + vector.getZ();
        return this;
    }

    public Vector subtractVectorFromVector(Vector vector) {
        x = x - vector.getX();
        y = y - vector.getY();
        z = z - vector.getZ();
        return this;
    }

    public Vector rotateXY(double degrees) {
        double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.x) - (Math.sin(angle) * this.y);
        Double y = (Math.sin(angle) * this.x) + (Math.cos(angle) * this.y);
        return new Vector(x, y, z);
    }

    public Vector rotateYZ(double degrees) {
        double angle = Math.toRadians(degrees);
        Double y = (Math.cos(angle) * this.y) - (Math.sin(angle) * this.z);
        Double z = (Math.sin(angle) * this.y) + (Math.cos(angle) * this.z);
        return new Vector(x, y, z);
    }

    public Vector rotateXZ(double degrees) {
        double angle = Math.toRadians(degrees);
        Double x = (Math.cos(angle) * this.x) + (Math.sin(angle) * this.z);
        Double z = (Math.cos(angle) * this.z) - (Math.sin(angle) * this.x);
        return new Vector(x, y, z);
    }

    public Vector scale(double s0, double s1, double s2) {
        x = x * s0;
        y = y * s1;
        z = z * s1;
        return this;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getZ() {
        return z;
    }
    
    public String stringValue() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
