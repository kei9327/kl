package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-16.
 */

public class VideoBody {
    float scale;
    float originx;
    float originy;
    float endx;
    float endy;
    float w;
    float h;
    float videoprogress;
    float volume;

    public VideoBody(float scale, float originx, float originy, float endx, float endy, float w, float h, float videoprogress, float volume) {
        this.scale = scale;
        this.originx = originx;
        this.originy = originy;
        this.endx = endx;
        this.endy = endy;
        this.w = w;
        this.h = h;
        this.videoprogress = videoprogress;
        this.volume = volume;
    }

    public float getScale() {
        return scale;
    }

    public float getOriginx() {
        return originx;
    }

    public float getOriginy() {
        return originy;
    }

    public float getEndx() {
        return endx;
    }

    public float getEndy() {
        return endy;
    }

    public float getW() {
        return w;
    }

    public float getH() {
        return h;
    }

    public float getVideoprogress() {
        return videoprogress;
    }

    public float getVolume() {
        return volume;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setOriginx(float originx) {
        this.originx = originx;
    }

    public void setOriginy(float originy) {
        this.originy = originy;
    }

    public void setEndx(float endx) {
        this.endx = endx;
    }

    public void setEndy(float endy) {
        this.endy = endy;
    }

    public void setW(float w) {
        this.w = w;
    }

    public void setH(float h) {
        this.h = h;
    }

    public void setVideoprogress(float videoprogress) {
        this.videoprogress = videoprogress;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
