package com.knowlounge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.knowlounge.apprtc.KlgeClientController;
import com.knowlounge.apprtc.KlgeClientSinkCallback;
import com.knowlounge.apprtc.KlgeConnection;
import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.apprtc.WebSocketKlgeSession;
import com.knowlounge.model.RoomSpec;
import com.wescan.alo.rtc.RtcCapturer;
import com.wescan.alo.rtc.RtcChatArguments;
import com.wescan.alo.rtc.RtcChatClient;
import com.wescan.alo.rtc.RtcChatSession;
import com.wescan.alo.rtc.RtcErrorEvents;
import com.wescan.alo.rtc.RtcPeerChannel;
import com.wescan.alo.rtc.client.RtcClientActivity;
import com.wescan.alo.rtc.client.RtcClientController;

import org.appspot.apprtc.AppRTCAudioManager;
import org.appspot.apprtc.util.LooperExecutor;
import org.webrtc.Camera1Enumerator;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

import java.util.Map;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-07.
 */

@SuppressWarnings("unused")
public abstract class RtcSupportActivity extends RtcClientActivity {

    private static final String TAG = RtcSupportActivity.class.getSimpleName();

    private RtcPeerChannel mPeerChannel;

    protected AppRTCAudioManager mAudioManager;

    private RtcChatSession mSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // initialize AloRtc arguments
        RtcChatArguments.setDebugEnabled(false);
        RtcChatArguments.setTracingEnabled(false);
        RtcChatArguments.setVideoWidth(640);
        RtcChatArguments.setVideoHeight(480);

        // initialize AloRtc client
        RtcChatClient.instance().initialize(this, this);

        super.onCreate(savedInstanceState);

        /*
         * RtcChatClient 에러 처리 이벤트 핸들러 등록
         */
        RtcChatClient.instance().addRtcErrorEventsListener(mRtcErrorEvents);
    }

    @Override
    protected void onDestroy() {
        RtcChatClient.instance().destroy(this);
        super.onDestroy();

        if (getSessionFactory() instanceof WebSocketCreator) {
            ((WebSocketCreator)getSessionFactory()).stop();
        }

        /*
         * RtcChatClient 에러 처리 이벤트 핸들러 등록 해제
         */
        RtcChatClient.instance().removeRtcErrorEventsListener(mRtcErrorEvents);
    }

    public void destroy() {
        RtcChatClient.instance().destroy(this);

        if (getSessionFactory() instanceof WebSocketCreator) {
            ((WebSocketCreator)getSessionFactory()).stop();
        }

        /*
         * RtcChatClient 에러 처리 이벤트 핸들러 등록 해제
         */
        RtcChatClient.instance().removeRtcErrorEventsListener(mRtcErrorEvents);
    }

    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RtcChatContext>
     */

    /**
     * 비디오 캡쳐러 인스턴스를 생성하여 리턴한다. camera1, camera2, screen capturer...
     *
     * [UI Thread]
     *
     * ex 1) Camera1 or Camera2
     * Camera1Enumerator enumerator = new Camera1Enumerator();
     * return createVideoCapturer(enumerator)
     *
     * ex 2)
     * return new RtcCapturer(new ScreenCapturerAndroid(), false);
     */
    @Override
    public RtcCapturer createVideoCapturer() {
        Camera1Enumerator enumerator = new Camera1Enumerator(RtcChatArguments.isCaptureFramesToTexture());
        return createVideoCapturer(enumerator);
    }

    /**
     * RtcPeerChannel 인스턴스가 생성이 완료되고, RtcChatSession과 서로 바인딩이 완료되면 호출이 된다.
     *
     * [RTC Thread]
     *
     * ex)
     * WebSocketChatSession webSession = (WebSocketChatSession) session;
     * webSession.update(RtcChatState.STATE_CONNECTING);
     * @param session 생성된 peer connectin이 소속되어 있는 세션
     * @param peer 생성된 peer connectin 인스턴스
     */
    @Override
    public void onBuildPeerChannelComplete(final RtcChatSession session, final RtcPeerChannel peer) {
        Log.d(TAG, "<onBuildPeerChannelComplete>");
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<callbacks : RtcLifeCycleCallback>
     */

    @Override
    protected void onChannelOpen(RtcChatSession session) {
        mSession = session;
    }

    @Override
    protected void onRemoteDescription(RtcChatSession session, SessionDescription sdp, Map<String,String> properties) {

    }

    @Override
    protected void onRemoteIceCandidate(RtcChatSession session, IceCandidate candidate, Map<String,String> properties) {

    }

    @Override
    protected void onChannelClose(RtcChatSession session) {

    }

    @Override
    protected void onLocalDescription(RtcChatSession session, RtcPeerChannel peer, SessionDescription sdp) {

    }

    @Override
    protected void onIceCandidate(RtcChatSession session, RtcPeerChannel peer, IceCandidate candidate) {

    }

    @Override
    protected void onIceConnected(RtcChatSession session, RtcPeerChannel peer) {
        super.onIceConnected(session, peer);
    }

    @Override
    protected void onIceDisconnected(RtcChatSession session, RtcPeerChannel peer) {
        super.onIceDisconnected(session, peer);
    }

    @Override
    protected void onPeerConnectionStatsReady(RtcChatSession session, RtcPeerChannel peer, StatsReport[] reports) {
        /**
         * TODO 샘플 - RTC 기능
         */
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<methods>
     */

    /**
     * getRtcClientController() 메소드에서 인스턴스가 없을 경우에 호출되는 메소드로, 클라이언트의 RTC 기능을
     * 구현한 Controller의 인스턴스를 생성하여 리턴한다.
     *
     * ex)
     * <code>
     * RtcClientController createRtcClientController() {
     *      return new AloClientController(this, new SinkCallback());
     * }
     * </code>
     *
     * ...
     *
     * [inner class]
     * <code>
     * class SinkCallback implements AloClientSinkCallback<Args> {
     *
     *      public RtcChatSession newChat(Args arguments) {
     *          return RtcSampleActivity.this.newChat(argument);
     *      }
     *
     *      public void onRtcStart(RtcChatSession session) {
     *          RtcSampleActivity.this.onRtcStart(session);
     *      }
     * }
     * </code>
     *
     * ...
     *
     * 웹소켓의 시그널링 이벤트와 UI 간에 통신이 필요할 때 다음의 인터페이스를 확장하여 Activity와 통신을 한다.
     *
     * <code>
     * interface AloClientSinkCallback<A extends Args> extends RtcClientSinkCallback<A> {
     *      void custom(RtcChatSession session);
     * }
     * </code>
     */
    @Override
    protected RtcClientController createRtcClientController() {
        return new KlgeClientController(new SinkCallback(), getPeerWatcher());
    }

    public abstract KlgePeerWatcher getPeerWatcher();

    /**
     * getSessionFactory() 메소드에서 SessionFactory 인스턴스가 없을 경우 호출되는 메소드로, 클라이언트의 시그널
     * 프로토콜을 구현한 세션 인스턴스를 생성해주는 팩터리 클래스 인스턴스를 리턴한다.
     *
     * ex)
     * return new WebSocketCreator();
     *
     * ...
     *
     * [inner class]
     * class WebSocketCreator implements SessionFactory {
     *
     * }
     */
    @Override
    protected SessionFactory createSessionFactory() {
        return new WebSocketCreator();
    }

    protected KlgeClientController getController() {
        return (KlgeClientController) getRtcClientController();
    }

    protected abstract RtcChatSession newChat(KlgeConnection connection);

    /**
     * RTC session이 시작되었을 때 호출된다. startChat
     */
    protected void onRtcStart(RtcChatSession session) {

    }

    /**
     * Rtc session이 종료될 때 항상 호출된다. stopChat, closeChat
     */
    protected void onRtcStop(RtcChatSession session) {

    }

    /**
     * 모든 Rtc session이 종료될 때 호출된다. stopAllChats
     */
    protected void onRtcStopAll() {

    }

    protected void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {

    }

    protected void onFailRtcStatus(WebSocketKlgeSession session, JsonObject response) {

    }

    /**
     * peer_status 시그널 처리
     */
    protected void onPeerStatus(JsonObject response) {
        /**
         * TODO 추후 에러처리에 대한 대응 필요
         */
    }

    /**
     * peer_init 시그널 처리
     */
    protected void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {

    }

    /**
     * rtcBusy-request 를 받았을 때 응답 처리를 위한 준비
     */
    protected void onPeerBusyQuery(JsonObject response) {
        /**
         * TODO busy request를 받은 상태 처리에 대한 완전 무결한 대응 방안을 만수르님이 세울 예정.
         */
    }

    protected void onPeerBusyReply(JsonObject response) {
        /**
         * TODO busy response를 받은 상태 처리에 대한 완전 무결한 대응 방안을 만수르님이 세울 예정.
         */
    }

    protected void onReconnectSession() {

    }



    /**
     * AppRTCAudioManager 인스턴스를 생성하고 초기화한다.
     */
    public void startRtcAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = AppRTCAudioManager.create(this, new Runnable() {
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            mAudioManager.init();
        }
    }

    /**
     * AppRTCAudioManager를 종료한다.
     */
    public void stopRtcAudioManager() {
        if (mAudioManager != null) {
            mAudioManager.close();
            mAudioManager = null;
        }
    }

    /**
     * AppRTCAudioManager의 오디오 모드를 설정한다.
     *
     * @param mode the requested audio mode
     *             AudioManager.MODE_NORMAL,
     *             AudioManager.MODE_RINGTONE,
     *             AudioManager.MODE_IN_CALL,
     *             AudioManager.MODE_IN_COMMUNICATION
     */
    public void setRtcAudioMode(int mode) {
        if (mAudioManager != null) {
            mAudioManager.setAudioMode(mode);
        }
    }

    /**
     * AppRTCAudioManager의 resume 상태반영
     */
    protected void resumeRtcAudioManager() {
        if (mAudioManager != null) {
            mAudioManager.onResume();
        }
    }

    /**
     * AppRTCAudioManager의 pause 상태반영
     */
    protected void pauseRtcAudioManager() {
        if (mAudioManager != null) {
            mAudioManager.onPause();
        }
    }

    /**
     * 오디오 디바이스가 변경될 경우 호출된다.
     */
    protected void onAudioManagerChangedState() {

    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<inner>
     */

    /**
     * WebThread안에서 자바 웹소켓 인스턴스를 생성하는 팩터리 클래스
     */
    class WebSocketCreator implements SessionFactory {
        private LooperExecutor mWebThread;

        private WebSocketCreator() {

        }

        boolean isAlive() {
            return mWebThread != null && mWebThread.isAlive();
        }

        @Override
        public RtcChatSession create() {
            /**
             * Note!
             * Let the exception occur when chatContext is null, so that we can find out where the
             * error is.
             */
            WebSocketKlgeSession session = new WebSocketKlgeSession(RtcSupportActivity.this, getLooperThread());
            session.addRtcClientEventsListener(getController());
            return session;
        }

        void stop() {
            if (mWebThread != null) {
                mWebThread.requestStop();
            }
        }

        /**
         * @return websocket looper thread
         */
        LooperExecutor getLooperThread() {
            if (mWebThread == null) {
                mWebThread = new LooperExecutor();
                mWebThread.requestStart();
            }
            return mWebThread;
        }
    }

    /**
     * RTC 연결 이벤트 중계자. 웹소켓에 대한 이벤트를 해당뷰와 같이 처리하도록 한다.
     */
    class SinkCallback implements KlgeClientSinkCallback<KlgeConnection> {

        @Override
        public RtcChatSession newChat(KlgeConnection connection) {
            return RtcSupportActivity.this.newChat(connection);
        }

        @Override
        public void onRtcStart(RtcChatSession session) {
            RtcSupportActivity.this.onRtcStart(session);
        }

        @Override
        public void onIceConnected(RtcChatSession session, RtcPeerChannel peer) {

        }

        @Override
        public void onIceDisconnected(RtcChatSession rtcChatSession, RtcPeerChannel peer) {

        }

        @Override
        public void onIceFailed(RtcChatSession rtcChatSession, RtcPeerChannel peer) {

        }

        @Override
        public void onIceClosed(RtcChatSession rtcChatSession, RtcPeerChannel peer) {

        }


        @Override
        public void onRtcStop(RtcChatSession session) {
            RtcSupportActivity.this.onRtcStop(session);
        }

        @Override
        public void onRtcStopAll() {
            RtcSupportActivity.this.onRtcStopAll();
        }

        @Override
        public void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {
            RtcSupportActivity.this.onFailAuthMessage(session, response);
        }

        @Override
        public void onFailRtcStatus(WebSocketKlgeSession session, JsonObject response) {
            RtcSupportActivity.this.onFailRtcStatus(session, response);
        }

        @Override
        public void onPeerStatus(JsonObject response) {
            RtcSupportActivity.this.onPeerStatus(response);
        }

        @Override
        public void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {
            RtcSupportActivity.this.onPeerInit(fromUserId, turnServers, sessionId);
        }

        @Override
        public void onPeerBusyQuery(JsonObject response) {
            RtcSupportActivity.this.onPeerBusyQuery(response);
        }

        @Override
        public void onPeerBusyReply(JsonObject response) {
            RtcSupportActivity.this.onPeerBusyReply(response);
        }

        @Override
        public void onReconnectSession() {
            RtcSupportActivity.this.onReconnectSession();
        }
    }

    /**
     * RtcChatClient 에러 처리 이벤트 핸들러 구현
     */
    private RtcErrorEvents mRtcErrorEvents = new RtcErrorEvents() {

        /**
         * RtcChatClient 처리 시 발생하는 에러들을에 대하여 대응하도록 한다.
         *
         * ex)
         *  if (!isError) {
         *      isError = true;
         *  }
         */
        @Override
        public void onRtcClientError(String error) {

        }

        /**
         * RtcChatClient 종료 이벤트
         */
        @Override
        public void onRtcClientClosed() {

        }
    };


    public NetworkConnectionEvents mNetworkConnectionEvents;
    public interface NetworkConnectionEvents {
        void onConnected();
        void onDisconnected();
    }

    public void setNetworkConnectionEvents(NetworkConnectionEvents events) {
        mNetworkConnectionEvents = events;
    }

    public RoomEvents mRoomEvents;
    public interface RoomEvents {
        void initializeRoom(RoomSpec roomSpec);
    }

    public void setRoomEvents(RoomEvents events) {
        mRoomEvents = events;
    }
}
