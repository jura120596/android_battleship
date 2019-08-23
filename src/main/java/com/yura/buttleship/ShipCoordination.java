package com.yura.buttleship;

public class ShipCoordination implements Comparable{
    private int  x;
    private int y;
    public ShipCoordination(int x, int y){
        setX(x);
        setY(y);
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

    @Override
    public int compareTo(Object o) {
        return  this.getX() == ((ShipCoordination)o).getX() && this.getY() == ((ShipCoordination)o).getY() ? 0 : 1;
    }
    public boolean equals(Object o){
        return this.getX() == ((ShipCoordination)o).getX() && this.getY() == ((ShipCoordination)o).getY();
    }
    public  String toString(){
        return getX() + " " + getY();
    }
}