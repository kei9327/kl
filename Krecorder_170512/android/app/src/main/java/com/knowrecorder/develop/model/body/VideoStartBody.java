package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-16.
 */

public class VideoStartBody {
    int b;
    int e;
    long mid;
    float startvalue;
    long totalvideotime;

    public VideoStartBody(int b, int e, long mid, float startvalue, long totalvideotime) {
        this.b = b;
        this.e = e;
        this.mid = mid;
        this.startvalue = startvalue;
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

    public float getStartvalue() {
        return startvalue;
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

    public void setStartvalue(float startvalue) {
        this.startvalue = startvalue;
    }

    public void setTotalvideotime(long totalvideotime) {
        this.totalvideotime = totalvideotime;
    }
}

