package com.knowrecorder.develop.opencourse;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by we160303 on 2017-03-08.
 */

public class PanelWrapper extends FrameLayout {
    public PanelWrapper(Context context) {
        super(context);
    }

    public PanelWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
