package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-14.
 */

public class PointEndBody {
    boolean isEraser;
    int b ;
    int e ;
    float x ;
    float y ;
    float stkwidth ;

    public PointEndBody(boolean iseraser, int b, int e, float x, float y, float stkwidth) {
        this.isEraser = iseraser;
        this.b = b;
        this.e = e;
        this.x = x;
        this.y = y;
        this.stkwidth = stkwidth;
    }
    public  boolean getIsEraser() { return isEraser; }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getStkwidth() {
        return stkwidth;
    }

    public void setIsEraser(boolean iseraser){ this.isEraser = iseraser ; }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setStkwidth(float stkwidth) {
        this.stkwidth = stkwidth;
    }

    @Override
    public String toString() {
        return "[ b : "+b+" e : "+e+" x : "+x+" y : "+y+" stkwidth : "+stkwidth+" ]";
    }
}
