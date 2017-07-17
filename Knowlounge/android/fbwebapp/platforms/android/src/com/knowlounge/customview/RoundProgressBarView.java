package com.knowlounge.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.knowlounge.R;

/**
 * Created by we160303 on 2016-06-07.
 */
public class RoundProgressBarView extends View {
    /**
     * Brush object reference
     */
    private Paint paint;

    /**
     * The colors of the rings
     */
    private int roundColor;

    /**
     * The progress of the color ring
     */
    private int roundProgressColor;

    /**
     * The middle of the string progress percentage color
     */
    private int textColor;

    /**
     * The middle of the string progress percentage font
     */
    private float textSize;

    /**
     * Ring width
     */
    private float roundWidth;

    /**
     * If the cover ring, the only effective use when filling is filled, whether has the outer ring, 1 expressed reservations, 0 said they did not keep
     */
    private int keepRoundType;
    public static final int KEEP = 1;
    public static final int NOT_KEEP = 0;


    /**
     * The biggest progress
     */
    private int max;

    /**
     * The current progress
     */
    private int progress;
    /**
     * Display intermediate schedule
     */
    private boolean textIsDisplayable;

    /**
     * The progress of the style, solid or hollow
     */
    private int style;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RoundProgressBarView(Context context) {
        this(context, null);
    }

    public RoundProgressBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();


        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        //Get a custom attribute and the default value
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
        textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.GREEN);
        textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 20);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
        keepRoundType = mTypedArray.getInt(R.styleable.RoundProgressBar_keepRoundType, 0);//Keep the outer ring is filled circle
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 30);
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
        style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);

        mTypedArray.recycle();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * Painting the outermost ring
         */
        int centre = getWidth()/2; //X coordinates acquisition center
        int radius = (int) (centre - roundWidth/2); //The radius of the ring
        paint.setColor(roundColor); //Set the colors of the rings
        paint.setStyle(Paint.Style.STROKE); //A hollow
        paint.setStrokeWidth(roundWidth); //Set the ring width
        paint.setAntiAlias(true);  //Antialiasing
        canvas.drawCircle(centre, centre, radius, paint); //Draw a circle

        //Log.e("log", centre + "");






        int percent = (int)(((float)progress / (float)max) * max);  //Percentage of completion in the middle, first converted to float in a division operation, or 0

        String prgressLabelTxt = "남은 시간";
        String progressText = String.format(getResources().getString(R.string.poll_wait_time), (max-percent)/100);

        float subTextWidth = paint.measureText("남은 시간");           // Measure the font width, we need according to the font width in the middle ring
        float progressTextWidth = paint.measureText(progressText);   // Measure the font width, we need according to the font width in the middle ring

        // 프로그레스 라벨 텍스트 스타일 정의
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT); //Set the font
        canvas.drawText(prgressLabelTxt, centre - subTextWidth / 2, centre-textSize, paint); //Draw the percentage

        // 프로그레스 텍스트 스타일 정의
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD); //Set the font
        if(textIsDisplayable && percent != 0 && style == STROKE){
            canvas.drawText(progressText, centre-progressTextWidth / 2, centre+textSize, paint); //Draw the percentage
        }

        /**
         * In an arc, draw ring schedule
         */

        //Setup schedule is solid or hollow
        paint.setStrokeWidth(roundWidth); //Set the ring width
        paint.setColor(roundProgressColor);  //The progress of the color settings

        RectF oval = null;
        if(keepRoundType == KEEP){   //Retaining ring
            radius -= roundWidth;
        }

        oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius);  //Used to define the shape and the size of the circular boundaries

        switch (style) {
            case STROKE :
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(oval, -90, 360 * progress / max, false, paint);  //According to the progress of an arc
                break;

            case FILL :
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0)
                    canvas.drawArc(oval, -90, 360 * progress / max, true, paint);  //According to the progress of an arc
                break;

        }

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


    public int getCricleColor() {
        return roundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }



}