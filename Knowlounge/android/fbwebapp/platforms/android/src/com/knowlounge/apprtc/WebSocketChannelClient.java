package com.knowlounge.apprtc;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.appspot.apprtc.util.LooperExecutor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

@SuppressWarnings("WeakerAccess")
public class WebSocketChannelClient {
    private static final String TAG = WebSocketChannelClient.class.getSimpleName();

    /**
     * Possible WebSocket connection states.
     */
    public enum WebSocketState {
        NEW, CONNECTED, AUTHORIZING, AUTHORIZED, CLOSED, ERROR
    }

    /**
     * Callback interface for messages delivered on WebSocket.
     * All events are dispatched from a looper executor thread.
     */
    public interface WebSocketEvents {
        void onFailAuthMessage(JsonObject response);
        void onFailRtcStatus(JsonObject response);
        void onWebSocketOpen();
        void onWebSocketMessage(String message);
        void onWebSocketClose();
        void onWebSocketError(String description);
    }

    private final LooperExecutor mExecutor;
    private final WebSocketEvents mEvents;
    private final String mSessionId;
    private WebSocketChannel mWsSocket;
    private WebSocketState mWsSocketState;
    private String mWsSocketUrl;
    private KlgeConnection mConnection;

    // WebSocket send queue. Messages are added to the queue when WebSocket
    // client is not registered and are consumed in subscribe() call.
    private final LinkedList<String> mWsSendQueue = new LinkedList<>();


    public WebSocketChannelClient(String sessionId, LooperExecutor executor, WebSocketEvents events) {
        mExecutor = executor;
        mEvents = events;
        mWsSocketState = WebSocketState.NEW;
        mSessionId = sessionId;
    }

    /**
     * Helper method for debugging purposes. Ensures that WebSocket method is
     * called on a looper thread.
     */
    private void checkIsOnValidThread() {
        if (!mExecutor.checkOnLooperThread()) {
            throw new IllegalArgumentException("WebSocket method is not called on valid thread");
        }
    }

    public void connect(KlgeConnection connection)
            throws URISyntaxException, IOException, InterruptedException, KeyManagementException, NoSuchAlgorithmException {
        checkIsOnValidThread();

        if (mWsSocketState != WebSocketState.NEW) {
            Log.e(TAG, "<WebSocketChannelClient / ZICO> WebSocket is already connected.");
            return;
        }

        mConnection = connection;
        mWsSocketUrl = connection.toWssUrl();

        Log.d(TAG, "<WebSocketChannelClient / ZICO> connecting WebSocket to: " + mWsSocketUrl + " instance: " + this);

        mWsSocket = new WebSocketChannel(new URI(mWsSocketUrl));
        connectSslSocket(mWsSocket);
    }

    private void connectSslSocket(WebSocketChannel channel) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext ssl = SSLContext.getInstance("TLSv1");
        /**
         * will use java's default key and trust store which is sufficient unless you deal with
         * self-signed certificates
         */
        ssl.init(null, null, null);

        SocketFactory factory = ssl.getSocketFactory();
        channel.setSocket(factory.createSocket());
        channel.connectBlocking();
    }

    private void authorize() {
        checkIsOnValidThread();

        if (mWsSocketState != WebSocketState.CONNECTED) {
            Log.e(TAG, "<WebSocketChannelClient / ZICO> WebSocket subscribe() in state " + mWsSocketState);
            return;
        }

        Log.d(TAG, "<WebSocketChannelClient / ZICO> authorizing WebSocket ");

        try {

            // presence packet
            String packet = new AuthCommand(mSessionId)
                    .zicoAccessToken(mConnection.getZicoAccessToken())
                    .userId(mConnection.getUserId())
                    .roomId(mConnection.getRoomId())
                    .execute();

            mWsSocket.send(packet);
            mWsSocketState = WebSocketState.AUTHORIZING;
            Log.d(TAG, "<WebSocketChannelClient / ZICO> sending login packet: " + packet);

        } catch (JSONException e) {
            Log.e(TAG, "<WebSocketChannelClient / ZICO> WebSocket json error: " + e.getMessage());
        }
    }

    public void disconnect(boolean waitForComplete) {
        checkIsOnValidThread();

        Log.d(TAG, "<WebSocketChannelClient / ZICO> disconnecting WebSocket. State: " + mWsSocketState);
        if (mWsSocketState == WebSocketState.AUTHORIZED) {
            mWsSocketState = WebSocketState.CONNECTED;
        }

        /**
         * Note!
         * 만약 웹소켓 메세지가 남아있다면, 모두 클리어시켜서 메세지를 실행시키지 못하도록 방지한다.
         * 웹소켓 시작과 동시에 조욜하면, sendqueue에 메세지가 남아있고 이를 실행하면서 에러가 발생하기때문.
         */
        mWsSendQueue.clear();

        // Close WebSocket in CONNECTED or ERROR states only.
        if (mWsSocketState == WebSocketState.CONNECTED || mWsSocketState == WebSocketState.ERROR) {
            if (waitForComplete) {
                try {
                    mWsSocket.closeBlocking();
                } catch (InterruptedException e) {
                    Log.e(TAG, "<WebSocketChannelClient / ZICO> wait error: ", e);
                }
            } else {
                Log.d(TAG, "<WebSocketChannelClient / ZICO> WebSocketClient close()");
                mWsSocket.close();
            }

            mWsSocketState = WebSocketState.CLOSED;
        }
        Log.d(TAG, "<WebSocketChannelClient / ZICO> Closing WebSocket done");
    }

    public void send(String message) {
        checkIsOnValidThread();

        switch (mWsSocketState) {
            case NEW:
            case CONNECTED:
                // Store outgoing messages and send them after WebSocket client is registered.
                Log.d(TAG, "<WebSocketChannelClient / ZICO> insert message into SendQueue " + message);
                mWsSendQueue.add(message);
                return;

            case ERROR:
            case CLOSED:
                Log.e(TAG, "<WebSocketChannelClient / ZICO> WebSocket send() in error or closed state : " + message);
                return;

            case AUTHORIZED:
                Log.d(TAG, "<WebSocketChannelClient / ZICO> send(): " + message);
                if (mWsSocket.isOpen())  // WebsocketNotConnectedException 현상을 방지하기 위해 소켓이 열려있는지 여부를 확인한 후에 send() 한다.
                    mWsSocket.send(message);
//                else
//                    retrySend(message);
                break;
        }
    }

    public WebSocketState getState() {
        return mWsSocketState;
    }

    public boolean isConnected() {
        return mWsSocket != null && mWsSocket.isOpen();
    }

    public String getSessionId() {
        return mSessionId;
    }

    /*
     * send할 때 WebsocketNotConnectedException이 발생하는 현상에 대응하기 위해, 웹소켓이 OPEN된 상태일 때만 send를 수행하고 아니면 OPEN될때까지 재시도하는 메서드를 추가함.
     * - 보류 중
     */
    int sendCnt = 0;
    private void retrySend(final String message) {
        final Timer timer = new Timer();
        TimerTask setUpViewTask = new TimerTask() {
            @Override
            public void run() {
                if (mWsSocket.isOpen() && mWsSocketState == WebSocketState.AUTHORIZED) {
                    send(message);
                    timer.cancel();
                    sendCnt = 0;
                }
                if (sendCnt == 3) {
                    timer.cancel();
                    sendCnt = 0;
                }
                sendCnt++;
            }
        };
        timer.schedule(setUpViewTask, 0, 500);
    }

    private void onAuthMessage(JsonObject response) throws JSONException {
        checkIsOnValidThread();

        String requestId = response.get("request_id").getAsString();
        int result = response.get("result").getAsInt();
        //String msg = response.getString("msg");

        Log.d(TAG, "<onAuthMessage> result : " + result);
        if (result == 0) {
            mWsSocketState = WebSocketState.AUTHORIZED;

            mEvents.onWebSocketOpen();

            // Send any previously accumulated messages.
            for (String message : mWsSendQueue) {
                send(message);
            }
            mWsSendQueue.clear();

        } else {
            onWtfError("<WebSocketChatSession> WebSocket failed to authorize reason: ");

            mEvents.onFailAuthMessage(response);
        }
    }

    private void onStatusMessage(JsonObject response) {
        int result = response.get("result").getAsInt();
        if (result == 0) {

        } else if (result == -117) { // DUPLICATED_SESSION

        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void onWtfError(final String description) {
        Log.e(TAG, description);

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mWsSocketState != WebSocketState.ERROR) {
                    mWsSocketState = WebSocketState.ERROR;
                    mEvents.onWebSocketError(description);
                }
            }
        });
    }


    private class WebSocketChannel extends WebSocketClient {

        public WebSocketChannel(URI serverURI) {
            super(serverURI);
        }

        public WebSocketChannel(URI serverUri, Draft draft) {
            super(serverUri, draft);
        }

        public WebSocketChannel(URI serverUri, Draft draft, Map<String, String> headers, int connecttimeout) {
            super(serverUri, draft, headers, connecttimeout);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d(TAG, "<WebSocketChannelClient> WebSocket connection opened to: " + mWsSocketUrl + ", http status code: " + handshakedata.getHttpStatus());

            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mWsSocketState = WebSocketState.CONNECTED;

                    // Check if we have pending subscribe request.
                    authorize();
                }
            });
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d(TAG, "<WebSocketChannelClient> WebSocket connection closed(" + code + "). reason: " + reason + ". State: " + mWsSocketState);

            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mWsSocketState != WebSocketState.CLOSED) {
                        mWsSocketState = WebSocketState.CLOSED;
                        mEvents.onWebSocketClose();
                    }
                }
            });
        }

        @Override
        public void onError(Exception ex) {
            Log.e(TAG, "<WebSocketChannelClient> WebSocket error. ", ex);
        }

        @Override
        public void onMessage(final String message) {
            Log.d(TAG, "<WebSocketChannelClient> onMessage(): " + message);

            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    JsonObject response = new JsonParser().parse(message).getAsJsonObject();
                    if (mWsSocketState == WebSocketState.CONNECTED || mWsSocketState == WebSocketState.AUTHORIZING) {
                        try {
                            String command = response.get("cmd").getAsString();
                            if (command.equals("rtcAuth")) {
                                onAuthMessage(response);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "<WebSocketChannelClient> WebSocket json error: " + e.toString());
                        }
                    }
                    else if (mWsSocketState == WebSocketState.AUTHORIZED) {
                        String command = response.get("cmd").getAsString();
                        if (command.equals("rtcStatus")) {
                            onStatusMessage(response);
                        } else {
                            mEvents.onWebSocketMessage(message);
                        }
                    }
                }
            });
        }
    }

    private static class AuthCommand {
        private String mZicoAccessToken;
        private String mUserId;
        private String mRoomId;
        private String mSessionId;

        public AuthCommand(String sessionId) {
            mSessionId = sessionId;
        }

        public String execute() throws JSONException {
            JSONObject packet = new JSONObject();
            packet.put("route", "rtc");
            packet.put("cmd", "rtcAuth");
            packet.put("request_id", mSessionId);
            packet.put("access_token", mZicoAccessToken);
            packet.put("roomid", mRoomId);
            packet.put("userno", mUserId);
            return packet.toString();
        }

        AuthCommand zicoAccessToken(String zicoAccessToken) {
            mZicoAccessToken = zicoAccessToken;
            return this;
        }

        AuthCommand userId(String userId) {
            mUserId = userId;
            return this;
        }

        AuthCommand roomId(String roomId) {
            mRoomId = roomId;
            return this;
        }
    }
}
