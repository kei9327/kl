package com.knowrecorder.Video;

import android.graphics.Bitmap;

/**
 * Created by ssyou on 2016-03-23.
 */
public class RecorderBitmap {
    public Bitmap bitmap;
    public boolean isThisEndPacket = false;
    public long timestamp;

    public RecorderBitmap(Bitmap bitmap, Boolean isThisEndPacket) {
        this(bitmap, isThisEndPacket, -1);
    }

    public RecorderBitmap(Bitmap bitmap, Boolean isThisEndPacket, long timestamp) {
        this.bitmap = bitmap;
        this.isThisEndPacket = isThisEndPacket;
        this.timestamp = timestamp;
    }
}
