package com.knowrecorder.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.knowrecorder.develop.fragment.TimeLine.HorizontalListView;

/**
 * Created by we160303 on 2017-03-13.
 */

public class LiveViewHolyShit extends RelativeLayout {
    private final String TAG = "TimeLine";
    private GestureDetector mGestureDetector;
    private final GestureListener mGestureListener = new GestureListener();


    public LiveViewHolyShit(Context context)
    {
        super(context);
        init(context);
    }

    public LiveViewHolyShit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LiveViewHolyShit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mGestureDetector = new GestureDetector(context, mGestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP){
            for(int i = 0; i < getChildCount() ; i++){
                ((HorizontalListView)getChildAt(i)).conpleteScroll();
            }

            return true;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            for(int i = 0; i < getChildCount() ; i++){
                ((HorizontalListView)getChildAt(i)).onDown(e);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling");
//            for(int i = 0; i < getChildCount() ; i++){
//                ((HorizontalListView)getChildAt(i)).onFling(e1, e2, velocityX, velocityY);
//            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll");
            for(int i = 0; i < getChildCount() ; i++){
                ((HorizontalListView)getChildAt(i)).onScroll(e1, e2, distanceX, distanceY);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed");
            for(int i = 0; i < getChildCount() ; i++){
                ((HorizontalListView)getChildAt(i)).onSingleTapConfirmed(e);
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
            for(int i = 0; i < getChildCount() ; i++){
                ((HorizontalListView)getChildAt(i)).onLongPress(e);
            }
        }
    };
}
