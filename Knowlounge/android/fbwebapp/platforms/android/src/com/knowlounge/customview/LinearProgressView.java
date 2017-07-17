package com.knowlounge.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;

/**
 * Created by Mansu on 2016-12-14.
 */

public class LinearProgressView extends View {

    private static final String TAG = "LinearProgressView";

    /**
     * The biggest progress
     */
    private int max;

    /**
     * The current progress
     */
    private int progress;

    /**
     * The middle of the string progress percentage color
     */
    private int textColor;

    /**
     * The middle of the string progress percentage font
     */
    private float textSize;

    private Paint paint;
    private Paint textPaint;
    private Paint iconPaint;

    private boolean isUnlimit = false;

    public LinearProgressView(Context context) {
        this(context, null);
    }

    public LinearProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LinearProgressBar);

        textColor = mTypedArray.getColor(R.styleable.LinearProgressBar_textColor, Color.GREEN);
        textSize = mTypedArray.getDimension(R.styleable.LinearProgressBar_textSize, 20 * KnowloungeApplication.density);

        textPaint = new Paint();
        textPaint.setStrokeWidth(0);

        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.DEFAULT); //Set the font

    }

    public void setIsUnlimit(boolean flag) {
        isUnlimit = flag;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();

        //int percent = (int)(((float)progress / (float)max) * max);  //Percentage of completion in the middle, first converted to float in a division operation, or 0

        int percent = (width * progress) / max;


        String progressText = isUnlimit ? getResources().getString(R.string.poll_btn_finish) : String.format(getResources().getString(R.string.poll_wait_time), (max-progress)/100);

        //Log.d("text", progressText);

        RectF rect = new RectF(0, 0, width - percent, 20 * KnowloungeApplication.density);

        RectF bgRect = new RectF(0, 0, width, 20 * KnowloungeApplication.density);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#f5f5f5"));
        canvas.drawRoundRect(bgRect, 13.f, 13.f, paint);  // 배경

        paint.setColor(Color.parseColor("#ffdc37"));
        paint.setAntiAlias(true);
        if (!isUnlimit)
            canvas.drawRoundRect(rect, 13.f, 13.f, paint);   // 프로그레스 바

        textPaint.setColor(isUnlimit ? Color.parseColor("#b4b4b4") : Color.parseColor("#ffffff"));
        textPaint.setAntiAlias(true);
        float x;
        float y;
        if (isUnlimit) {
            x = canvas.getWidth()/2 - (textPaint.measureText(progressText) / 2);
            y = 13.f * KnowloungeApplication.density;
            //x = x / KnowloungeApplication.density;

//            Log.d(TAG, getWidth() + "");
//            Log.d(TAG, canvas.getWidth() + " / " + textPaint.measureText(progressText));
            Log.d(TAG, x + "");

            Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_poll_unlimited);
            Bitmap mutableBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
//            mutableBitmap.setWidth((int) (20 * KnowloungeApplication.density));
//            mutableBitmap.setHeight((int) (20 * KnowloungeApplication.density));
            canvas.drawBitmap(mutableBitmap, x - (15 * KnowloungeApplication.density), 0.f, null);

            x = x + (15 * KnowloungeApplication.density);
        } else {
            x = 10 * KnowloungeApplication.density;
            y = 13 * KnowloungeApplication.density;
        }
        canvas.drawText(progressText, x, y, textPaint); // 남은 시간 텍스트 출력
    }


    public synchronized int getMax() {
        return max;
    }

    /**
     * Set the maximum schedule
     * @param max
     */
    public synchronized void setMax(int max) {
        if(max <0){
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * Getting progress need synchronization
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * Set schedule, this is the thread safety control, considering the multi line problem, need to synchronize
     * Refresh the interface called postInvalidate () can refresh in the non UI thread
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if(progress <0){
            throw new IllegalArgumentException("progress not less than 0");
        }
        if(progress > max){
            progress = max;
        }
        if(progress <= max){
            this.progress = progress;
            postInvalidate();
        }

    }
}
