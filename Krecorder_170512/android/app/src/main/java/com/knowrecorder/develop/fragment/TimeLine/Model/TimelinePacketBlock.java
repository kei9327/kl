package com.knowrecorder.develop.fragment.TimeLine.Model;

import android.graphics.drawable.Drawable;

import com.knowrecorder.Utils.PixelUtil;

/**
 * Created by we160303 on 2017-03-14.
 */

public class TimelinePacketBlock {
    private int leftGap;
    private int rightGap;
    private int width;
    private int type;
    private Drawable icon;
    private long mid;
    private boolean isBasicWidth;

    public TimelinePacketBlock(int leftGap, int rightGap, int width, int type, Drawable icon, long mid) {
        this.leftGap = leftGap;
        this.rightGap = rightGap;
        this.type = type;
        this.icon = icon;
        this.mid = mid;

        if(width == 0){
            this.width = (int)PixelUtil.getInstance().convertDpToPixel(30);
            isBasicWidth = true;
        }else{
            this.width = width;
            isBasicWidth = false;
        }
    }

    public int getLeftGap() {
        return leftGap;
    }

    public int getRightGap() {
        return rightGap;
    }

    public int getWidth() {
        return width;
    }

    public int getType() {
        return type;
    }

    public Drawable getIcon() {
        if(width < PixelUtil.getInstance().convertDpToPixel(30))
            return null;

        return icon;
    }

    public long getMid() {
        return mid;
    }

    public void addRightGap(int rightGap){
        this.rightGap += rightGap;
    }

    public boolean isBasicWidth(){
        return this.isBasicWidth;
    }
}
