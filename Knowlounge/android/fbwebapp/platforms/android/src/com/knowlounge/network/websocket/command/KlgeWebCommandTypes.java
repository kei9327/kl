package com.knowlounge.network.websocket.command;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-09.
 */

public final class KlgeWebCommandTypes {

    public static final String PEER = "peer";
    public static final String PEER_INIT = "init";
    public static final String PEER_OFFER_SDP = "offer_sdp";
    public static final String PEER_ANSWER_SDP = "answer_sdp";
    public static final String PEER_ICE_CANDIDATE = "ice_candidate";
    public static final String RTC_LIST = "rtcList";
    public static final String RTC_STATUS = "rtcStatus";
    public static final String RTC_UPDATE = "rtcUpdate";
    public static final String RTC_BUSY = "rtcBusy";
    public static final String RTC_BUSY_REQUEST = "request";
    public static final String RTC_BUSY_RESPONSE = "response";
}
