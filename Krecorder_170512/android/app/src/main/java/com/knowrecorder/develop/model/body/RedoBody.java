package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-28.
 */

public class RedoBody {

    int b;
    int e;
    long target;

    public RedoBody(int b, int e, long target) {
        this.b = b;
        this.e = e;
        this.target = target;
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

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
