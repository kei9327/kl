package com.knowrecorder.develop.model.body;

import com.knowrecorder.Toolbox.Toolbox;

/**
 * Created by we160303 on 2017-02-13.
 */

public class PointMoveBody {
    int b ;
    int e ;
    float x ;
    float y ;
    float stkwidth ;

    public PointMoveBody(int b, int e, float x, float y, float stkwidth) {
        this.b = b;
        this.e = e;
        this.x = x;
        this.y = y;
        this.stkwidth = stkwidth;
    }

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
