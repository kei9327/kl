package com.knowlounge.customview;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Minsu on 2016-03-26.
 * DrawerLayout Customize class
 * When main container touch, this class always return false. You can touch main container area regardless of drawer container opened.
 */
public class CustomDrawerLayout extends DrawerLayout {

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // always false and event will be send to each view
        return false;
    }

}
