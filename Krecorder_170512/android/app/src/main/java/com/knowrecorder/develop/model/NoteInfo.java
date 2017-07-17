package com.knowrecorder.develop.model;

/**
 * Created by we160303 on 2017-02-21.
 */

public class NoteInfo {
    String platform;
    float width;
    float height;
    float density;

    public NoteInfo(String platform, float width, float height, float density) {
        this.platform = platform;
        this.width = width;
        this.height = height;
        this.density = density;
    }

    public String getPlatform() {
        return platform;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDensity() {
        return density;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setDensity(float density) {
        this.density = density;
    }
}
