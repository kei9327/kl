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

public class PeerBusyReplyWebCommand extends WebCommand {

    private String mFromUserId;
    private String mToUserId;
    private String mSessionId;
    private String mStatus;

    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.RTC_BUSY);
        packet.addProperty("from", mFromUserId);
        packet.addProperty("to", mToUserId);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", KlgeWebCommandTypes.RTC_BUSY_RESPONSE);
        payload.addProperty("session_id", mSessionId);
        payload.addProperty("status", mStatus);
        packet.add("payload", payload);

        return packet.toString();
    }

    public PeerBusyReplyWebCommand from(String from) {
        mFromUserId = from;
        return this;
    }

    public PeerBusyReplyWebCommand to(String to) {
        mToUserId = to;
        return this;
    }

    public PeerBusyReplyWebCommand sessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }

    /**
     * @param status BUSY 상태 값 {"OK", "BUSY"}
     */
    public PeerBusyReplyWebCommand status(String status) {
        mStatus = status;
        return this;
    }
}
