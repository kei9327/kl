package com.knowlounge.network.websocket.command;

import com.google.gson.JsonObject;

import org.webrtc.IceCandidate;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class PeerIceCandidateWebCommand extends WebCommand {

    private String mFromUserId;
    private String mToUserId;

    private String mSessionId;
    private IceCandidate mBody;


    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.PEER);
        packet.addProperty("from", mFromUserId);
        packet.addProperty("to", mToUserId);

        JsonObject candidate = new JsonObject();
        candidate.addProperty("sdpMid", mBody.sdpMid);
        candidate.addProperty("sdpMLineIndex", mBody.sdpMLineIndex);
        candidate.addProperty("candidate", mBody.sdp);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", KlgeWebCommandTypes.PEER_ICE_CANDIDATE);
        payload.addProperty("session_id", mSessionId);
        payload.add("body", candidate);
        packet.add("payload", payload);

        return packet.toString();
    }

    public PeerIceCandidateWebCommand from(String from) {
        mFromUserId = from;
        return this;
    }

    public PeerIceCandidateWebCommand to(String to) {
        mToUserId = to;
        return this;
    }

    public PeerIceCandidateWebCommand sessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }

    public PeerIceCandidateWebCommand body(IceCandidate body) {
        mBody = body;
        return this;
    }
}
