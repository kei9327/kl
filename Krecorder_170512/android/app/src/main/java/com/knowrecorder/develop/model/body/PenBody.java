package com.knowrecorder.develop.model.body;

/**
 * Created by ChangHa on 2017-02-13.
 */

public class PenBody {
    boolean isEraser;
    float stkwidth;
    float angle;
    long color;
    int b;
    int e;
    float opacity;
    float[][] points;

    public PenBody(boolean iseraser ,float stkwidth, float angle, long color, int b, int e, float opacity, float[][] points) {
        this.isEraser = iseraser;
        this.stkwidth = stkwidth;
        this.angle = angle;
        this.color = color;
        this.b = b;
        this.e = e;
        this.opacity = opacity;
        this.points = points;
    }
    public boolean getIsEraser() { return isEraser; }

    public float getStkwidth() {
        return stkwidth;
    }

    public float getAngle() {
        return angle;
    }

    public long getColor() {
        return color;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public float getOpacity() {
        return opacity;
    }

    public float[][] getPoints() {
        return points;
    }

    public void setIsEraser(boolean iseraser) { this.isEraser = iseraser; }

    public void setStkwidth(float stkwidth) {
        this.stkwidth = stkwidth;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setColor(long color) {
        this.color = color;
    }

    public void setB(short b) {
        this.b = b;
    }

    public void setE(short e) {
        this.e = e;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setPoints(float[][] points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "[ stkwidth : "+stkwidth+" angle : "+angle+" color : "+color+" b : "+b+" e : "+e+" opacity : "+opacity+" points : "+points+" ]";
    }
}
