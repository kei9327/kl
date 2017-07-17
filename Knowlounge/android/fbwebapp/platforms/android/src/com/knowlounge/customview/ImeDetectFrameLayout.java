package com.knowlounge.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.knowlounge.util.ImeUtil;

/**
 * Created by Minsu on 2016-05-27.
 */
public class ImeDetectFrameLayout extends FrameLayout {
    public ImeDetectFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredHeight = getMeasuredHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (measuredHeight != getMeasuredHeight() && getContext() instanceof ImeUtil.ImeStateHost) {
            ((ImeUtil.ImeStateHost) getContext()).onDisplayHeightChanged(heightMeasureSpec);
        }
    }
}
