package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-16.
 */

public class VolumeProgressingBody {
    int b;
    int e;
    float volumeprogress;
    long mid;

    public VolumeProgressingBody(int b, int e, float volumeprogress, long mid) {
        this.b = b;
        this.e = e;
        this.volumeprogress = volumeprogress;
        this.mid = mid;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public float getVolumeprogress() {
        return volumeprogress;
    }

    public long getMid() {
        return mid;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setVolumeprogress(float volumeprogress) {
        this.volumeprogress = volumeprogress;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }
}
