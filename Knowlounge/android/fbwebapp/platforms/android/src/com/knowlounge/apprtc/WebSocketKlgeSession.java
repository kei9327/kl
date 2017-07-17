package com.knowlounge.apprtc;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.knowlounge.model.KlgePeer;
import com.knowlounge.network.websocket.command.KlgeWebCommandTypes;
import com.knowlounge.network.websocket.command.PeerAnswerSdpWebCommand;
import com.knowlounge.network.websocket.command.PeerBusyQueryWebCommand;
import com.knowlounge.network.websocket.command.PeerBusyReplyWebCommand;
import com.knowlounge.network.websocket.command.PeerIceCandidateWebCommand;
import com.knowlounge.network.websocket.command.PeerInitWebCommand;
import com.knowlounge.network.websocket.command.PeerOfferSdpWebCommand;
import com.knowlounge.network.websocket.command.PeerStatusWebCommand;
import com.knowlounge.network.websocket.command.PeerUpdateWebCommand;
import com.wescan.alo.rtc.RtcChatContext;
import com.wescan.alo.rtc.RtcChatSession;
import com.wescan.alo.rtc.RtcPeerChannel;

import org.appspot.apprtc.util.LooperExecutor;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 *
 * KnowLounge 웹소켓 구현
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

@SuppressWarnings("unused")
public class WebSocketKlgeSession extends RtcChatSession implements WebSocketChannelClient.WebSocketEvents {

    private static final String TAG = WebSocketKlgeSession.class.getSimpleName();

    public static final String PEER_STATE_BUSY = "BUSY";
    public static final String PEER_STATE_OK = "OK";

    private enum ConnectionState {
        NEW, CONNECTED, CLOSED, ERROR
    }

    private String mSessionId;
    private final LooperExecutor mExecutor;
    private WebSocketChannelClient mWebSocket;
    private ConnectionState mConnectionState;
    private KlgeConnection mConnection;

    private boolean isAlive;
    private long mBeginTimeMs = 0;

    /**
     * [user-id, peer_connection]
     */
    private HashMap<String, KlgePeerChannel> mChannels = new HashMap<>();

    private Set<RtcClientEvents> mRtcClientEvents
            = Collections.synchronizedSet(new LinkedHashSet<RtcClientEvents>());


    public WebSocketKlgeSession(RtcChatContext rtcChatContext, LooperExecutor executor) {
        super(rtcChatContext);
        mExecutor = executor;
        mConnectionState = ConnectionState.NEW;
        mSessionId = buildSessionId();
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RtcChatSession>
     */

    /**
     * Connects to ALO chat room
     * - function runs on a local looper thread.
     */
    @Override
    public void connect(final Connection connection) {
        Log.d(TAG, "<connect / ZICO>");
        if (!(connection instanceof KlgeConnection)) {
            throw new IllegalArgumentException("Wtf! KlgeConnection class needed as argument.");
        }

        mConnection = (KlgeConnection) connection;
        mBeginTimeMs = System.currentTimeMillis();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mConnectionState = ConnectionState.NEW;
                mWebSocket = new WebSocketChannelClient(mSessionId, mExecutor, WebSocketKlgeSession.this);

                Log.d(TAG, "<WebSocketKlgeSession> connecting to host: " + mConnection.toWssUrl() + " session-id: " + getSessionId());

                try {
                    mWebSocket.connect((KlgeConnection) connection);
                } catch (URISyntaxException
                        | IOException
                        | InterruptedException
                        | NoSuchAlgorithmException
                        | KeyManagementException e) {
                    Log.e(TAG, "<WebSocketChannelClient> WebSocket open exception ", e);
                    return;
                }

                notifyOnOpenSession(WebSocketKlgeSession.this);
            }
        });
    }


    /**
     * RtcChatSession의 abstract disconnect()를 오버라이드 함.
     */
    @Override
    public void disconnect() {
        Log.d(TAG, "<WebSocketKlgeSession / ZICO> disconnect() session-id: " + getSessionId());
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                runClose();
            }
        });
    }


    /**
     * RtcChatSession의 abstract close()를 오버라이드 함.
     */
    @Override
    public void close() {
        Log.e(TAG, "<WebSocketKlgeSession / ZICO> close() session-id: " + getSessionId());
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                runClose();
            }
        });
    }

    /**
     * @param peer mPeer connection instance
     * @param sdp sdp
     * @param properties extra properties. ex) from, to
     */
    @Override
    public void sendOfferSdp(RtcPeerChannel peer, final SessionDescription sdp, final Map<String,String> properties) {
        if (peer.isError()) {
            Log.e(TAG, "<WebSocketKlgeSession> sendOfferSdp() mPeer connection is not initialized yet.");
            return;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.CONNECTED) {
                    onWtfError("<WebSocketKlgeSession> sending offer SDP in non connected state.");
                    return;
                }

                Log.d(TAG, "<WebSocketKlgeSession / ZICO> sendOfferSdp()");

                String toUserId = properties.get("to");
                KlgePeerChannel channel = mChannels.get(toUserId);

                if (channel != null) {
                    if (!channel.isValidSignalSessionId()) {
                        Log.e(TAG, "<WebSocketKlgeSession / ZICO> sendOfferSdp() error occur while sending peer busy replay message. invalid signal session id");
                        return;
                    }

                    new PeerOfferSdpWebCommand()
                            .from(properties.get("from"))
                            .to(toUserId)
                            .body(sdp)
                            .sessionId(channel.getSignalSessionId())
                            .socket(mWebSocket)
                            .execute();
                }
            }
        });
    }

    @Override
    public void sendAnswerSdp(RtcPeerChannel peer, final SessionDescription sdp, final Map<String,String> properties) {
        if (peer.isError()) {
            Log.e(TAG, "<WebSocketKlgeSession / ZICO> sendAnswerSdp() mPeer connection is not initialized yet.");
            return;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.CONNECTED) {
                    onWtfError("<WebSocketKlgeSession / ZICO> sending answer SDP in non connected state.");
                    return;
                }

                Log.d(TAG, "<WebSocketKlgeSession / ZICO> sendAnswerSdp()");

                String toUserId = properties.get("to");
                KlgePeerChannel channel = mChannels.get(toUserId);

                if (!channel.isValidSignalSessionId()) {
                    Log.e(TAG, "<WebSocketKlgeSession / ZICO> sendAnswerSdp() error occur while sending peer busy replay message. invalid signal session id");
                    return;
                }

                new PeerAnswerSdpWebCommand()
                        .from(properties.get("from"))
                        .to(toUserId)
                        .body(sdp)
                        .sessionId(channel.getSignalSessionId())
                        .socket(mWebSocket)
                        .execute();
            }
        });
    }

    @Override
    public void sendLocalIceCandidate(RtcPeerChannel peer, final IceCandidate iceCandidate, final Map<String,String> properties) {
        if (peer.isError()) {
            Log.e(TAG, "<WebSocketKlgeSession / ZICO> sendLocalIceCandidate() mPeer connection is not initialized yet.");
            return;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.CONNECTED) {
                    onWtfError("<WebSocketKlgeSession / ZICO> sending local ICE candidate in non connected state");
                    return;
                }

                Log.d(TAG, "<WebSocketKlgeSession / ZICO> sendLocalIceCandidate()");

                String toUserId = properties.get("to");
                KlgePeerChannel channel = mChannels.get(toUserId);

                if (!channel.isValidSignalSessionId()) {
                    Log.e(TAG, "<WebSocketKlgeSession / ZICO> sendLocalIceCandidate() error occur while sending peer busy replay message. invalid signal session id");
                    return;
                }

                new PeerIceCandidateWebCommand()
                        .from(properties.get("from"))
                        .to(toUserId)
                        .body(iceCandidate)
                        .sessionId(channel.getSignalSessionId())
                        .socket(mWebSocket)
                        .execute();
            }
        });
    }

    @Override
    public void sendLocalIceCandidate(final RtcPeerChannel peer, final Map<String,String> properties) {
        if (peer.isError()) {
            Log.e(TAG, "<WebSocketKlgeSession> sendLocalIceCandidate() mPeer connection is not initialized yet.");
            return;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.CONNECTED) {
                    onWtfError("<WebSocketKlgeSession> sending local ICE candidate in non connected state");
                    return;
                }

                Log.d(TAG, "<WebSocketKlgeSession> sendLocalIceCandidate()");

                String toUserId = properties.get("to");
                KlgePeerChannel channel = mChannels.get(toUserId);

                if (!channel.isValidSignalSessionId()) {
                    Log.e(TAG, "<WebSocketKlgeSession> sendLocalIceCandidate() error occur while sending peer busy replay message. invalid signal session id");
                    return;
                }

                /**
                 * Note.
                 * LocalIceCandidate를 추가하거나 보내는 과정에서 ModificationConcurrentException이
                 * 발생함에 따라 동기화 처리 추가함.
                 */
                List<IceCandidate> candidates = peer.getLocalIceCandidate();
                if (candidates != null && candidates.size() > 0) {
                    synchronized (peer.getLocalCandidateLock()) {
                        for (IceCandidate iceCandidate : candidates) {
                            new PeerIceCandidateWebCommand()
                                    .from(properties.get("from"))
                                    .to(toUserId)
                                    .body(iceCandidate)
                                    .sessionId(channel.getSignalSessionId())
                                    .socket(mWebSocket)
                                    .execute();
                        }
                    }
                    candidates.clear();
                }

            }
        });
    }

    @Override
    public String getSessionId() {
        return mSessionId;
    }

    @Override
    public void addPeerChannel(RtcPeerChannel peer) {
        super.addPeerChannel(peer);
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        mChannels.put(klge.getToUserId(), klge);
    }

    @Override
    public void removePeerChannel(RtcPeerChannel peer) {
        super.removePeerChannel(peer);
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        mChannels.remove(klge.getToUserId());
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : WebSocketChannelClient.WebSocketEvents>
     */

    @Override
    public void onFailAuthMessage(final JsonObject response) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.ERROR) {
                    mConnectionState = ConnectionState.ERROR;
                    notifyOnFailAuthMessage(WebSocketKlgeSession.this, response);
                }
            }
        });
    }

    @Override
    public void onFailRtcStatus(final JsonObject response) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.ERROR) {
                    mConnectionState = ConnectionState.ERROR;
                    notifyOnFailRtcStatus(WebSocketKlgeSession.this, response);
                }
            }
        });
    }

    @Override
    public void onWebSocketOpen() {
        Log.d(TAG, "<WebSocketKlgeSession / ZICO> onWebSocketOpen() WebScoket opened. knowlounge session successfully authorized.");

        /**
         * Note!
         * start keep alive timer
         * new Timer()를 실행할 경우 user thread에서 실행된다. 이 상황에서 TimerTask에서 exception이
         * 발생하면 태스크가 작동을 하지 않는 현상이 발생한다.
         */
        //startKeepAlive();

        mConnectionState = ConnectionState.CONNECTED;
        notifyOnChannelOpen(this);
    }

    /**
     * 웹소켓 메세지를 받았을 때 호출된다.
     *  - response
     *  - notify
     */
    @Override
    public void onWebSocketMessage(String message) {
        if (mWebSocket.getState() != WebSocketChannelClient.WebSocketState.AUTHORIZED) {
            Log.e(TAG, "<WebSocketKlgeSession> Got WebSocket message in non registered state.");
            return;
        }

        Log.d(TAG, "<WebSocketKlgeSession / ZICO> onWebSocketMessage(): " + message);

        JsonObject response = new JsonParser().parse(message).getAsJsonObject();
        if (!response.has("cmd")) {
            throw new RuntimeException("onWebSocketMessage() WebSocket has illegal response message");
        }

        String command = response.get("cmd").getAsString();
        switch (command) {
            case KlgeWebCommandTypes.RTC_STATUS:
                onParseResponsePeerStatus(message);
                break;

            case KlgeWebCommandTypes.RTC_LIST:
                onParseNotifyPeerList(message);
                break;

            case KlgeWebCommandTypes.PEER:
                onParseResponsePeer(response);
                break;

            case KlgeWebCommandTypes.RTC_UPDATE:
                onParseResponsePeerUpdate(response);
                break;

            case KlgeWebCommandTypes.RTC_BUSY:
                onParseResponsePeerBusy(response);
                break;
        }

    }

    @Override
    public void onWebSocketClose() {
        Log.d(TAG, "<WebSocketKlgeSession / ZICO> onWebSocketClose() WebSocket closed.");

//        if (mWebSocket.getState() != WebSocketChannelClient.WebSocketState.AUTHORIZING) {
//            // TODO 인증 중에 웹소켓이 끊기면 재 연결 시도..
//            notifyOnReconnectSession();
//            return;
//        }

        // stop keep alive timer
        //stopKeepAlive();

        notifyOnChannelClose(this);

        notifyOnReconnectSession();

    }

    @Override
    public void onWebSocketError(String description) {
        onWtfError("<WebSocketKlgeSession> WebSocket error: " + description);
    }

    @SuppressWarnings("WeakerAccess")
    protected void onWtfError(final String description) {
        Log.e(TAG, description);

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionState != ConnectionState.ERROR) {
                    mConnectionState = ConnectionState.ERROR;
                    notifyOnChannelError(WebSocketKlgeSession.this, description);
                }
            }
        });
    }

    private void onParseResponsePeer(JsonObject response) {

        String fromId  = response.get("from").getAsString();
        String toId = response.get("to").getAsString();

        JsonObject payload = response.get("payload").getAsJsonObject();

        if (!payload.has("session_id") || TextUtils.isEmpty(payload.get("session_id").getAsString())) {
            Log.e(TAG, "<onParseResponsePeer> Received packet not have Session Id from user " + fromId);
            return;
        }

        String cmd = payload.get("cmd").getAsString();
        String sessionId = payload.get("session_id").getAsString();

        switch (cmd) {
            case KlgeWebCommandTypes.PEER_INIT:
                onParsePeerInit(fromId, toId, payload.get("turnserver").getAsJsonArray(), sessionId);
                break;

            case KlgeWebCommandTypes.PEER_OFFER_SDP:
                onParsePeerSdp(fromId, toId, payload.get("body").getAsJsonObject());
                break;

            case KlgeWebCommandTypes.PEER_ANSWER_SDP:
                onParsePeerSdp(fromId, toId, payload.get("body").getAsJsonObject());
                break;

            case KlgeWebCommandTypes.PEER_ICE_CANDIDATE:
                /**
                 * 웹에서 보내는 ice candidate에서 마지막 candidate은 null이 내려오므로 null 예외처리
                 */
                if (payload.get("body") instanceof JsonNull) {   // 웹에서는 마지막 candidate가 null로 내려오기 때문에 예외처리 - 2017.03.10
                    break;
                } else {
                JsonObject body =  payload.get("body").getAsJsonObject();
                    if (body != null) {
                        onParsePeerCandidate(fromId, toId, body);
                    }
                    break;
                }

        }
    }

    private void onParseResponsePeerBusy(JsonObject response) {
        JsonObject payload = response.get("payload").getAsJsonObject();
        String cmd = payload.get("cmd").getAsString();

        switch (cmd) {
            case KlgeWebCommandTypes.RTC_BUSY_REQUEST:
                onParseResponsePeerBusyQuery(response);
                break;

            case KlgeWebCommandTypes.RTC_BUSY_RESPONSE:
                onParseResponsePeerBusyReply(response);
                 break;
        }
    }

    private void notifyOnReconnectSession() {
        Log.d(TAG, "<WebSocketKlgeSession / ZICO> notifyOnReconnectSession() WebSocket retry connect.");
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onReconnectSession();
        }
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<methods>
     */

    static String buildSessionId() {
        return UUID.randomUUID().toString().replace("-","").substring(0, 6) + "_" + System.currentTimeMillis();
    }

    public long getTimeDuration() {
        return System.currentTimeMillis() - mBeginTimeMs;
    }

    public KlgePeerChannel find(String userId) {
        return mChannels.get(userId);
    }

    /**
     * 웹소켓 연결을 종료하고 모든 이벤트 리스너들을 제거한다.
     */
    private void runClose() {
        mConnectionState = ConnectionState.CLOSED;
        if (mWebSocket != null) {
            if (mWebSocket.isConnected()) {
                mWebSocket.disconnect(true);
            }
            mWebSocket = null;
        }

        notifyOnCloseSession(this);

        // WebRtc lifecycle events
        clearRtcConnectionEventsListener();
        clearRtcSignalEventsListener();

        // client events
        clearRtcClientEventsListener();
    }


    private void tryReconnect() {

    }

    private void notifyOnFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onFailAuthMessage(session, response);
        }
    }

    private void notifyOnFailRtcStatus(WebSocketKlgeSession session, JsonObject response) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onFailRtcStatus(session, response);
        }
    }

    private void notifyOnOpenSession(WebSocketKlgeSession session) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onOpenSession(session);
        }
    }

    private void notifyOnCloseSession(WebSocketKlgeSession session) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onCloseSession(session);
        }
    }

    /**
     * rtcStatus
     */
    public void sendPeerStatus(final Map<String,String> status) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                new PeerStatusWebCommand()
                        .userId(status.get("userno"))
                        .roomId(status.get("roomid"))
                        .requestId(getSessionId())
                        .video(status.get("video"))
                        .audio(status.get("audio"))
                        .osVersion(status.get("os_version"))
                        .device(status.get("device"))
                        .socket(mWebSocket)
                        .execute();
            }
        });
    }

    private void onParseResponsePeerStatus(String message) {
        notifyOnPeerStatus((JsonObject) new JsonParser().parse(message));
    }

    private void notifyOnPeerStatus(JsonObject response) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onPeerStatus(response);
        }
    }

    /**
     * rtcList
     */
    private void onParseNotifyPeerList(String message) {
        JsonObject notify = (JsonObject) new JsonParser().parse(message);
        List<KlgePeer> peers
                = new Gson().fromJson(notify.get("list"), new TypeToken<List<KlgePeer>>(){}.getType());

        notifyOnPeerList(peers);
    }

    private void notifyOnPeerList(List<KlgePeer> peers) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onPeerList(peers);
        }
    }

    /**
     * peer_init
     */
    public void sendPeerInit(final String from, final String to, final String signalSessionId, final JsonElement turnServers) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                new PeerInitWebCommand()
                        .from(from)
                        .to(to)
                        .sessionId(signalSessionId)
                        .turnServers(turnServers)
                        .socket(mWebSocket)
                        .execute();
            }
        });
    }

    private void onParsePeerInit(String from, String to, JsonArray payload, String sessionId) {
        // TODO 기존 정책으로 파싱할 것

        notifyOnPeerInit(from, payload, sessionId);
    }

    private void notifyOnPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {
        // TODO 기존 정책으로 파싱할 것

        for (RtcClientEvents event : mRtcClientEvents) {
            event.onPeerInit(fromUserId, turnServers, sessionId);
        }
    }

    /**
     * offer_sdp & answer_sdp
     */
    private void onParsePeerSdp(String from, String to, JsonObject body) {
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(
                        body.get("type").getAsString()), body.get("sdp").getAsString());

        Map<String,String> properties = new HashMap<>();
        properties.put("from", from);
        properties.put("to", to);

        notifyOnRemoteDescription(this, sdp, properties);
    }

    /**
     * remote ice_candidate
     */
    private void onParsePeerCandidate(String from, String to, JsonObject body) {
        IceCandidate candidate = new IceCandidate(
                body.get("sdpMid").getAsString(),
                body.get("sdpMLineIndex").getAsInt(),
                body.get("candidate").getAsString());

        Map<String,String> properties = new HashMap<>();
        properties.put("from", from);
        properties.put("to", to);

        notifyOnRemoteIceCandidate(this, candidate, properties);
    }

    /**
     * rtcUpdate
     */
    public void sendPeerUpdate(final String[] users, final String roomId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // TODO 사용자 리스트들을 정리해서 사용할 것.
                new PeerUpdateWebCommand()
                        .roomId(roomId)
                        .callers("")
                        .sessionId(getSessionId())
                        .execute();
            }
        });
    }

    private void onParseResponsePeerUpdate(JsonObject response) {
        // TODO 기존 정책으로 파싱할 것

        notifyOnPeerUpdate();
    }

    private void notifyOnPeerUpdate() {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onPeerUpdate();
        }
    }

    /**
     * rtcBusy request
     */
    public void sendPeerBusyQuery(final String from, final String to, final String signalSessionId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                /*
                 * 세션 아이디와 관련하여 전해 내려오는 전설.
                 * 세션 아이디는 offer 쪽에서 생성한 값으로 answer 쪽으로 전달하며
                 * answer측에서는 받은 세션 아이디를 가지고 시그널링을 한다.
                 */
                new PeerBusyQueryWebCommand()
                        .from(from)
                        .to(to)
                        .sessionId(signalSessionId)
                        .socket(mWebSocket)
                        .execute();
            }
        });
    }

    private void onParseResponsePeerBusyQuery(JsonObject response) {
        String fromUserId = response.get("from").getAsString();
        JsonObject payload = response.get("payload").getAsJsonObject();
        String signalSessionId = payload.get("session_id").getAsString();

        /*
         * 장문의 주석
         * offer를 보내는 측에서 session_id를 설정하지 않고 보내는 경우에는 에러를 발생 시켜야한다.
         * session_id는 보내는 측에서 answer에게 값을 전달하여 동일한 값을 사용하도록 유도한다.
         */
        if (TextUtils.isEmpty(signalSessionId)) {
            throw new RuntimeException("Invalid signaling session id!!!");
        }

        notifyOnPeerBusyQuery(response);
    }

    private void notifyOnPeerBusyQuery(JsonObject response) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onPeerBusyQuery(response);
        }
    }

    /**
     * rtcBusy response
     */
    public void sendPeerBusyReply(final String from, final String to, final String signalSessionId, final String status) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                new PeerBusyReplyWebCommand()
                        .from(from)
                        .to(to)
                        .status(status)
                        .sessionId(signalSessionId)
                        .socket(mWebSocket)
                        .execute();
            }
        });
    }

    private void onParseResponsePeerBusyReply(JsonObject response) {
        // TODO 기존 정책으로 파싱할 것

        notifyOnPeerBusyReply(response);
    }

    private void notifyOnPeerBusyReply(JsonObject response) {
        for (RtcClientEvents event : mRtcClientEvents) {
            event.onPeerBusyReply(response);
        }
    }

    public boolean isBusyNow() {
        final List<RtcPeerChannel> peers = getPeerChannels();
        int count = 0;
        boolean isBusy = false;

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (peers) {
            if (!peers.isEmpty()) {
                for (RtcPeerChannel peer : peers) {
                    KlgePeerChannel channel = (KlgePeerChannel) peer;
                    if (peer.iceConnectionState() == PeerConnection.IceConnectionState.NEW ||
                            peer.iceConnectionState() == PeerConnection.IceConnectionState.CHECKING) {
                        count++;
                    }
                }
            }
        }

        if (count > KlgePeerChannel.MAX_PEER_BUSY_COOUNT) {
            isBusy = true;
        }

        return isBusy;
    }

    public boolean isOpen() {
        return mWebSocket != null && mWebSocket.isConnected();
    }


    /*
     *----------------------------------------------------------------------------------------------
     * notify client events
     */

    public boolean addRtcClientEventsListener(RtcClientEvents events) {
        return mRtcClientEvents.add(events);
    }

    public boolean removeRtcClientEventsListener(RtcClientEvents events) {
        return mRtcClientEvents.remove(events);
    }

    public void clearRtcClientEventsListener() {
        mRtcClientEvents.clear();
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<events>
     */

    @SuppressWarnings("WeakerAccess")
    public interface RtcClientEvents {
        void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response);

        void onFailRtcStatus(WebSocketKlgeSession session, JsonObject response);

        void onOpenSession(WebSocketKlgeSession session);

        void onCloseSession(WebSocketKlgeSession session);

        void onPeerStatus(JsonObject response);

        void onPeerList(List<KlgePeer> peers);

        void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId);

        void onPeerUpdate();

        void onPeerBusyQuery(JsonObject response);

        void onPeerBusyReply(JsonObject response);

        void onReconnectSession();
    }
}
