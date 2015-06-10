public class Point {
    
    private int x, y, z;
    
    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point addVectorToPoint(Vector vector) {
        int x = this.x + vector.getX();
        int y = this.y + vector.getY();
        int z = this.z + vector.getZ();
        return new Point(x, y, z);
    }
    
    public Point subtractVectorFromPoint(Vector vector) {
        int x = this.x - vector.getX();
        int y = this.y - vector.getY();
        int z = this.z - vector.getZ();
        return this;
    }
    
    public Vector subtractPointFromPoint(Point point) {
        int x = this.x - point.getX();
        int y = this.y - point.getY();
        int z = this.z - point.getZ();
        return new Vector(x, y, z);
    }
    
    public void setPointToPoint(Point point) {
        x = point.getX();
        y = point.getY();
        z = point.getZ();
    }
    
    public void drawPoint() {
        System.out.println("(" + x + ", " + y + ", " + z + ")");
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
    
}