package com.knowlounge.util;

import android.content.Context;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p>
 * GoogleAnalytics 관련 처리
 * <p>
 * author: Jang-hyeok Park
 * date: 2016-06-30.
 */
public class GoogleAnalyticsService extends GoogleAnalyticsImpl {

    private static volatile GoogleAnalyticsService sInstance;

    public GoogleAnalyticsService(Context context) {
        super(context);
    }

    public static void initialize(Context context) {
        sInstance = new GoogleAnalyticsService(context);
        sInstance.getDefaultTracker().setSessionTimeout(1800);
    }

    public static GoogleAnalyticsService get() {
        return sInstance;
    }

}
