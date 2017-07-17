package com.knowrecorder.OpenCourse.Papers;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.knowrecorder.Toolbox.Toolbox;

/**
 * Created by ssyou on 2016-02-01.
 */
public class OCDrawingPanel extends FrameLayout {

    private Context context;

    public OCDrawingPanel(Context context) {
        super(context);
        this.context = context;
        setBackgroundColor(Color.parseColor("#ffffff"));
        initPanel();
    }

    public OCDrawingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setBackgroundColor(Color.parseColor("#ffffff"));
        initPanel();
    }

    public OCDrawingPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setBackgroundColor(Color.parseColor("#ffffff"));
        initPanel();
    }

    public void initPanel() {
        Toolbox.getInstance().setContext(context);
    }
}
