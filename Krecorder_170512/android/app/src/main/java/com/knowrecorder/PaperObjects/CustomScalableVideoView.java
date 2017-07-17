package com.knowrecorder.PaperObjects;

/**
 * Created by hazuki21 on 2017. 1. 4..
 */

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.TextureView;

import com.yqritc.scalablevideoview.ScalableVideoView;

public class CustomScalableVideoView extends ScalableVideoView implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private VideoStateCallback mCallback;

    public CustomScalableVideoView(Context context) {
        super(context, null);
    }

    public CustomScalableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CustomScalableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        super.onSurfaceTextureAvailable(surfaceTexture, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return super.onSurfaceTextureDestroyed(surface);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        if (mCallback != null)
            mCallback.onPlayed(mMediaPlayer.getCurrentPosition());

        super.onSurfaceTextureUpdated(surface);
    }

    public void setCallback(VideoStateCallback callback) {
        this.mCallback = callback;
    }

    public interface VideoStateCallback {
        void onPlayed(int msec);
    }
}
