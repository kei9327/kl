package com.knowrecorder.Timeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;

public class TimelineBlock extends View {
    private Bitmap canvasBitmap = null;
    private Canvas drawCanvas;
    private Paint canvasPaint;
    private boolean isRed = false;
    private final static int DP2_PIXEL = (int) PixelUtil.getInstance().convertDpToPixel(2);
    private final static int HEIGHT_DP = (int) PixelUtil.getInstance().convertDpToPixel(30);

    public TimelineBlock(Context context) {
        super(context);
    }

    public TimelineBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimelineBlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void drawBlock(int width, int color, int type) {
        canvasBitmap = Bitmap.createBitmap(width, HEIGHT_DP, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        RectF rectF = new RectF();
        rectF.set(0, 0, width, HEIGHT_DP);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(color);

        drawCanvas.drawRoundRect(rectF, DP2_PIXEL, DP2_PIXEL, paint);

        paint.setStyle(Paint.Style.STROKE);
        if (isRed) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.BLACK);
        }
        Bitmap icon = null;

        switch (type) {
            case BlockType.MOVE:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_hand);
                break;

            case BlockType.SCALE:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_hand);
                break;

            case BlockType.PEN:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_pen);
                break;

            case BlockType.ERASE:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_eraser);
                break;

            case BlockType.TEXT:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_text);
                break;

            case BlockType.VIDEO:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_movie);
                break;

            case BlockType.IMAGE:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_image);
                break;

            case BlockType.SHAPE:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_shape);
                break;

            case BlockType.PDF:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_pdf);
                break;

            case BlockType.NEW_PAGE:
            case BlockType.PREV_PAGE:
            case BlockType.NEXT_PAGE:
                break;

            case BlockType.START_PAUSE:
                break;

            case BlockType.REMOVE:
                break;

            case BlockType.AUDIO:
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_trackbar_sound);
                break;
        }

        if (icon != null) {
            if (width > icon.getWidth()) {
                drawCanvas.drawBitmap(icon, (int) (DP2_PIXEL / 2), (int) (DP2_PIXEL / 2), null);
            }
        }
        drawCanvas.drawRoundRect(rectF, DP2_PIXEL, DP2_PIXEL, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
    }
}
