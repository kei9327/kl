package com.knowlounge;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.knowlounge.apprtc.KlgeClientController;
import com.knowlounge.apprtc.KlgeClientSink;
import com.knowlounge.apprtc.WebSocketKlgeSession;
import com.knowlounge.base.BaseFragment;
import com.wescan.alo.rtc.RtcChatContext;
import com.wescan.alo.rtc.RtcChatSession;
import com.wescan.alo.rtc.RtcPeerChannel;
import com.wescan.alo.rtc.client.RtcClientContainer;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 *
 * WebRtc life cycle 이벤트를 등록하고 이와 관련된 UI이벤트 처리들을 구현한다.
 *
 * ex)
 * RtcSupportFragment extends Fragment implements AloClientSink {
 *
 * }
 *
 * ...
 *
 * AloClientSink extends RtcClientSink {
 *
 * }
 *
 * 클라이언트 구현시 필요한 구성요소
 *
 * [Field]
 * AloClientController mController;
 *
 * [onAttach()]
 * mController = (AloClientController) ((RtcClientContainer)context).getRtcClientController();
 *
 * [onCreate()]
 * mController.addRtcClientSink(this);
 *
 * [onDestroy()]
 * mController.removeRtcClientSink(this);
 *
 * author: Jun-hyoung Lee
 * date: 2017-02-27.
 */

public abstract class RtcSupportFragment extends BaseFragment implements KlgeClientSink {

    private RtcChatContext mRtcChatContext;

    private KlgeClientController mController;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof RtcChatContext)) {
            throw new IllegalArgumentException("RtcSupportFragment must have RtcChatContext context.");
        }
        if (!(context instanceof RtcClientContainer)) {
            throw new IllegalArgumentException("RtcSupportFragment must have RtcClientContainer parent.");
        }

        mRtcChatContext = (RtcChatContext) context;
        mController = (KlgeClientController)((RtcClientContainer)context).getRtcClientController();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController.addRtcClientSink(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mController.removeRtcClientSink(this);
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RtcClientSink>
     */

    @Override
    public RtcChatContext getRtcContext() {
        return mRtcChatContext;
    }

    @Override
    public void onRtcStart(RtcChatSession session) {

    }

    @Override
    public void onIceConnected(RtcChatSession session, RtcPeerChannel peer) {

    }

    @Override
    public void onIceDisconnected(RtcChatSession session, RtcPeerChannel peer) {

    }

    @Override
    public void onIceFailed(RtcChatSession session, RtcPeerChannel peer) {

    }

    @Override
    public void onIceClosed(RtcChatSession session, RtcPeerChannel peer) {

    }


    @Override
    public void onRtcStop(RtcChatSession session) {

    }

    @Override
    public void onRtcStopAll() {

    }

    @Override
    public KlgeClientController getController() {
        return mController;
    }

    @Override
    public void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {

    }

    /**
     * peer_status 시그널 처리
     */
    public void onPeerStatus(JsonObject response) {

    }

    /**
     * peer_init 시그널 처리
     */
    public void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {

    }

    /**
     * rtcBusy-request 를 받았을 때 응답 처리를 위한 준비
     */
    public void onPeerBusyQuery(JsonObject response) {

    }

    public void onPeerBusyReply(JsonObject response) {

    }


    public void onReconnectSession() {

    }
}
