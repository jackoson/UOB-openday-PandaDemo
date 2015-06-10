package client.aiview;

public class Point {

    private Double x, y, z;

    public Point(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point addVectorToPoint(Vector vector) {
        Double x = this.x + vector.getX();
        Double y = this.y + vector.getY();
        Double z = this.z + vector.getZ();
        return new Point(x, y, z);
    }

    public Point subtractVectorFromPoint(Vector vector) {
        Double x = this.x - vector.getX();
        Double y = this.y - vector.getY();
        Double z = this.z - vector.getZ();
        return this;
    }

    public Vector subtractPointFromPoint(Point point) {
        Double x = this.x - point.getX();
        Double y = this.y - point.getY();
        Double z = this.z - point.getZ();
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

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getZ() {
        return z;
    }

}
