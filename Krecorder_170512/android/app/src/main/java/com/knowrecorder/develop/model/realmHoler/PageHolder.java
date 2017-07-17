package com.knowrecorder.develop.model.realmHoler;


/**
 * Created by we160303 on 2017-02-03.
 */

public class PageHolder{

    private int id;
    private int pagenum;
    private float scale;
    private float focalPointX;
    private float focalPointY;

    public int getId() {
        return id;
    }

    public int getPagenum() {
        return pagenum;
    }

    public float getScale() {
        return scale;
    }

    public float getFocalPointX() {
        return focalPointX;
    }

    public float getFocalPointY() {
        return focalPointY;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setFocalPointX(float focalPointX) {
        this.focalPointX = focalPointX;
    }

    public void setFocalPointY(float focalPointY) {
        this.focalPointY = focalPointY;
    }
}
