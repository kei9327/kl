package com.knowrecorder.Widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.knowrecorder.Events.EraserStrokeChanged;
import com.knowrecorder.Events.PenStrokeChanged;

public class StrokePreview extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect mRect = null;
    private Context context;

    public StrokePreview(Context context) {
        super(context);
        this.context = context;
        initializeMe();
    }

    public StrokePreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initializeMe();
    }

    private void initializeMe() {
        float defaultWidth = convertDpToPixel(10);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(defaultWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(255);

        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        mShadowPaint.setColor(Color.argb(70, 0, 0, 0));

        mClearPaint.setStyle(Paint.Style.FILL);
        mClearPaint.setStrokeCap(Paint.Cap.ROUND);
        mClearPaint.setStrokeJoin(Paint.Join.MITER);
        mClearPaint.setColor(Color.WHITE);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRect = new Rect(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float y = mRect.height() / 2;
        float one_dp = convertDpToPixel(1);


//        canvas.drawCircle(Util.convertDpToPixel(17) + one_dp,
//                y + one_dp,
//                mPaint.getStrokeWidth() / 2,
//                mShadowPaint);

//        canvas.drawCircle(Util.convertDpToPixel(17),
//                y,
//                mPaint.getStrokeWidth() / 2,
//                mClearPaint);
        canvas.drawCircle(convertDpToPixel(17),
                y,
                mPaint.getStrokeWidth() / 2,
                mPaint);


//        canvas.drawLine(Util.convertDpToPixel(37) + one_dp,
//                y + one_dp,
//                Util.convertDpToPixel(140) + one_dp,
//                y + one_dp,
//                mShadowPaint);
//        canvas.drawLine(Util.convertDpToPixel(37),
//                y,
//                Util.convertDpToPixel(140),
//                y,
//                mClearPaint);
        canvas.drawLine(convertDpToPixel(37),
                y,
                convertDpToPixel(140),
                y,
                mPaint);
    }

    public void setStrokeWidth(int width) {
        float widthInDP = convertDpToPixel(width);
        mPaint.setStrokeWidth(widthInDP);
        mShadowPaint.setStrokeWidth(widthInDP);
        mClearPaint.setStrokeWidth(widthInDP);
    }

    public void setStrokeColor(int color) {
        mPaint.setColor(color);
    }

    public void setStrokeOpacity(int opacity) {
        mPaint.setAlpha(opacity);
    }

    // events
    public void onEvent(EraserStrokeChanged event) {
        setStrokeWidth(event.eraserWidth);

        invalidate();
    }

    public void onEvent(PenStrokeChanged event) {
        setStrokeWidth(event.strokeWidth);

        mPaint.setColor(event.strokeColor);
        mPaint.setAlpha(event.strokeOpacity);

        invalidate();
    }

    public float convertDpToPixel(float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}
