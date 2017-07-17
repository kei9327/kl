package com.knowlounge.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.knowlounge.R;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;

/**
 * Created by Minsu on 2015-12-21.
 */
public class PreviewCanvas extends View {

    private Paint painter;

    private Paint smallShapeFillPainter;
    private Paint smallShapeStrokePainter;
    private Paint bigShapeFillPainter;
    private Paint bigShapeStrokePainter;

    private Path previewPenPath;
    private String drawType;

    private float density;

    private int penWidth = 4;
    private int penAlpha = 100;
    private String penColor = "#0064fa";
    private float WIDTH_RADIO = 1.5f;


    private String eraserColor = "#DEDEDE";
    private int eraserWidth;
    private float ERASER_WIDTH_RADIO = 0.7f;

    private int shapeWidth = 4;
    private int shapeAlpha = 100;

    private String shapeType = "6";   // 5:직선, 6:사각형, 7:원
    private String shapeFillType = "0"; // 0 - Fill , 1 - Border

    private String lpenColor = "#0064fa";
    private String lpenBorderColor = "#0064fa";
    private String spenColor = "#0064fa";
    private String spenBorderColor = "#0064fa";
    private String cpenColor = "#0064fa";
    private String cpenBorderColor = "#0064fa";

    private int lpenWidth = 4;
    private int spenWidth = 4;
    private int cpenWidth = 4;

    private int lpenAlpha = 100;
    private int spenAlpha = 100;
    private int cpenAlpha = 100;

    private float LINE_SHAPE_WIDTH_RADIO = 1.1f;
    private float SHAPE_WIDTH_RADIO = 0.7f;



    private static float startX = 26;
    private static float startY = 26;

    // Curved line 좌표..
    private static float lineX1 = 60;
    private static float lineY1 = 10;
    private static float lineX2 = 120;
    private static float lineY2 = 82;
    private static float lineX3 = 215;
    private static float lineY3 = 28;

    //25, 22, 180, 22
    private static float lineShapeX1 = 35;
    private static float lineShapeY1 = 39;
    private static float lineShapeX2 = 200;
    private static float lineShapeY2 = 39;

    // 40, 10, 60, 30
    private static float smallShapeX1 = 75;
    private static float smallShapeY1 = 25;
    private static float smallShapeX2 = 95;
    private static float smallShapeY2 = 45;

    // 80, 10, 170, 30
    private static float bigShapeX1 = 125;
    private static float bigShapeY1 = 25;
    private static float bigShapeX2 = 185;
    private static float bigShapeY2 = 45;

    // 40, 20, 10
    private static float smallCircleX = 85;
    private static float smallCircleY = 35;
    private static float smallCircleRadius = 10;


    private String mode;
    private Context mContext;

    public PreviewCanvas(Context context) {
        super(context);
        String mode = "pen";
        mContext = context;
        init(context, mode);
    }

    public PreviewCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        String mode = context.obtainStyledAttributes(attrs, R.styleable.PreviewCanvas).getString(R.styleable.PreviewCanvas_previewMode);
        mContext = context;
        init(context, mode);
    }

    private void init(Context context, String mode) {
        this.mode = mode;
        density = context.getResources().getDisplayMetrics().density;
        Log.d("density", density + "");

        if("pen".equals(mode) || "eraser".equals(mode)) {

            float lineWidthPx = CommonUtils.convertPixelWithDpi(this.penWidth * WIDTH_RADIO, density);

            float startXParam = AndroidUtils.getPxFromDp(context,startX);
            float startYParam = AndroidUtils.getPxFromDp(context,startY);

            float lineX1Param = AndroidUtils.getPxFromDp(context,lineX1);
            float lineY1Param = AndroidUtils.getPxFromDp(context,lineY1);
            float lineX2Param = AndroidUtils.getPxFromDp(context,lineX2);
            float lineY2Param = AndroidUtils.getPxFromDp(context,lineY2);
            float lineX3Param = AndroidUtils.getPxFromDp(context,lineX3);
            float lineY3Param = AndroidUtils.getPxFromDp(context,lineY3);

            previewPenPath = new Path();

            previewPenPath.moveTo(startXParam, startYParam);
            //previewPenPath.cubicTo(40, 10, 110, 78, 320, 33);
            //previewPenPath.cubicTo(90, 10, 150, 78, 360, 33);
            previewPenPath.cubicTo(lineX1Param, lineY1Param, lineX2Param, lineY2Param, lineX3Param, lineY3Param);

            painter = new Paint(Paint.ANTI_ALIAS_FLAG);
            if("eraser".equals(mode)) {
                painter.setColor(Color.parseColor(this.eraserColor));
            } else {
                painter.setColor(Color.parseColor(this.penColor));
            }

            painter.setStrokeWidth(lineWidthPx);
            painter.setStyle(Paint.Style.STROKE);
            painter.setStrokeCap(Paint.Cap.ROUND);
            painter.setStrokeJoin(Paint.Join.ROUND);

        } else if("shape".equals(mode)) {
            painter = new Paint(Paint.ANTI_ALIAS_FLAG);
            smallShapeFillPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            smallShapeStrokePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bigShapeFillPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bigShapeStrokePainter = new Paint(Paint.ANTI_ALIAS_FLAG);

            if("5".equals(this.shapeType)) {
                painter.setColor(Color.parseColor(this.penColor));
                painter.setStrokeWidth(this.shapeWidth);
                painter.setStyle(Paint.Style.STROKE);
                painter.setStrokeCap(Paint.Cap.ROUND);
                painter.setStrokeJoin(Paint.Join.ROUND);
            }

            else if("6".equals(this.shapeType)) {
                smallShapeFillPainter.setStyle(Paint.Style.FILL);
                smallShapeFillPainter.setColor(Color.parseColor(this.spenColor));
                smallShapeFillPainter.setStrokeWidth(this.shapeWidth);

                smallShapeStrokePainter.setStyle(Paint.Style.STROKE);
                smallShapeStrokePainter.setStrokeWidth(3);
                smallShapeStrokePainter.setColor(Color.parseColor(this.spenBorderColor));

                bigShapeFillPainter.setStyle(Paint.Style.FILL);
                bigShapeFillPainter.setStrokeWidth(3);
                bigShapeFillPainter.setColor(Color.parseColor(this.spenColor));

                bigShapeStrokePainter.setStyle(Paint.Style.STROKE);
                bigShapeStrokePainter.setStrokeWidth(3);
                bigShapeStrokePainter.setColor(Color.parseColor(this.spenBorderColor));

            }

            else if("7".equals(this.shapeType)) {
                smallShapeFillPainter.setStyle(Paint.Style.FILL);
                smallShapeFillPainter.setStrokeWidth(3);
                smallShapeFillPainter.setColor(Color.parseColor(this.cpenColor));

                smallShapeFillPainter.setStyle(Paint.Style.STROKE);
                smallShapeFillPainter.setStrokeWidth(3);
                smallShapeFillPainter.setColor(Color.parseColor(this.cpenBorderColor));

                bigShapeFillPainter.setStyle(Paint.Style.FILL);
                bigShapeFillPainter.setStrokeWidth(3);
                bigShapeFillPainter.setColor(Color.parseColor(this.cpenColor));

                bigShapeFillPainter.setStyle(Paint.Style.STROKE);
                bigShapeFillPainter.setStrokeWidth(3);
                bigShapeFillPainter.setColor(Color.parseColor(this.cpenBorderColor));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(this.getClass().getSimpleName(), "onDraw() fired..");
        //float[] points = {40, 40, 360, 40};
        //canvas.drawLines(points, painter);

        if("pen".equals(this.mode) || "eraser".equals(this.mode)) {

            Log.d("onDraw()", "pen mode..");
            canvas.drawPath(previewPenPath, painter);

        } else if("shape".equals(this.mode)) {

            Log.d("onDraw()", "shape mode..");

            if("5".equals(this.shapeType)) {

                float lineShapeX1Param = AndroidUtils.getPxFromDp(mContext,lineShapeX1);
                float lineShapeY1Param = AndroidUtils.getPxFromDp(mContext,lineShapeY1);
                float lineShapeX2Param = AndroidUtils.getPxFromDp(mContext,lineShapeX2);
                float lineShapeY2Param = AndroidUtils.getPxFromDp(mContext,lineShapeY2);

                float[] points = {lineShapeX1Param, lineShapeY1Param, lineShapeX2Param, lineShapeY2Param};
                canvas.drawLines(points, painter);
            }

            else if("6".equals(this.shapeType)) {

                float smallShapeX1Param = AndroidUtils.getPxFromDp(mContext,smallShapeX1);
                float smallShapeY1Param = AndroidUtils.getPxFromDp(mContext,smallShapeY1);
                float smallShapeX2Param = AndroidUtils.getPxFromDp(mContext,smallShapeX2 );
                float smallShapeY2Param = AndroidUtils.getPxFromDp(mContext,smallShapeY2 );

                float bigShapeX1Param = AndroidUtils.getPxFromDp(mContext,bigShapeX1);
                float bigShapeY1Param = AndroidUtils.getPxFromDp(mContext,bigShapeY1);
                float bigShapeX2Param = AndroidUtils.getPxFromDp(mContext,bigShapeX2);
                float bigShapeY2Param = AndroidUtils.getPxFromDp(mContext,bigShapeY2);

                canvas.drawRect(smallShapeX1Param, smallShapeY1Param, smallShapeX2Param, smallShapeY2Param, smallShapeFillPainter);
                canvas.drawRect(smallShapeX1Param, smallShapeY1Param, smallShapeX2Param, smallShapeY2Param, smallShapeStrokePainter);
                canvas.drawRect(bigShapeX1Param, bigShapeY1Param, bigShapeX2Param, bigShapeY2Param, bigShapeFillPainter);
                canvas.drawRect(bigShapeX1Param, bigShapeY1Param, bigShapeX2Param, bigShapeY2Param, bigShapeStrokePainter);
            }

            else if("7".equals(this.shapeType)) {

                float smallCircleXParam = AndroidUtils.getPxFromDp(mContext,smallCircleX );
                float smallCircleYParam = AndroidUtils.getPxFromDp(mContext,smallCircleY);
                float smallCircleRadiusParam = AndroidUtils.getPxFromDp(mContext,smallCircleRadius);

                float bigShapeX1Param = AndroidUtils.getPxFromDp(mContext,bigShapeX1);
                float bigShapeY1Param = AndroidUtils.getPxFromDp(mContext,bigShapeY1 );
                float bigShapeX2Param = AndroidUtils.getPxFromDp(mContext,bigShapeX2 );
                float bigShapeY2Param = AndroidUtils.getPxFromDp(mContext,bigShapeY2 );

                canvas.drawCircle(smallCircleXParam, smallCircleYParam, smallCircleRadiusParam, smallShapeFillPainter);
                canvas.drawCircle(smallCircleXParam, smallCircleYParam, smallCircleRadiusParam, smallShapeStrokePainter);
                canvas.drawOval(new RectF(bigShapeX1Param, bigShapeY1Param, bigShapeX2Param, bigShapeY2Param), bigShapeFillPainter);
                canvas.drawOval(new RectF(bigShapeX1Param, bigShapeY1Param, bigShapeX2Param, bigShapeY2Param), bigShapeStrokePainter);
            }
        }

    }


    /**
     * Set pen width
     * @param widthVal : progress value
     */
    public void setPenWidth(@IntRange(from=1,to=8) int widthVal) {
        int width = (int)(widthVal / 12.5);
        if(width < 1) width = 1;
        if(width > 8) width = 8;

        this.penWidth = width;
        Log.d("setPenWidth", this.penWidth + "");

        painter.setStrokeWidth((this.penWidth * WIDTH_RADIO) * density);
        invalidate();
    }


    /**
     * Set pen opacity
     * @param alphaVal : progress value
     */
    public void setPenAlpha(int alphaVal) {
        this.penAlpha = alphaVal;
        Log.d("setPenAlpha", this.penAlpha + "");
        painter.setAlpha((int) Math.round(this.penAlpha * 2.55));
        invalidate();
    }


    /**
     * Set pen color
     * @param colorStr : RGB code string
     */
    public void setPenColor(String colorStr) {
        this.penColor = colorStr;
        painter.setColor(Color.parseColor(colorStr));
        invalidate();
    }


    /**
     * Set eraser width
     * @param widthVal : progress value
     */
    public void setEraserWidth(@IntRange(from=4,to=18) int widthVal) {  // 지우개 너비 4 ~ 18
        this.eraserWidth = (int)(widthVal / 5.5);
        if(this.eraserWidth < 4) this.eraserWidth = 4;
        Log.d("setEraserWidth", this.eraserWidth + "");
        painter.setStrokeWidth((this.eraserWidth * ERASER_WIDTH_RADIO) * density);
        invalidate();
    }


    /**
     * Set shape width
     * @param widthVal : progress value
     * @param fillType : 1 - Fill , 2 - Border
     */
    public void setShapeWidth(@IntRange(from=1,to=100) int widthVal, @Nullable int fillType) {
        int width = (int)(widthVal / 12.5);
        if(width < 1) width = 1;
        if(width > 8) width = 8;

        if ("5".equals(shapeType)){
            this.lpenWidth = width;
            shapeWidth = lpenWidth;
        } else if ("6".equals(shapeType)) {
            this.spenWidth = width;
            shapeWidth = spenWidth;
        } else if ("7".equals(shapeType)) {
            this.cpenWidth = width;
            shapeWidth = cpenWidth;
        }

        //this.shapeWidth = width;
        Log.d("setShapeWidth", this.shapeWidth + "");

        if("5".equals(shapeType)) {
            painter.setStrokeWidth((this.shapeWidth * LINE_SHAPE_WIDTH_RADIO) *density);
        } else {
            if("0".equals(this.shapeFillType)) {
                smallShapeFillPainter.setStrokeWidth((this.shapeWidth * SHAPE_WIDTH_RADIO) * density);
                bigShapeFillPainter.setStrokeWidth((this.shapeWidth * SHAPE_WIDTH_RADIO) * density);
            } else if("1".equals(this.shapeFillType)) {
                smallShapeStrokePainter.setStrokeWidth((this.shapeWidth * SHAPE_WIDTH_RADIO) * density);
                bigShapeStrokePainter.setStrokeWidth((this.shapeWidth * SHAPE_WIDTH_RADIO) * density);
            }
        }

        invalidate();
    }


    /**
     * Set shape opacity
     * @param alphaVal : progress value
     */
    public void setShapeAlpha(int alphaVal) {
        boolean isSelectTransparent = false;
        if ("5".equals(shapeType)) {
            this.lpenAlpha = alphaVal;
            shapeAlpha = lpenAlpha;
            painter.setAlpha((int) Math.round(this.shapeAlpha * 2.55));
        } else {
            if ("6".equals(shapeType)) {
                this.spenAlpha = alphaVal;
                shapeAlpha = spenAlpha;
                isSelectTransparent = this.spenColor.equals("#00000000") ? true : false;
            } else if ("7".equals(shapeType)) {
                this.cpenAlpha = alphaVal;
                shapeAlpha = cpenAlpha;
                isSelectTransparent = this.cpenColor.equals("#00000000") ? true : false;
            }

            if(isSelectTransparent) {
                smallShapeFillPainter.setAlpha(0);
                bigShapeFillPainter.setAlpha(0);
            } else {
                smallShapeFillPainter.setAlpha((int) Math.round(this.shapeAlpha * 2.55));
                bigShapeFillPainter.setAlpha((int) Math.round(this.shapeAlpha * 2.55));
            }
            smallShapeStrokePainter.setAlpha((int) Math.round(this.shapeAlpha * 2.55));
            bigShapeStrokePainter.setAlpha((int) Math.round(this.shapeAlpha * 2.55));
        }
        Log.d("setShapeAlpha", this.shapeAlpha + "");
        invalidate();
    }


    /**
     * Set shape color
     * @param colorStr : RGB code string
     */
    public void setShapeColor(String colorStr) {
        if("5".equals(this.shapeType)) {
            this.lpenColor = "".equals(colorStr) ? this.lpenColor : colorStr;
            painter.setColor(Color.parseColor(this.lpenColor));
            setShapeAlpha(lpenAlpha);
        } else if("6".equals(this.shapeType)) {
            if("0".equals(this.shapeFillType)) {
                this.spenColor = "".equals(colorStr) ? this.spenColor : colorStr;

                if("#00000000".equals(this.spenColor)){   // 투명컬러..
                    smallShapeFillPainter.setColor(Color.TRANSPARENT);
                    bigShapeFillPainter.setColor(Color.TRANSPARENT);
                } else {
                    smallShapeFillPainter.setColor(Color.parseColor(this.spenColor));
                    bigShapeFillPainter.setColor(Color.parseColor(this.spenColor));
                }
            } else if("1".equals(this.shapeFillType)) {
                this.spenBorderColor = "".equals(colorStr) ? this.spenBorderColor : colorStr;
                smallShapeStrokePainter.setColor(Color.parseColor(this.spenBorderColor));
                bigShapeStrokePainter.setColor(Color.parseColor(this.spenBorderColor));
            }

            // 투명컬러 프리뷰 처리..
            if("#00000000".equals(this.spenColor)){
                smallShapeFillPainter.setAlpha(0);
                bigShapeFillPainter.setAlpha(0);
            } else {
                setShapeAlpha(spenAlpha);
            }

        } else if("7".equals(this.shapeType)) {
            if("0".equals(this.shapeFillType)) {
                this.cpenColor = "".equals(colorStr) ? this.cpenColor : colorStr;

                if("#00000000".equals(this.cpenColor)) {
                    smallShapeFillPainter.setColor(Color.TRANSPARENT);
                    bigShapeFillPainter.setColor(Color.TRANSPARENT);
                } else {
                    smallShapeFillPainter.setColor(Color.parseColor(this.cpenColor));
                    bigShapeFillPainter.setColor(Color.parseColor(this.cpenColor));
                }
            } else if("1".equals(this.shapeFillType)) {
                this.cpenBorderColor = "".equals(colorStr) ? this.cpenBorderColor : colorStr;
                smallShapeStrokePainter.setColor(Color.parseColor(this.cpenBorderColor));
                bigShapeStrokePainter.setColor(Color.parseColor(this.cpenBorderColor));
            }

            // 투명컬러 프리뷰 처리..
            if("#00000000".equals(this.cpenColor)){
                smallShapeFillPainter.setAlpha(0);
                bigShapeFillPainter.setAlpha(0);
            } else {
                setShapeAlpha(cpenAlpha);
            }
        }
        invalidate();
    }


    /**
     * Set shape type
     * @param shapeType 5:Line, 6:Rectangle, 7:Circle
     */
    public void setShapeType(int shapeType) {
        this.shapeType = shapeType + "";
        invalidate();
    }

    public void setShapeFillType(int fillType) {
        this.shapeFillType = fillType + "";
    }

    public int getPenWidth() {
        return this.penWidth;
    }

    public int getPenAlpha() {
        return this.penAlpha;
    }

    public String getPenColor() {
        return this.penColor;
    }


    public int getEraserWidth() {
        return this.eraserWidth;
    }

    public int getShapeWidth() {
        int shapeWidth = 4;
        if("5".equals(shapeType)) {
            shapeWidth = this.lpenWidth;
        } else if("6".equals(shapeType)) {
            shapeWidth = this.spenWidth;
        } else if("7".equals(shapeType)) {
            shapeWidth = this.cpenWidth;
        }
        return shapeWidth;
    }

    public int getShapeAlpha() {
        int shapeAlpha = 4;
        if("5".equals(shapeType)) {
            shapeAlpha = this.lpenAlpha;
        } else if("6".equals(shapeType)) {
            shapeAlpha = this.spenAlpha;
        } else if("7".equals(shapeType)) {
            shapeAlpha = this.cpenAlpha;
        }
        return shapeAlpha;
    }

    public String getLpenColor() {
        return this.lpenColor;
    }
    public String getSpenColor() {
        return this.spenColor;
    }
    public String getCpenColor() {
        return this.cpenColor;
    }









}
