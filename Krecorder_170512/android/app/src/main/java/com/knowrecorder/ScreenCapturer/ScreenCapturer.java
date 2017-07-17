package com.knowrecorder.ScreenCapturer;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by hazuki21 on 2017. 1. 3..
 */

public class ScreenCapturer {

    View mRootView;

    public ScreenCapturer(View mRootView) {
        this.mRootView = mRootView;
    }

    public static ScreenCapturer getInstance(View view) {
        return new ScreenCapturer(view);
    }

    public Bitmap getBitmap() {
        return ScreenCaptureUtil.getScreenShotBitmap(mRootView, null);
    }

    public Bitmap getBitmap(View... ignoredViews) {
        return ScreenCaptureUtil.getScreenShotBitmap(mRootView, ignoredViews);
    }
}
