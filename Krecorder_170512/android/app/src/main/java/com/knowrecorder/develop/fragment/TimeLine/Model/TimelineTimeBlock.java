package com.knowrecorder.develop.fragment.TimeLine.Model;

/**
 * Created by we160303 on 2017-03-14.
 */

public class TimelineTimeBlock {
    private int leftGap;
    private int rightGap;
    private String time;

    public TimelineTimeBlock(int leftGap, int rightGap, String time) {
        this.leftGap = leftGap;
        this.rightGap = rightGap;
        this.time = time;
    }

    public int getLeftGap() {
        return leftGap;
    }

    public int getRightGap() {
        return rightGap;
    }

    public String getTime() {
        return time;
    }

    public void addRightGap(int rightGap){
        this.rightGap += rightGap;
    }
}
