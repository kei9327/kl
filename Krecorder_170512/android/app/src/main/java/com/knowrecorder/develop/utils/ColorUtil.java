package com.knowrecorder.develop.utils;

import android.graphics.Color;

/**
 * Created by we160303 on 2017-02-09.
 */

public class ColorUtil {
    public static long ARGBFromColor(int argb){
        long a = argb >>24 & 0xff;
        long r = argb >> 16 & 0xff;
        long g = argb >> 8 & 0xff;
        long b = argb & 0xff;

        return (r<<24)|(g<<16)|(b<<8)|a;
    }

    public static int colorFromRGBA(long rgba){
        int red = (int)rgba >> 24 & 0xff;
        int green = (int)rgba >> 16 & 0xff;
        int blue = (int)rgba >> 8 & 0xff;
        int alpha = (int)rgba & 0xff;

        return Color.argb(alpha, red, green, blue);
    }
}
