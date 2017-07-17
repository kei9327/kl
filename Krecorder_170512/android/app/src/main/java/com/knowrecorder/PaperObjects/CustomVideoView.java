//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.util.AttributeSet;
//import android.widget.VideoView;
//
//import com.knowrecorder.Managers.Recorder;
//
//public class CustomVideoView extends VideoView {
//    private VideoLayout layout;
//    private PlayPauseListener mListener;
//
//    public CustomVideoView(Context context) {
//        super(context);
//        init();
//    }
//
//    public CustomVideoView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//
//    }
//
//    public void setPlayPauseListner(PlayPauseListener listner) {
//        mListener = listner;
//    }
//
//    private void init() {
//
//    }
//
//
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//    }
//
//    public void setLayout(VideoLayout layout) {
//        this.layout = layout;
//    }
//
//    @Override
//    public void pause() {
//        pause(false);
//    }
//
//    @Override
//    public void start() {
//        start(false);
//    }
//
//    @Override
//    public void seekTo(int msec) {
//        seekTo(msec, false);
//    }
//
//    public void setmListener(PlayPauseListener mListener) {
//        this.mListener = mListener;
//    }
//
//    public void pause(boolean superMode) {
//        if (Recorder.getInstance().isPlaying() && !superMode)
//            return;
//
//        if (mListener != null) {
//            if (mListener.onPause()) {
//                super.pause();
//            }
//        }
//    }
//
//    public void start(boolean superMode) {
//        if (Recorder.getInstance().isPlaying() && !superMode)
//            return;
//
//        if (mListener != null) {
//            if (mListener.onPlay()) {
//                super.start();
//            }
//        }
//    }
//
//    public void seekTo(int msec, boolean superMode) {
//        if (Recorder.getInstance().isPlaying() && !superMode)
//            return;
//
//        if (mListener != null) {
//            if (mListener.onSeekChanged(msec)) {
//                super.seekTo(msec);
//            }
//        }
//    }
//
//    public interface PlayPauseListener {
//        boolean onPlay();
//        boolean onPause();
//        boolean onSeekChanged(int msec);
//    }
//}
