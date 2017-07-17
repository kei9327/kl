package com.knowrecorder.develop.papers.PaperObjects.shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.develop.papers.PaperObjects.ViewObject;

/**
 * Created by Changha on 2017-02-06.
 */

public class Rectangle extends ViewObject {

    private Paint paint;
    private Path mPath;

    public Rectangle(Context context, Path path) {
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
        drawingRectangel(width, height);
        moveTo(mPosX, mPosY);
    }

    public void initView(int color) {
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawingRectangel(float w, float h){
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(w, 0);
        mPath.lineTo(w, h);
        mPath.lineTo(0, h);
        mPath.lineTo(0, 0);
        mPath.lineTo(0, 0);

        setRegion(mPath);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingRectangel(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Log.d("onDraw", "Rectangle");
        canvas.save();
        canvas.drawPath(mPath, paint);
        canvas.restore();
    }
}
