package com.knowlounge.network.websocket.command;

import com.google.gson.JsonObject;

import org.webrtc.SessionDescription;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class PeerAnswerSdpWebCommand extends WebCommand {

    private String mFromUserId;
    private String mToUserId;

    private String mSessionId;
    private SessionDescription mBody;


    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.PEER);
        packet.addProperty("from", mFromUserId);
        packet.addProperty("to", mToUserId);

        JsonObject sdp = new JsonObject();
        sdp.addProperty("type", mBody.type.canonicalForm());
        sdp.addProperty("sdp", mBody.description);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", KlgeWebCommandTypes.PEER_ANSWER_SDP);
        payload.addProperty("session_id", mSessionId);
        payload.add("body", sdp);
        packet.add("payload", payload);

        return packet.toString();
    }

    public PeerAnswerSdpWebCommand from(String from) {
        mFromUserId = from;
        return this;
    }

    public PeerAnswerSdpWebCommand to(String to) {
        mToUserId = to;
        return this;
    }

    public PeerAnswerSdpWebCommand sessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }

    public PeerAnswerSdpWebCommand body(SessionDescription body) {
        mBody = body;
        return this;
    }
}
