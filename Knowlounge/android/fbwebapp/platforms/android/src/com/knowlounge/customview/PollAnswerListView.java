package com.knowlounge.customview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.knowlounge.view.room.RoomActivity;

/**
 * Created by we160303 on 2016-05-27.
 */
public class PollAnswerListView extends ListView {


    public PollAnswerListView(Context context) {
        super(context);
    }
    public PollAnswerListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public PollAnswerListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = 0;
        ListAdapter adapter = getAdapter();
        int count = adapter != null ? adapter.getCount() : 0;
        count = count <= 4 ? count : 5;
        for (int i = 0; i < count; i++) {
            Rect bgPadding = new Rect();
            View childView = adapter.getView(i, null, this);
            childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            height += childView.getMeasuredHeight() + bgPadding.top + bgPadding.bottom;
        }
        height += getDividerHeight() * (count-1) ;
        if(count == 5){
         height -= (int)(20 * RoomActivity.activity.density);
        }
        setMeasuredDimension(width, height);
    }
}