package com.yura.buttleship;

import java.io.Serializable;

public class Target implements Serializable, Comparable{
    private int i;
    private int j;
    private String text;
    private int color;
    public Target(){}
    public Target(int i, int j) {
        setI(i);
        setJ(j);
    }

    public String toString(){
        return getI() + " "+ getJ();
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    @Override
    public int compareTo(Object o) {
        return this.getI() == ((Target)o).getI() && this.getJ() == ((Target)o).getJ()? 0 : 1;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}