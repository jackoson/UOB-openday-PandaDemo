import java.util.*;

public class Vector {
    
    private int x, y, z;
    private List<Vector> vectors;
    
    public Vector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vectors = new ArrayList<Vector>();
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
        int x = (int) Math.round((Math.cos(angle) * this.x) - (Math.sin(angle) * this.y));
        int y = (int) Math.round((Math.sin(angle) * this.x) + (Math.cos(angle) * this.y));
        return new Vector(x, y, z);
    }
    
    public Vector rotateYZ(double degrees) {
        double angle = Math.toRadians(degrees);
        int y = (int) Math.round((Math.cos(angle) * this.y) - (Math.sin(angle) * this.z));
        int z = (int) Math.round((Math.sin(angle) * this.y) + (Math.cos(angle) * this.z));
        return new Vector(x, y, z);
    }
    
    public Vector rotateXZ(double degrees) {
        double angle = Math.toRadians(degrees);
        int x = (int) Math.round((Math.cos(angle) * this.x) + (Math.sin(angle) * this.z));
        int z = (int) Math.round((Math.cos(angle) * this.z) - (Math.sin(angle) * this.x));
        return new Vector(x, y, z);
    }
    
    public Vector scale(double s0, double s1, double s2) {
        x = (int) Math.round(x * s0);
        y = (int) Math.round(y * s1);
        z = (int) Math.round(z * s1);
        return this;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public void addVector(Vector vector) {
        vectors.add(vector);
    }
    
    public List<Vector> getVectors() {
        return vectors;
    }
    
}