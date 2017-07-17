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
public class FullListView extends ListView {


    public FullListView(Context context) {
        super(context);
    }
    public FullListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FullListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = 0;
        ListAdapter adapter = getAdapter();
        int count = adapter != null ? adapter.getCount() : 0;
        for (int i = 0; i < count; i++) {
            Rect bgPadding = new Rect();
            View childView = adapter.getView(i, null, this);
            childView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            height += childView.getMeasuredHeight() + bgPadding.top + bgPadding.bottom;
        }
        height += getDividerHeight() * (count-1) ;
        setMeasuredDimension(width, height);
    }
}