package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-15.
 */

public class ImageBody {
    private float scale;
    private float originx;
    private float originy;
    private float endx;
    private float endy;
    private float w;
    private float y;

    public ImageBody(float scale, float originx, float originy, float endx, float endy, float w, float y) {
        this.scale = scale;
        this.originx = originx;
        this.originy = originy;
        this.endx = endx;
        this.endy = endy;
        this.w = w;
        this.y = y;
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

    public float getY() {
        return y;
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

    public void setY(float y) {
        this.y = y;
    }
}
