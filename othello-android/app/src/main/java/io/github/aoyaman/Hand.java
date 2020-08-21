package io.github.aoyaman;

public class Hand {
    private int x;
    private int y;
    private int point;

    public Hand(int x, int y, int point) {
        this.x = x;
        this.y = y;
        this.point = point;
    }
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public int getPoint() {
        return this.point;
    }
}
