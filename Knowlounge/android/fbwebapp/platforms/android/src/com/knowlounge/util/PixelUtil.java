package com.knowlounge.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by ssyou on 2016-03-21.
 */
public class PixelUtil {
    private static PixelUtil ourInstance = new PixelUtil();
    private Context context = null;

    public static PixelUtil getInstance() {
        return ourInstance;
    }

    public float convertDpToPixel(float dp) {
        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = dp * (metrics.densityDpi / 160f);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return px;
    }

    public float pixelToDp(float pixel) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = pixel / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    private PixelUtil() {
    }

    public void setContext(Context context) {
        this.context = context;
    }
}