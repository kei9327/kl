package com.knowlounge.customview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by we160303 on 2016-05-27.
 */
public class NotiFullListView extends ListView {
    WenotePreferenceManager preferenceManager;
    public NotiFullListView(Context context) {
        super(context);
        init(context);
    }
    public NotiFullListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public NotiFullListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init(context);
    }
    private void init(Context context) {
        preferenceManager = WenotePreferenceManager.getInstance(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = 0;
        ListAdapter adapter = getAdapter();
        int count = adapter != null ? adapter.getCount() : 0;
        for (int i = 0; i < count; i++) {
            View childView = adapter.getView(i, null, this);
            childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            height += childView.getMeasuredHeight() + (int)(15*preferenceManager.getDensity());
        }
        height += getDividerHeight() * (count-1) ;

        setMeasuredDimension(width, height);
    }
}