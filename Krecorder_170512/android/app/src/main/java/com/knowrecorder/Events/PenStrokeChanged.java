package com.knowrecorder.Events;

public class PenStrokeChanged {
    public int strokeWidth; // in pixel
    public int strokeColor;
    public int strokeOpacity; // 0 ~ 255

    public PenStrokeChanged(int w, int c, int o) {
        strokeWidth = w;
        strokeColor = c;
        strokeOpacity = o;
    }
}
