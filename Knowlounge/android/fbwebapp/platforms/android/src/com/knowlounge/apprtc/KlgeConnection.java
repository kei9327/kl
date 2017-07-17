package com.knowlounge.apprtc;

import com.wescan.alo.rtc.RtcChatSession;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class KlgeConnection extends RtcChatSession.Connection {

    private final String mServer;
    private final String mPort;
    private final String mZicoAccessToken;
    private final String mUserId;
    private final String mRoomId;

    private KlgeConnection(KlgeConnection.Builder builder) {
        super(builder.mLoopback);
        mServer = builder.mServer;
        mPort = builder.mPort;
        mZicoAccessToken = builder.mZicoAccessToken;
        mUserId = builder.mUserId;
        mRoomId = builder.mRoomId;
    }

    public String getHost() {
        return mServer;
    }

    public String getPort() {
        return mPort;
    }

    public String getZicoAccessToken() {
        return mZicoAccessToken;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String toWssUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("wss://");
        sb.append(mServer);
        sb.append(":");
        sb.append(mPort);
        return sb.toString();
    }


    public static class Builder {
        private boolean mLoopback;
        private String mServer;
        private String mPort;
        private String mZicoAccessToken;
        private String mUserId;
        private String mRoomId;

        public Builder host(String host) {
            mServer = host;
            return this;
        }

        public Builder port(String port) {
            mPort = port;
            return this;
        }

        public Builder zicoAccessToken(String zicoAccessToken) {
            mZicoAccessToken = zicoAccessToken;
            return this;
        }

        public Builder userId(String userId) {
            mUserId = userId;
            return this;
        }

        public Builder roomId(String roomId) {
            mRoomId = roomId;
            return this;
        }

        public KlgeConnection build() {
            return new KlgeConnection(this);
        }
    }
}
