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

public class PeerUpdateWebCommand extends WebCommand {

    private String mSessionId;
    private String mRoomId;
    private String mCallers;

    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.RTC_UPDATE);
        packet.addProperty("request_id", mSessionId);
        packet.addProperty("roomid", mRoomId);
        packet.addProperty("caller", mCallers);

        return packet.toString();
    }

    public PeerUpdateWebCommand sessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }

    public PeerUpdateWebCommand roomId(String roomId) {
        mRoomId = roomId;
        return this;
    }

    public PeerUpdateWebCommand callers(String callers) {
        mCallers = callers;
        return this;
    }
}
