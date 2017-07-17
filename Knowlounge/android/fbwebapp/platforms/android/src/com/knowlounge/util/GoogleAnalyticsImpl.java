package com.knowlounge.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.knowlounge.KnowloungeApplication;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p>
 * GoogleAnalytics 관련 처리
 * <p>
 * author: Jang-hyeok Park
 * date: 2016-06-30.
 */
public class GoogleAnalyticsImpl extends GoogleAnalyticsFactory {

    private Tracker mTracker;

    public GoogleAnalyticsImpl(Context context) {
        super(context);
        mTracker = getDefaultTracker();
    }

    @Override
    public void sendAnalyticsEvent(String category, String action) {
        Log.d("sendAnalytics","event");
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build()
        );
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GoogleAnalytics.getInstance(mContext).dispatchLocalHits();
            }
        }, 1000);
    }

    @Override
    public void sendAnalyticsScreen(String screen) {
        Log.d("sendAnalytics","screen");
        mTracker.setScreenName(screen);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GoogleAnalytics.getInstance(mContext).dispatchLocalHits();
            }
        }, 1000);

    }
}
