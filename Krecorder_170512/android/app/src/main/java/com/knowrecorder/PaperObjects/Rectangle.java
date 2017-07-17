package com.knowrecorder.PaperObjects;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

import com.knowrecorder.Toolbox.Toolbox;

/**
 * Created by ssyou on 2016-02-01.
 */
public class Rectangle extends PaperObject {

    private Paint paint;
    private Path path = new Path();

    public Rectangle(Context context) {
        super(context);
    }

    public Rectangle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initView() {
        paint = new Paint();
        paint.setColor(Toolbox.getInstance().currentShapeColor);
        paint.setStyle(Paint.Style.FILL);
    }

    public void initView(int color) {
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!isDeleted) {
            path.reset();
            canvas.save();

            Log.d("onDraw", "Rectangle");
            float[] srcPoint = new float[2];
            float[] dstPoint = new float[2];

            srcPoint[0] = startX;
            srcPoint[1] = startY;
            dstPoint[0] = endX;
            dstPoint[1] = endY;

            path.moveTo(srcPoint[0], srcPoint[1]);
            path.lineTo(dstPoint[0], srcPoint[1]);
            path.lineTo(dstPoint[0], dstPoint[1]);
            path.lineTo(srcPoint[0], dstPoint[1]);
            path.lineTo(srcPoint[0], srcPoint[1]);

            canvas.drawPath(path, paint);
            canvas.restore();
        }
    }
}
