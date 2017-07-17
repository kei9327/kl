package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-16.
 */

public class VideoProgressingBody {
    int b;
    int e;
    long mid;
    float videoprogress;

    public VideoProgressingBody(int b, int e, long mid, float videoprogress) {
        this.b = b;
        this.e = e;
        this.mid = mid;
        this.videoprogress = videoprogress;
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

    public float getVideoprogress() {
        return videoprogress;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public void setVideoprogress(float videoprogress) {
        this.videoprogress = videoprogress;
    }
}
