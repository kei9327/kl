package com.knowrecorder.Timeline;

public class BlockData {
    private int width;
    private int color;
    private int type;
    private int gap;
    private int rightMargin = 0;
    private long downTime;
    private long upTime;

    public BlockData(int width, int color, int type, int gap) {
        this.width = width;
        this.color = color;
        this.type = type;
        this.gap = gap;
    }

    public BlockData(int width, int color, int type, int gap, long downTime, long upTime) {
        this.width = width;
        this.color = color;
        this.type = type;
        this.gap = gap;
        this.downTime = downTime;
        this.upTime = upTime;
    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public long getDownTime() {
        return downTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public long getUpTime() {
        return upTime;
    }
}
