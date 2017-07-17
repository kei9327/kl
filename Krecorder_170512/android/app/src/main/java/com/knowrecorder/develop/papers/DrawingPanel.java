package com.knowrecorder.develop.papers;

/**
 * Created by ChangHa on 2017-02-02.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.rxjava.RxEventFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DrawingPanel extends FrameLayout {

    private Context context;
    public static AtomicInteger id = new AtomicInteger(0);
    public static AtomicInteger mid = new AtomicInteger(0);
    public static AtomicInteger pageId = new AtomicInteger(0);

    public DrawingPanel(Context context) {
        super(context);
        this.context = context;
        setWillNotDraw(false);
    }

    public DrawingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DrawingPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("DrawingPanel", "onDraw");

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return ProcessStateModel.getInstanse().isPlaying();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        //if(ObjectPaperV2.currentFocusedTextMid != -1)
        ObjectPaperV2.currentFocusedTextMid = 0;
        RxEventFactory.get().post(new EventType(EventType.CLEAR_TEXT_FOCUS));
        return false;
    }
    public static void setIndexId(int num){
        id.set(num);
    }

    public static void setMid(int num){
        mid.set(num);
    }

    public static void setPageId(int num) { pageId.set(num);}

}