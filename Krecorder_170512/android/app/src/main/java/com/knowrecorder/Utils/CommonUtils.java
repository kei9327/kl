package com.knowrecorder.Utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by we160303 on 2017-01-12.
 */

public class CommonUtils {
    public static float getDisplayRatio(Context context, float width, float height){
        float dWidth = getDisplaySize(context,0);
        float dHeight = getDisplaySize(context,1);
        float ratio;

        if(width > height) {
            ratio = width > dWidth ? dWidth / width : width / dWidth ;
        }else {
            ratio = height > dHeight ? dHeight / height : height / dHeight;
        }

        Log.d("PDFRendererV2", "ratio : " + ratio);
        Log.d("PDFRendererV2", "display width : " + dWidth + "  display height : "+ dHeight);

        return ratio;
    }
    public static float getDisplayRatio(Context context, float width, float height, float videoDensity, float resoluationRate){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dWidth = metrics.widthPixels * resoluationRate;
        float dHeight = metrics.heightPixels * resoluationRate;
        float dencity = metrics.density;

        float ratio;
        float densityRatio = videoDensity/dencity;

        width *= densityRatio;
        height *= densityRatio;

        if(width > height) {
            ratio = width > dWidth ? dWidth / width : width / dWidth ;
        }else {
            ratio = height > dHeight ? dHeight / height : height / dHeight;
        }

        Log.d("PDFRendererV2", "ratio : " + ratio);
        Log.d("PDFRendererV2", "display width : " + dWidth + "  display height : "+ dHeight);

        return ratio;
    }
    public static float getDisplayDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    public static float getDisplaySize(Context context, int type){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if(type == 0 )
            return metrics.widthPixels;
        else
            return metrics.heightPixels;
    }
}
