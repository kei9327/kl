package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-14.
 */

public class ResizeObjBody {
    int b;
    int e;
    long target;
    float scale;

    public ResizeObjBody(int b, int e, long target, float scale) {
        this.b = b;
        this.e = e;
        this.target = target;
        this.scale = scale;
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

    public float getScale() {
        return scale;
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

    public void setScale(float scale) {
        this.scale = scale;
    }
}
