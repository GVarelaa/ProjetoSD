package SharedState;

import java.io.*;

public class Position {
    private int x;
    private int y;

    public Position() {
        this.x = 0;
        this.y = 0;
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position pos) {
        this.x = pos.getX();
        this.y = pos.getY();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Checks if the current position is in a given radius of a center position
     * @param center center of the radius
     * @param radius radius of circumference
     */
    public boolean inRadius(Position center, int radius){
        return this.distanceTo(center) <= (double)radius;
    }

    /**
     * Calculates the distance between two points (the current one and other)
     * @param other the other point
     * @return the distance (float) between these two points
     */
    public double distanceTo(Position other){
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public static Position deserialize(byte[] data) throws IOException {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        int x = is.readInt();
        int y = is.readInt();

        return new Position(x,y);
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(8);
        DataOutputStream os = new DataOutputStream(byteArray);

        os.writeInt(this.x);
        os.writeInt(this.y);

        return byteArray.toByteArray();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        sb.append("(").append(this.x);
        sb.append(",").append(this.y);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position that = (Position) o;
        return this.x == that.getX() && this.y == that.getY();
    }

    @Override
    public Position clone() {
        return new Position(this);
    }
}
