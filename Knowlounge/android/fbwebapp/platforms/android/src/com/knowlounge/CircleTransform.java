package com.knowlounge;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.knowlounge.common.GlobalConst;
import com.squareup.picasso.Transformation;

/**
 * Created by Minsu on 2015-12-21.
 */
public class CircleTransform implements Transformation {

    private final int BORDER_RADIUS = 2;
    private int circleType;

    public CircleTransform(int circleType){
        this.circleType = circleType;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;

        // Prepare the background
        Paint paintBg = new Paint();
        if(circleType == GlobalConst.TYPE_CIRCLE_THUMB)
            paintBg.setColor(Color.WHITE);
        else
            paintBg.setColor(Color.parseColor("#96c81e"));
        paintBg.setAntiAlias(true);

        // Draw the background circle
//        canvas.drawCircle(r, r, r, paintBg);

        // Draw the image smaller than the background so a little border will be seen
//        canvas.drawCircle(r, r, r - BORDER_RADIUS, paint);

        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
