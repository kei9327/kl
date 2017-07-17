package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-16.
 */

public class VideoPauseBody {
    int b;
    int e;
    long mid;
    float endvalue;
    long totalvideotime;

    public VideoPauseBody(int b, int e, long mid, float endvalue, long totalvideotime) {
        this.b = b;
        this.e = e;
        this.mid = mid;
        this.endvalue = endvalue;
        this.totalvideotime = totalvideotime;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public long getMid() {
        return mid;
    }

    public float getEndvalue() {
        return endvalue;
    }

    public long getTotalvideotime() {
        return totalvideotime;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public void setEndvalue(float endvalue) {
        this.endvalue = endvalue;
    }

    public void setTotalvideotime(long totalvideotime) {
        this.totalvideotime = totalvideotime;
    }
}
