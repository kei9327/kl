package com.knowrecorder.Widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class WCover extends View {
    private GestureDetectorCompat mGestureDetector;

    private Bitmap mBitmap = null;
    private Paint mBgPaint = new Paint();
    private boolean mUseDimmedEffect = false;
    private boolean mUseBlurEffect = false;

    public WCover(Context context) {
        super(context);
        initializeMe(context);
    }

    public WCover(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WCover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeMe(context);
    }

    private void initializeMe(Context context) {
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mGestureDetector.setOnDoubleTapListener(null);

        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setFilterBitmap(true);
    }

    public void setDimmedEffect(boolean value) {
        mUseDimmedEffect = value;
        invalidate();
    }

    public void setBlur(boolean value) {
        mUseBlurEffect = value;

        if (mUseBlurEffect) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            MaskFilter blurFilter = new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL);
            mBgPaint.setMaskFilter(blurFilter);
            mBgPaint.setColor(Color.BLUE);
        }
    }

    public void releaseBitmap() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            int w = this.getMeasuredWidth();
            int h = this.getMeasuredHeight();

            Rect rect = new Rect(0, 0, w, h);
            Rect srcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());

            Paint paint = null;
            if (mUseBlurEffect)
                paint = mBgPaint;

            canvas.drawBitmap(mBitmap, srcRect, rect, paint);
        } else {
            super.onDraw(canvas);
        }
    }

    public void setGestureListener(GestureDetector.SimpleOnGestureListener listener) {
        mGestureListener = listener;
    }

    public void setGestureDetector(GestureDetectorCompat detector) {
        mGestureDetector = detector;
    }

    public void setOnDoubleTapListner(GestureDetector.OnDoubleTapListener listener) {
        if (mGestureDetector != null)
            mGestureDetector.setOnDoubleTapListener(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }

        return false;
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

    };
}
