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

public class Triangle extends ViewObject {

    private Paint paint;
    private Path mPath;
    private boolean swap;

    public Triangle(Context context)
    {
        super(context);
        mPath = new Path();

        isMovable = true;
        isScalable = true;
        maxScale = 5.0f;
        minScale = 0.1f;
    }

    public void initView(long id, float posX, float posY, float width, float height, boolean swap) {

        mid = id;
        
        paint = new Paint();
        paint.setColor(Toolbox.getInstance().currentShapeColor);
        paint.setStyle(Paint.Style.FILL);
        this.swap = swap;

        mPosX = posX;
        mPosY = posY;
        drawingTriangle(width, height);
        moveTo(mPosX, mPosY);
    }

    public void initView(int color) {
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawingTriangle(float w, float h){
        float[] vertexTop = new float[2];
        float[] vertexLeft = new float[2];
        float[] vertexRight = new float[2];

        vertexTop[0] = w / 2;
        vertexTop[1] = swap ? h : 0;

        vertexLeft[0] = swap ? w : 0;
        vertexLeft[1] = swap ? 0 : h;

        vertexRight[0] = swap ? 0 : w;
        vertexRight[1] = swap ? 0 : h;


        mPath.reset();
        mPath.moveTo(vertexTop[0], vertexTop[1]);        // Vertex Top
        mPath.lineTo(vertexLeft[0], vertexLeft[1]);      // Vertex Top -> Vertex Left
        mPath.lineTo(vertexRight[0], vertexRight[1]);    // Vertex Left -> Vertext Right
        mPath.lineTo(vertexTop[0], vertexTop[1]);        // Vertext Right -> Vertex Top

        setRegion(mPath);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingTriangle(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();

        Log.d("onDraw", "Circle");
        canvas.drawPath(mPath, paint);
        canvas.restore();
    }
}
