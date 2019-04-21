package ui;

public class Segment {
    private int number;
    private int density;
    private int x;
    private int y;

    public Segment(int number, int x, int y) {
        this.number = number;
        this.x = x;
        this.y = y;
        density = 0;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDensity() {
        return density;
    }

    public void setDensity(int density) {
        this.density = density;
    }
}
