package com.knowrecorder.RealmHolder;

public class VideoHolder {
    private int id = -1;
    private String event;
    private int msec;
    private float touchX;
    private float touchY;

    private float btnTop;
    private float videoTop;

    private float btnLeft;
    private float videoLeft;

    private float paddingLeft;
    private float paddingTop;
    private float paddingRight;
    private float paddingBottom;

    private String filePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTouchX() {
        return touchX;
    }

    public void setTouchX(float touchX) {
        this.touchX = touchX;
    }

    public float getTouchY() {
        return touchY;
    }

    public void setTouchY(float touchY) {
        this.touchY = touchY;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public float getBtnTop() {
        return btnTop;
    }

    public void setBtnTop(float btnTop) {
        this.btnTop = btnTop;
    }

    public float getVideoTop() {
        return videoTop;
    }

    public void setVideoTop(float videoTop) {
        this.videoTop = videoTop;
    }

    public float getBtnLeft() {
        return btnLeft;
    }

    public void setBtnLeft(float btnLeft) {
        this.btnLeft = btnLeft;
    }

    public float getVideoLeft() {
        return videoLeft;
    }

    public void setVideoLeft(float videoLeft) {
        this.videoLeft = videoLeft;
    }

    public int getMsec() {
        return msec;
    }

    public void setMsec(int msec) {
        this.msec = msec;
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public VideoHolder clone() {
        VideoHolder videoHolder = new VideoHolder();
        videoHolder.setId(id);
        videoHolder.setEvent(event);
        videoHolder.setMsec(msec);
        videoHolder.setTouchX(touchX);
        videoHolder.setTouchY(touchY);
        videoHolder.setBtnTop(btnTop);
        videoHolder.setVideoTop(videoTop);
        videoHolder.setBtnLeft(btnLeft);
        videoHolder.setVideoLeft(videoLeft);
        videoHolder.setPaddingLeft(paddingLeft);
        videoHolder.setPaddingTop(paddingTop);
        videoHolder.setPaddingRight(paddingRight);
        videoHolder.setPaddingBottom(paddingBottom);
        videoHolder.setFilePath(filePath);

        return videoHolder;
    }
}
