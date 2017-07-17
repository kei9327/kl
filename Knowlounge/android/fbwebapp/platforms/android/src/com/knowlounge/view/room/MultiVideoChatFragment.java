package com.knowlounge.view.room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.knowlounge.AndroidContext;
import com.knowlounge.R;
import com.knowlounge.RtcSupportActivity;
import com.knowlounge.RtcSupportFragment;
import com.knowlounge.apprtc.KlgePeerChannel;
import com.knowlounge.apprtc.KlgePeerNode;
import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.apprtc.WebSocketKlgeSession;
import com.knowlounge.dagger.HasComponent;
import com.knowlounge.dagger.component.DaggerMultiVideoChatFragmentComponent;
import com.knowlounge.dagger.component.MultiVideoChatFragmentComponent;
import com.knowlounge.dagger.modules.KlgeClientControllerModule;
import com.knowlounge.dagger.modules.RoomMvpViewModule;
import com.knowlounge.dagger.modules.RtcContextModule;
import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.KlgePeer;
import com.knowlounge.model.KlgeStatus;
import com.knowlounge.model.Room;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.model.RoomUser;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.widget.RenderView;
import com.wescan.alo.rtc.RtcChatClient;
import com.wescan.alo.rtc.RtcChatSession;
import com.wescan.alo.rtc.RtcMediaChannel;
import com.wescan.alo.rtc.RtcMediaChannelEvents;
import com.wescan.alo.rtc.RtcPeerChannel;

import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mansu on 2017-03-17.
 *  - WebRTC와 관련된 Fragment
 *  - Dagger2를 이용하여 MVP 모델로 분리되어 있다. (M : RoomSpec, V : RoomView, P : RoomPresenter)
 *  - UI 처리는 Fragment에서 관장하고, 로직 처리는 RoomPresenter에서 관장한다.
 */

public class MultiVideoChatFragment extends RtcSupportFragment implements RoomView, HasComponent<MultiVideoChatFragmentComponent>,
        KlgePeerWatcher.KlgePeerEvents,
        RtcMediaChannelEvents,
        RtcStatusDialogFragment.OnStatusChangeListener,
        RoomUserPresenter.RoomUserEvent,
        RtcSupportActivity.NetworkConnectionEvents,
        RtcSupportActivity.RoomEvents,
        RoomActivity.VideoEvents,
        KlgePeerWatcher.RtcListListener {

    private static final String TAG = MultiVideoChatFragment.class.getSimpleName();

    // VIEW
    @BindView(R.id.fragment_multi_video_chat) HorizontalScrollView mVideoContainer;
    @BindView(R.id.video_wrapper) FrameLayout mVideoWrapper;
    private RenderView mLocalRenderView;

    private View mRootView;

    // DATA
    private WenotePreferenceManager prefManager;
    private RoomSpec mRoomSpec;
    private RoomSpec mParaentRoomSpec;

    // <user-id, RenderView>
    private ConcurrentHashMap<String,RenderView> mRenderViewMap = new ConcurrentHashMap<>();

    private KlgePeerWatcher mPeerWatcher;

    // CONTROLLERS
    private MultiVideoChatFragmentComponent mComponent;

    @Inject
    RoomPresenter mPresenter;
    RoomUserPresenter mRoomUserPresenter;

    private boolean isInitializeRoom = false;

    private boolean isExitRoom = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private NetworkState mNetworkState;
    public enum NetworkState {
        CONNECTED, DISCONNECTED
    }

    public static MultiVideoChatFragment newInstance(RoomSpec arguments) {
        MultiVideoChatFragment fragment = new MultiVideoChatFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("arguments", arguments);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
        if (context instanceof RtcSupportActivity) {
            mPeerWatcher = ((RtcSupportActivity) context).getPeerWatcher();
            if (mPeerWatcher != null) {
                mPeerWatcher.addKlgePeerEventsListener(this);
                mPeerWatcher.addRtcListListener(this);
            }
        }
        mRoomUserPresenter = ((RoomActivity) context).getRoomUserPresenter();
        mRoomUserPresenter.addRoomUserEventsListener(this);
        ((RoomActivity) getActivity()).setVideoEvents(this);

        ((RtcSupportActivity)context).setNetworkConnectionEvents(this);
        ((RtcSupportActivity)context).setRoomEvents(this);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpArguments();
        setUpInjector();
        RtcChatClient.instance().addCameraSwitchHandler(mCameraSwitchCallback);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_multi_video_chat, container, false);
        ButterKnife.bind(this, mRootView);

        mLocalRenderView = new RenderView(mRootView.findViewById(R.id.local_render_layout), 0);
        //setUpView(mRootView);

        final Timer timer = new Timer();
        final TimerTask setUpViewTask = new TimerTask() {
            @Override
            public void run() {
                if (isInitializeRoom && mLocalRenderView != null) {
                    setUpView();
                    timer.cancel();
                }

            }
        };
        timer.schedule(setUpViewTask, 0, 500);
        return mRootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "<onActivityCreated / ZICO>");
        super.onActivityCreated(savedInstanceState);
        RtcChatClient.instance().addRtcMediaChannelEventsListener(getRtcContext(), this);   // MediaStream이 추가되거나 삭제될 때 발생하는 이벤트 리스너를 등록한다.
    }


    @Override
    public void onStart() {
        super.onStart();
        /**
         * 로컬 미디어 스트림을 생성한다. 이미 존재한다면 로컬 미디어 스트림을 생성하지 않고 이전에 생성한 인스턴스를
         * 가지고 현재의 비디오 렌더러와 바인딩할 수 있도록 이벤트만 호출한다.
         *
         * 만약에 다른 Fragment화면에서 로컬 미디어 스트림을 바인딩할 필요가 있을 때도 다음의 미디어 스트림 생성
         * 메소드를 사용하여 렌더러뷰와 바인딩한다.
         */
        Log.d(TAG, "<onStart / ZICO> createLocalMediaChannel call..");
        RtcChatClient.instance().createLocalMediaChannel(getRtcContext());  // 로컬 스트림을 생성한다. 생성이 성공하면 onLocalMediaChannel 콜백 호출..
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "<onDestroy / ZICO>");
        super.onDestroy();
        isExitRoom = true;
        mPresenter.destroy();
        if (mPeerWatcher != null) {
            mPeerWatcher.removeKlgePeerEventsListener(this);
        }

        RtcChatClient.instance().removeCameraSwitchHandler(mCameraSwitchCallback);
        destroyRtcView();
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <implements : HasComponent<RoomView>>
     */
    @Override
    public MultiVideoChatFragmentComponent getComponent() {
        if (mComponent == null) {
            mComponent = DaggerMultiVideoChatFragmentComponent.builder()
                    .appComponent(AndroidContext.instance().getAppComponent())
                    .rtcContextModule(new RtcContextModule(getRtcContext()))
                    .roomMvpViewModule(new RoomMvpViewModule(this))
                    .klgeClientControllerModule(new KlgeClientControllerModule(getController()))
                    .build();
        }
        return mComponent;
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : KlgePeerWatcher.KlgePeerEvents>
     */

    /**
     * 새로운 피어를 생성하고 연결해야 하는 경우 호출된다. KlgePeerWatcher에서 호출한다.
     * @param toUserId 상대방의 사용자 아이디
     * @param peer 피어 정보
     */
    @Override
    public void onPeerCreate(String toUserId, KlgePeerNode peer) {
        Log.d(TAG, "<onPeerCreate/ ZICO> toUserId : " + toUserId);
        mPresenter.onPeerCreate(toUserId, peer);

        RenderView render = mRenderViewMap.get(toUserId);
        if (render == null)
            render = addRemoteVideoView(toUserId);

        Glide.with(this)
                .load(R.drawable.img_cam_loading)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(render.getLoaderView());
        render.visibleLoader();

        RoomUser user = mRoomUserPresenter.getRoomUserList().get(toUserId);
        if (user != null)
            setUpRenderView(render, user);
    }


    /**
     * 연결된 피어를 끊어야하는 경우 호출된다. Watcher 안에서 피어케넥션 맵에서 해당 피어를 삭제해준다.
     * @param fromUserId 상대방의 사용자 아이디
     * @param peer 피어 정보
     */
    @Override
    public void onPeerDestroy(String fromUserId, KlgePeerNode peer) {
        Log.d(TAG, "<onPeerDestroy / ZICO> fromUserId : " + fromUserId);
        mPresenter.onPeerDestroy(fromUserId, peer);
        removeRemoteVideoView(fromUserId);
    }


    /**
     * 새로 들어온 상대방이 송출자일 경우에 필요한 UI의 처리를 수행한다.
     * @param fromUserNo
     * @param peer
     */
    @Override
    public void onRemoteCaller(final String fromUserNo, final KlgePeerNode peer) {
        Log.d(TAG, "<onRemoteCaller / ZICO>");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RenderView render = mRenderViewMap.get(fromUserNo);
                if (render == null)
                    render = addRemoteVideoView(fromUserNo);

                RoomUser user = mRoomUserPresenter.getRoomUserList().get(fromUserNo);
                if (user != null)
                    setUpRenderView(render, user);
                if (mRoomSpec.isWhiteboardMode()) {
                    Log.d(TAG, "<onRemoteCaller / ZICO> 화이트모드 수업입니다. 상대방의 영상 화면을 숨깁니다.");
                    render.gone();
                    render.goneRenderer();
                } else {
                    render.visible();
                    render.goneRenderer();
                }

                if ("enable".equals(peer.getPeer().getStatus().getVideo())) {
                    Log.d(TAG, "<onRemoteCaller / ZICO> 비디오 활성화 유저입니다. 렌더러를 보여줍니다.");
                    render.visibleLoader();
//                    render.visibleRenderer();
//                    render.init(getRtcContext().getEglContext(), null);
                } else if ("disable".equals(peer.getPeer().getStatus().getVideo())) {
                    Log.d(TAG, "<onRemoteCaller / ZICO> 비디오 비활성화 유저입니다. 렌더러를 숨깁니다.");
                    render.goneRenderer();
                }


            }
        });
        mPresenter.onRemoteCaller(fromUserNo, peer);
    }


    @Override
    public void onNewRemoteCaller(final String fromUserNo, final KlgePeerNode peer) {
        Log.d(TAG, "<onNewRemoteCaller / ZICO>");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RenderView render = mRenderViewMap.get(fromUserNo);
                if (render == null)
                    render = addRemoteVideoView(fromUserNo);
                Glide.with(getContext())
                        .load(R.drawable.img_cam_loading)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(render.getLoaderView());

                RoomUser user = mRoomUserPresenter.getRoomUserList().get(fromUserNo);
                if (user != null)
                    setUpRenderView(render, user);
                if (mRoomSpec.isWhiteboardMode()) {
                    Log.d(TAG, "<onNewRemoteCaller / ZICO> 화이트모드 수업입니다. 상대방의 영상 화면을 숨깁니다.");
                    render.gone();
                    render.goneRenderer();
                    render.goneLoader();
                } else {
                    render.visible();
                    render.goneRenderer();
                }

                if ("enable".equals(peer.getPeer().getStatus().getVideo())) {
                    Log.d(TAG, "<onNewRemoteCaller / ZICO> 비디오 활성화 유저입니다. 렌더러를 보여줍니다.");
                    render.goneThumbnail();
                    render.visibleLoader();
//                    render.visibleRenderer();
//                    render.init(getRtcContext().getEglContext(), null);
                } else if ("disable".equals(peer.getPeer().getStatus().getVideo())) {
                    Log.d(TAG, "<onNewRemoteCaller / ZICO> 비디오 비활성화 유저입니다. 렌더러를 숨깁니다.");
                    render.goneRenderer();
                }
            }
        });
    }


    /**
     * 초기에 입장할 때 비디오 정렬 처리
     * @param user
     */
    private void orderVideoView(RoomUser user) {
        boolean isMaster = user.isMaster();
        String userNo = user.getUserNo();
        Log.d(TAG, "<orderVideoView / ZICO> isMaster : " + isMaster);
        if (isMaster) {
            RenderView remoteView = mRenderViewMap.get(user.getUserNo());
            FrameLayout.LayoutParams remoteLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            remoteLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 0, 0, 0, 0);
            remoteView.getView().setLayoutParams(remoteLp);

            FrameLayout.LayoutParams localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 1, 0, 0, 0);
            mLocalRenderView.getView().setLayoutParams(localLp);

            int position = 2;
            for (Map.Entry<String, RenderView> entry : mRenderViewMap.entrySet()) {
                String entryUserNo = entry.getKey();
                RenderView entryView = entry.getValue();
                if (entryUserNo.equals(RoomActivity.activity.getUserNo()) || entryUserNo.equals(userNo))
                    continue;
                else {
                    FrameLayout.LayoutParams newViewLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    newViewLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * position, 0, 0, 0);
                    entryView.getView().setLayoutParams(newViewLp);
                    position++;
                }
            }

        } else {
            if (RoomActivity.activity.getMasterFlag()) {
                Log.d(TAG, "<orderVideoView / ZICO> 나는 마스터, 들어온 유저는 비 마스터");
                FrameLayout.LayoutParams localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 0, 0, 0, 0);
                mLocalRenderView.getView().setLayoutParams(localLp);
            } else {
                if (!mRoomUserPresenter.isMasterExist()) {
                    Log.d(TAG, "<orderVideoView / ZICO> 나는 비 마스터, 들어온 유저도 비 마스터, 하지만 누군가가 마스터는 존재함");
                    FrameLayout.LayoutParams localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 0, 0, 0, 0);
                    mLocalRenderView.getView().setLayoutParams(localLp);
                } else {
                    Log.d(TAG, "<orderVideoView / ZICO> 나는 비 마스터, 들어온 유저도 비 마스터, 마스터는 아무도 없음.");
                    FrameLayout.LayoutParams localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 1, 0, 0, 0);
                    mLocalRenderView.getView().setLayoutParams(localLp);
                }
            }

            RenderView remoteView = mRenderViewMap.get(user.getUserNo());
            FrameLayout.LayoutParams remoteLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            remoteLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * mRenderViewMap.size(), 0, 0, 0);
            remoteView.getView().setLayoutParams(remoteLp);
        }
    }


    /**
     * 유저가 들어오거나 나갈 때의 비디오 UI 정렬
     */
    private void reorderVideoView() {
        ConcurrentHashMap<String, RoomUser> roomUserMap = mRoomUserPresenter.getRoomUserList();
        int remoteUserPosition;
        if (RoomActivity.activity.getMasterFlag()) {
            // 내가 진행자일 때 상대방 영상 유저의 위치는 1부터 시작한다.
            remoteUserPosition = 1;
        } else {
            if (mRoomUserPresenter.isMasterExist()) {
                // 내가 진행자가 아닐 때, 진행자가 수업에 있으면 상대방 영상 유저의 위치는 2부터 시작한다.
                remoteUserPosition = 2;
            } else {
                // 내가 진행자가 아닐 때, 진행자가 수업에 없으면 상대방 영상 유저의 위치는 2부터 시작한다.
                remoteUserPosition = 1;
            }
        }

        for(Map.Entry<String, RoomUser> entry : roomUserMap.entrySet()) {
            String userNo = entry.getKey();
            boolean isMaster = entry.getValue().isMaster();
            RenderView remoteView = mRenderViewMap.get(userNo);

            // 내 영상 위치
            if (userNo.equals(RoomActivity.activity.getUserNo())) {
                FrameLayout.LayoutParams localLp;
                if (isMaster) {
                    Log.d(TAG, "<reorderVideoView / ZICO> 나는 마스터 유저입니다. userNm : " + entry.getValue().getUserNm());
                    localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 0, 0, 0, 0);
                } else {
                    if (mRoomUserPresenter.isMasterExist()) {
                        Log.d(TAG, "<reorderVideoView / ZICO> 나는 마스터가 아니고 수업에 마스터가 존재합니다. userNm : " + entry.getValue().getUserNm());
                        localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 1, 0, 0, 0);
                    } else {
                        Log.d(TAG, "<reorderVideoView / ZICO> 나는 마스터가 아니고 수업에도 마스터가 없습니다. userNm : " + entry.getValue().getUserNm());
                        localLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        localLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 0, 0, 0, 0);
                    }
                }
                setUpRenderView(mLocalRenderView, entry.getValue());
                mLocalRenderView.getView().setLayoutParams(localLp);
                continue;
            }


            // 다른 사람의 영상 위치
            if (remoteView != null) {
                FrameLayout.LayoutParams remoteLp;
                if (entry.getValue().isMaster()) {
                    Log.d(TAG, "<reorderVideoView / ZICO> 마스터 유저입니다. userNm : " + entry.getValue().getUserNm() + ", idx : " + remoteUserPosition);
                    remoteLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    remoteLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * 0, 0, 0, 0);
                } else {
                    Log.d(TAG, "<reorderVideoView / ZICO> 마스터 유저가 아닙니다. userNm : " + entry.getValue().getUserNm() + ", idx : " + remoteUserPosition);
                    remoteLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    remoteLp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * remoteUserPosition, 0, 0, 0);
                    remoteUserPosition++;
                }
                remoteView.getView().setLayoutParams(remoteLp);
                setUpRenderView(remoteView, entry.getValue());
            }

        }
    }


    @Override
    public void onRemoteNotCaller(String fromUserId, KlgePeerNode peer) {
        mPresenter.onRemoteNotCaller(fromUserId, peer);
    }

    @Override
    public void onPeerUpdate(String fromUserId, KlgePeerNode peer) {
//        mPresenter.onPeerDestroy(fromUserId, peer);
    }

    /**
     * Watcher에서 내가 송출자가 되었을 때 호출한다.
     * 내가 송출자인 경우의 액션을 정의함.
     */
    @Override
    public void onPeerCaller() {
        Log.d(TAG, "<onPeerCaller / ZICO>");

        mPresenter.onCallerAction();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRoomSpec.isVideoEnabled()) {
                    RtcChatClient.instance().startVideo();
                    mLocalRenderView.visible();
                    mLocalRenderView.visibleRenderer();
                    RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
                    if (localMediaChannel != null) {
                        localMediaChannel.enableVideoTrack(mRoomSpec.isVideoEnabled());
                        localMediaChannel.enableAudioTrack(mRoomSpec.isAudioEnabled());

                        Log.d(TAG, "<onPeerCaller / ZICO> rebind LocalMediaChannel to Renderer");
                        mLocalRenderView.init(getRtcContext().getEglContext(), null);
                        localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
                        updateLocalVideoView();
                    }
                } else {
                    if (mRoomSpec.isVideoControlEnable())
                        mLocalRenderView.gone();
                    else
                        mLocalRenderView.visible();
                    RtcChatClient.instance().stopVideo();
                    mLocalRenderView.goneRenderer();
                }

                /*
                if (!mRoomSpec.isVideoEnabled()) {   // 비디오가 비활성화 상태면 렌더러를 숨긴다.
                    RtcChatClient.instance().stopVideo();  // 비디오가 비활성화면 Capturer를 정지 시킨다.
                    if (mRoomSpec.isParentMaster()) {
                        mLocalRenderView.goneRenderer();
                        mLocalRenderView.getNoVideoView().setVisibility(View.VISIBLE);
                    } else {
                        mLocalRenderView.gone();
                    }
                } else {
                    if (mRoomSpec.isVideoControlEnable()) {
                        if (!mRoomSpec.isParentMaster()) {
                            RtcChatClient.instance().stopVideo();  // 비디오가 비활성화면 Capturer를 정지 시킨다.
                            mLocalRenderView.gone();
                        } else {
                            Log.d(TAG, "<onPeerCaller / ZICO> 영상 제어 모드에서 참여자의 비디오가 활성화 됩니다.");
                            RtcChatClient.instance().startVideo();  // 정지된 Capturer를 다시 시작함
                            mLocalRenderView.visible();
                            mLocalRenderView.visibleRenderer();
                            RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
                            if (localMediaChannel != null) {
                                Log.d(TAG, "<onPeerCaller / ZICO> rebind LocalMediaChannel to Renderer");
                                localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
                                updateLocalVideoView();
                            }
                        }
                    }
                    RtcChatClient.instance().startVideo();  // 정지된 Capturer를 다시 시작함
                    mLocalRenderView.visibleRenderer();
                    RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
                    if (localMediaChannel != null) {
                        Log.d(TAG, "<onPeerCaller / ZICO> rebind LocalMediaChannel to Renderer");
                        localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
                        updateLocalVideoView();
                    }
                }
                */
            }
        });


    }


    /**
     * Watcher에서 내가 비송출자가 되었을 때 호출한다.
     * 내가 비송출자인 경우의 액션을 정의함.
     */
    @Override
    public void onPeerNotCaller() {
        Log.d(TAG, "<onPeerNotCaller / ZICO>");

        RtcChatClient.instance().stopVideo();  // Capturer를 정지 시킨다.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mRoomSpec.isParentMaster()) {
                    if (mRoomSpec.isVideoControlEnable()) {
                        Log.d(TAG, "<onPeerNotCaller / ZICO> 영상 제어 모드로 변경되어 내 영상을 숨깁니다.");
                        mLocalRenderView.gone();
                        mLocalRenderView.goneRenderer();
                        mLocalRenderView.getNoVideoView().setVisibility(View.GONE);
                    }
                } else {
                    mLocalRenderView.goneRenderer();
                    mLocalRenderView.getNoVideoView().setVisibility(View.VISIBLE);
                    mLocalRenderView.getNoVideoView().setImageResource(R.drawable.btn_video_user_blank);
                }
            }
        });

        mPresenter.onNotCallerAction();
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RtcMediaChannelEvents>
     */
    /**
     * 로컬 미디어 스트림이 생성된 경우
     * [RTC Thread]
     */
    @Override
    public void onLocalMediaChannel(final RtcMediaChannel localMediaChannel) {
        Log.d(TAG, "<onLocalMediaChannel / ZICO> start......");
        /*
         * Note!
         *
         * RtcChatClient.instance().createLocalMediaChannel()를 호출하여 로컬 미디어 스트림 생성시 호출된다.
         * RtcChatClient.instance().getOrCreateLocalMediaChannel()를 호출할 때 로컬 미디어 스트림이
         * 존재하지 않을 때에도 내부적으로 RtcChatClient.instance().createLocalMediaChannel() 메소드가
         * 호출되어 결과적으로 현재의 메소드가 호출된다.
         *
         * 로컬 미디어 스트림이 생성된 경우, 해당하는 LocalVideoTrack과 LocalAudioTrack은 비어 있는 상태로
         * 만들어진다. 따라서 구현시 필요한 정책에 따라 LocalVideoTrack과 LocalAudioTrack을 아래와 같이
         * 추가할 수 있다.
         *
         * local video track
         * ex)
         *  if (!localMediaChannel.isVideoTrackExist()) {
         *      VideoTrack localVideoTrack = RtcChatClient.instance().createLocalVideoTrack();
         *      if (localVideoTrack != null) {
         *          localMediaChannel.setVideoTrack(localVideoTrack);
         *      }
         *  }
         *
         * local audio track
         * ex)
         *  if (!localMediaChannel.isAudioTrackExist()) {
         *      AudioTrack localAudioTrack = RtcChatClient.instance().createLocalAudioTrack();
         *      if (localAudioTrack != null) {
         *          localMediaChannel.setAudioTrack(localAudioTrack);
         *      }
         *  }
         */

        VideoTrack localVideoTrack = null;
        if (!localMediaChannel.isVideoTrackExist()) {
            localVideoTrack = RtcChatClient.instance().createLocalVideoTrack();
            if (localVideoTrack != null) {
                Log.d(TAG, "<onLocalMediaChannel / ZICO> setVideoTrack");
                localMediaChannel.setVideoTrack(localVideoTrack);
            }
        }

        if (!localMediaChannel.isAudioTrackExist()) {
            AudioTrack localAudioTrack = RtcChatClient.instance().createLocalAudioTrack();
            if (localAudioTrack != null) {
                Log.d(TAG, "<onLocalMediaChannel / ZICO> setAudioTrack");
                localMediaChannel.setAudioTrack(localAudioTrack);
            }
        }

        /**
         * 로컬 비디오 트랙이 처음으로 생성된 경우, 렌더러를 생성하여 추가하고 비디오 소스를 시작한다.
         */
//        if (localMediaChannel.isVideoTrackExist() && localVideoTrack != null) {
            if (!mRoomSpec.isParentMaster()) {
                if (mRoomSpec.isVideoControlEnable()) {
                    if (!mRoomSpec.isAllowCaller()) {
                        Log.d(TAG, "<onLocalMediaChannel / ZICO> Video Control mode.. My Video UI hide..");
                    } else {
                        localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
                        updateLocalVideoView();
                    }
//                    mLocalRenderView.gone();
                }
            } else {
//                Log.d(TAG, "<onLocalMediaChannel> Default mode.. My Video UI show..");
//
//                mLocalRenderView.visibleRenderer();
//
//                localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
//                updateLocalVideoView();
//
//                RoomUser user = mRoomUserPresenter.getRoomUserList().get(mRoomSpec.getUserNo());
//                Log.d(TAG, "<onLocalMediaChannel> userNm : " + user.getUserNm() + ", master : " + user.isMaster());
//                setUpRenderView(mLocalRenderView, user);

//                RtcChatClient.instance().startVideo();
            }
//        }
    }

    /**
     * 상대방 미디어 스트림이 생성된 경우 호출된다. (onAddStream)
     * [RTC Thread]
     * @param session 웹소켓 세션
     * @param peer 피어 커넥션
     * @param remoteMediaChannel 상대방 미디어 스트림 컨테이너
     */
    @Override
    public void onRemoteMediaChannel(final RtcChatSession session, RtcPeerChannel peer, final RtcMediaChannel remoteMediaChannel) {
        final String userNo = ((KlgePeerChannel) peer).getToUserId();
        final RoomUser user = mRoomUserPresenter.getRoomUserList().get(userNo);

        Log.d(TAG, "<onRemoteMediaChannel / ZICO> peer userNo : " + userNo + ", userNm : " + user.getUserNm());
        Log.d(TAG, "<onRemoteMediaChannel / ZICO> mRoomSpec.isVolumeEnabled() : " + mRoomSpec.isVolumeEnabled());

        // 스피커 on/off 여부에 따라 리모트 스트림의 오디오 트랙을 제어한다.
        remoteMediaChannel.enableAudioTrack(mRoomSpec.isVolumeEnabled());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RenderView render = mRenderViewMap.get(userNo);
                if (render == null)
                    render = addRemoteVideoView(userNo);

                // 수업에 나가는 찰나에서 onRemoteMediaChannel이 실행될때를 대비한 예외처리 구문
                if (render == null)
                    return;

                if (isExitRoom)
                    return;

                render.goneLoader();  // 원격 비디오가 출력되기 시작했으므로 로더이미지는 숨긴다.

                if (mRoomSpec.isWhiteboardMode()) {   // 화이트보드 모드에서는 비디오 UI는 보이되 렌더러만 감추는 방식이다. - 2017.04.24
                    Log.d(TAG, "<onRemoteMediaChannel / ZICO> remoteMediaChannel.isVideoTrackEnabled() : " + remoteMediaChannel.isVideoTrackEnabled());
                    render.goneRenderer();
                } else {
                    Log.d(TAG, "<onRemoteMediaChannel / ZICO> remoteMedia add" );
                    render.visibleRenderer();
                    render.init(getRtcContext().getEglContext(), null);
                    remoteMediaChannel.rebindVideoRenderer(new VideoRenderer(render.getRenderer()));
                }
                if (user != null)
                    setUpRenderView(render, user);

                KlgePeerNode remoteNode = mPeerWatcher.getPeerNode(userNo);
                if (remoteNode != null) {
                    KlgePeer remotePeer = remoteNode.getPeer();
                    KlgeStatus remoteStatus = remotePeer.getStatus();
                    if ("disable".equals(remoteStatus.getAudio())) {
                        remoteMediaChannel.enableAudioTrack(false);
                    }
                }
            }
        });
    }

    @Override
    public void onRemoveStream(final RtcChatSession session, RtcPeerChannel peer) {
        Log.d(TAG, "<onRemoveStream> userId : " + ((KlgePeerChannel) peer).getToUserId());
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<overrides>
     */
    @Override
    public void onIceConnected(RtcChatSession session, RtcPeerChannel peer) {
        Log.d(TAG, "<onIceConnected / ZICO>");
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        Log.d(TAG, "<onIceConnected> from user: " + klge.getToUserId());
        mPresenter.updateConnectingQueue(klge.getToUserId());

        RenderView renderView = mRenderViewMap.get(klge.getToUserId());
        if (renderView != null) {
            if (mRoomSpec.isWhiteboardMode()) {
                renderView.goneRenderer();
                renderView.getNoVideoView().setVisibility(View.VISIBLE);
            } else {
                renderView.visibleRenderer();
                renderView.getNoVideoView().setVisibility(View.GONE);
                renderView.goneLoader();
            }
        }
    }

    /**
     * iceDisconnect 시에 호출된다. 다시 iceConnect로 전환될 수 있는 상태이므로, 로더를 활성화시키는 UI처리를 동작시킨다.
     * connectionQueue에 데이터가 남아있으면 클리어 시킨다.
     * @param session
     * @param peer
     */
    @Override
    public void onIceDisconnected(RtcChatSession session, RtcPeerChannel peer) {
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        Log.d(TAG, "<onIceDisconnected> from user: " + klge.getToUserId());
//        klge.close();
//        removeRemoteVideoView(( (KlgePeerChannel) peer).getToUserId());

        RenderView renderView = mRenderViewMap.get(((KlgePeerChannel) peer).getToUserId());
        if (renderView != null) {
            renderView.goneRenderer();

            renderView.visibleLoader();
        }

        mPresenter.updateConnectingQueue(klge.getToUserId());
    }


    /**
     * connectionQueue에 데이터가 남아있으면 클리어 시킨다.
     * @param rtcChatSession
     * @param peer
     */
    @Override
    public void onIceFailed(RtcChatSession rtcChatSession, RtcPeerChannel peer) {
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        Log.d(TAG, "<onIceFailed> from user: " + klge.getToUserId());
        mPresenter.updateConnectingQueue(klge.getToUserId());
    }


    @Override
    public void onIceClosed(RtcChatSession rtcChatSession, RtcPeerChannel peer) {
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        Log.d(TAG, "<onIceClosed> from user: " + klge.getToUserId());
        RenderView renderView = mRenderViewMap.get(klge.getToUserId());
        if (renderView != null) {
            renderView.goneRenderer();
            renderView.goneLoader();
            renderView.visibleThumbnail();
            Glide.with(getActivity())
                    .load(AndroidUtils.changeSizeThumbnail(mRoomUserPresenter.getRoomUser(klge.getToUserId()).getThumbnail(), 130))
                    .error(getResources().getDrawable(R.drawable.img_video_user_novideo))
                    .into(renderView.getNoVideoView());
        }
    }

    @Override
    public void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {
        int result = response.get("result").getAsInt();
        Log.e(TAG, "<onFailAuthMessage / ZICO> result : " + result);
        switch (result) {
            case -101 :   // NO_NODE : getRtcServer.json 서버 할당없이 접근한 경우..
                mPresenter.onStatusInit(mRoomSpec);
                break;
            case -116 :   // INVALID_CONNECTION
                break;
            case -8001 :  // AUTH_INVALID_PARAMETER
                break;
            case -8011 :  // AUTH_EXPIRED_TOKEN
                Log.e(TAG, "<onFailAuthMessage / ZICO> expired token");

                break;
            case -8080 :  // AUTH_SERVER_ERROR
                break;
        }
    }

    @Override
    public void onFailRtcStatus(WebSocketKlgeSession session, JsonObject response) {
        int result = response.get("result").getAsInt();
        Log.e(TAG, "<onFailRtcStatus / ZICO> result : " + result);
        switch (result) {
            case -117 :   // DUPLICATED_SESSION
                break;
            case -8000 :   // NOT_AUTHORIZED
                break;
        }
    }

    @Override
    public void onPeerInit(String fromUserId, JsonArray turnServers, String sessionId) {
        Log.d(TAG, "<onPeerInit / ZICO>");
        mPresenter.onPeerInit(fromUserId, turnServers, sessionId);
    }

    /**
     * 장문의 소설 1탄
     *
     * 안드로이드에서 BUSY를 판단하는 기준은 현재 피어커넥션 리스트에서 연결중인 상태의 피어커넥션의 개수를 가지고
     * 판단하도록 한다.
     *
     * 연결중인 피어커넥션의 상태는 iceConnectionState.NEW, iceConnectionState.CHECKING 인 상태를
     * BUSY라고 판단한다.
     *
     * @param response from, to, session_id
     */
    @Override
    public void onPeerBusyQuery(JsonObject response) {
        mPresenter.onPeerBusyQuery(response);
    }

    /**
     * 단문의 소설 2탄
     *
     * 상대방의 BUSY상태에 대한 결과를 받았을 때 처리사항
     * @param response from, to, session_id, status
     */
    @Override
    public void onPeerBusyReply(JsonObject response) {
        mPresenter.onPeerBusyReply(response);
    }


    /**
     * onWebSocketClose() 시점에서 호출된다. 웹소켓이 외부 요인으로 인해 close되면 바로 재접속하는 플로우를 실행시킨다.
     */
    @Override
    public void onReconnectSession() {
        Log.d(TAG, "<onReconnectSession / ZICO>");
        // TODO 웹소켓 접속을 다시 시작해야 하는 경우
        mPresenter.destroy();

        final Timer timer = new Timer();
        TimerTask setUpViewTask = new TimerTask() {
            @Override
            public void run() {
                if (mNetworkState == NetworkState.CONNECTED) {
                    Log.d(TAG, "<onReconnectSession / ZICO> reconnect webscoket execute.. try getRtcServer.json");
                    mPresenter.onStatusInit(mRoomSpec);
                    timer.cancel();
                }
                Log.d(TAG, "<onReconnectSession / ZICO> Try reconnect webscoket again..");
            }
        };
        timer.schedule(setUpViewTask, 500, 1000);
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RoomView>
     */
    @SuppressLint("InflateParams")
    @Override
    public RenderView addRemoteVideoView(String userNo) {
        Log.d(TAG, "<addRemoteVideoView / ZICO> userNo : " + userNo + ", position : " + (mPeerWatcher.getPeerCount()-1));

        if (TextUtils.isEmpty(userNo)) {
            throw new IllegalArgumentException("Invalid user number for remote render view.");
        }
        View videoView = null;
        if (getContext() != null)   // Remote 뷰가 그려질려는 찰나에 수업을 나가면 NullPointerException이 발생함 - 2017.03.30
            videoView = LayoutInflater.from(getContext()).inflate(R.layout.layout_renderer_item, mVideoContainer, false);

        if (videoView == null)
            return null;
        if (isExitRoom)
            return null;

        RenderView render = new RenderView(videoView, mPeerWatcher.getPeerCount()-1);
        render.init(getRtcContext().getEglContext(), null);
        updateRemoteVideoView(render);

        render.setUserNo(userNo);
        mRenderViewMap.put(userNo, render);

        FrameLayout.LayoutParams lp= new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        lp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * mRenderViewMap.size(), 0, 0, 0);
        videoView.setLayoutParams(lp);
        mVideoWrapper.addView(videoView);

        render.visible();
        render.goneRenderer();

        if (mLocalRenderView != null) {
            mLocalRenderView.hideControllMenuView();
        }
        for(Map.Entry<String, RenderView> entry : mRenderViewMap.entrySet()){
            entry.getValue().hideControllMenuView();
        }
        final RoomUser remoteRoomUser = mRoomUserPresenter.getRoomUser(userNo);

        final Timer timer = new Timer();
        TimerTask setUpViewTask = new TimerTask() {
            @Override
            public void run() {
                if (remoteRoomUser != null) {
                    Log.d(TAG, "<addRemoteVideoView / ZICO> order video view");
                    orderVideoView(remoteRoomUser);
                    timer.cancel();
                }
                Log.d(TAG, "<addRemoteVideoView / ZICO> Try order video view again..");
            }
        };
        timer.schedule(setUpViewTask, 0, 500);

        return render;
    }


    @Override
    public void removeRemoteVideoView(String userNo) {
        if (TextUtils.isEmpty(userNo)) {
            throw new IllegalArgumentException("Invalid userNo for remote render view.");
        }

        RenderView render = mRenderViewMap.remove(userNo);
        if (render != null) {
            Log.d(TAG, "<removeRemoteVideoView / ZICO> remote View remove..");
            mVideoWrapper.removeView(render.getView());
            render.release();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reorderVideoView();
            }
        });
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <implements : RtcStatusDialogFragment.OnStatusChangeListener>
     */
    @Override
    public void onStatusInit(final RoomSpec roomSpec, boolean isChange) {
        Log.d(TAG, "<onStatusInit / ZICO>");
        mRoomSpec = roomSpec;
        if (!mRoomSpec.isSeparate()) {
            Log.d(TAG, "<onStatusInit / ZICO> 분리된 수업이 아닙니다.");
            // 분리된 수업이 아니면 roomSpec 정보를 parentRoomSpec에도 복사한다.
            mParaentRoomSpec = roomSpec;
        }

//        RtcChatClient.instance().createLocalMediaChannel(getRtcContext());

//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mRoomSpec = roomSpec;
//                if (mRoomSpec.isVideoControlEnable()) {
//                    if (!mRoomSpec.isParentMaster()) {
//                        mLocalRenderView.gone();
//                    }
//                } else {
//                    if (!mRoomSpec.isVideoEnabled() && !mRoomSpec.isAudioEnabled()) {
//                        mLocalRenderView.goneRenderer();
//                    } else {
//                        mLocalRenderView.visibleRenderer();
//                    }
//                }
//            }
//        });

        if (!mRoomSpec.isParentMaster()) {
            if (mRoomSpec.isVideoControlEnable()) {    // 영상 제어 모드일 때, 학생이면 CameraCapturer를 정지시킴..
                Log.d(TAG, "<onStatusInit / ZICO> 영상 제어 수업의 참여자이므로 비디오를 OFF 합니다.");
                RtcChatClient.instance().stopVideo();
                mLocalRenderView.gone();
            }
        } else {
            if (!mRoomSpec.isVideoEnabled()) {
                Log.d(TAG, "<onStatusInit / ZICO> 비디오 설정이 OFF 입니다.");
                RtcChatClient.instance().stopVideo();
                mLocalRenderView.goneRenderer();
            }
        }
        if (mRoomSpec.isVideoEnabled()) {
            Log.d(TAG, "<onStatusInit / ZICO> 비디오 설정이 ON 입니다.");
//            RtcChatClient.instance().startVideo();
//            mLocalRenderView.visible();
//            mLocalRenderView.visibleRenderer();
//            RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
//            if (localMediaChannel != null) {
//                mLocalRenderView.init(getRtcContext().getEglContext(), null);
//                localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
//                updateLocalVideoView();
//            }
        }

        Toast.makeText(getContext().getApplicationContext(), "수업을 시작합니다.", Toast.LENGTH_SHORT).show();

        mPresenter.onStatusInit(roomSpec);
        int videoCtrl = mRoomSpec.isVideoControlEnable() ? 1 : 0;
        int soundOnly = mRoomSpec.isWhiteboardMode() ? 1 : 0;
        if (mRoomSpec.isParentMaster() && isChange) {
            if (mRoomSpec.isSeparate()) {
                Log.d(TAG, "<onStatusInit> Separated room.. videoCtrl : " + videoCtrl + ", soundOnly : " + soundOnly);
                ((RoomActivity) getActivity()).mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.videoOptions(" + videoCtrl + "," + soundOnly + ");");
            } else {
                Log.d(TAG, "<onStatusInit> NOT Separated room.. roomId : " + mRoomSpec.getRoomId() + ", videoCtrl : " + videoCtrl + ", soundOnly : " + soundOnly);
                ((RoomActivity) getActivity()).mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.BroadCast.videoOptions('all', '" + mRoomSpec.getRoomId() + "', " + videoCtrl + "," + soundOnly + ");");
            }
        }
    }

    @Override
    public void onStatusChange(RoomSpec roomSpec, boolean isChange) {
        Log.d(TAG, "<onStatusChange / ZICO>");
        RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
        if (localMediaChannel != null) {
            localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
            updateLocalVideoView();
        }
        mRoomSpec = roomSpec;
        if (mRoomSpec.isSeparate()) {
            mRoomSpec = roomSpec;
        }

        // 로컬미디어의 비디오, 오디오 트랙에 설정된 값 반영
        localMediaChannel.enableVideoTrack(mRoomSpec.isVideoEnabled());
        localMediaChannel.enableAudioTrack(mRoomSpec.isAudioEnabled());

        Toast.makeText(getContext().getApplicationContext(), "미디어 상태가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
        mPresenter.onStatusChange(roomSpec);

        int videoCtrl = mRoomSpec.isVideoControlEnable() ? 1 : 0;
        int soundOnly = mRoomSpec.isWhiteboardMode() ? 1 : 0;
        if (mRoomSpec.isParentMaster() && isChange) {
            if (mRoomSpec.isSeparate()) {
                Log.d(TAG, "<onStatusInit> Separated room.. videoCtrl : " + videoCtrl + ", soundOnly : " + soundOnly);
                ((RoomActivity) getActivity()).mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.videoOptions(" + videoCtrl + "," + soundOnly + ");");
            } else {
                Log.d(TAG, "<onStatusInit> NOT Separated room.. roomId : " + mRoomSpec.getRoomId() + ", videoCtrl : " + videoCtrl + ", soundOnly : " + soundOnly);
                ((RoomActivity) getActivity()).mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.BroadCast.videoOptions('all', '" + mRoomSpec.getRoomId() + "', " + videoCtrl + "," + soundOnly + ");");
            }
        }
    }


    @Override
    public void onStatusCancel() {
        Log.d(TAG, "<onStatusCancel>");
        RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
        if (localMediaChannel != null) {
            localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalRenderView.getRenderer()));
            updateLocalVideoView();
        }
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RoomUserPresenter.RoomUserEvent>
     */
    @Override
    public void onEnterUser(final RoomUser newUser) {
        Log.d(TAG, "<onEnterUser> user : " + newUser.getUserNm() + ", list : " + mRoomUserPresenter.getRoomUserList().size());
        RoomUser user = mRoomUserPresenter.getRoomUserList().get(newUser.getUserNo());
        if (mRoomSpec.getUserNo().equals(user.getUserNo())) {
            Log.d(TAG, "<onEnterUser> 내 정보 이므로 LocalRenderView가 존재하는지 체크합니다.");
            if (mLocalRenderView != null) {
                Log.d(TAG, "<onEnterUser> LocalRenderView가 존재하므로 내 정보를 UI에 반영합니다.");
                setUpRenderView(mLocalRenderView, newUser);
            }
        } else {
            Log.d(TAG, "<onEnterUser> 상대방 정보 이므로 RemoteRenderView가 존재하는지 체크합니다.");
            final RenderView remoteRenderView = mRenderViewMap.get(newUser.getUserNo());
            if (remoteRenderView != null) {
                Log.d(TAG, "<onEnterUser> RemoteRenderView가 존재하므로 상대방 정보를 UI에 반영합니다.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUpRenderView(remoteRenderView, newUser);
                    }
                });
            }
        }


//        for (RoomUser user : mRoomUserPresenter.getRoomUserList()) {
//            if (newUser.getUserNo().equals(user.getUserNo())) {   // newuser가 먼저 실행된 경우..
//                if (mRoomSpec.getUserNo().equals(user.getUserNo())) {
//                    if (mLocalRenderView != null)
//                        setUpRenderView(mLocalRenderView, newUser);
//                } else {
//                    if (mRenderViewMap.get(newUser.getUserNo()) != null)
//                        setUpRenderView(mRenderViewMap.get(newUser.getUserNo()), newUser);
//                }
//
//            }
//        }
    }

    @Override
    public void onExitUser(RoomUser removeUser) {
        Log.d(TAG, "<onExitUser> user : " + removeUser.getUserNm());
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : NetworkConnectionEvents>
     */

    /**
     * 네트워크가 연결되면 호출되는 콜백
     */
    @Override
    public void onConnected() {
        Log.d(TAG, "<onConnected / ZICO>");
        if (mNetworkState == NetworkState.DISCONNECTED) {
            Log.d(TAG, "<onConnected / ZICO> DISCONNECTED -> CONNECTED");
//            mPresenter.onStatusInit(mRoomSpec.isVideoEnabled(), mRoomSpec.isAudioEnabled(), mRoomSpec.isVideoControlEnable());
        }
        mNetworkState = NetworkState.CONNECTED;
    }

    /**
     * 네트워크가 끊어지면 호출되는 콜백
     */
    @Override
    public void onDisconnected() {
        Log.d(TAG, "<onDisconnected / ZICO>");
        mNetworkState = NetworkState.DISCONNECTED;
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<override : RoomEvents>
     */
    @Override
    public void initializeRoom(RoomSpec roomSpec) {
        Log.d(TAG, "<initializeRoom / ZICO>");
        mRoomSpec = roomSpec;
        isInitializeRoom = true;

    }


    @SuppressWarnings("unused")
    @OnClick(R.id.video_wrapper)
    void onVideoWrapperClick() {
        Log.d(TAG, "onVideoWrapperClick");
        if (mLocalRenderView != null) {
//            mLocalRenderView.showHideControllMenu();
            mLocalRenderView.setControllMenuView();
        }
        for(Map.Entry<String, RenderView> entry : mRenderViewMap.entrySet()){
//            entry.getValue().showHideControllMenu();
            entry.getValue().setControllMenuView();
        }
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <implements : VideoPluginEvents.onVideoOptionChange>
     */
    @Override
    public void onVideoOptionChange(String roomId, boolean videoCtrl, boolean soundOnly) {
        Log.d(TAG, "<onVideoOptionChange / ZICO> roomId : " + roomId + ", videoCtrl : " + videoCtrl + ", soundOnly : " + soundOnly);
        mRoomSpec.setIsVideoControlEnable(videoCtrl);
        mRoomSpec.setIsWhiteboardMode(soundOnly);

        boolean sendStatus = false;

        if (mRoomSpec.isSeparate()) {
            if (!roomId.equals(mRoomSpec.getRoomId()))
                return;
        }

        if (!mRoomSpec.isParentMaster()) {
            if (videoCtrl) {  // 영상 제어 모드로 변경되었으면 내 미디어 상태를 전부 off 시킨다.
                mRoomSpec.setIsVideoEnable(false);
                mRoomSpec.setIsAudioEnable(false);
                sendStatus = true;
            } else {  // 영상 제어 모드가 해제되었으면 미디어 설정 다이얼로그를 출력한다.
                if (soundOnly) {
                    mRoomSpec.setIsVideoEnable(false);
                    mRoomSpec.setIsAudioEnable(true);
                }
                RtcStatusDialogFragment fragment = RtcStatusDialogFragment.newInstance(mRoomSpec);
                fragment.setOnStatusChangeListener(MultiVideoChatFragment.this);
                fragment.setCancelable(true);
                fragment.show(getFragmentManager(), RtcStatusDialogFragment.class.getSimpleName());
                sendStatus = false;
            }
        }

        if (sendStatus) {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("userno", mRoomSpec.getUserNo());
            arguments.put("roomid", mRoomSpec.getRoomId());
            arguments.put("video", mRoomSpec.isVideoEnabled() ? "enable" : "disable");
            arguments.put("audio", mRoomSpec.isAudioEnabled() ? "enable" : "disable");
            arguments.put("os_version", Build.VERSION.RELEASE);
            arguments.put("device", Build.MODEL);
            mPresenter.sendPeerStatus(arguments);
        }
    }


    /**
     * 영상 그룹 분리
     * @param roomId
     * @param separate
     */
    @Override
    public void onVideoGroup(String roomId, boolean separate) {
        Log.d(TAG, "<onVideoGroup / ZICO> roomId : " + roomId + ", separate : " + separate);

        mRoomSpec.setIsSeparate(separate);

        mPresenter.destroy();
        for (Map.Entry<String, RenderView> entry : mRenderViewMap.entrySet()) {
            RenderView render = mRenderViewMap.remove(entry.getKey());
            if (render != null) {
                Log.d(TAG, "<onVideoGroup / ZICO> remote View remove..");
                mVideoWrapper.removeView(render.getView());
                render.release();
            }
        }

//        String roomId = "";
//        if (separate)
//            roomId = ((RoomActivity) getActivity()).getRoomId();
//        else {
//            int specialCharPosition = roomId.indexOf("_");
//            roomId = specialCharPosition < 0 ? roomId : roomId.substring(0, specialCharPosition);
//
// }

        // 웹소켓 서버 정보 초기화
        mRoomSpec.setHost("");
        mRoomSpec.setPort("");

        mRoomSpec.setName("");
        mRoomSpec.setRoomId(roomId);

        if (!separate) {
            // 분리된 수업이 합쳐지면 부모 수업의 영상 제어 모드 여부와 화이트보드 모드 여부값을 읽어와서 설정한다.
//            mRoomSpec.setIsVideoControlEnable(mParaentRoomSpec.isVideoControlEnable());
//            mRoomSpec.setIsWhiteboardMode(mParaentRoomSpec.isWhiteboardMode());

//            RtcStatusDialogFragment fragment = RtcStatusDialogFragment.newInstance(mRoomSpec);
//            fragment.setOnStatusChangeListener(MultiVideoChatFragment.this);
//            fragment.setCancelable(true);
//            fragment.show(getFragmentManager(), RtcStatusDialogFragment.class.getSimpleName());
        } else {
            mRoomSpec.setIsVideoControlEnable(false);
            mRoomSpec.setIsWhiteboardMode(false);
        }

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mPresenter.start();
//            }
//        }, 1000);
    }

    @Override
    public void onVideoNoti(String action, String fromUserNo, String toUserNo) {
        if (action.equals("request")) {

        } else if (action.equals("connect")) {
            // 영상 제어 수업에서 송출자로써 승인을 받았을 때는 비디오와 오디오를 활성화시키고 설정 다이얼로그를 띄운다.
            mRoomSpec.setIsVideoEnable(true);
            mRoomSpec.setIsAudioEnable(true);
            mRoomSpec.setIsAllowCaller(true);
            RtcStatusDialogFragment fragment = RtcStatusDialogFragment.newInstance(mRoomSpec);
            fragment.setOnStatusChangeListener(MultiVideoChatFragment.this);
            fragment.setCancelable(true);
            fragment.show(getFragmentManager(), RtcStatusDialogFragment.class.getSimpleName());

        } else if (action.equals("disconnect")) {
            mRoomSpec.setIsAllowCaller(false);
        }
    }

    @Override
    public void onMasterChange() {
        Log.d(TAG, "<onMasterChange / ZICO>");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reorderVideoView();
            }
        });

    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : KlgePeerWatcher.RtcListListener>
     */
    @Override
    public void onPeerList() {
        Log.d(TAG, "<onPeerList / ZICO>");
//        updateVideoPosition();
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<methods>
     */
    public void openRtcSettingDialog(RoomSpec roomSpec) {
        mRoomSpec = roomSpec;
        if (TextUtils.isEmpty(mRoomSpec.getHost())) {
            RtcStatusDialogFragment fragment = RtcStatusDialogFragment.newInstance(mRoomSpec);
            fragment.setOnStatusChangeListener(this);
            fragment.setCancelable(true);
            fragment.show(getFragmentManager(), RtcStatusDialogFragment.class.getSimpleName());
        }
    }

    /**
     * 비디오 UI 정렬을 업데이트 하는 메서드. 유저 퇴장, 진행권한 변경 시마다 호출되어 비디오 UI의 정렬을 업데이트 한다.
     * - 1번째 : 수업 진행자
     * - 2번째 : 나 자신
     * - 3번째 : 순서대로
     */
    private void updateVideoPosition() {
        Log.d(TAG, "<updateVideoPosition / ZICO>");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConcurrentHashMap<String, KlgePeerNode> peerList = mPeerWatcher.getPeers();

                for (Map.Entry<String, KlgePeerNode> entry : peerList.entrySet()) {
                    String peerUserNo = entry.getKey();
                    for (Map.Entry<String, RenderView> view : mRenderViewMap.entrySet()) {
                        RenderView peerView = view.getValue();
                        if (peerUserNo.equals(view.getKey())) {
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * mRenderViewMap.size(), 0, 0, 0);  // 기존 순차적 방식의 영상 배열
                            peerView.getView().setLayoutParams(lp);
                        }
                    }
                }

//                int index = 0;
//                for (Map.Entry<String, KlgePeerNode> entry : peerList.entrySet()) {
//                    String peerUserNo = entry.getKey();
//                    boolean isMaster = mRoomUserPresenter.getRoomUser(peerUserNo).isMaster();
//                    boolean isMySelf = peerUserNo.equals(RoomActivity.activity.getUserNo());
//                    for (Map.Entry<String, RenderView> view : mRenderViewMap.entrySet()) {
//                        RenderView peerView = view.getValue();
//                        if(peerUserNo.equals(view.getKey())) {
//                            // 영상 UI 배치하는 로직 영역
//                            Log.d(TAG, "<updateVideoPosition / ZICO> userNm : " + mRoomUserPresenter.getRoomUser(peerUserNo).getUserNm() + ", isMaster : " + isMaster);
//                            int videoPosition;
//                            if (isMySelf) {
//                                if (isMaster) {
//                                    videoPosition = 0;
//                                } else {
//                                    videoPosition = 1;
//                                }
//                            } else {
//                                if (isMaster) {
//                                    videoPosition = 0;
//                                } else {
//                                    videoPosition = index;
//                                }
//                            }
//                            peerView.setPosition(videoPosition);
//
//                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                            lp.setMargins(AndroidUtils.getPxFromDp(getContext(), 130) * videoPosition, 0, 0, 0);
//                            peerView.getView().setLayoutParams(lp);
//                        }
//                    }
//                    index++;
//                }
            }
        });
    }

    /**
     * 비디오 UI에 유저명 출력, 진행자 여부 아이콘 표시, 아이콘 처리를 수행하는 메서드이다.
     * @param renderView
     * @param user
     */
    private void setUpRenderView(final RenderView renderView, final RoomUser user) {
        Log.d(TAG, "<setUpRenderView / ZICO> Render View set up. userNm : " + user.getUserNm() + ", isMaster : " + user.isMaster());
        KlgePeerNode remotePeerNode = mPeerWatcher.getPeerNode(user.getUserNo());
        final KlgeStatus remotePeerStatus = remotePeerNode != null ? remotePeerNode.getPeer().getStatus() : null;


        // 유저 썸네일 이미지 출력
        Glide.with(getActivity())
            .load(AndroidUtils.changeSizeThumbnail(user.getThumbnail(), 130))
            .error(getResources().getDrawable(R.drawable.img_video_user_novideo))
            .into(renderView.getNoVideoView());

        // 영상 최대화 버튼 이벤트
        renderView.getScreenMaxBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "준비 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 영상 설정 버튼 이벤트
        renderView.getVideoSettingBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RtcStatusDialogFragment fragment = RtcStatusDialogFragment.newInstance(mRoomSpec);
                fragment.setOnStatusChangeListener(MultiVideoChatFragment.this);
                fragment.setCancelable(true);
                fragment.show(getFragmentManager(), RtcStatusDialogFragment.class.getSimpleName());
            }
        });

        // 리커넥트 버튼 이벤트
        renderView.getReconnectBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "<MultiVideoChatFragment> reconnect button clicked..");
                //((WebSocketKlgeSession) session).sendPeerInit(mRoomSpec.getUserNo(), userId, session.getSessionId(), );
                mPresenter.reconnect(user.getUserNo());
//                RenderView renderView = mRenderViewMap.get(user.getUserNo());
//                if (renderView != null) {
//                    renderView.visibleLoader();
//                    renderView.goneRenderer();
//                }


            }
        });


        // 영상 볼륨 ON/OFF 버튼 이벤트
        renderView.getVolumeControlBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean audioEnable = mPresenter.enableRemoteAudio(user.getUserNo());
                if (audioEnable) {
                    renderView.getVolumeControlBtn().setImageResource(R.drawable.btn_video_user_volume_on);
                } else {
                    renderView.getVolumeControlBtn().setImageResource(R.drawable.btn_video_user_volume);
                }
            }
        });
        renderView.setUserNm(user.getUserNm());

        // 리모트 피어의 음성 상태값에 따라서 버튼 UI 처리..
        if (remotePeerNode != null) {
            KlgeStatus peerStatus = remotePeerNode.getPeer().getStatus();
            if (TextUtils.equals("disable", peerStatus.getAudio())) {
                renderView.getVolumeControlBtn().setImageResource(R.drawable.btn_video_user_volume);
            } else {
                renderView.getVolumeControlBtn().setImageResource(R.drawable.btn_video_user_volume_on);
            }
        }

        // 진행자 여부에 따라 UI 처리
        if (!user.isMaster()) {
            renderView.getIcoVideoAuthrity().setVisibility(View.INVISIBLE);
            renderView.getRenderer().setBackground(null);
        } else {
            renderView.getIcoVideoAuthrity().setVisibility(View.VISIBLE);
            renderView.getRenderer().setBackground(getResources().getDrawable(R.drawable.video_renderer_style_41e169_border));
        }
    }

    private void setUpArguments() {
        Bundle arguments = getArguments();
        mRoomSpec = arguments.getParcelable("arguments");
        Log.d(TAG, "<setUpArguments> mRoomSpec roomId : " + mRoomSpec.getRoomId());
    }

    private void setUpInjector() {
        getComponent().inject(this);
        mPresenter.initialize(mRoomSpec, getComponent());
    }

    private void setUpView() {
        Log.d(TAG, "<setUpView / ZICO> start");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mRoomSpec.isParentMaster()){
                    if (mRoomSpec.isVideoControlEnable()) {  // 선생이 아니고 비디오 제어모드 일때는 나의 영상 UI를 숨긴다.
                        Log.d(TAG, "<setUpView / ZICO> Video Control mode.. My Video UI hide..");
                        mLocalRenderView.gone();
                        return;
                    }
                }
                Log.d(TAG, "<setUpView / ZICO> Normal mode.. My Video UI initialize.");
                mLocalRenderView.visible();
                // TODO : 썸네일 삽입 시 크기가 정확히 맞지 않는 이슈가 있음..
                Glide.with(getActivity())
                        .load(AndroidUtils.changeSizeThumbnail(prefManager.getUserThumbnail(), 130))
                        .error(getResources().getDrawable(R.drawable.img_video_user_novideo))
                        .into(mLocalRenderView.getNoVideoView());

                mLocalRenderView.hideReconnectBtn();
                mLocalRenderView.hideVolumeControlBtn();
                mLocalRenderView.showVideoSettingBtn();
            }
        });
    }


    private void updateLocalVideoView() {
        mLocalRenderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        mLocalRenderView.setMirror(RtcChatClient.instance().isFrontCamera());
    }

    private void updateRemoteVideoView(RenderView render) {
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        render.setMirror(false);
    }

    private void destroyRtcView() {
        if (mLocalRenderView != null) {
            mLocalRenderView.release();
            mLocalRenderView = null;
        }

        for (Map.Entry<String, RenderView> render : mRenderViewMap.entrySet()) {
            render.getValue().release();
        }
        mRenderViewMap.clear();
    }

    /*
     *----------------------------------------------------------------------------------------------
     *--<inner>
     */

    private CameraVideoCapturer.CameraSwitchHandler
            mCameraSwitchCallback = new CameraVideoCapturer.CameraSwitchHandler() {

        @Override
        public void onCameraSwitchDone(boolean isFrontCamera) {
            updateLocalVideoView();
        }

        @Override
        public void onCameraSwitchError(String s) {

        }
    };

}
