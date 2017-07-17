package com.knowlounge.util;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.knowlounge.R;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p>
 * GoogleAnalytics 관련 처리
 * <p>
 * author: Jang-hyeok Park
 * date: 2016-06-30.
 */
public abstract class GoogleAnalyticsFactory {

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link MultiDexApplication}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    protected Context mContext;

    public GoogleAnalyticsFactory(Context context) {
        this.mContext = context;
    }

    public abstract void sendAnalyticsEvent(String category, String action);
    public abstract void sendAnalyticsScreen(String screen);
}
