package com.knowrecorder.develop.papers.PaperObjects.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.develop.papers.PaperObjects.ViewObject;

/**
 * Created by Changha on 2017-02-06.
 */

public class Circle extends ViewObject {

    private Paint paint;
    private Path mPath;

    public Circle(Context context) {
        super(context);
        mPath = new Path();
        isMovable = true;
        isScalable = true;
        maxScale = 5.0f;
        minScale = 0.1f;
    }

    public void initView(long id, float posX, float posY, float width, float height) {

        mid = id;

        paint = new Paint();
        paint.setColor(Toolbox.getInstance().currentShapeColor);
        paint.setStyle(Paint.Style.FILL);

        mPosX = posX;
        mPosY = posY;

        drawingOval(width, height);
        moveTo(mPosX, mPosY);
    }

    public void initView(int color) {
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawingOval(float w, float h){
        RectF rectF = new RectF();
        rectF.set(0, 0, w, h);

        mPath.reset();
        mPath.addOval(rectF,Path.Direction.CW);
        setRegion(mPath);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingOval(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d("onDraw", "Circle");
        canvas.save();

        canvas.drawPath(mPath, paint);

        canvas.restore();
    }


}

