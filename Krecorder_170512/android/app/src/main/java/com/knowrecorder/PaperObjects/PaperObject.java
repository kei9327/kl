package com.knowrecorder.PaperObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ssyou on 2016-02-01.
 */
public class PaperObject extends View {

    protected float startX;
    protected float startY;
    protected float endX;
    protected float endY;

    protected float originStartX;
    protected float originStartY;
    protected float originEndX;
    protected float originEndY;

    protected int index;
    protected boolean isDeleted = false;
    //    @Deprecated
    public boolean isPdf = false;

    protected boolean ySwapped;

    public PaperObject(Context context) {
        super(context);
    }

    public PaperObject(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCoordinates(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public void setOriginCoordinates(float startX, float startY, float endX, float endY) {
        this.originStartX = startX;
        this.originStartY = startY;
        this.originEndX = endX;
        this.originEndY = endY;
    }

    public float[] getCoordinates() {
        float[] pts = new float[]{startX, startY, endX, endY};
        return pts;
    }

    public float[] getOriginCoordinates() {
        float[] pts = new float[]{originStartX, originStartY, originEndX, originEndY
        };
        return pts;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isInThisArea(float pointX, float pointY, boolean isAutoLoad) {
        float startX = this.startX, endX = this.endX, startY = this.startY, endY = this.endY;

        if (startX <= pointX && startY <= pointY
                && endX >= pointX && endY >= pointY) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        isDeleted = true;
    }

    public void setySwapped(boolean ySwapped) {
        this.ySwapped = ySwapped;
    }
}
