package com.knowrecorder.develop.fragment.TimeLine.Model;

/**
 * Created by we160303 on 2017-03-14.
 */

public class TImelineAudioBlock {
    int leftGap;
    int rightGap;
    int width;
    int duration;

    public TImelineAudioBlock(int leftGap, int rightGap, int width, int duration) {
        this.leftGap = leftGap;
        this.rightGap = rightGap;
        this.width = width;
        this.duration = duration;
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

    public int getDuration() {
        return duration;
    }

    public void setLeftGap(int leftGap) {
        this.leftGap = leftGap;
    }

    public void addRightGap(int rightGap) {
        this.rightGap += rightGap;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
