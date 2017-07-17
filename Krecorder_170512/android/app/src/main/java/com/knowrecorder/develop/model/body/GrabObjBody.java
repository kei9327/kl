package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-14.
 */

public class GrabObjBody {
    int b;
    int e;
    long target;
    boolean grabbed;

    public GrabObjBody(int b, int e, long target, boolean grabbed) {
        this.b = b;
        this.e = e;
        this.target = target;
        this.grabbed = grabbed;
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

    public boolean isGrabbed() {
        return grabbed;
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

    public void setGrabbed(boolean grabbed) {
        this.grabbed = grabbed;
    }
}
