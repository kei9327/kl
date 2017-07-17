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

public class PeerStatusWebCommand extends WebCommand {

    private String mRequestId;
    private String mUserId;
    private String mRoomId;
    private String mVideo;
    private String mAudio;
    private String mAndroidVersion;
    private String mDeviceName;


    @Override
    String makeJson() {
        JsonObject packet = new JsonObject();
        packet.addProperty("route", "rtc");
        packet.addProperty("cmd", KlgeWebCommandTypes.RTC_STATUS);
        packet.addProperty("request_id", mRequestId);
        packet.addProperty("roomid", mRoomId);
        packet.addProperty("userno", mUserId);

        JsonObject status = new JsonObject();
        status.addProperty("video", mVideo);
        status.addProperty("audio", mAudio);
        packet.add("status", status);

        JsonObject device = new JsonObject();
        device.addProperty("os", "android");
        device.addProperty("os_version", mAndroidVersion);
        device.addProperty("device", mDeviceName);
        packet.add("description", device);

        return packet.toString();
    }

    public PeerStatusWebCommand requestId(String requestId) {
        mRequestId = requestId;
        return this;
    }

    public PeerStatusWebCommand userId(String userId) {
        mUserId = userId;
        return this;
    }

    public PeerStatusWebCommand roomId(String roomId) {
        mRoomId = roomId;
        return this;
    }

    public PeerStatusWebCommand video(String video) {
        mVideo = video;
        return this;
    }

    public PeerStatusWebCommand audio(String audio) {
        mAudio = audio;
        return this;
    }

    public PeerStatusWebCommand osVersion(String osVersion) {
        mAndroidVersion = osVersion;
        return this;
    }

    public PeerStatusWebCommand device(String deviceName) {
        mDeviceName = deviceName;
        return this;
    }
}
