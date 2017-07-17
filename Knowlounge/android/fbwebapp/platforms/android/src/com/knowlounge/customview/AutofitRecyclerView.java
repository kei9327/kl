package com.knowlounge.customview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Minsu on 2016-03-25.
 */
public class AutofitRecyclerView extends RecyclerView {

    private boolean scrollAble = true;

    public AutofitRecyclerView(Context context) {
        super(context);
    }

    public AutofitRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutofitRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!scrollAble) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (!scrollAble) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }
    @Override
    public boolean canScrollVertically(int direction) {
        if (scrollAble) {
            if (direction < 1) {
                boolean original = super.canScrollVertically(direction);
                return !original && getChildAt(0) != null && getChildAt(0).getTop() < 0 || original;
            }
            return super.canScrollVertically(direction);
        } else {
            return false;
        }
    }

    public void setScrollAble(boolean able){
        scrollAble = able;
    }
}