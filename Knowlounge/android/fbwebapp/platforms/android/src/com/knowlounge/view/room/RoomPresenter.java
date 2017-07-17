package com.knowlounge.view.room;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.apprtc.KlgeClientController;
import com.knowlounge.apprtc.KlgeConnection;
import com.knowlounge.apprtc.KlgePeerChannel;
import com.knowlounge.apprtc.KlgePeerNode;
import com.knowlounge.apprtc.WebSocketKlgeSession;
import com.knowlounge.dagger.component.MultiVideoChatFragmentComponent;
import com.knowlounge.model.KlgePeer;
import com.knowlounge.network.restful.func.ListApiRetry;
import com.knowlounge.network.restful.zico.command.FetchRtcServerRestCommand;
import com.knowlounge.network.restful.zico.command.FetchTurnServerRestCommand;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.base.BasePresenter;
import com.knowlounge.dagger.scopes.PerFragment;
import com.wescan.alo.rtc.RtcChatClient;
import com.wescan.alo.rtc.RtcChatContext;
import com.wescan.alo.rtc.RtcMediaChannel;
import com.wescan.alo.rtc.RtcPeerChannel;

import org.webrtc.PeerConnection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 * Description : MVP 모델에서 Presenter에 해당하는 클래스
 * ZICO RTC에 필요한 로직들이 정의되어 있다. 상황에 따라 필요한 로직을 처리하고 뷰 (엑티비티, 혹은 Fragment) 쪽에 이벤트를 호출시켜주는 방식으로 구성된다.
 */

@PerFragment
public class RoomPresenter extends BasePresenter<RoomView> {

    private final static String TAG = RoomPresenter.class.getSimpleName();

    private static final int BUSY_QUERY_INTERVAL = 1000;
    private static final int MAX_BUSY_PEER_COUNT = 1;
    private RoomSpec mRoomSpec;


    private MultiVideoChatFragmentComponent mComponent;

    private WebSocketKlgeSession mSession;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * mPeerSessionCache : key : userNo, value : signal-session-id
     * 사용자와 시그널 세션 아이디를 잠시 저장해 놓은 맵으로서. 상대방과 연결을 하기 위하여 할당해 놓은
     * 시그널 세션 아이디를 잠시 가지고 있다가 세션이 시작하고 필요한 시점에서
     * KlgePeerChannel을 생성하면서 시그널 세션아이디로 전달한다.
     */
    private LinkedHashMap<String, String> mPeerSessionCache = new LinkedHashMap<>();

    private ConcurrentLinkedQueue<String> waitingQueue = new ConcurrentLinkedQueue<String>();
    private ConcurrentLinkedQueue<String> connectingQueue = new ConcurrentLinkedQueue<String>();

    @Inject
    public RoomPresenter(RoomView view) {
        bindView(view);
    }

    /**
     * ZICO RTC의 진입점이 되는 메서드
     * WebSocketClient를 생성하고 소켓 연결을 시도하며 연결이 완료되면 rtcAuth 인증절차를 수행한다.
     * rtcAuth 인증절차가 완료되면 onChannelOpen 이벤트를 호출하게 되는데
     * onChannelOpen 에서는 내 RTC 미디어 상태를 서버에 업데이트 하는 rtcStatus 프로토콜을 호출한다.
     * onChannelOpen은 RoomActivity에 재정의 되어있으므로 상세한 내용은 RoomActivity 내 오버라이드된 구문을 참조해보기.
     *
     */
    @Override
    public void start() {
        Log.d(TAG, "<start / ZICO> Session build..");
        Log.d(TAG, "<start / ZICO> mRoomSpec.getHost() : " + mRoomSpec.getHost());
        Log.d(TAG, "<start / ZICO> mRoomSpec.getPort() : " + mRoomSpec.getPort());
        Log.d(TAG, "<start / ZICO> mRoomSpec.getName() : " + mRoomSpec.getName());
        Log.d(TAG, "<start / ZICO> mRoomSpec.getRoomId() : " + mRoomSpec.getRoomId());
        /*
         * Step
         * 1. 웹소켓 연결.
         *
         * 인증 성공시
         * 2. RoomActivity.onChannelOpen() 에서 sendPeerStatus() 실행
         * 3. RoomActivity.onPeerStatus()
         * 4. RoomActivity.onPeerCreate() or RoomActivity.onDestroyPeer()
         *
         * 인증 실패시
         * 2. RoomActivity.onFailAuthMessage() 에러 처리
         */
        if (mSession == null) {
            mSession = (WebSocketKlgeSession) getController().startChat(new KlgeConnection.Builder()
                    .host(mRoomSpec.getHost())
                    .port(mRoomSpec.getPort())
                    .zicoAccessToken(mRoomSpec.getAccessToken())
                    .userId(mRoomSpec.getUserNo())
                    .roomId(mRoomSpec.getRoomId())
                    .build());
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        super.destroy();

        if (mSession != null) {
            getController().stopAllChats(getRtcContext());
            //Log.d(TAG, "<ReadyScreenFragment> onDestroy() total peer size : " + mSession.getPeerChannels().size());
            mSession = null;
        }
    }

    private KlgeClientController getController() {
        return mComponent.getController();
    }

    private RtcChatContext getRtcContext() {
        return mComponent.getRtcContext();
    }

    public void initialize(RoomSpec roomSpec, MultiVideoChatFragmentComponent component) {
        mRoomSpec = roomSpec;
        mComponent = component;
    }


    /**
     * 새로운 Peer를 생성해야 될 때 호출되는 메서드이다.
     * 새로운 PeerChannel 인스턴스를 생성하고, 상대방에서 Busy 질의 패킷을 전송한다.
     * - Busy 로직 설명
     *   사전 준비 : 2개의 Queue가 필요함
     *      - waitingQueue : Busy 질의를 보내야 하는 대기 유저들에 대한 Queue.
     *      - connectingQueue : Busy 질의를 보내고 Busy 응답을 기다리고 있는 유저에 대한 Queue.
     *   BUSY 여부를 판단하는 기준 : connectingQueue의 사이즈가 MAX_BUSY_PEER_COUNT 보다 크면 BUSY, 아니면 OK
     *
     * [송출자 Bob의 시점]
     *   1) 새로운 Peer인 Alice가 들어오면 먼저 waitingQueue에 Alice를 추가한다.
     *   2) connectingQueue 현재 사이즈를 체크하고 가용 범위 내에 해당하면 Alice에게 웹소켓으로 Busy 질의를 날린다. 그리고 connectingQueue에 Alice를 추가하고, waitingQueue에서는 Alice를 삭제한다.
     *     - 만약 Alice의 Busy 응답이 바쁘다고 오면, waitingQueue에 Alice를 다시 넣고, connectingQueue에서는 Alice를 삭제한다.
     *   3) ice connection state를 체크하여 connected로 rtc 연결이 수립된 시점에 connectingQueue에서도 Alice를 삭제해준다.
     *
     * [수신자 Alice의 시점]
     *   1) Bob에게서 Busy 질의 패킷을 받으면 waitingQueue에서 Bob을 삭제하고, connectingQueue에 Bob을 추가한다. 그리고 Busy 응답을 Bob에게 보낸다.
     *     - 만약 Alice가 바쁘면, waitingQueue에 Bob을 다시 넣고, connectingQueue에서는 Bob을 삭제한다.
     *   2) Bob과의 iceConnectionState를 체크하여 connected로 RTC 연결이 수립된 시점에 connectingQueue에서 Bob을 삭제해준다.
     *
     * @param toUserId
     * @param peer
     */
    void onPeerCreate(final String toUserId, final KlgePeerNode peer) {
        Log.d(TAG, "<onPeerCreate / ZICO> user: "+ toUserId);

        KlgePeerChannel channel = mSession.find(peer.getPeer().getUserId());
        if (channel != null) {
            Log.d(TAG, "onPeerCreate() user(" + toUserId + ") already exist with session: " + mSession.getSessionId());
            return;
        }

        waitingQueue.add(toUserId);
        Log.d(TAG, "<onPeerCreate / ZICO> 새로 입장한 유저는 waitingQueue에 추가됩니다.");
        Log.d(TAG, "<onPeerCreate / ZICO> waitingQueue : " + waitingQueue.toString());
        Log.d(TAG, "<onPeerCreate / ZICO> connectingQueue : " + connectingQueue.toString());

        final Timer timer = new Timer();
        final TimerTask setUpViewTask = new TimerTask() {
            @Override
            public void run() {
                /**
                 * 큐에 제한된 개수 만큼 남아있을 때까지 TimerTask를 구동시킨다.
                 */
                if (connectingQueue.size() < MAX_BUSY_PEER_COUNT) {
                    if (!connectingQueue.contains(toUserId))
                        connectingQueue.add(toUserId);
                    waitingQueue.remove(toUserId);
                    Log.d(TAG, "<onPeerCreate / ZICO> Busy 요청을 보내므로 connectingQueue에 넣습니다.");
                    Log.d(TAG, "<onPeerCreate / ZICO> waitingQueue : " + waitingQueue.toString());
                    Log.d(TAG, "<onPeerCreate / ZICO> connectingQueue : " + connectingQueue.toString());

                    String signalSessionId = KlgePeerChannel.buildSessionId();
                    mPeerSessionCache.put(toUserId, signalSessionId);

                    mSession.sendPeerBusyQuery(mRoomSpec.getUserNo(), toUserId, signalSessionId);
                    // TODO rtcBusy를 보내는 시점에서 busy 상태를 업데이트한다.
                    timer.cancel();
                }
            }
        };
        timer.schedule(setUpViewTask, 0, 1000);

//        if (connectingQueue.size() < MAX_BUSY_PEER_COUNT) {
//            /*
//             * BUSY 쿼리 발신할 때에는 상대방의 시그널 세션을 큐에 저장한다.
//             */
//            if (!connectingQueue.contains(toUserId))
//                connectingQueue.add(toUserId);
//            waitingQueue.remove(toUserId);
//            Log.d(TAG, "<onPeerCreate / ZICO> Busy 요청을 보내므로 connectingQueue에 넣습니다.");
//            Log.d(TAG, "<onPeerCreate / ZICO> waitingQueue : " + waitingQueue.toString());
//            Log.d(TAG, "<onPeerCreate / ZICO> connectingQueue : " + connectingQueue.toString());
//
//            String signalSessionId = KlgePeerChannel.buildSessionId();
//            mPeerSessionCache.put(toUserId, signalSessionId);
//
//            mSession.sendPeerBusyQuery(mRoomSpec.getUserNo(), toUserId, signalSessionId);
//            // TODO rtcBusy를 보내는 시점에서 busy 상태를 업데이트한다.
//        }
    }



    void onPeerDestroy(String fromUserId, final KlgePeerNode peer) {
        Log.d(TAG, "<onPeerDestroy / ZICO> user: " + fromUserId + " left peer count: " + mComponent.getController().getPeerWatcher().getPeerCount());
        KlgePeerChannel channel = mSession.find(fromUserId);
        if (channel != null) {
            channel.close();
            if (getMvpView() != null)
                getMvpView().removeRemoteVideoView(peer.getPeer().getUserId());
            else
                Log.e(TAG, "<onPeerDestroy / ZICO> getMvpView() is null");
        }
        removeQueue(fromUserId);
    }


    /**
     * 상대방이 비송출자 유저인 경우, 영상 UI를 제거함.
     * @param fromUserId
     * @param peer
     */
    void onRemoteNotCaller(String fromUserId, final KlgePeerNode peer) {
        Log.d(TAG, "<onRemoteNotCaller / ZICO> user: " + fromUserId + ", left peer count: " + mComponent.getController().getPeerWatcher().getPeerCount());
        getMvpView().removeRemoteVideoView(peer.getPeer().getUserId());
    }


    void onRemoteCaller(String fromUserId, final KlgePeerNode peer) {
        Log.d(TAG, "<onRemoteCaller / ZICO>");
        KlgePeerChannel channel = mSession.find(fromUserId);
        //getMvpView().addRemoteVideoView(peer.getPeer().getUserId());
    }

    void onPeerUpdate(String fromUserId, KlgePeerNode peer) {

    }

    /**
     * 송출자로부터 peer init 패킷을 받았을 때 호출되는 메서드
     * @param fromUserId
     * @param turnServers
     * @param sessionId
     */
    void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {
        Log.d(TAG, "<onPeerInit / ZICO> user(" + fromUserId + ") into session: " + mSession.getSessionId());

        String signalSessionId = mPeerSessionCache.remove(fromUserId);
        if (signalSessionId == null) {
            signalSessionId = sessionId;
            mPeerSessionCache.put(fromUserId, signalSessionId);
        }
        if (TextUtils.isEmpty(signalSessionId)) {
            Timber.e("onPeerInit() invalid session id from user(%s). map error.", signalSessionId);
            return;
        }

        KlgePeerChannel channel;
//        if (mSession.find(fromUserId) != null) {
//            Log.d(TAG, "리커넥트가 들어왔네?? 뷰를 삭제합시다~~");
//            channel = mSession.find(fromUserId);
//            channel.close();
//            //mSession.removePeerChannel(channel);
//
//            getMvpView().removeRemoteVideoView(fromUserId);
//
//            KlgePeerChannel newChannel = new KlgePeerChannel(mSession, false, fromUserId, signalSessionId);
//            channel = newChannel;
//        } else {
//            Log.d(TAG, "새로운 피어니까 커넥션 맺자..");
//            // peer connection for answer
//            channel = new KlgePeerChannel(mSession, false, fromUserId, signalSessionId);
//        }

        channel = new KlgePeerChannel(mSession, false, fromUserId, signalSessionId);
        //getMvpView().removeRemoteVideoView(fromUserId);
        setUpIceServers(channel, turnServers);
        mSession.addPeerChannel(channel);
        channel.build(getRtcContext());          // build with session
    }


    /**
     * [수신자 쪽] 수신자가 rtcBusy 요청을 받았을 때 호출된다.
     * @param response
     */
    void onPeerBusyQuery(JsonObject response) {
        String toUserId = response.get("from").getAsString();
        JsonObject payload = response.get("payload").getAsJsonObject();
        String signalSessionId = payload.get("session_id").getAsString();

        /*
         * BUSY 쿼리 수신을 받았을 때 상대방의 시그널 세션을 큐에 저장한다.
         */
        mPeerSessionCache.put(toUserId, signalSessionId);
        if (connectingQueue.size() < MAX_BUSY_PEER_COUNT) {
            waitingQueue.remove(toUserId);

            if (!connectingQueue.contains(toUserId))
                connectingQueue.add(toUserId);

            Log.d(TAG, "<onPeerBusyQuery / ZICO> 바쁘지 않으므로 connectingQueue에 추가하고 Busy 응답을 전송합니다.  toUserId : " + toUserId);
            Log.d(TAG, "<onPeerBusyQuery / ZICO> waitingQueue : " + waitingQueue.toString());
            Log.d(TAG, "<onPeerBusyQuery / ZICO> connectingQueue : " + connectingQueue.toString());

            mSession.sendPeerBusyReply(mRoomSpec.getUserNo(), toUserId, signalSessionId, WebSocketKlgeSession.PEER_STATE_OK);
        } else {
            if (!waitingQueue.contains(toUserId))
                waitingQueue.add(toUserId);
            connectingQueue.remove(toUserId);
            Log.d(TAG, "<onPeerBusyQuery / ZICO> 바쁩니다. 바쁘다고 Busy 응답을 전송합니다. toUserId : " + toUserId);
            Log.d(TAG, "<onPeerBusyQuery / ZICO> waitingQueue : " + waitingQueue.toString());
            Log.d(TAG, "<onPeerBusyQuery / ZICO> connectingQueue : " + connectingQueue.toString());
            mSession.sendPeerBusyReply(mRoomSpec.getUserNo(), toUserId, signalSessionId, WebSocketKlgeSession.PEER_STATE_BUSY);
        }
    }


    /**
     * 송출자가 상대방에게 rtcBusy 응답을 받았을 때 호출된다.
     * @param response
     */
    int busyRetryCnt = 0;
    void onPeerBusyReply(JsonObject response) {
        Log.d(TAG, "<onPeerBusyReply / ZICO>");

        final String fromUserId = response.get("from").getAsString();
        JsonObject payload = response.get("payload").getAsJsonObject();
        String status = payload.get("status").getAsString();


        if (WebSocketKlgeSession.PEER_STATE_OK.equals(status)) {
            if (mSession.find(fromUserId) != null) {
                // 이미 연결된 사용자일 경우 종료.
                return;
            }

            waitingQueue.remove(fromUserId);
            if (!connectingQueue.contains(fromUserId))
                connectingQueue.add(fromUserId);
            Log.d(TAG, "<onPeerBusyReply / ZICO> 바쁘지 않다고 하네요. 연결을 진행합니다.");
            Log.d(TAG, "<onPeerBusyReply / ZICO> waitingQueue : " + waitingQueue.toString());
            Log.d(TAG, "<onPeerBusyReply / ZICO> connectingQueue : " + connectingQueue.toString());
            /*
             * 상대방이 바쁘지 않을 경우, REST 턴서버 조회
             */
            new FetchTurnServerRestCommand()
                    .callerUserId(mRoomSpec.getUserNo())
                    .calleeUserId(fromUserId)
                    .buildApi()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JsonObject>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(JsonObject response) {
                            Log.d(TAG, "<getTurnServer.json / ZICO> response : " + response.toString());
                            JsonObject elem = (JsonObject) response.get("data");

                            /*
                             * Create webrtc peer connection asynchronously and bind it with RtcChatSession instance.
                             * RtcPeerChannel instance will be returned synchronously. after that binding peer connection
                             * with RtcChatSession is done asynchronously. finally onBindChatSessionComplete will let you
                             * know the session and RtcPeerChannel are ready to control.
                             * @param session WebRtc connection.
                             * @return created peer connection instance.
                             */

                            String signalSessionId = mPeerSessionCache.remove(fromUserId);
                            if (TextUtils.isEmpty(signalSessionId)) {
                                Timber.e("onPeerBusyReply() invalid session id from user(%s). map error.", signalSessionId);
                                return;
                            }

                            // Step 1. peer_init을 생성하여 callee에게 전송한다.
                            mSession.sendPeerInit(mRoomSpec.getUserNo(), fromUserId, signalSessionId, elem.get("callee"));

                            // peer connection for offer
                            KlgePeerChannel channel = new KlgePeerChannel(mSession, true, fromUserId, signalSessionId);
                            setUpIceServers(channel, elem.getAsJsonArray("caller"));

                            // attach KlgePeerChannel into PeerWatcher.
                            mSession.addPeerChannel(channel);

                            // build with session
                            channel.build(getRtcContext());
                        }
                    });
        } else {
            // 응답이 BUSY이면, 일정 시간 후에 다시 BusyQuery를 날린다.
            waitingQueue.add(fromUserId);
            connectingQueue.remove(fromUserId);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (busyRetryCnt >= 3)


                    waitingQueue.remove(fromUserId);
                    if (!connectingQueue.contains(fromUserId))
                        connectingQueue.add(fromUserId);

                    Log.d(TAG, "<onPeerBusyReply / ZICO> 바쁘다는 응답을 받았습니다. waitingQueue에 다시 추가합니다.");
                    Log.d(TAG, "<onPeerBusyReply / ZICO> waitingQueue : " + waitingQueue.toString());
                    Log.d(TAG, "<onPeerBusyReply / ZICO> connectingQueue : " + connectingQueue.toString());

                    String signalSessionId = mPeerSessionCache.get(fromUserId);

                    // 비동기로 동작하는 프로세스이므로 다른 스레드에서 mSession이 destroy 되었을 때 동작하면 NullPointerException이 발생한다. 때문에 null 체크 후 동작시킨다.
                    if (mSession != null) {
                        mSession.sendPeerBusyQuery(mRoomSpec.getUserNo(), fromUserId, signalSessionId);
                        busyRetryCnt++;
                    }

                }
            }, BUSY_QUERY_INTERVAL);
        }
    }


    /**
     * ICE Connection이 변경되었을 때, 큐에서 해당 유저를 삭제한다.
     * @param userId
     */
    void updateConnectingQueue(String userId) {
        Log.d(TAG, "<updateConnectingQueue / ZICO>");
        connectingQueue.remove(userId);
        Log.d(TAG, "<updateConnectingQueue / ZICO> waitingQueue : " + waitingQueue.toString());
        Log.d(TAG, "<updateConnectingQueue / ZICO> connectingQueue : " + connectingQueue.toString());
    }


    void sendPeerStatus(Map<String, String> arguments) {
        mSession.sendPeerStatus(arguments);
    }

    /**
     * WebRTC 끊고 다시 맺는 메서드
     * - Turn 서버 조회하는 단계부터 다시 수행된다. (Turn 서버 조회 -> peer init 패킷으로 Turn 서버 정보 전달 -> PeerConnection 연결하기)
     * @param toUserNo
     */
    void reconnect(final String toUserNo) {
        /*
         * 리커넥트 시에는 TURN 서버 조회부터 시작
         */
        new FetchTurnServerRestCommand()
                .callerUserId(mRoomSpec.getUserNo())
                .calleeUserId(toUserNo)
                .buildApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(JsonObject response) {
                        Log.d(TAG, "<getTurnServer / ZICO> response : " + response.toString());
                        JsonObject elem = (JsonObject) response.get("data");

                            /*
                             * Create webrtc peer connection asynchronously and bind it with RtcChatSession instance.
                             * RtcPeerChannel instance will be returned synchronously. after that binding peer connection
                             * with RtcChatSession is done asynchronously. finally onBindChatSessionComplete will let you
                             * know the session and RtcPeerChannel are ready to control.
                             * @param session WebRtc connection.
                             * @return created peer connection instance.
                             */


                        KlgePeerChannel channel;
                        if (mSession.find(toUserNo) != null) {
                            channel = mSession.find(toUserNo);
                            channel.close();
                            mSession.removePeerChannel(channel);

                            //getMvpView().removeRemoteVideoView(toUserNo);
                        }

                        String signalSessionId = KlgePeerChannel.buildSessionId();

                        // Step 1. peer_init을 생성하여 callee에게 전송한다.
                        mSession.sendPeerInit(mRoomSpec.getUserNo(), toUserNo, signalSessionId, elem.get("callee"));

                        KlgePeerChannel newChannel = new KlgePeerChannel(mSession, true, toUserNo, signalSessionId);
                        setUpIceServers(newChannel, elem.getAsJsonArray("caller"));
                        mSession.addPeerChannel(newChannel);   // attach KlgePeerChannel into PeerWatcher.
                        newChannel.build(getRtcContext());
                    }
                });
    }


    boolean enableRemoteAudio(String userNo) {
        KlgePeerChannel channel = mSession.find(userNo);
        if (channel == null)
            return true;
        boolean audioEnable = !channel.isRemoteAudioEnabled();
        channel.enableRemoteAudio(audioEnable);
        return audioEnable;
    }

    /**
     * 내가 송출자일 때의 액션을 정의한 메서드이다.
     * 내가 송출자일 때, PeerChannel 리스트를 탐색하여 로컬스트림의 유무를 체크하고
     * 로컬스트림이 있으면, 나는 이전에도 송출자였음을 의미하므로 아무 액션이 없고,
     * 로컬스트림이 없으면, 나는 이전에 비송출자였음을 의미하므로 로컬스트림을 다시 add()하는 액션을 동작시킨다.
     */
    void onCallerAction() {
        List<RtcPeerChannel> channelList = mSession.getPeerChannels();
        for(final RtcPeerChannel channel : channelList) {
            if (channel.isLocalMediaChannelEnabled()) {  // 이전에 송출자였다면..
                Log.d(TAG, "<onCallerAction / ZICO> 난 송출자 -> 송출자");
                // PASS..
            } else {
                Log.d(TAG, "<onCallerAction / ZICO> 난 비송출자 -> 송출자");
                channel.enableLocalMediaChannel(true);

                // 제거한 로컬스트림을 다시 addStream한 후에는 반드시 renegotiation을 진행해야 한다.
                channel.setIsOffer(true);
                RtcChatClient.instance().createOfferSdp(channel);
            }
        }
    }

    /**
     * 내가 비송출자일 때의 액션을 정의한 메서드이다.
     * 내가 비송출자일 때, PeerChannel 리스트를 탐색하여 로컬스트림이 있는지 유무를 체크하고
     * 로컬스트림이 있으면, 나는 이전에 송출자였음을 의미하므로 로컬스트림을 remove하는 액션을 동작시키고,
     * 로컬스트림이 없으면, 나는 이전에도 비송출자였음을 의미하므로 아무 액션이 없다.
     */
    void onNotCallerAction() {
        List<RtcPeerChannel> channelList = mSession.getPeerChannels();

        // 내가 비송출자면 waitingQueue와 connectingQueue를 비운다. - 2017.04.21
        if (waitingQueue.size() > 0)
            waitingQueue.clear();
        if (connectingQueue.size() > 0)
            connectingQueue.clear();
        Log.d(TAG, "<onNotCallerAction / ZICO> waitingQueue & connectingQueue clear()");
        Log.d(TAG, "<onNotCallerAction / ZICO> waitingQueue : " + waitingQueue.toString());
        Log.d(TAG, "<onNotCallerAction / ZICO> connectingQueue : " + connectingQueue.toString());

        for(RtcPeerChannel channel : channelList) {
            if (channel.isLocalMediaChannelEnabled()) {  // 이전에 송출자였다면..
                Log.d(TAG, "<onNotCallerAction / ZICO> 난 송출자 -> 비송출자");
                channel.enableLocalMediaChannel(false);

                // 제거한 로컬스트림을 다시 addStream한 후에는 반드시 renegotiation을 진행해야 한다.
                channel.setIsOffer(true);
                RtcChatClient.instance().createOfferSdp(channel);
            } else {
                Log.d(TAG, "<onNotCallerAction / ZICO> 난 비송출자 -> 비송출자");
                // PASS..
            }
        }
    }


    /**
     * 네트워크 때문에 끊어진 소켓을 다시 연결하고자 할 때 호출하는 메서드 - 2017.04.26
     *  - 현재 사용하지 않음
     */
    void reconnectSocket() {
        if (mSession.isOpen()) {
            Log.d(TAG, "<reconnectSocket / ZICO>");
        }
    }

    /**
     * 영상 설정창에서 수업 입장하기 버튼을 눌렀을 때 호출되는 메서드 (RoomActivity.onStatusInit()에서 호출함)
     * @param roomSpec
     */
    void onStatusInit(RoomSpec roomSpec) {
        mRoomSpec = roomSpec;
        Log.d(TAG, "<onStatusInit / ZICO> mRoomSpec.getRoomId() : " + mRoomSpec.getRoomId());
        Log.d(TAG, "<onStatusInit / ZICO> mRoomSpec.isVideoControlEnable() : " + mRoomSpec.isVideoControlEnable());

//        mRoomSpec.setIsVideoEnable(isVideoEnabled);
//        mRoomSpec.setIsAudioEnable(isAudioEnabled);
//        mRoomSpec.setIsVideoControlEnable(isClassMode);


        // 설정값에 따라에 비디오, 오디오 on/off 여부를 제어한다.
//        RtcChatClient.instance().enableLocalVideo(mRoomSpec.isVideoEnabled());
//        RtcChatClient.instance().enableLocalAudio(mRoomSpec.isAudioEnabled());

        /**
         * [REST]
         * 방정보를 이용하여 서버를 할당하는 REST APO를 호출한다.
         */
        new FetchRtcServerRestCommand()
                .roomId(mRoomSpec.getRoomId())
                .auto(mRoomSpec.isVideoControlEnable() ? 0 : 1)
                .buildApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new ListApiRetry(3, 1000))
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "<getRtcServer.json> onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO  서버 정보 조회 실패시 에러 처리
                        Log.d(TAG, "<getRtcServer.json> onError");
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "<getRtcServer.json / ZICO> onNext / result : " + jsonObject.toString());
                        int resultCode = jsonObject.get("result").getAsInt();
                        if (resultCode == 0) {
                            JsonObject data = jsonObject.get("data").getAsJsonObject();
                            String serverName = data.get("name").getAsString();
                            String serverHost = data.get("host").getAsString();
                            int serverPort = data.get("port").getAsInt();

                            mRoomSpec.setHost(serverHost);
                            mRoomSpec.setName(serverName);
                            mRoomSpec.setPort(Integer.toString(serverPort));

                            start();

                            /*
                             * 로컬 미디어 스트림을 생성한다. 이미 존재한다면 로컬 미디어 스트림을 생성하지 않고 이전에 생성한 인스턴스를
                             * 가지고 현재의 비디오 렌더러와 바인딩할 수 있도록 이벤트만 호출한다.
                             *
                             * 만약에 다른 Fragment화면에서 로컬 미디어 스트림을 바인딩할 필요가 있을 때도 다음의 미디어 스트림 생성
                             * 메소드를 사용하여 렌더러뷰와 바인딩한다.
                             */
                            if (!mRoomSpec.isVideoEnabled() && !mRoomSpec.isAudioEnabled()) {

                            } else {
                                Log.d(TAG, "create localMediaStream");
                                //RtcChatClient.instance().createLocalMediaChannel(getRtcContext());
                            }

                        } else {

                        }
                    }
                });
    }


    /**
     * 사용자가 영상 설정창으로 미디어 상태를 재설정한 후에 호출되는 메서드
     * @param roomSpec
     */
    void onStatusChange(RoomSpec roomSpec) {
        mRoomSpec = roomSpec;
        boolean isVideoEnabled = roomSpec.isVideoEnabled();
        boolean isAudioEnabled = roomSpec.isAudioEnabled();

        // TODO : 설정값에 따라에 비디오, 오디오 on/off 여부를 제어한다. 트랙제어 필요
        RtcChatClient.instance().enableLocalVideo(isVideoEnabled);
        RtcChatClient.instance().enableLocalAudio(isAudioEnabled);

        Map<String,String> arguments = new HashMap<>();
        arguments.put("userno", mRoomSpec.getUserNo());
        arguments.put("roomid", mRoomSpec.getRoomId());
        arguments.put("video", isVideoEnabled ? "enable" : "disable");
        arguments.put("audio", isAudioEnabled ? "enable" : "disable");
        arguments.put("os_version", Build.VERSION.RELEASE);
        arguments.put("device", Build.MODEL);

        sendPeerStatus(arguments);
    }

    void onCreateLocalMedia(RtcMediaChannel mediaChannel) {

    }


    private void setUpIceServers(RtcPeerChannel channel, JsonArray turnServers) {
        for (JsonElement ice : turnServers) {
            JsonObject item = (JsonObject) ice;
            String urls = item.get("urls").getAsString();
            String username = "";
            String credential = "";

            if (item.has("username")) {
                username = item.get("username").getAsString();
            }
            if (item.has("credential")) {
                credential= item.get("credential").getAsString();
            }

            channel.addIceServer(new PeerConnection.IceServer(urls, username, credential));
        }
    }


    /**
     * rtcList에서 빠져서 내려오면 큐에서 모두 삭제해준다.
     * @param userId
     */
    private void removeQueue(String userId) {
        waitingQueue.remove(userId);
        connectingQueue.remove(userId);
        Log.d(TAG, "<removeQueue / ZICO> waitingQueue : " + waitingQueue.toString());
        Log.d(TAG, "<removeQueue / ZICO> connectingQueue : " + connectingQueue.toString());
    }
}