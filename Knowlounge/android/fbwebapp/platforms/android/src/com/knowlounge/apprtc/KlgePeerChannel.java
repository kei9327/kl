package com.knowlounge.apprtc;

import android.text.TextUtils;
import android.util.Log;

import com.wescan.alo.rtc.RtcChatSession;
import com.wescan.alo.rtc.RtcPeerChannel;

import java.util.UUID;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-08.
 */

public class KlgePeerChannel extends RtcPeerChannel {

    /**
     * BUSY인지 아닌지 여부를 판단할 수 있는 최대의 개수
     */
    public static final int MAX_PEER_BUSY_COOUNT = 4;

    private final String mToUserId;
    private String mSignalSessionId;

    public KlgePeerChannel(RtcChatSession rtcChatSession, boolean isOffer, String toUserId, String signalSessionId) {
        super(rtcChatSession, isOffer);
        Log.d("KlgePeerChannel", "<constructor> toUserId : " + toUserId);
        mToUserId = toUserId;
        mSignalSessionId = signalSessionId;
    }

    public final String getToUserId() {
        return mToUserId;
    }

    public static String buildSessionId() {
        return UUID.randomUUID().toString().replace("-","").substring(0, 6) + "_" + System.currentTimeMillis();
    }

    String getSignalSessionId() {
        return mSignalSessionId;
    }

    boolean isValidSignalSessionId() {
        return !TextUtils.isEmpty(mSignalSessionId);
    }

}
