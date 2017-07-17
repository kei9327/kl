package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-14.
 */

public class TransObjBody {

    int b;
    int e;
    long target;
    float dx;
    float dy;

    public TransObjBody(int b, int e, long target, float dx, float dy) {
        this.b = b;
        this.e = e;
        this.target = target;
        this.dx = dx;
        this.dy = dy;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public long getTarget() {
        return target;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }
}
