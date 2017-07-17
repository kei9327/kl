package com.knowlounge.apprtc;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.knowlounge.model.KlgePeer;
import com.wescan.alo.rtc.client.RtcClientController;
import com.wescan.alo.rtc.client.RtcClientSink;

import java.util.List;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-07.
 */

public class KlgeClientController extends RtcClientController<KlgeConnection> implements WebSocketKlgeSession.RtcClientEvents {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private KlgeClientSinkCallback<KlgeConnection> mSinkCallback;

    private KlgePeerWatcher mPeerWatcher;

    public KlgeClientController(KlgeClientSinkCallback<KlgeConnection> callback, KlgePeerWatcher watcher) {
        super(callback);
        mSinkCallback = callback;
        mPeerWatcher = watcher;
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : WebSocketKlgeSession.RtcClientEvents>
     */

    @Override
    public void onFailAuthMessage(final WebSocketKlgeSession session, final JsonObject response) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnFailAuthMessage(session, response);
            }
        });
    }

    @Override
    public void onFailRtcStatus(final WebSocketKlgeSession session, final JsonObject response) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnFailRtcStatus(session, response);
            }
        });
    }

    @Override
    public void onOpenSession(WebSocketKlgeSession session) {

    }

    @Override
    public void onCloseSession(WebSocketKlgeSession session) {

    }

    @Override
    public void onPeerStatus(final JsonObject response) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnPeerStatus(response);
            }
        });
    }

    @Override
    public void onPeerList(final List<KlgePeer> peers) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mPeerWatcher.onPeerList(peers);
            }
        });

    }

    @Override
    public void onPeerInit(final String fromUserId, final JsonArray turnServers, final String sessionId) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnPeerInit(fromUserId, turnServers, sessionId);
            }
        });
    }

    @Override
    public void onPeerUpdate() {
        /**
         * TODO 추후에 피어 업데이트에 대한 기능을 구현한다.
         */
    }

    @Override
    public void onPeerBusyQuery(final JsonObject response) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnPeerBusyQuery(response);
            }
        });
    }

    @Override
    public void onPeerBusyReply(final JsonObject response) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnPeerBusyReply(response);
            }
        });
    }

    @Override
    public void onReconnectSession() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                dispatchOnReconnectSession();            }
        });
    }

    /*
     *----------------------------------------------------------------------------------------------
     *--<methods>
     */

    private void runOnMainThread(Runnable action) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }

    public KlgePeerWatcher getPeerWatcher() {
        return mPeerWatcher;
    }

    private KlgeClientSinkCallback<KlgeConnection> getSinkCallback() {
        return mSinkCallback;
    }

    private void dispatchOnFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {
        getSinkCallback().onFailAuthMessage(session, response);

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onFailAuthMessage(session, response);
            }
        }
    }

    private void dispatchOnFailRtcStatus(WebSocketKlgeSession session, JsonObject response) {
        getSinkCallback().onFailRtcStatus(session, response);

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onFailRtcStatus(session, response);
            }
        }
    }

    private void dispatchOnPeerStatus(JsonObject response) {
        getSinkCallback().onPeerStatus(response);

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onPeerStatus(response);
            }
        }
    }

    private void dispatchOnPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {
        getSinkCallback().onPeerInit(fromUserId, turnServers, sessionId);

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onPeerInit(fromUserId, turnServers, sessionId);
            }
        }
    }

    private void dispatchOnPeerBusyQuery(JsonObject response) {
        getSinkCallback().onPeerBusyQuery(response);

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onPeerBusyQuery(response);
            }
        }
    }

    private void dispatchOnPeerBusyReply(JsonObject response) {
        getSinkCallback().onPeerBusyReply(response);

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onPeerBusyReply(response);
            }
        }
    }


    private void dispatchOnReconnectSession() {
        getSinkCallback().onReconnectSession();

        for (RtcClientSink client : getRtcClientSinks()) {
            if (client instanceof KlgeClientSink) {
                ((KlgeClientSink) client).onReconnectSession();
            }
        }
    }

}
