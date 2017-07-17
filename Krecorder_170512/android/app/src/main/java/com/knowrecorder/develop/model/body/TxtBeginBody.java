package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-17.
 */

public class TxtBeginBody {
    int b;
    int e;
    float beginx;
    float beginy;
    float endx;
    float endy;
    float w;
    float h;
    String content;

    public TxtBeginBody(int b, int e, float beginx, float beginy, float endx, float endy, float w, float h, String content) {
        this.b = b;
        this.e = e;
        this.beginx = beginx;
        this.beginy = beginy;
        this.endx = endx;
        this.endy = endy;
        this.w = w;
        this.h = h;
        this.content = content;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public float getBeginx() {
        return beginx;
    }

    public float getBeginy() {
        return beginy;
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

    public String getContent() {
        return content;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setBeginx(float beginx) {
        this.beginx = beginx;
    }

    public void setBeginy(float beginy) {
        this.beginy = beginy;
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

    public void setContent(String content) {
        this.content = content;
    }
}
