package com.knowlounge.dagger.modules;

import com.wescan.alo.rtc.RtcChatContext;

import dagger.Module;
import dagger.Provides;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

@Module
public class RtcContextModule {

    private final RtcChatContext mContext;

    public RtcContextModule(RtcChatContext context) {
        this.mContext = context;
    }

    @Provides
    RtcChatContext provideRtcContext() {
        return mContext;
    }
}
