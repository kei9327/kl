package com.knowrecorder.develop.model.realm;

import io.realm.RealmObject;

/**
 * Created by we160303 on 2017-02-03.
 */

public class Page extends RealmObject {

    private long id;
    private long noteid;
    private int pagenum;
    private float runtime;
    private boolean isStatic;
    private boolean isPDFPage;
    private float scale;
    private float focalPointX;
    private float focalPointY;

    public long getId() {
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

    public long getNoteid() {
        return noteid;
    }

    public float getRuntime() {
        return runtime;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isPDFPage() {
        return isPDFPage;
    }

    public void setId(long id) {
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

    public void setNoteid(long noteid) {
        this.noteid = noteid;
    }

    public void setRuntime(float runtime) {
        this.runtime = runtime;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public void setPDFPage(boolean PDFPage) {
        isPDFPage = PDFPage;
    }

    public void setFocalPointY(float focalPointY) {
        this.focalPointY = focalPointY;
    }

}
