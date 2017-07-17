package com.knowrecorder.develop.papers.PaperObjects.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.knowrecorder.develop.papers.PaperObjects.ViewObject;

/**
 * Created by we160303 on 2017-02-24.
 */

public class PDFView extends ViewObject{

    private String pdfFile;
    private Bitmap bitmap = null;
    Path mPath;

    public PDFView(Context context) {
        super(context);
        mPath = new Path();
        isMovable = true;
        isScalable = true;
        maxScale = 10.0f;
        minScale = 0.1f;
    }

    public PDFView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView(long id, float posX, float posY, float width, float height) {
        mid = id;

        mPosX = posX;
        mPosY = posY;
        mWidth = width;
        mHeight = height;

        drawingRectangle(width, getHeight());
        moveTo(mPosX, mPosY);
    }

    private void drawingRectangle(float w, float h){
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
        drawingRectangle(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (bitmap != null) {
            RectF rectF = new RectF(0, 0, mWidth, mHeight);
//            canvas.drawColor(Color.RED);
            canvas.drawBitmap(bitmap, null, rectF, null);
        }
        canvas.restore();
    }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }
    public void setPdfFile(String pdfFile){
        this.pdfFile = pdfFile;
    }

    public void resetBitmap() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
