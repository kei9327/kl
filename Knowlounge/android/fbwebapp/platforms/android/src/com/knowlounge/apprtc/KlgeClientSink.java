package com.knowlounge.apprtc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wescan.alo.rtc.client.RtcClientSink;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-07.
 */

public interface KlgeClientSink extends RtcClientSink {

    KlgeClientController getController();

    void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response);

    void onFailRtcStatus(WebSocketKlgeSession session, JsonObject response);

    void onPeerStatus(JsonObject response);

    void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId);

    void onPeerBusyQuery(JsonObject response);

    void onPeerBusyReply(JsonObject response);

    void onReconnectSession();   // 2017.03.29 추가
}
