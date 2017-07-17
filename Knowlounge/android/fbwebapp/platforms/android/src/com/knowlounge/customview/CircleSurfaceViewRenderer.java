package com.knowlounge.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import com.knowlounge.util.AndroidUtils;

import org.webrtc.SurfaceViewRenderer;

/**
 * Created by Mansu on 2017-03-28.
 */

public class CircleSurfaceViewRenderer extends SurfaceViewRenderer {
    private Context context;
    private Path clipPath;
    public CircleSurfaceViewRenderer(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CircleSurfaceViewRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        init();
    }

    private void init() {
        clipPath = new Path();
        //TODO: define the circle you actually want
        clipPath.addCircle(AndroidUtils.getPxFromDp(context, 80), AndroidUtils.getPxFromDp(context, 80), AndroidUtils.getPxFromDp(context, 80), Path.Direction.CW);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
    }
}
