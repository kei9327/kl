package com.knowrecorder.Timeline;

/**
 * Created by ssyou on 2016-10-05.
 */

public class TimeTextBlock {
    private long time;
    private int rightMargin = 0;

    public TimeTextBlock(long time, int rightMargin) {
        this.time = time;
        this.rightMargin = rightMargin;
    }

    public TimeTextBlock(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }
}
