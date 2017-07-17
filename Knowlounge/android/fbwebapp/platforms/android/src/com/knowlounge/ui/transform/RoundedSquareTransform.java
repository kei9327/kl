package com.knowlounge.ui.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by Minsu on 2016-06-08.
 */
public class RoundedSquareTransform extends BitmapTransformation {

    public RoundedSquareTransform(Context context) {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool bitmapPool, Bitmap source, int width, int height) {
        Bitmap result = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Paint paintBg = new Paint();
        paintBg.setColor(Color.parseColor("#C8C8C8"));
        paintBg.setAntiAlias(true);

        canvas.drawRect(0, 0, source.getWidth(), source.getHeight(), paintBg);
        canvas.drawRect(5, 5, source.getWidth() - 5, source.getHeight() - 5, paint);

        return result;
    }

    @Override
    public String getId() {
        // Return some id that uniquely identifies your transformation.
        return this.getClass().getPackage().getName();
    }



}
