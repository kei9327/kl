package com.knowrecorder.PaperObjects;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import com.knowrecorder.Toolbox.Toolbox;

/**
 * Created by ssyou on 2016-02-01.
 */
public class Circle extends PaperObject {

    private Paint paint;

    public Circle(Context context) {
        super(context);
    }

    public Circle(Context context, AttributeSet attrs, int defStyleAttr) {
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
            canvas.save();

            Log.d("onDraw", "Circle");

            float radius = (endX > startX) ? (endX - startX) / 2  : (startX - endX) / 2;
            float cx = (startX + endX) / 2;
            float cy = (startY + endY) / 2;
            canvas.drawCircle(cx, cy, radius, paint);
            canvas.restore();
        }
    }
}
