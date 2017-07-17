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
public class Triangle extends PaperObject {

    private Paint paint;
    private Path path = new Path();

    public Triangle(Context context) {
        super(context);
    }

    public Triangle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final static int X = 0;
    private final static int Y = 1;

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

            Log.d("onDraw", "Circle");

            float[] vertexTop = new float[2];
            float[] vertexLeft = new float[2];
            float[] vertexRight = new float[2];

            float tmp;
            float startX = this.startX, endX = this.endX, startY = this.startY, endY = this.endY;

            if (ySwapped) {
                tmp = startX;
                startX = endX;
                endX = tmp;

                tmp = startY;
                startY = endY;
                endY = tmp;
            }

            vertexTop[X] = (startX + endX) / 2;
            vertexTop[Y] = startY;

            vertexLeft[X] = startX;
            vertexLeft[Y] = endY;

            vertexRight[X] = endX;
            vertexRight[Y] = endY;

            path.moveTo(vertexTop[X], vertexTop[Y]);        // Vertex Top
            path.lineTo(vertexLeft[X], vertexLeft[Y]);      // Vertex Top -> Vertex Left
            path.lineTo(vertexRight[X], vertexRight[Y]);    // Vertex Left -> Vertext Right
            path.lineTo(vertexTop[X], vertexTop[Y]);        // Vertext Right -> Vertex Top

            canvas.drawPath(path, paint);
            canvas.restore();
        }
    }
}
