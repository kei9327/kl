package com.knowrecorder.develop.model.body;

import com.knowrecorder.Toolbox.Toolbox;

/**
 * Created by we160303 on 2017-02-13.
 */

public class LaserBody {
    float x ;
    float y ;
    int ico ;
    int pointertag ;
    int b;
    int e;

    public LaserBody(float x, float y, int ico, int pointertag, int b, int e) {
        this.x = x;
        this.y = y;
        this.ico = ico;
        this.pointertag = pointertag;
        this.b = b;
        this.e = e;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getIco() {
        return ico;
    }

    public int getPointertag() {
        return pointertag;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setIco(int ico) {
        this.ico = ico;
    }

    public void setPointertag(int pointertag) {
        this.pointertag = pointertag;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "[ x : "+x+" y : "+y+" ico : "+ico+" pointertag : "+pointertag+" b : "+b+" e : "+e+" ]";
    }
}
