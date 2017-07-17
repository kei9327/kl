package com.knowlounge.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.util.AndroidUtils;

/**
 * Created by Mansu on 2016-12-01.
 */

public class DragSelectableView extends View {

    private Paint strokePaint;
    private Paint fillPaint;
    private Context mContext;


    float strokePadding = 1.5f * KnowloungeApplication.density;

    float startX = 0f;
    float startY = 0f;
    float endX = 0f;
    float endY = 0f;

    public DragSelectableView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DragSelectableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.parseColor("#FFFFFFFF"));
        //fillPaint.setStrokeWidth(this.shapeWidth);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2.f * KnowloungeApplication.density);
        strokePaint.setColor(getResources().getColor(R.color.app_base_color));
    }
    @Override
    protected void onDraw(Canvas canvas) {

        float startXVal = AndroidUtils.getPxFromDp(mContext, startX);
        float startYVal = AndroidUtils.getPxFromDp(mContext, startY);
        float endXVal = AndroidUtils.getPxFromDp(mContext, endX);
        float endYVal = AndroidUtils.getPxFromDp(mContext, endY);

        startXVal = startX;
        startYVal = startY;
        endXVal = endX;
        endYVal = endY;

//        float startYVal = AndroidUtils.getDpFromPx(mContext, startY);
//        float startXVal = AndroidUtils.getDpFromPx(mContext, startX);
//        float endXVal = AndroidUtils.getDpFromPx(mContext, endX);
//        float endYVal = AndroidUtils.getDpFromPx(mContext, endY);


//        canvas.drawRect(startXVal, startYVal, endXVal, endYVal, fillPaint);
//        canvas.drawRect(startXVal, startYVal, endXVal, endYVal, strokePaint);
//

        Path strokePath = new Path();
        strokePath.addRect(startXVal + strokePadding, startYVal + strokePadding, endXVal - strokePadding, endYVal - strokePadding, Path.Direction.CCW);

        canvas.drawColor(Color.TRANSPARENT);
        canvas.save();


        //canvas.drawPath(strokePath, strokePaint);   // 영역 선택 부분에 스트로크 처리
        canvas.clipRect(startXVal, startYVal, endXVal, endYVal, Region.Op.DIFFERENCE);
        //canvas.drawPaint(strokePaint);
        //canvas.clipRect(0, 0, 1000, 1000);
        canvas.drawColor(Color.parseColor("#80000000"));   // 딤드 색상지정
        canvas.restore();

        super.onDraw(canvas);
    }



    public void updateSelectorPaint(float startX, float startY, float endX, float endY) {

        if(startX > endX) {
            this.startX = endX;
            this.endX = startX;
        } else {
            this.startX = startX;
            this.endX = endX;
        }

        if(startY > endY) {
            this.startY = endY;
            this.endY = startY;
        } else {
            this.startY = startY;
            this.endY = endY;
        }



//        this.startX = startX;
//        this.startY = startY;
//        this.endX = endX;
//        this.endY = endY;
        invalidate();
    }

}
