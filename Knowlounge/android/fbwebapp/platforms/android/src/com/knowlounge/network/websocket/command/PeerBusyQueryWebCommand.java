package com.knowlounge.network.websocket.command;

import com.google.gson.JsonObject;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class PeerBusyQueryWebCommand extends WebCommand {

    private String mFromUserId;
    private String mToUserId;
    private String mSessionId;

    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.RTC_BUSY);
        packet.addProperty("from", mFromUserId);
        packet.addProperty("to", mToUserId);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", KlgeWebCommandTypes.RTC_BUSY_REQUEST);
        payload.addProperty("session_id", mSessionId);
        packet.add("payload", payload);

        return packet.toString();
    }

    public PeerBusyQueryWebCommand from(String from) {
        mFromUserId = from;
        return this;
    }

    public PeerBusyQueryWebCommand to(String to) {
        mToUserId = to;
        return this;
    }

    public PeerBusyQueryWebCommand sessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }
}
