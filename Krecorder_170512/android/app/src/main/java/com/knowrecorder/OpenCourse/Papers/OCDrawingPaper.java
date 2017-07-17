package com.knowrecorder.OpenCourse.Papers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.knowrecorder.Toolbox.Toolbox;

/**
 * Created by ssyou on 2016-01-29.
 */
public class OCDrawingPaper extends View {

    private final int POINTER_X = 0;
    private final int POINTER_Y = 1;
    private Path drawPath;
    private float[] pointerPts;
    private Paint drawPaint, canvasPaint, pointerPaint;
    private int paintColor;
    private int pointerColor = 0x77990000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Context context;
    private boolean isAlreadyDown = false;
    private float startX;
    private float startY;
    private Toolbox toolbox;

    public OCDrawingPaper(Context context, Toolbox toolbox) {
        super(context);
        this.context = context;
        this.toolbox = toolbox;
        initView();
    }

    public OCDrawingPaper(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public OCDrawingPaper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OCDrawingPaper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView();
    }

    public void initView() {
        drawPath = new Path();
        drawPaint = new Paint();
        pointerPaint = new Paint();
        pointerPts = new float[]{-999, -999};
        paintColor = toolbox.currentStrokeColor;

        drawPaint.setColor(paintColor);
        pointerPaint.setColor(pointerColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(toolbox.currentStrokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setDither(true);

        pointerPaint.setAntiAlias(true);
        pointerPaint.setStrokeWidth(100);
        pointerPaint.setStyle(Paint.Style.STROKE);
        pointerPaint.setStrokeJoin(Paint.Join.ROUND);
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);

        if (toolbox.getCurrentPointerBitmap() != null) {
            canvas.drawBitmap(toolbox.getCurrentPointerBitmap(), pointerPts[0], pointerPts[1], null);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void onActionDown(float touchX, float touchY, boolean isEraser) {
        if (Build.VERSION.SDK_INT >= 11 && isEraser) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            drawPaint.setStrokeWidth(toolbox.currentEraserWidth);
        } else {
            drawPaint.setColor(toolbox.currentStrokeColor);
            drawPaint.setStrokeWidth(toolbox.currentStrokeWidth);
        }

        drawPaint.setAlpha(toolbox.currentStrokeOpacity);
        drawPath.moveTo(touchX, touchY);
        startX = touchX;
        startY = touchY;
    }

    public void onActionMove(float touchX, float touchY) {
        drawPath.quadTo(startX, startY, (touchX + startX) / 2, (touchY + startY) / 2);
        startX = touchX;
        startY = touchY;
    }

    public void onActionUp(boolean isEraser) {
        drawPath.lineTo(startX, startY);
        drawCanvas.drawPath(drawPath, drawPaint);
        drawPath.reset();

        if (Build.VERSION.SDK_INT >= 11 && isEraser) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            drawPaint.setXfermode(null);
        }

        isAlreadyDown = false;
    }

    public void onPointerDown(float touchX, float touchY) {
        pointerPts[POINTER_X] = touchX;
        pointerPts[POINTER_Y] = touchY;
    }

    public void onPointerMove(float touchX, float touchY) {
        pointerPts[POINTER_X] = touchX;
        pointerPts[POINTER_Y] = touchY;
    }

    public void onPointerUp() {
        pointerPts[POINTER_X] = -999;
        pointerPts[POINTER_Y] = -999;

        isAlreadyDown = false;
    }

    public void clearCanvas() {
        drawPath.reset();
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.invalidate();
    }

    public void setToolbox(Toolbox toolbox) {
        this.toolbox = toolbox;
    }
}
