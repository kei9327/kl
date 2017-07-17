package com.knowlounge.network.websocket.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class PeerInitWebCommand extends WebCommand {

    private String mFromUserId;
    private String mToUserId;

    private String mSessionId;
    private JsonElement mTurnServer;


    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.PEER);
        packet.addProperty("from", mFromUserId);
        packet.addProperty("to", mToUserId);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", KlgeWebCommandTypes.PEER_INIT);
        payload.addProperty("session_id", mSessionId);
        payload.add("turnserver", mTurnServer);
        packet.add("payload", payload);

        return packet.toString();
    }

    public PeerInitWebCommand from(String from) {
        mFromUserId = from;
        return this;
    }

    public PeerInitWebCommand to(String to) {
        mToUserId = to;
        return this;
    }

    public PeerInitWebCommand sessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }

    public PeerInitWebCommand turnServers(JsonElement turnServer) {
        mTurnServer = turnServer;
        return this;
    }
}
