package com.knowlounge.view.room;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.PollPopupDialog;
import com.knowlounge.PollPopupDialogPhone;
import com.knowlounge.R;
import com.knowlounge.RoomSwitchActivity;
import com.knowlounge.RtcSupportActivity;
import com.knowlounge.apprtc.KlgeConnection;
import com.knowlounge.apprtc.KlgePeerChannel;
import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.apprtc.WebSocketKlgeSession;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.customview.DragSelectableView;
import com.knowlounge.customview.PreviewCanvas;
import com.knowlounge.dagger.HasComponent;
import com.knowlounge.dagger.component.DaggerRoomActivityComponent;
import com.knowlounge.dagger.component.RoomActivityComponent;
import com.knowlounge.dagger.modules.ActivityModule;
import com.knowlounge.dagger.modules.RoomSpecModule;
import com.knowlounge.dagger.modules.RoomUsersModule;
import com.knowlounge.fragment.ConfigEditFragment;
import com.knowlounge.fragment.ConfigFragment;
import com.knowlounge.fragment.NotificationApprove;
import com.knowlounge.fragment.RoomLeftmenuFragment;
import com.knowlounge.fragment.RoomRightSlideFragment;
import com.knowlounge.fragment.StarShopFragment;
import com.knowlounge.fragment.WebViewFragment;
import com.knowlounge.fragment.dialog.ExtendReqDialogFragment;
import com.knowlounge.fragment.dialog.KnowloungeDialogFragment;
import com.knowlounge.fragment.dialog.RoomInfoDialogFragment;
import com.knowlounge.fragment.dialog.TeacherCallDialogFragment;
import com.knowlounge.fragment.poll.DrawingPollNotifyFragment;
import com.knowlounge.fragment.poll.PollAnswerFragment;
import com.knowlounge.gcm.GcmRegistStatePreference;
import com.knowlounge.inapp.InAppRootDispatcher;
import com.knowlounge.login.LoginActivity;
import com.knowlounge.manager.SharedPreferencesManager;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.Chat;
import com.knowlounge.model.ChatUser;
import com.knowlounge.model.CommentItem;
import com.knowlounge.model.OtherUser;
import com.knowlounge.model.PollCreateData;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.model.RoomUser;
import com.knowlounge.model.RoomUsers;
import com.knowlounge.model.User;
import com.knowlounge.multipage.MultiPageFragment;
import com.knowlounge.multipage.data.AbstractDataProvider;
import com.knowlounge.multipage.data.MultiPageDataProvider;
import com.knowlounge.plugins.CommunicationPlugin;
import com.knowlounge.plugins.RoomPlugin;
import com.knowlounge.plugins.UserPlugin;
import com.knowlounge.plugins.VideoPlugin;
import com.knowlounge.receiver.NetworkStateReceiver;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.ChattingEvent;
import com.knowlounge.rxjava.message.MultiPageEvent;
import com.knowlounge.rxjava.message.VideoIdEvent;
import com.knowlounge.rxjava.message.Wtf;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.GoogleAnalyticsService;
import com.knowlounge.util.RestClient;
import com.knowlounge.youtube.YouTubeSearchDialogFragment;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wescan.alo.rtc.RtcChatArguments;
import com.wescan.alo.rtc.RtcChatClient;
import com.wescan.alo.rtc.RtcChatSession;
import com.wescan.alo.rtc.RtcMediaChannel;
import com.wescan.alo.rtc.RtcPeerChannel;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.SessionDescription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import rx.Subscriber;
import yuku.ambilwarna.AmbilWarnaDialog;

import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;


/**
 * 수업에 입장했을 때 실행되는 Activity이다.
 * 초기에 데이터를 받아서 설정하고 뷰를 초기화하는 진입점은 initializeRoom이며, onCreate()에서는 데이터 없이 초기화할 수 있는 내용들에 한해서만 초기화를 진행한다.
 * 실제로 데이터를 받아서 뷰를 초기화 하는 진입점은 자바스크립트에서 제공하고 있다.
 *  - 자바스크립트에서 'deviceready' 이벤트가 발생하면 canvas/get.json 을 ajax로 호출하여 룸의 데이터를 조회한다.
 *  - 자바스크립트에서 조회한 룸의 데이터는 Cordova 플러그인을 통해 안드로이드로 넘겨주게 되는데 이 때 호출하는 자바 메서드가 initializeRoom() 이다.
 *
 * @author Minsoo Kim
 * @see WebViewFragment
 * @see org.apache.cordova.CordovaInterface
 * @since 2015.12.21
 */
public class RoomActivity extends RtcSupportActivity
        implements View.OnClickListener,
        RoomInfoDialogFragment.OnRoomInfoListener,
        RoomLeftmenuFragment.SetLeftDrawerListener,
        ConfigFragment.OnSetRoomConfigListener,
        ConfigEditFragment.OnRoomInfoListener,
        RoomPlugin.RoomEventListener,
        UserPlugin.MasterChangeEventListener,
        CommunicationPlugin.ChattingEventListener,
        CommunicationPlugin.CommentEventListener,
        TeacherCallDialogFragment.TeacherCallDialogListener,
        UserPlugin.UserListEventListener,
        VideoPlugin.VideoPluginEvents,
        StarShopFragment.InAppBillingListener,
        HasComponent<RoomActivityComponent> {

    private static String TAG = "RoomActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public WebViewFragment mWebViewFragment;       // CordovaWebView Fragment

    @Inject
    KlgePeerWatcher mPeerWatcher;
    @Inject
    RoomUserPresenter mRoomUserPresenter;

    // VIEW - Widget
    @BindView(R.id.room_toolbar) Toolbar mToolbar;

    // VIEW - View Component
    @BindView(R.id.txt_move_room) TextView txtMoveRoom;
    @BindView(R.id.ico_move_room) ImageView icoMoveRoom;
    @BindView(R.id.txt_answer_poll_guide) TextView tooltipAnswerPollGuide;
    @BindView(R.id.txt_poll) TextView txtAnswerPoll;
    @BindView(R.id.layer_document_submenu) ScrollView mDocumentSubMenu;
    @BindView(R.id.layer_poll_submenu) ScrollView mPollSubMenu;
    @BindView(R.id.layer_text_submenu) ScrollView mTextSubMenu;
    @BindView(R.id.draggable_draw_container) DragSelectableView selectableView;
    @BindView(R.id.right_menu_area_scroll) ScrollView rightMenu;
    @BindView(R.id.video_user_invite_btn) ImageView mVideoInviteBtn;
    @BindView(R.id.video_separate_btn) ImageView mVideoSeperateBtn;

    // View - Buttons
    @BindView(R.id.btn_rightmenu_fold) ImageView mBtnMenuFold;
    @BindView(R.id.btn_rightmenu_fold_phone) ImageView mBtnMenuFoldPhone;
    @BindView(R.id.btn_rightmenu_hand) ImageView mBtnMenuHand;
    @BindView(R.id.btn_rightmenu_hand_phone) ImageView mBtnMenuHandPhone;
    @BindView(R.id.btn_rightmenu_pen) ImageView mBtnMenuPen;
    @BindView(R.id.btn_rightmenu_pen_phone) ImageView mBtnMenuPenPhone;
    @BindView(R.id.btn_rightmenu_pen_landscape) ImageView mBtnMenuPenLand;
    @BindView(R.id.btn_rightmenu_eraser) ImageView mBtnMenuEraser;
    @BindView(R.id.btn_rightmenu_eraser_phone) ImageView mBtnMenuEraserPhone;
    @BindView(R.id.btn_rightmenu_eraser_landscape) ImageView mBtnMenuEraserLand;
    @BindView(R.id.btn_rightmenu_laser_phone) ImageView mBtnMenuLaserPhone;
    @BindView(R.id.btn_rightmenu_text) ImageView mBtnMenuText;
    @BindView(R.id.btn_rightmenu_text_phone) ImageView mBtnMenuTextPhone;
    @BindView(R.id.btn_rightmenu_document) ImageView mBtnMenuDocument;
    @BindView(R.id.btn_rightmenu_document_phone) ImageView mBtnMenuDocumentPhone;
    @BindView(R.id.btn_rightmenu_document_landscape) ImageView mBtnMenuDocumentLand;
    @BindView(R.id.btn_rightmenu_memo) ImageView mBtnMemo;
    @BindView(R.id.btn_rightmenu_poll_phone) ImageView mBtnMenuPollPhone;
    @BindView(R.id.btn_rightmenu_poll_landscape) ImageView mBtnMenuPollLand;
    @BindView(R.id.btn_rightmenu_multipage_phone) ImageView mBtnMultipagePhone;
    @BindView(R.id.btn_rightmenu_multipage_landscape) ImageView mBtnMultipageLand;

    // View - Layouts
    @BindView(R.id.right_drawer_room) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_layout) FrameLayout mRoomContent;
    @BindView(R.id.right_drawer_panel_room) FrameLayout mDrawerRightPanel;
    @BindView(R.id.right_drawer_layout) LinearLayout mDrawerContent;
    @BindView(R.id.left_drawer_layout) LinearLayout mLeftDrawerContent;
    @BindView(R.id.rightmenu_area) LinearLayout mRightMenuArea;
    @BindView(R.id.phone_rightmenu_area) LinearLayout mPhoneRightMenuArea;
    @BindView(R.id.layout_move_room) LinearLayout layoutBottom;
    @BindView(R.id.container_move_room) LinearLayout containerMoveRoom;
    @BindView(R.id.container_poll) LinearLayout containerAnswerPoll;
    @BindView(R.id.service_guide_layout) FrameLayout serviceGuideLayout;
    @BindView(R.id.right_menu_tablet) LinearLayout mRightMenuTablet;
    @BindView(R.id.right_menu_phone) LinearLayout mRightMenuPhone;
    @BindView(R.id.right_menu_landscape) LinearLayout mRightMenuLand;

    private LinearLayout multiPageSlidingLayout;
    @BindView(R.id.multi_page_sliding_layout_phone) LinearLayout mMultiPageLayoutPhone;
    @BindView(R.id.multi_page_sliding_layout) LinearLayout mMultiPageLayout;

    private ActionBar actionBar;
    private View mCustomActionBar;
    public PopupWindow mPopupWindow;

    private RoomActivityComponent mComponent;

    private static int windowMode = -1;

    // DATA
    private RoomSpec mRoomSpec;
    private RoomUsers mRoomUsers;

    // FLAG
    private static boolean isFavoriteOn = false;
    private boolean isRightMenuFold = false;
    private boolean teacherFlag;
    private boolean creatorFlag = false;
    private boolean masterFlag = false;
    private boolean guestFlag = false;
    private boolean isHandMode = false;
    private boolean isTextMode = false;
    private boolean isExitRoom = false;
    private boolean isSelectorMode = false;
    private boolean isPollProgress = false;
    private boolean isVideoSeparate = false;
    private boolean isVideoControl = false;

    private static Context context;

    public static RoomActivity activity = null;

    private String mServiceType;    // knowlounge or premium
    private static String roomCode = "";
    private static String roomId = "";
    private static String parentRoomId = "";
    private static String roomTitle = "";
    private static String userId = "";
    private static String userNo = "";
    private static String userNm = "";
    private static String userType = "";
    private String creatorNo = "";
    private String masterUserId = "";
    private String masterUserNo = "";
    private String classMasterUserNo = "";
    private String masterRoomSeqNo = "";
    private String snsType = "";
    private String guestNm = "";
    private int userLimitCnt;
    public String userMaxCnt;
    public int videoLimit;
    private String deviceIdForGuest = "";
    private JSONObject authInfo = null;
    private JSONObject bgInfo = null;
    private int zoomVal = 100;   // Canvas zoom value


    //폴 관련 View

    private static boolean isSubMenuCheck = false;

    public float density;
    private static int screenWidth;
    private static int screenHeight;

    private String receiverId;

    private RoomTools roomTools;

    private boolean isDevicePhone;

    private int currentMenu = -1;
    private int currentMode = -1;


    public int totalBadge = 0;
    public int reqBadgeCount = 0;
    public int chatBadgeCnt = 0;
    public int classChatBadgeCnt =  0;
    public int commentBadgeCnt = 0;

    private Runnable exitRoomRunnable;
    private Handler roomHandler = new Handler();

    private boolean allowExitRoom = true;

    private boolean classLoading = false;  // 수업 로딩 관련

    //Dialog is Showing
    private int dialogIsShowing = -1;

    private boolean toggleKeyClicked = false;

    private ImageView btnHeaderAuthority;

    private boolean isExitProcessing = false;

    public View progressView = null;

    //Todo CanvasRightMenu 부분 - start- 창하쓰
    private boolean isOpenRightMenu = false;
    public static int currentParent = -1;
    public static int currentUserChild = -1;
    public static int currentCommunityChild = -1;

    //RoomUserListFragment List 관련
    private Handler rightMenuHandler = new Handler(Looper.getMainLooper());
    public static ArrayList<User> userList = new ArrayList<User>();   // 참여자 리스트
    public static ArrayList<RoomUser> classUserList = new ArrayList<RoomUser>();  // 수업 참여자 리스트
    public static ArrayList<OtherUser> otherUserList = new ArrayList<OtherUser>();  // 미참여자 리스트

    public static ArrayList<RoomUser> myInfoList = new ArrayList<RoomUser>();

    //ChattingFragment && CommnetFragment 관련
    public static ArrayList<Chat> chatList = new ArrayList<Chat>();
    public static ArrayList<ChatUser> chatUserList = new ArrayList<ChatUser>();
    public static ArrayList<Chat> classChatList = new ArrayList<Chat>();
    public static ArrayList<ChatUser> classChatUserList = new ArrayList<ChatUser>();
    public static ArrayList<CommentItem> commentList = new ArrayList<CommentItem>();

    private final int GET_MASTER_AUTH_INTERVAL = 200;
    private long mLastClickTime = 0;
    private boolean isMultiPageOpen = false;


    //todo test 구역
    @BindView(R.id.notification_container) FrameLayout notificationLayout;
    private boolean isNotificationShown = false;
    private SharedPreferencesManager pref;


    private MultiPageDataProvider multiPageDataProvider;
    private String currentPage;

    private RtcStatusDialogFragment mStatusDialogFragment;

    private boolean isUseCamera = false;

    private boolean isQuestioner = false;

    //todo test 구역

    public interface onOpenRightMenuListener {
        void openRightMenu(int parent, int child, boolean isCanvas);
    }
    public interface notiChangeData {
        void onNotiChangeData();
    }

    public interface onCheckArriveBadge {
        void checkArriveBadge();
    }
    public static onOpenRightMenuListener mRightMenuCallBack;
    public static notiChangeData mNotiChangeCallBack;
    public static ArrayList<onCheckArriveBadge> mBadgeCallBack;

    public static void setOnOpenRightMenuListener(onOpenRightMenuListener listener){
        mRightMenuCallBack = listener;
    }


    public static void setOnNotiChangeData(notiChangeData listener){
        mNotiChangeCallBack = listener;
    }


    public static void addOnCheckArriveBadge(onCheckArriveBadge listener){
        if(mBadgeCallBack == null) {
            mBadgeCallBack = new ArrayList<onCheckArriveBadge>();
            mBadgeCallBack.add(listener);
        }else {
            mBadgeCallBack.add(listener);
        }
    }
    public static void removeCheckArriveBadge(onCheckArriveBadge listener){
        if(mBadgeCallBack == null)
            return;
        mBadgeCallBack.remove(listener);
    }

    //Todo CanvasRightMenu 부분 - end- 창하쓰

    // 신규 추가 이벤트
    private VideoEvents mVideoEvents;
    public interface VideoEvents {
        void onVideoOptionChange(String roomId, boolean videoCtrl, boolean soundOnly);
        void onVideoGroup(String roomId, boolean separate);
        void onVideoNoti(String action, String fromUserNo, String toUserNo);
        void onMasterChange();
    }
    public void setVideoEvents(VideoEvents events) {
        mVideoEvents = events;
    }

    private NetworkStateReceiver networkStateReceiver = null;
    private AlertDialog noNetwork;


    private InAppRootDispatcher mDispatcher;
    private InAppRootDispatcher.InAppListener inAppListener = new InAppRootDispatcher.InAppListener() {
        @Override
        public void onFinished() {
            // 실제 구매가 완료가 되었을 때, UI 또는 기타 처리는 여기에서 하면 됩니다
            getStarBalance();
        }
    };


    public int lastChatMode = -1;
    public boolean isLoad = true;

    public PollCreateData pollData = null;  // 폴 데이터..

    private Subscriber<? super Object> mSubscriber = new Subscriber<Object>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            if(o instanceof VideoIdEvent){
                final VideoIdEvent data = (VideoIdEvent)o;
                Log.d(TAG, "video id : "+data.getVideoId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String title = data.getTitle().replaceAll("'", "%27");
                        Log.d(TAG, "replaceText : " + title);

                        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                        webView.sendJavascript("Ctrl.VShare.attachVideo('"+ title + "','" + data.getVideoId() + "');");
                    }
                });
           } else if(o instanceof Wtf) {

           }
        }
    };

    //BookLoading bookLoading;

    @Override
    public void onMoveTeacherRoom(String roomCode) {
        moveRoom(roomCode, null);
    }

    // get RoomActivity's Context
    public static Context getContext() {
        return context;
    }

    private WenotePreferenceManager prefManager;


    @Override
    public KlgePeerWatcher getPeerWatcher() {
        return mPeerWatcher;
    }

    @Override
    protected RtcChatSession newChat(KlgeConnection connection) {
        Log.d(TAG, "<newChat / ZICO>");
        // 이곳에서 인스턴스를 생성한다. 인스턴스가 있다면 존재하는 웹소켓 인스턴스를 사용한다.
        WebSocketKlgeSession session
                = (WebSocketKlgeSession) RtcChatClient.instance().createChatSession(this);
        session.connect(connection);

        return session;
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : RtcChatContext>
     */

    /**
     * RtcPeerChannel 인스턴스가 생성이 완료되고, RtcChatSession과 서로 바인딩이 완료되면 호출이 된다.
     * 최초로 수업에 입장하여 channel.build()가 성공하였을 때 호출되는 콜백
     * [RTC Thread]
     *
     * ex)
     * WebSocketChatSession webSession = (WebSocketChatSession) session;
     * webSession.update(RtcChatState.STATE_CONNECTING);
     * @param session 생성된 peer connectin이 소속되어 있는 세션
     * @param peer 생성된 peer connection 인스턴스
     */
    @Override
    public void onBuildPeerChannelComplete(final RtcChatSession session, final RtcPeerChannel peer) {
        Log.d(TAG, "<onBuildPeerChannelComplete / ZICO> start......");
        /*
         * 로컬 미디어 스트림이 만들어지지 않았다면 비동기적으로 생성하고 이미 존재한다면,
         * 존재하는 로컬미디어 스트림을 그대로 리턴한다.
         */
        RtcChatClient.instance().getOrCreateLocalMediaChannel(this,
                new RtcChatClient.LocalMediaChannelCallback() {
                    @Override
                    public void onCreateLocalMediaChannelComplete(RtcMediaChannel localMediaChannel) {
                        /*
                         * Note!
                         * 현재 생성된 피어커넥션에 로컬 미디어 스트림을 추가하는 과정.
                         */

//                        if (mRoomSpec.isVideoEnabled() || mRoomSpec.isVideoEnabled()) {
//                            Log.d(TAG, "<onCreateLocalMediaChannelComplete> 로컬미디어 스트림을 피어커넥션에 추가합니다.");
//                            peer.attachLocalMediaChannel(localMediaChannel);
//                        }
                        boolean myIsCaller = mPeerWatcher.getPeerNode(mRoomSpec.getUserNo()).getPeer().isCaller();
                        Log.d(TAG, "<onCreateLocalMediaChannelComplete / ZICO> myIsCaller : " + myIsCaller);
                        peer.setLocalMediaChannel(localMediaChannel);
                        if (myIsCaller) {
                            Log.d(TAG, "<onBuildPeerChannelComplete / ZICO> attachLocalMedia.. toUserId : " + ((KlgePeerChannel) peer).getToUserId());
                            peer.enableLocalMediaChannel(myIsCaller);
                            // 로컬미디어의 비디오, 오디오 트랙에 설정된 값 반영
                            localMediaChannel.enableVideoTrack(mRoomSpec.isVideoEnabled());
                            localMediaChannel.enableAudioTrack(mRoomSpec.isAudioEnabled());
                        } else {
                            Log.d(TAG, "<onBuildPeerChannelComplete / ZICO> detachLocalMedia.. toUserId : " + ((KlgePeerChannel) peer).getToUserId());
                            peer.enableLocalMediaChannel(myIsCaller);
                        }
                        //peer.enableLocalMediaChannel(mPeerWatcher.getMyPeerNode().getPeer().isCaller());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                KlgePeerChannel klge = (KlgePeerChannel) peer;
                                Log.d(TAG, "<onBuildPeerChannelComplete / ZICO> PeerChannel: " + klge.getToUserId()
                                        + " is binded into session: " + session.getSessionId()
                                        + " count: " + session.getPeerChannels().size());

                                boolean isCaller = peer.isOffer();

                                // Create offer. Offer SDP will be sent to answering client in
                                // PeerConnectionEvents.onLocalDescription event.
                                if (isCaller && !peer.hasLocalSdp()) {
                                    RtcChatClient.instance().createOfferSdp(peer);
                                }
                            }
                        });
                    }
                });
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<callbacks : RtcLifeCycleCallback>
     */

    @Override
    protected void onChannelOpen(RtcChatSession session) {
        super.onChannelOpen(session);
        Log.d(TAG, "<onChannelOpen / ZICO> WebSocket is successfully authorized.");

        Map<String,String> arguments = new HashMap<>();
        arguments.put("userno", mRoomSpec.getUserNo());
        arguments.put("roomid", mRoomSpec.getRoomId());
        arguments.put("video", mRoomSpec.isVideoEnabled() ? "enable" : "disable");
        arguments.put("audio", mRoomSpec.isAudioEnabled() ? "enable" : "disable");
        arguments.put("os_version", Build.VERSION.RELEASE);
        arguments.put("device", Build.MODEL);

        Log.d(TAG, "<onChannelOpen / ZICO> video : " + (mRoomSpec.isVideoEnabled() ? "enable" : "disable"));
        Log.d(TAG, "<onChannelOpen / ZICO> audio : " + (mRoomSpec.isAudioEnabled() ? "enable" : "disable"));
        Log.d(TAG, "<onChannelOpen / ZICO> videoCtrl : " + (mRoomSpec.isVideoControlEnable() ? 1 : 0));

        WebSocketKlgeSession ws = (WebSocketKlgeSession) session;
        ws.sendPeerStatus(arguments);
        int videoCtrl = mRoomSpec.isVideoControlEnable() ? 1 : 0;
        int soundOnly = mRoomSpec.isWhiteboardMode() ? 1 : 0;
    }


    @Override
    protected void onChannelClose(RtcChatSession session) {
        Log.d(TAG, "<onChannelClose / ZICO> ");
        /*
         * TODO 샘플 - RTC 기능
         */
        //stopAllChats();

    }

    /**
     * createOffer가 성공했을 때 호출되는 콜백이다.
     * @param session
     * @param peer
     * @param sdp
     */
    @Override
    protected void onLocalDescription(RtcChatSession session, RtcPeerChannel peer, SessionDescription sdp) {
        Log.d(TAG, "<onLocalDescription / ZICO> got Local SDP user. mServiceType : " + sdp.type);

        KlgePeerChannel channel = (KlgePeerChannel) peer;
        Map<String,String> properties = new HashMap<>();
        properties.put("from", mRoomSpec.getUserNo());
        properties.put("to", channel.getToUserId());

        if (sdp.type == SessionDescription.Type.OFFER)
            session.sendOfferSdp(peer, sdp, properties);
        else if (sdp.type == SessionDescription.Type.ANSWER)
            session.sendAnswerSdp(peer, sdp, properties);

        if (RtcChatArguments.getVideoMaxBitrate() > 0) {
            Logging.d(TAG, "onLocalDescription() Set video maximum bitrate: " + RtcChatArguments.getVideoMaxBitrate());
            RtcChatClient.instance().setVideoMaxBitrate(peer, RtcChatArguments.getVideoMaxBitrate());
        }
    }

    @Override
    protected void onRemoteDescription(RtcChatSession session, SessionDescription sdp, Map<String,String> properties) {
        String fromUserId = properties.get("from");
        Log.d(TAG, "<onRemoteDescription / ZICO> got Remote SDP form user: " + fromUserId + ", mServiceType : " + sdp.type);

        WebSocketKlgeSession klge = (WebSocketKlgeSession) session;
        KlgePeerChannel channel = klge.find(fromUserId);
        RtcChatClient.instance().setRemoteDescription(channel, sdp);

        // Create answer. Answer SDP will be sent to offering client in
        // PeerConnectionEvents.onLocalDescription event.
        if (channel == null) {
            Log.e(TAG, fromUserId + "'s PeerChannel not exist. This Remote description is invalid description.");
            return;
        }
        /*
        if (!channel.isOffer() && !channel.hasLocalSdp()) {
            RtcChatClient.instance().createAnswerSdp(channel);
        }
        */
        if (sdp.type == SessionDescription.Type.OFFER) {
            Log.d(TAG, "<onRemoteDescription / ZICO> createAnswer");
            channel.setIsOffer(false);
            RtcChatClient.instance().createAnswerSdp(channel);
        }

    }

    @Override
    protected void onRemoteIceCandidate(RtcChatSession session, IceCandidate candidate, Map<String,String> properties) {
        String fromUserId = properties.get("from");
        Log.d(TAG, "<onRemoteIceCandidate / ZICO> got remote ice candidate form user: " + fromUserId);

        WebSocketKlgeSession klge = (WebSocketKlgeSession) session;
        KlgePeerChannel channel = klge.find(fromUserId);
        RtcChatClient.instance().addRemoteIceCandidate(channel, candidate);
    }

    @Override
    protected void onIceCandidate(RtcChatSession session, RtcPeerChannel peer, IceCandidate candidate) {
        if (!peer.isError()) {
            KlgePeerChannel channel = (KlgePeerChannel) peer;
            Map<String,String> properties = new HashMap<>();
            properties.put("from", mRoomSpec.getUserNo());
            properties.put("to", channel.getToUserId());

            session.sendLocalIceCandidate(peer, candidate, properties);
        }
    }

    @Override
    protected void onIceConnected(RtcChatSession session, RtcPeerChannel peer) {
        super.onIceConnected(session, peer);
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        RoomUser remoteRoomUser = mRoomUserPresenter.getRoomUser(klge.getToUserId());
        String name = remoteRoomUser != null ? remoteRoomUser.getUserNm() : klge.getToUserId();
        Toast.makeText(getApplicationContext(), name + "님과 연결되었습니다. (ice-connected)", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onIceDisconnected(RtcChatSession session, RtcPeerChannel peer) {
        super.onIceDisconnected(session, peer);
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        RoomUser remoteRoomUser = mRoomUserPresenter.getRoomUser(klge.getToUserId());
        String name = remoteRoomUser != null ? remoteRoomUser.getUserNm() : klge.getToUserId();
        Toast.makeText(getApplicationContext(), name + "님과 연결이 불안정합니다. (ice-disconnected)", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onIceClosed(RtcChatSession session, RtcPeerChannel peer) {
        super.onIceClosed(session, peer);
        KlgePeerChannel klge = (KlgePeerChannel) peer;
        RoomUser remoteRoomUser = mRoomUserPresenter.getRoomUser(klge.getToUserId());
        String name = remoteRoomUser != null ? remoteRoomUser.getUserNm() : klge.getToUserId();
        Toast.makeText(getApplicationContext(), name + "님과 연결이 끊겼습니다. (ice-closed)", Toast.LENGTH_SHORT).show();
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<overrides>
     */

    @Override
    protected void onFailAuthMessage(WebSocketKlgeSession session, JsonObject response) {
        super.onFailAuthMessage(session, response);
        Log.d(TAG, "<onFailAuthMessage / ZICO> response : " + response.toString());
    }

    @Override
    protected void onPeerStatus(JsonObject response) {
        super.onPeerStatus(response);
        Log.d(TAG, "<onPeerStatus / ZICO> response " + response.toString());
        Log.d(TAG, "<onPeerStatus / ZICO> videoCtrl : " + mRoomSpec.isVideoControlEnable() + ", soundOnly : " + mRoomSpec.isWhiteboardMode());

    }

    @Override
    protected void onReconnectSession() {
        super.onReconnectSession();
        Log.d(TAG, "<onReconnectSession>");
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : HasComponent<RoomActivityComponent>>
     */

    @Override
    public RoomActivityComponent getComponent() {
        if (mComponent == null) {
            mComponent = DaggerRoomActivityComponent.builder()
                    .appComponent(((KnowloungeApplication) getApplicationContext()).getAppComponent())
                    .activityModule(new ActivityModule(this))
                    .roomSpecModule(new RoomSpecModule(mRoomSpec))  // 컴포넌트에 RoomSpec 객체 주입
                    .roomUsersModule(new RoomUsersModule(mRoomUsers))
                    .build();
        }
        return mComponent;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() fired..!");
        super.onCreate(savedInstanceState);

        mRoomUsers = new RoomUsers.Builder().build();
        Intent intent = getIntent();
        if (intent.hasExtra("arguments")) {
            mRoomSpec = intent.getParcelableExtra("arguments");
            if (mRoomSpec != null)
                Log.d(TAG, "<onCreate> mRoomSpec roomId : " + mRoomSpec.getRoomId());
        } else {
            Toast.makeText(this, "Can not load Turn server information..", Toast.LENGTH_SHORT).show();
            finish();
        }

        getComponent().inject(this);
        //mRoomUserPresenter.initialize(getComponent());

        prefManager = WenotePreferenceManager.getInstance(this);

        prefManager.setAppRunningFlag(true);

        // 레이아웃 설정
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);

        EventBus.get().getBustObservable().subscribe(mSubscriber);

        pollData = new PollCreateData();

        //bookLoading = (BookLoading) findViewById(R.id.book_loading);

        // InApp 처리 관련 위임
        mDispatcher = new InAppRootDispatcher(this);
        mDispatcher.setListener(inAppListener);
        mDispatcher.onCreate();

        //prefManager = WenotePreferenceManager.getInstance(this);
        pref = SharedPreferencesManager.getInstance(this);

        // 스크린의 Orientation 설정
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE :
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_PORTRAIT :
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
        }

        // Status바 색상 변경 (롤리팝부터 가능)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.app_canvas_color));
        }

        //bookLoading.start();
        Glide.with(this).load(R.drawable.loading_gif).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView)findViewById(R.id.room_loading_img));
        ((Button)findViewById(R.id.cancel_room_loading)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //채팅 유저리스트에 default 넣기
        addDefaultChatUserItem();

        roomTools = new RoomTools(this);

//        mDrawerLayout = (DrawerLayout)findViewById(R.id.right_drawer_room);
//        mDrawerContent = (LinearLayout)findViewById(R.id.right_drawer_layout);
//        mDrawerRightPanel = (FrameLayout)findViewById(R.id.right_drawer_panel_room);
//        mRoomContent = (FrameLayout)findViewById(R.id.main_layout);

        ((LinearLayout)findViewById(R.id.canvas_container)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSelectorMode) {
                    Log.d(TAG, "Touch disable");
                    return true;
                } else {
                    Log.d(TAG, "Touch enabled");
                    return false;
                }
            }
        });

        mLeftDrawerContent = (LinearLayout)findViewById(R.id.left_drawer_layout);

        rightMenu =  (ScrollView)findViewById(R.id.right_menu_area_scroll);

        final ViewGroup rootView = (ViewGroup) findViewById(R.id.main_layout);

        findViewById(R.id.deemed_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
                    closeLeftNavDrawer();
                }
            }
        });

        // DrawerLayout 이벤트 리스너 정의
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                isOpenRightMenu = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isOpenRightMenu = false;
                currentParent = -1;
                currentCommunityChild = -1;
                currentUserChild = -1;

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(rootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                findViewById(R.id.deemed_layer).clearAnimation();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mDrawerLayout.requestDisallowInterceptTouchEvent(true);
        mDrawerLayout.closeDrawer(Gravity.RIGHT);

        String roomUrl = intent.getStringExtra("roomurl");   // 룸 코드 가져오기
        guestNm = intent.hasExtra("guest") ? intent.getStringExtra("guest") : "";     // 게스트명
        userNm = !TextUtils.isEmpty(guestNm) ? guestNm : "";
        deviceIdForGuest = intent.hasExtra("deviceid") ? intent.getStringExtra("deviceid") : "";
        mServiceType = intent.hasExtra("mServiceType") ? intent.getStringExtra("mServiceType") : "knowlounge";

        // 로딩화면 내 메세지 분기 처리
        int actionMode = intent.getIntExtra("mode", GlobalConst.ENTER_ROOM_MODE);
        switch (actionMode) {
            case GlobalConst.CREATE_ROOM_MODE :
                Glide.with(this).load(R.drawable.gif_create_class).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView) findViewById(R.id.room_loading_img));
                ((TextView)findViewById(R.id.txt_canvas_loading)).setText(getResources().getString(R.string.splash_create));
                break;
            case GlobalConst.ENTER_ROOM_MODE :
                Glide.with(this).load(R.drawable.gif_create_class).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView) findViewById(R.id.room_loading_img));
                ((TextView)findViewById(R.id.txt_canvas_loading)).setText(getResources().getString(R.string.splash_load));
                break;
            case GlobalConst.MOVE_ROOM_MODE :
                Glide.with(this).load(R.drawable.gif_multi_board).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView) findViewById(R.id.room_loading_img));
                ((Button)findViewById(R.id.cancel_room_loading)).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.txt_canvas_loading)).setText(getResources().getString(R.string.splash_switch));
                break;
        }

        Map<String, Object> queryStrMap = CommonUtils.convertQueryStringToMap(roomUrl);
        roomCode = queryStrMap != null ? (String) queryStrMap.get("code") : "";
        Log.d(TAG, "roomUrl : " + roomUrl + ", roomCode : " + roomCode);

        //evtListener = FreeboardEventListener.getInstance();

        density = prefManager.getDensity();

        // context와 activity 저장
        context = getApplicationContext();
        activity = this;

        // 캠 화상 Fragment 세팅

        // 폰일 때 영상 컨테이너 길이 조절
//        FrameLayout videoContainer = (FrameLayout) findViewById(R.id.user_video_container);
//        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
//            lp.setMargins(0, 0, 0, 0);
//            videoContainer.setLayoutParams(lp);
//        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.user_video_container, MultiVideoChatFragment.newInstance(mRoomSpec), MultiVideoChatFragment.class.getSimpleName())
                .commit();

        // 우측 슬라이드 Fragment 세팅
        RoomRightSlideFragment roomRightSlideFragment = new RoomRightSlideFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.right_drawer_panel_room, roomRightSlideFragment, "RoomRightSlideFragment").commit();  // 슬라이딩 메뉴

        // 웹뷰 Fragment 세팅
        mWebViewFragment = new WebViewFragment();
        Bundle paramBundle = new Bundle();
        paramBundle.putString("roomurl", roomUrl);
        mWebViewFragment.setArguments(paramBundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.webview_container, mWebViewFragment, "WebViewFragment").commit();  // 웹뷰

        RoomLeftmenuFragment roomLeftMenuFragment = new RoomLeftmenuFragment();
        Bundle leftNavParams = new Bundle();
//        leftNavParams.putString("roomcode", roomCode);
//        leftNavParams.putString("roomtitle", roomTitle);
//        roomLeftMenuFragment.setArguments(leftNavParams);
        getSupportFragmentManager().beginTransaction().replace(R.id.left_drawer_panel_room, roomLeftMenuFragment, "RoomLeftmenuFragment").commit();  // 좌측 슬라이딩 메뉴


        // 멀티페이지 Fragment 세팅
        MultiPageFragment multiPageFragment = new MultiPageFragment();
        getSupportFragmentManager().beginTransaction().replace(KnowloungeApplication.isPhone ? R.id.multi_page_container_phone : R.id.multi_page_container, multiPageFragment, "MultiPageFragment").commit();  // 슬라이딩 메뉴

        //mToolbar = (Toolbar) findViewById(R.id.room_toolbar);
        setSupportActionBar(mToolbar);

        if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {   // 테블릿일 때..
            isDevicePhone = false;
            mCustomActionBar = getLayoutInflater().inflate(R.layout.actionbar_room_custom, null, false);
            actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(true);


            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

            actionBar.setCustomView(mCustomActionBar, params);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // Actionbar home button 설정
            actionBar.setDefaultDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);  // Actionbar title 설정
            actionBar.setDisplayUseLogoEnabled(false);  // Actionbar logo 설정

            Toolbar parent = (Toolbar) mCustomActionBar.getParent();
            parent.setContentInsetsAbsolute(0, 0);
            parent.setPadding(0, 0, 0, 0);

//            btnHeaderAuthority = (ImageView) findViewById(R.id.btn_header_authority);

            //actionBar.hide();

            final ImageButton toggleBtn = (ImageButton) mCustomActionBar.findViewById(R.id.btn_left_nav_toggle);
            toggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    mDrawerLayout.openDrawer(mLeftDrawerContent);
                    findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);
                }
            });

            /*
            btnHeaderAuthority.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (guestFlag) {  // 게스트일 때는 사용할 수 없다는 토스트 메세지 띄우기..
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        String authType = authInfo.has("authtype") ? authInfo.getString("authtype") : "0";
                        if (creatorFlag) {
                            if("0".equals(authType)) {
                                setAlertDialog("8", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_auth_deny) + "\n" + getString(R.string.allow_cohosting));
                            } else {
                                if (!masterFlag) {
                                    setAlertDialog("5", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_authority_take) + "|get_authority|" + userId);
                                }
                            }
                        } else {
                            if (!masterFlag) {
                                if ("0".equals(authType)) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.canvas_auth_deny), Toast.LENGTH_SHORT).show();
                                } else {
                                    setAlertDialog("5", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_authority_take) + "|get_authority|" + userId);
                                }
                            }
                        }
                    } catch (JSONException e) {

                    }
                }
            });
            */

            ((TextView) mCustomActionBar.findViewById(R.id.bar_room_code_txt)).setText(roomCode);
            mCustomActionBar.findViewById(R.id.room_code_layer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getSupportFragmentManager();
                    RoomInfoDialogFragment dialogFragment = new RoomInfoDialogFragment();
                    dialogFragment.show(fm, "test");
                }
            });

            mCustomActionBar.findViewById(R.id.btn_header_video).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (TextUtils.equals(authInfo.getString("vcamopt"), "1") && (!creatorFlag && !TextUtils.equals(userType, "2"))) {
                            Toast.makeText(getContext(), getString(R.string.toast_authority_no), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (JSONException e) {

                    }
                    View videoView = getWindow().findViewById(R.id.user_video_container);
                    if (videoView.isShown()) {
                        ((ImageView)mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video);
                        videoView.setVisibility(View.GONE);

                    } else {
                        ((ImageView)mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video_on);
                        videoView.setVisibility(View.VISIBLE);

                    }

                    closeSubMenuLayer();
                }
            });


            // 헤더의 유저리스트 버튼 이벤트
            mCustomActionBar.findViewById(R.id.btn_header_userlist).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (notificationLayout.isShown()) {
                        isNotificationShown = false;
                        Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.scale_fade_out_phone : R.anim.scale_fade_out);
                        notificationLayout.startAnimation(animation);
                        notificationLayout.setVisibility(View.GONE);
                    }
                    if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                        mDrawerLayout.closeDrawer(Gravity.RIGHT);
                        isOpenRightMenu = false;
                    } else {
                        isOpenRightMenu = true;
                        mDrawerLayout.openDrawer(Gravity.RIGHT);
                        mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW, true);
                        closeSubMenuLayer();
                    }
                }
            });

            mCustomActionBar.findViewById(R.id.btn_header_setting).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRoomSetting();
                }
            });

            mCustomActionBar.findViewById(R.id.btn_header_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAlertDialog("0", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_exit_body1));
                }
            });

            mCustomActionBar.findViewById(R.id.btn_header_multipage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roomTools.onMultiPage();
                }
            });

            // 질문/답변 메뉴가 헤더로 이동함 - 2917.04.18
            mCustomActionBar.findViewById(R.id.btn_header_poll).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roomTools.onOpenPollTools();
                }
            });

        } else {   // 폰일 때..
            isDevicePhone = true;
            rePlaceSubMenu();
            mCustomActionBar = getLayoutInflater().inflate(R.layout.actionbar_room_custom_phone, null, false);

            actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(true);

            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(mCustomActionBar, params);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // Actionbar home button 설정
            actionBar.setDefaultDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);  // Actionbar title 설정
            actionBar.setDisplayUseLogoEnabled(false);  // Actionbar logo 설정

            Toolbar parent = (Toolbar) mCustomActionBar.getParent();
            parent.setContentInsetsAbsolute(0, 0);
            parent.setPadding(0, 0, 0, 0);

//            btnHeaderAuthority = (ImageView) findViewById(R.id.btn_header_authority);

            final ImageButton toggleBtn = (ImageButton)mCustomActionBar.findViewById(R.id.btn_left_nav_toggle);
            toggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(toggleKeyClicked)
                        return;

                    mDrawerLayout.openDrawer(mLeftDrawerContent);
                    findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);

                    toggleKeyClicked = !toggleKeyClicked;
                    toggleBtn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toggleKeyClicked = !toggleKeyClicked;
                        }
                    },500);
                }
            });

            // 폰에서의 권한 가져오는 마이크 버튼 이벤트 정의..
            /*
            btnHeaderAuthority.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (guestFlag) {  // 게스트일 때는 사용할 수 없다는 토스트 메세지 띄우기..
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        if (guestFlag) {  // 게스트일 때는 사용할 수 없다는 토스트 메세지 띄우기..
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String authType = authInfo.has("authtype") ? authInfo.getString("authtype") : "0";
                        if (creatorFlag) {
                            if("0".equals(authType)) {
                                setAlertDialog("8", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_auth_deny) + "\n" + getString(R.string.allow_cohosting));
                            } else {
                                if (!masterFlag) {
                                    setAlertDialog("5", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_authority_take) + "|get_authority|" + userId);
                                }
                            }
                        } else {
                            if (!masterFlag) {
                                if ("0".equals(authType)) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.canvas_auth_deny), Toast.LENGTH_SHORT).show();
                                } else {
                                    setAlertDialog("5", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_authority_take) + "|get_authority|" + userId);
                                }
                            }
                        }
                    } catch (JSONException e) {

                    }
                }
            });
            */

            ((TextView) mCustomActionBar.findViewById(R.id.bar_room_code_txt)).setText(roomCode);
            mCustomActionBar.findViewById(R.id.room_code_layer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getSupportFragmentManager();
                    RoomInfoDialogFragment dialogFragment = new RoomInfoDialogFragment();
                    dialogFragment.show(fm, "RoomInfoDialogFragment");
                }
            });

            mCustomActionBar.findViewById(R.id.btn_header_video).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View videoView = getWindow().findViewById(R.id.user_video_container);
                    if (videoView.isShown()) {
                        ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video);
                        videoView.setVisibility(View.GONE);
//                    videoCallback.setVideoStreamOnOff(false);
                    } else {
                        ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video_on);

                        videoView.setVisibility(View.VISIBLE);
//                    videoCallback.setVideoStreamOnOff(true);
                    }
                    closeSubMenuLayer();
                }
            });

            mCustomActionBar.findViewById(R.id.btn_phone_communication_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (notificationLayout.isShown()) {
                        isNotificationShown = false;
                        Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.scale_fade_out_phone : R.anim.scale_fade_out);
                        notificationLayout.startAnimation(animation);
                        notificationLayout.setVisibility(View.GONE);
                    }
                    closeSubMenuLayer();

                    roomTools.onOpenUserList();

//                    phoneRightMenuGone();
//                    if(currentMenu == GlobalConst.MENU_COMMUNICATION){
//                        currentMenu = -1;
////                        rightMenu.setVisibility(View.GONE);
//                    }else {
//                        currentMenu = GlobalConst.MENU_COMMUNICATION;
//                        findViewById(R.id.right_menu_communication).setVisibility(View.VISIBLE);
//                        rightMenu.setVisibility(View.VISIBLE);
//                        clearRightBtnSelected();
//
//                        View videoView = getWindow().findViewById(R.id.user_video_container);
//                        if (videoView.isShown()) {
//                            ((ImageView)findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam_on);
//                        } else {
//                            ((ImageView)findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam);
//                        }
//
//                        ((ImageView)mCustomActionBar.findViewById(R.id.btn_phone_communication_menu)).setImageResource(R.drawable.btn_header_userlist_on);
//                    }
                }
            });
        }



        // 우측 버튼에 이벤트 정의
        setButtonEvent(prefManager.getDeviceType());

        registBroadcastReceiver();

        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.setOnChangeNetworkStatusListener(networkStateListener);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        NotificationApprove notificationApprove = new NotificationApprove();
        getSupportFragmentManager().beginTransaction().replace(R.id.notification_container, notificationApprove, "Notification").commit();

        notificationLayout = (FrameLayout) findViewById(R.id.notification_container);

        multiPageSlidingLayout = KnowloungeApplication.isPhone ? mMultiPageLayoutPhone : mMultiPageLayout;

        if(KnowloungeApplication.isPhone) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) notificationLayout.getLayoutParams();
            params.topMargin = AndroidUtils.getPxFromDp(this, 44);
            params.rightMargin = AndroidUtils.getPxFromDp(this, 5);
            params.leftMargin = AndroidUtils.getPxFromDp(this, 5);
            notificationLayout.setLayoutParams(params);
        }


        // 서비스 가이드 말풍선 노출을 제어하는 부분이다.
        if (checkServiceGuideShown()) {
            if (KnowloungeApplication.isPhone) {
                if (classMasterUserNo.equals(userNo)) {
                    ((TextView) findViewById(R.id.service_guide_message3)).setVisibility(View.GONE);
                }
                ((LinearLayout) findViewById(R.id.service_guide_message1)).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.service_guide_message2)).setVisibility(View.GONE);
            } else {
                if (classMasterUserNo.equals(userNo)) {
                    ((TextView) findViewById(R.id.service_guide_message3)).setVisibility(View.GONE);
                }
                ((TextView) findViewById(R.id.service_guide_message4)).setVisibility(View.GONE);
            }

            serviceGuideLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    serviceGuideLayout.setVisibility(View.GONE);
                }
            });
        } else {
            serviceGuideLayout.setVisibility(View.GONE);
        }

        //notificationLayout.setClickable(true);
        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle argument = new Bundle();
                argument.putString("mServiceType", "room");
                argument.putString("roomid", prefManager.getCurrentRoomId());
                argument.putString("code", RoomActivity.activity.getRoomCode());
                argument.putString("masterno", prefManager.getCurrentTeacherUserNo());
                ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                dialogFragment.setArguments(argument);
                dialogFragment.show(getSupportFragmentManager(), "ExtendReqDialogFragment");

                isNotificationShown = false;
                Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.scale_fade_out_phone : R.anim.scale_fade_out);
                notificationLayout.startAnimation(animation);
                notificationLayout.setVisibility(View.GONE);
            }
        });

        // 판서형 질문의 영역 선택 캡쳐 취소 버튼 이벤트..
        findViewById(R.id.btn_cancel_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCapture(drawingContainerMode);
            }
        });

        // 판서형 질문의 영역 선택 캡쳐 확정 버튼 이벤트..
        findViewById(R.id.btn_confirm_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "[btn_confirm_capture clicked..] 선택한 영역을 캡쳐합니다.");
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("PollCtrl.UI.captureCanvas('" + drawingContainerMode + "', " + sx + ", " + sy + ", " + swidth + ", " + sheight + ", " + KnowloungeApplication.density + ");");
                invokeAreaSelector(drawingContainerMode);
                clearSelectorButton();

            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(getClass().getSimpleName(), "onStart fired..!");
        super.onStart();

        GoogleAnalyticsService.get().sendAnalyticsEvent(getClass().getSimpleName(), "Canvas");
        GoogleAnalyticsService.get().sendAnalyticsScreen("Canvas");
    }


//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        Log.d(TAG, "onSaveInstanceState");
//        super.onSaveInstanceState(outState);
//        outState.putBoolean("leave_class", false);
//    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        Bundle savedState = intent.getExtras();
        if(savedState != null) {
            Log.d(TAG, "leave_class : " + savedState.getBoolean("leave_class"));
        }
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <@OnClick Evnets>
     */

    /*
     *----------------------------------------------------------------------------------------------
     *-- <Click Events>
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_fold)
    void OnClickBtnRightMenuFold() {
        closeSubMenuLayer();
        if (isRightMenuFold) {
            ((LinearLayout) rightMenu.findViewById(R.id.rightmenu_area)).setVisibility(View.VISIBLE);
            mBtnMenuFold.setImageResource(R.drawable.btn_rightmenu_fold_on);
            isRightMenuFold = false;
        } else {
            ((LinearLayout) rightMenu.findViewById(R.id.rightmenu_area)).setVisibility(View.GONE);
            mBtnMenuFold.setImageResource(R.drawable.btn_rightmenu_fold);
            isRightMenuFold = true;
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_fold_phone)
    void OnClickBtnRightMenuFoldPhone() {
        closeSubMenuLayer();
        if (isRightMenuFold) {
            ((LinearLayout) rightMenu.findViewById(R.id.phone_rightmenu_area)).setVisibility(View.VISIBLE);
            mBtnMenuFoldPhone.setImageResource(R.drawable.btn_rightmenu_fold_on);
            isRightMenuFold = false;
        } else {
            ((LinearLayout) rightMenu.findViewById(R.id.phone_rightmenu_area)).setVisibility(View.GONE);
            mBtnMenuFoldPhone.setImageResource(R.drawable.btn_rightmenu_fold);
            isRightMenuFold = true;
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_hand)
    void OnClickBtnHand() {
        // 테블릿의 손 모드 버튼 클릭시..
        roomTools.onHandMode("toggle");
        mBtnMenuHand.setImageResource(R.drawable.btn_rightmenu_hand_on);
        currentMode = GlobalConst.MODE_HAND;
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_hand_phone)
    void OnClickBtnRightMenuHandPhone() {
        // 폰의 손 모드 버튼 클릭시..
        roomTools.onHandMode("toggle");
        mBtnMenuHandPhone.setImageResource(R.drawable.btn_rightmenu_hand_on);
        currentMode = GlobalConst.MODE_HAND;
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_pen)
    void OnClickBtnRightMenuPen() {
        // 테블릿의 펜 버튼 클릭시..
        roomTools.onPenTool();
        currentMode = GlobalConst.MODE_PEN;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_pen_phone)
    void OnClickBtnRightMenuPenPhone() {
        // 폰의 펜 버튼 클릭시..
        roomTools.onPenTool();
        currentMode = GlobalConst.MODE_PEN;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_pen_landscape)
    void OnClickBtnRightMenuPenLand() {
        // 폰 가로모드의 펜 버튼 클릭시..
        roomTools.onPenTool();
        currentMode = GlobalConst.MODE_PEN;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_eraser)
    void OnClickBtnRightMenuEraser() {
        // 테블릿의 지우개 버튼 클릭시..
        roomTools.onEraserTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_eraser_phone)
    void OnClickBtnRightMenuEraserPhone() {
        // 폰의 지우개 버튼 클릭시..
        roomTools.onEraserTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_eraser_landscape)
    void OnClickBtnRightMenuEraserLand() {
        // 폰 가로모드의 지우개 버튼 클릭시..
        roomTools.onEraserTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_shape)
    void OnClickBtnShape() {
        // 테블릿의 도형 버튼 클릭시..
        roomTools.onShapeTool();
    }

    // 폰에는 도형 버튼이 없다.
//    @SuppressWarnings("unused")
//    @OnClick(R.id.btn_rightmenu_shape_phone)
//    void OnClickBtnShapePhone() {
//        // 폰의 도형 버튼 클릭시..
//        roomTools.onShapeTool();
//        currentMode = GlobalConst.MODE_SHAPE;
//    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_pointer)
    void OnClickBtnRightMenuLaser() {
        // 테블릿의 레이저포인터 버튼 클릭시..
        roomTools.onLaserTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_laser_phone)
    void OnClickBtnRightMenuLaserPhone() {
        // 폰의 레이저포인터 버튼 클릭시..
        roomTools.onLaserTool();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_text)
    void OnClickBtnText() {
        // 테블릿의 텍스트 버튼 클릭시..
        closeSubMenuLayer();
        roomTools.onTextTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_text_phone)
    void OnClickBtnRightMenuTextPhone() {
        // 폰의 텍스트 버튼 클릭시..
        roomTools.onOpenTextTool();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_document)
    void OnClickBtnDocument() {
        // 테블릿의 도큐먼트 버튼 클릭시..
        roomTools.onOpenDocumentTools();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_document_phone)
    void OnClickBtnDocumentPhone() {
        // 폰의 도큐먼트 버튼 클릭시..
        roomTools.onOpenDocumentTools();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_document_landscape)
    void OnClickBtnDocumentLand() {
        // 폰 가로모드의 도큐먼트 버튼 클릭시..
        roomTools.onOpenDocumentTools();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_memo)
    void onClickBtnMemo() {
        // 테블릿의 메모 버튼 클릭시..
        roomTools.onMemoTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_redo)
    void onClickBtnRedo() {
        // 테블릿의 REDO 버튼 클릭시..
        roomTools.onRedoTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_undo)
    void onClickBtnUndo() {
        // 테블릿의 UNDO 버튼 클릭시..
        roomTools.onUndoTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_trash)
    void onClickBtnTrash() {
        // 테블릿의 휴지통 버튼 클릭시..
        clearRightBtnSelected();
        roomTools.onDeleteAll();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_multipage_phone)
    void OnClickBtnRightMenuMultipagePhone() {
        // 폰의 멀티페이지 버튼 클릭시..
        roomTools.onMultiPage();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_multipage_landscape)
    void OnClickBtnRightMenuMultipageLand() {
        // 폰 가로모드의 멀티페이지 버튼 클릭시..
        roomTools.onMultiPage();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_poll_phone)
    void OnClickBtnRightMenuPollPhone() {
        // 폰의 설문조사 버튼 클릭시..
        roomTools.onOpenPollTools();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_poll_landscape)
    void OnClickBtnRightMenuPollLand() {
        // 폰 가로모드의 설문조사 버튼 클릭시..
        roomTools.onOpenPollTools();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_undo_phone)
    void OnClickBtnRightMenuUndoPhone() {
        // 되돌리기 버튼을 눌렀을 때..
        roomTools.onUndoTool();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_trash_phone)
    void OnClickBtnRightMenuTrashPhone() {
        // 휴지통 버튼을 눌렀을 때..
        setAlertDialog("1", getResources().getString(R.string.global_delete), getResources().getString(R.string.canvas_delete_all));
        closeSubMenuLayer();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_chat)
    void OnClickBtnChat() {
        // 테블릿의 채팅 버튼 클릭시..
        roomTools.onChatTool();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_rightmenu_comment)
    void OnClickBtnComment() {
        // 테블릿의 코멘트 버튼 클릭시..
        roomTools.onCommentTool();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_cancel_capture)
    void OnClickCaptureCancel() {
        // 판서형 질문의 영역 선택 캡쳐 취소 버튼 이벤트..
        cancelCapture(drawingContainerMode);
    }


    // 판서형 질문의 영역 선택 캡쳐 확정 버튼 이벤트..
    @SuppressWarnings("unused")
    @OnClick(R.id.btn_confirm_capture)
    void OnClickCaptureConfirm() {
        Log.d(TAG, "[btn_confirm_capture clicked..] 선택한 영역을 캡쳐합니다.");
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("PollCtrl.UI.captureCanvas('" + drawingContainerMode + "', " + sx + ", " + sy + ", " + swidth + ", " + sheight + ", " + KnowloungeApplication.density + ");");
        invokeAreaSelector(drawingContainerMode);
        clearSelectorButton();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.video_user_invite_btn)
    void OnClickVideoInviteBtn() {
        if (mRoomSpec.isParentMaster()) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                isOpenRightMenu = false;
            } else {
                isOpenRightMenu = true;
                mDrawerLayout.openDrawer(Gravity.RIGHT);
                mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW, true);
                closeSubMenuLayer();
            }
            return;
        } else {
            if (mRoomSpec.isVideoEnabled()) {
                Toast.makeText(RoomActivity.activity.getApplicationContext(), "이미 영상을 이용중입니다_다국어 필요", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String masterUserNo = isVideoSeparate ? classMasterUserNo : creatorNo;
        mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.videoNoti('request', '" + userNo + "', '" + masterUserNo + "');");
        Toast.makeText(RoomActivity.activity.getApplicationContext(), "영상 초대 신청이 발송되었습니다_다국어 필요", Toast.LENGTH_SHORT).show();
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.video_separate_btn)
    void OnClickVideoSeparateBtn() {
        int specialCharPosition = roomId.indexOf("_");
        String parentRoomId = specialCharPosition < 0 ? roomId : roomId.substring(0, specialCharPosition);
        final String roomIdParam = isVideoSeparate ? parentRoomId : roomId;
        final CordovaWebView webView = mWebViewFragment.getCordovaWebView();

        isVideoSeparate = !isVideoSeparate;
        final int separateFlag = isVideoSeparate ? 1 : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this, R.style.AlertDialogCustom);
        String msg = isVideoSeparate ? getString(R.string.cam_split_alert) : getString(R.string.cam_return_alert);
        builder.setMessage(msg).setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "<OnClickVideoSeparateBtn / ZICO> roomId : " + roomIdParam);
                        webView.sendJavascript("PacketMgr.Master.BroadCast.videoGroup('all', '" + roomId + "', " + separateFlag + ");");
                        mVideoEvents.onVideoGroup(roomIdParam, isVideoSeparate);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isVideoSeparate = !isVideoSeparate;
                        dialog.dismiss();
                    }
                });
        builder.show();
    }


    public void closeLeftNavDrawer() {
        Animation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.deemed_layer).setVisibility(View.GONE);
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.deemed_layer).startAnimation(fadeOut);


        if(mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.closeDrawer(mLeftDrawerContent);
        }
    }


    private void showNotification() {
        Log.d(TAG, "showNotification");
        if (!isNotificationShown) {
            isNotificationShown = true;
            Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.scale_fade_in_phone : R.anim.scale_fade_in);
            notificationLayout.startAnimation(animation);
            notificationLayout.setVisibility(View.VISIBLE);

            /*
            notificationLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isNotificationShown = false;
                    Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.scale_fade_out_phone : R.anim.scale_fade_out);
                    notificationLayout.startAnimation(animation);
                    notificationLayout.setVisibility(View.GONE);
                }
            },3000);

            */
        }
    }

    /**
     * 서비스 가이드 말풍선 노출 여부를 체크한다.
     * @return
     */
    private boolean checkServiceGuideShown() {
        if (pref.getLastDay() == 0 ) {
            pref.setLastDay(System.currentTimeMillis());
            return true;
        } else {
            if (AndroidUtils.getDistintionTime(pref.getStartDay(), System.currentTimeMillis()) < 7 ) {
                if (AndroidUtils.getDistintionTime(pref.getLastDay() , System.currentTimeMillis()) == 1) {
                    pref.setLastDay(System.currentTimeMillis());
                    return true;
                }
            }
        }
        return false;
//        return true;   // 디버그 용
    }


    /**
     * Progress 형태의 알림 UI을 생성하는 메서드이다.
     * @param title
     * @param msg
     * @param roomCode
     * @param isProgress
     */
    private void openProgressLayout(final String title, final String msg, @Nullable final String roomCode, final boolean isProgress) {
        final FrameLayout container = (FrameLayout) findViewById(R.id.main_layout);
        progressView = getLayoutInflater().inflate(R.layout.layout_progress, container, false);

        final ProgressBar progressBar = (ProgressBar)progressView.findViewById(R.id.progress_bar);
        final TextView titleText = (TextView) progressView.findViewById(R.id.txt_progress_bar_content);
        final TextView contentText = (TextView) progressView.findViewById(R.id.txt_progress_bar_sub_content);

        progressBar.setScaleY(1.8f);

        // 폰 or 테블릿 UI 분기처리..
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) progressView.getLayoutParams();
        if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
            if (prefManager.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                lp.width = (int) (300 * prefManager.getDensity());
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            progressView.setLayoutParams(lp);
        } else {
            lp.width = (int) (300 * prefManager.getDensity());
            progressView.setLayoutParams(lp);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RoomLeftNavActivity.mDrawerLayout.closeDrawer(RoomLeftNavActivity.mDrawerContent);
                //Toast.makeText(RoomActivity.activity.getApplicationContext(), getString(R.string.toast_import_start), Toast.LENGTH_SHORT).show();
                titleText.setText(title);
                contentText.setText(msg);

                Animation slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                ((FrameLayout) findViewById(R.id.main_layout)).addView(progressView, 6);
                progressView.startAnimation(slideUpAnimation);

                if(!isProgress) {
                    TextView btnProgresOk = (TextView)progressView.findViewById(R.id.btn_progress_ok);
                    btnProgresOk.setVisibility(View.VISIBLE);
                    btnProgresOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            progressView.startAnimation(slideDownAnimation);
                            progressView.setVisibility(View.GONE);
                            ((FrameLayout) findViewById(R.id.main_layout)).removeView(progressView);
                            moveRoom(roomCode, null);
                        }
                    });

                    Animation slide_left = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_to_right_duration8s);
                    progressBar.startAnimation(slide_left);

                    progressView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            progressView.startAnimation(slide_down);
                            progressView.setVisibility(View.GONE);
                            ((FrameLayout) findViewById(R.id.main_layout)).removeView(progressView);
                        }
                    }, 12000);
                }

            }
        });
    }


    /**
     * 캔버스 화면을 이미지 파일로 저장하는 메서드이다.
     * @param downloadUrl
     */
    public void saveCanvasScreen(final String downloadUrl) {

        URL canvasImgUrl;
        URLConnection urlConnection;

        final String DCIM_PATH = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/knowlounge";
        //final String DCIM_PATH = Environment.getExternalStorageDirectory() + "/knowlounge";
        File imgFileDir = new File(DCIM_PATH);
        if(!imgFileDir.exists())
            imgFileDir.mkdir();
        final ProgressBar progressBar = (ProgressBar)progressView.findViewById(R.id.progress_bar);

        final TextView textViewProgressContent = (TextView)progressView.findViewById(R.id.txt_progress_bar_content);
        String roomTitleStr = roomTitle.replaceAll("[?]", "");
        try {
            canvasImgUrl = new URL(downloadUrl);
            urlConnection = (URLConnection) canvasImgUrl.openConnection();
            int totalBytes = urlConnection.getContentLength();
            urlConnection.connect();

            //roomTitle = roomTitle.replaceAll("%", "");

            Log.d(TAG, roomTitleStr + ".png 다운로드 중..");

            SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMdd-HHmmss", Locale.getDefault());
            Date currentTime = new Date ();
            String dTime = formatter.format (currentTime);

            File canvasFile = new File(imgFileDir, roomTitleStr + "_" + dTime + ".png");
//            if(canvasFile.exists()) {
//                String fileNm = canvasFile.getName();
//                int fileNo = (roomTitleStr.indexOf("(") == -1) ? 0
//                        : Integer.parseInt(fileNm.substring(roomTitleStr.indexOf("("), roomTitleStr.indexOf(")")));
//                fileNo += 1;
//                String newFileNm = roomTitleStr + " (" + fileNo + ")";
//                canvasFile = new File(imgFileDir, newFileNm + ".png");
//            }
            canvasFile.createNewFile();

            FileOutputStream fileOutput = new FileOutputStream(canvasFile);
            InputStream inputStream = urlConnection.getInputStream();

            int bufferLength = 0;
            int accumulateBuffer = 0;

            byte[] buffer = new byte[512];
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                accumulateBuffer += bufferLength;
                final int progress = (int)(((double) accumulateBuffer / (double) totalBytes) * 100.0);
                Log.d(TAG, accumulateBuffer + " / " + totalBytes + " = " + (int) progress + "%");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                        if(progress >= 100) {
                            textViewProgressContent.setText(getString(R.string.toast_import));
                            TextView btnProgresOk = (TextView)progressView.findViewById(R.id.btn_progress_ok);
                            btnProgresOk.setVisibility(View.VISIBLE);
                            btnProgresOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                                    progressView.startAnimation(slideDownAnimation);
                                    progressView.setVisibility(View.GONE);
                                    ((FrameLayout) findViewById(R.id.main_layout)).removeView(progressView);
                                }
                            });
                        }
                    }
                });

            }

            fileOutput.close();
            //Toast.makeText(RoomActivity.activity.getApplicationContext(), getString(R.string.toast_import), Toast.LENGTH_SHORT).show();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, canvasFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(RoomActivity.activity.getApplicationContext(), "download 실패", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(RoomActivity.activity.getApplicationContext(), "download 실패", Toast.LENGTH_SHORT).show();
        } finally {
            roomHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    progressView.startAnimation(slideDownAnimation);
                    progressView.setVisibility(View.GONE);
                    ((FrameLayout) findViewById(R.id.main_layout)).removeView(progressView);
                }
            }, 3000);
        }

    }


    // 업로드 프로그래스 작업중..
    public void uploadFileProgress(final int progress) {
        final FrameLayout container = (FrameLayout) findViewById(R.id.main_layout);
        final View progressView = getLayoutInflater().inflate(R.layout.layout_progress, container, false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Animation slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                container.addView(progressView, 6);
                progressView.startAnimation(slideUpAnimation);

                ProgressBar progressBar = (ProgressBar) progressView.findViewById(R.id.progress_bar);
                progressBar.setProgress(progress);
                if (progress >= 100) {
                    //textViewProgressContent.setText(getString(R.string.toast_import));
                    TextView btnProgresOk = (TextView) progressView.findViewById(R.id.btn_progress_ok);
                    btnProgresOk.setVisibility(View.VISIBLE);
                    btnProgresOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            progressView.startAnimation(slideDownAnimation);
                            progressView.setVisibility(View.GONE);
                            ((FrameLayout) findViewById(R.id.main_layout)).removeView(progressView);
                        }
                    });
                }

            }
        });
    }


    private void currentModeChecked() {
        if(currentMenu == GlobalConst.MENU_DRAWING){
            ((ImageView)findViewById(R.id.btn_rightmenu_pen_phone)).setImageResource(R.drawable.btn_rightmenu_pen_on);
        }else if(currentMenu == GlobalConst.MENU_PRESENTATION){
            ((ImageView)findViewById(R.id.btn_rightmenu_hand_phone)).setImageResource(R.drawable.btn_rightmenu_hand_on);
        }
    }

    private void rePlaceSubMenu() {
        RelativeLayout.LayoutParams params;

        params = (RelativeLayout.LayoutParams)findViewById(R.id.layer_document_submenu).getLayoutParams();
        params.topMargin = AndroidUtils.getPxFromDp(this, (float)170);
        findViewById(R.id.layer_document_submenu).setLayoutParams(params);

        params = (RelativeLayout.LayoutParams)findViewById(R.id.layer_poll_submenu).getLayoutParams();
        params.topMargin = AndroidUtils.getPxFromDp(this, (float)170);
        findViewById(R.id.layer_poll_submenu).setLayoutParams(params);

        params = (RelativeLayout.LayoutParams)mTextSubMenu.getLayoutParams();
        params.topMargin = AndroidUtils.getPxFromDp(this, (float)130);
        mTextSubMenu.setLayoutParams(params);

    }

    private void phoneRightMenuGone() {
        ((ImageView)mCustomActionBar.findViewById(R.id.btn_phone_communication_menu)).setImageResource(R.drawable.btn_header_userlist);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDispatcher.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult / requestCode : " + requestCode + ", resultCode : " + resultCode);
//        if(resultCode == RESULT_OK) {
//            Log.d(TAG, "onActivityResult / resultCode : RESULT_OK(" + resultCode + ")");
//        } else if(resultCode == RESULT_CANCELED) {
//            Log.d(TAG, "onActivityResult / resultCode : RESULT_CANCELED(" + resultCode + ")");
//        }

        if(requestCode == 9001 && resultCode == RESULT_OK) {
            // WebRTC 통화 모드를 다시 활성화 시킴..

            if (imgCropEnable) {
                cropCapturedImage(null);
            } else {
                mWebViewFragment.getCordovaWebView().sendJavascript("Ctrl.Uploader.uploadImgInCordova('" + imgPath + "', 'file1');");
            }

//            if (data != null) {
//                Uri photoUri = data.getData();
//                imgPath = getRealPathFromURI(photoUri);
//                if (imgCropEnable) {
//                    cropCapturedImage(mImageCaptureUri);
//                } else {
//                    mWebViewFragment.getCordovaWebView().sendJavascript("Ctrl.Uploader.uploadImgInCordova('" + imgPath + "', 'file1');");
//                }
//            }
        } else if (requestCode == 9002 && resultCode == RESULT_OK) {
            if (data != null) {
                //String path = getRealPathFromURI(mImageCaptureUri);
                //String path = mImageCaptureUri.getPath();
                String path = mImageCapturePath;
                Log.d(TAG, "onActivityResult / path : " + path);
                mWebViewFragment.getCordovaWebView().sendJavascript("Ctrl.Uploader.uploadImgInCordova('" + path + "', 'file1');");
                mImageCapturePath = null;
                //saveBitmapToJpeg(cropBitmap, path);
            } else {

            }
        } else {
            if (data != null) {
                Log.d(TAG, "onActivityResult / Intent data : " + data.toString());
            }
        }
    }

    String imgPath = "";
    private Uri mImageCaptureUri;
    private String mImageCapturePath;
    boolean imgCropEnable = true;

    private void saveBitmapToJpeg(Bitmap bitmap, String path){
        Log.d(TAG, "saveBitmapToJpeg() - path : " + path);
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        File newFile;
        try {
            newFile = new File(path);
            if (!newFile.isDirectory()) {
                newFile.mkdirs();
            }
//            if(newFile.exists()) {
//                newFile.delete();
//            }

            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException exception) {
            Log.e("FileNotFoundException", exception.getMessage());
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
        } finally {
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()))); // 갤러리를 갱신하기 위해..
            mWebViewFragment.getCordovaWebView().sendJavascript("Ctrl.Uploader.uploadImgInCordova('" + path + "', 'file1');");
            imgPath = "";
        }
    }


    private String getRealPathFromURI(Uri contentUri){
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public void cropCapturedImage(@Nullable String picPath){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        if(picPath == null) {
            //mImageCaptureUri = picUri;
            mImageCaptureUri = duplicateFileFromUri(mImageCaptureUri);
        } else {
            Uri picUri = FileProvider.getUriForFile(RoomActivity.this, "com.knowlounge.provider", new File(picPath));
            mImageCaptureUri = duplicateFileFromUri(picUri);
            Log.d(TAG, "mImageCaptureUri path : " + mImageCaptureUri.getPath());
//            mImageCapturePath = picPath;
        }

        cropIntent.setDataAndType(mImageCaptureUri, "image/*");
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.putExtra("output", mImageCaptureUri);
        //cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, 9002);

        /*
        Intent cropIntent = new Intent(Intent.ACTION_EDIT);
        mImageCaptureUri = picUri;
        cropIntent.setDataAndType(mImageCaptureUri, "image/*");
        startActivityForResult(cropIntent, 9002);
        */
    }

    private Uri duplicateFileFromUri(Uri uri) {
        Log.d(TAG, "duplicateFileFromUri");
        Uri resultUri = null;

        String destPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name);
        String destFileNm = String.valueOf(System.currentTimeMillis()) + ".jpg";
        InputStream is = null;
        OutputStream os = null;
        File resultFile = null;
        try {
            File dir = new File(destPath);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            resultFile = new File(destPath + "/" + destFileNm);
            is = getContentResolver().openInputStream(uri);
            os = new FileOutputStream(resultFile);

            byte buffer[] = new byte[1024];
            int length = 0;

            while((length = is.read(buffer)) > 0) {
                os.write(buffer,0,length);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
                //resultUri = Uri.fromFile(resultFile);
                resultUri = FileProvider.getUriForFile(RoomActivity.this, "com.knowlounge.provider", resultFile);
                mImageCapturePath = destPath + "/" + destFileNm;
            } catch (IOException e) {

            }
        }
        return resultUri;
    }






    @Override
    public void onBackPressed() {

        //super.onBackPressed();
        dialogIsShowing = -1;

        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        } else if (mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                closeLeftNavDrawer();
            }
        } else if(isMultiPageOpen) {
            showHideMultipage(isMultiPageOpen);
        } else if (isSelectorMode) {
            cancelCapture(drawingContainerMode);
        } else {
            Log.d(TAG, "onBackPress - exit room");
            setAlertDialog("0", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_exit_body1));
        }
    }

    @Override
    protected void onResume() {
        Log.d(getClass().getSimpleName(), "onResume fired..!");
        super.onResume();

        if(!isLoad)
            findViewById(R.id.loading_layer).setVisibility(View.GONE);

        if(exitRoomRunnable != null) {
            roomHandler.removeCallbacks(exitRoomRunnable);
        }

        checkNotiBadgeCount();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GcmRegistStatePreference.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GcmRegistStatePreference.REGISTRATION_PROCESSING));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GcmRegistStatePreference.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GcmRegistStatePreference.PUSH_RECEIVED));

//        if(videoSource != null && "INITIALIZING".equals(videoSource.state().name())) {
//            Log.d(TAG, "videoSource restart..");
//            videoSource.restart();
//        }


        RtcChatClient.instance().startVideo();
    }


    /**
     * StarShopFragment.InAppBillingListener 오버라이드..
     *
     *
     **/
    @Override
    public void onStarShopItemClicked(String productId) {
        Log.d(TAG, "onStarShopItemClicked");
        mDispatcher.start(productId);
    }

    public String getGuestNm(){
        return this.guestNm;
    }


    public void checkNotiBadgeCount() {
        TextView totalBadgeView, reqJoinBadgeView, chattingBadgeView, commentBadgeView;

        reqBadgeCount = prefManager.getReqjoinNotiBadgeCount(roomCode);
        totalBadge = KnowloungeApplication.isPhone ?
                reqBadgeCount + chatBadgeCnt + classChatBadgeCnt + commentBadgeCnt : reqBadgeCount + chatBadgeCnt + commentBadgeCnt;

        if (KnowloungeApplication.isPhone) {
            totalBadgeView = (TextView) findViewById(R.id.room_header_badge_count);
//            reqJoinBadgeView = (TextView) findViewById(R.id.room_right_badge_count);
//            chattingBadgeView = (TextView) findViewById(R.id.btn_rightmenu_chat_phone_badge_count);
//            commentBadgeView = (TextView) findViewById(R.id.btn_rightmenu_comment_phone_badge_count);
            if (totalBadge != 0) {
                totalBadgeView.setVisibility(View.VISIBLE);
                totalBadgeView.setText(String.valueOf(totalBadge));
            } else {
                totalBadgeView.setVisibility(View.GONE);
            }
        } else {
            reqJoinBadgeView = (TextView) findViewById(R.id.room_header_badge_count);
            chattingBadgeView = (TextView) findViewById(R.id.btn_rightmenu_chat_badge_count);
            commentBadgeView = (TextView) findViewById(R.id.btn_rightmenu_comment_badge_count);
        }

        if (reqBadgeCount != 0) {
//            reqJoinBadgeView.setVisibility(View.VISIBLE);
//            reqJoinBadgeView.setText(String.valueOf(reqBadgeCount));
        } else {
//            reqJoinBadgeView.setVisibility(View.GONE);
        }

        if ((chatBadgeCnt + classChatBadgeCnt) != 0) {
//            chattingBadgeView.setVisibility(View.VISIBLE);
//            chattingBadgeView.setText((chatBadgeCnt + classChatBadgeCnt)+"");
        } else {
//            chattingBadgeView.setVisibility(View.GONE);
        }

        if(commentBadgeCnt != 0){
//            commentBadgeView.setVisibility(View.VISIBLE);
//            commentBadgeView.setText(commentBadgeCnt+"");
        }else{
//            commentBadgeView.setVisibility(View.GONE);
        }
        if(mBadgeCallBack != null) {
            for (onCheckArriveBadge listener : mBadgeCallBack)
                listener.checkArriveBadge();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "<onPause / Knowlounge>");
//        Log.d(TAG, "videoSource state : " + videoSource.state().name());
        super.onPause();


        RtcChatClient.instance().stopVideo();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "<onStop / Knowlounge> packageName : " + getApplicationContext().getPackageName());
        super.onStop();

        prefManager.setAppRunningFlag(false);
        /*
        String topActivityName = AndroidUtils.getTopActivity(getApplicationContext());
        if(topActivityName.indexOf("camera") > -1 || topActivityName.indexOf("documentsui") > -1 || topActivityName.indexOf("gallery") > -1) {
            Log.d(TAG, "카메라나 파일 탐색기가 켜져 있습니다.");
            return;
        } else {
            if(allowExitRoom) {
                Log.d(TAG, "Knowlounge App is sleep..");
                exitRoomRunnable = new Runnable() {
                    @Override
                    public void run() {
                        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                        webView.sendJavascript("Ctrl.exit(false);");
                        isExitRoom = true;
                        Intent data = new Intent();
                        data.putExtra("roomcode", roomCode);
                        setResult(GlobalCode.CODE_EXIT_ROOM, data);
                        finish();
                    }
                };
                roomHandler.post(exitRoomRunnable);
                //roomHandler.postDelayed(exitRoomRunnable, 10*60*1000);
            }
        }*/

        // 홈버튼으로 앱을 내리면 15초 후에 수업을 퇴장시킴 - 2016.06.22
        /*
        if(!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
            if(allowExitRoom) {
                Log.d(TAG, "Knowlounge App is sleep..");
                exitRoomRunnable = new Runnable() {
                    @Override
                    public void run() {
                        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                        webView.sendJavascript("Ctrl.exit(false);");
                        isExitRoom = true;
                        Intent data = new Intent();
                        data.putExtra("roomcode", roomCode);
                        setResult(GlobalCode.CODE_EXIT_ROOM, data);
                        finish();
                    }
                };
                roomHandler.post(exitRoomRunnable);
                //roomHandler.postDelayed(exitRoomRunnable, 10*60*1000);
            }
        }*/
    }

    // 환경설정 저장
    @Override
    @Deprecated
    public void setRoomConfig(View view, JSONObject obj) {
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("Ctrl.Room.update('" + obj.toString() + "')");
        webView.sendJavascript("Ctrl.Background.save()");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    // 우측 버튼 이벤트 리스너 정의
    @Override
    public void onClick(View v) {
//        clearRightBtnSelected();
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            setOnPhoneClickRightSideMenu(v);
        } else {
            setOnTabletClickRightSideMenu(v);
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "<onDestroy / Knowlounge>");
        prefManager.clearAppRunningFlag();

        RoomLeftmenuFragment.clearCurrentPosition();
        if (!isExitRoom) {  // 비정상적인 상황에서 룸 나가기 처리가 안되었을 경우 룸 나가기 처리..
            final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
            webView.sendJavascript("Ctrl.exit(false);");
        }
        unregisterReceiver(networkStateReceiver);


        prefManager.clearRoomData();
        if(exitRoomRunnable != null) {
            roomHandler.removeCallbacks(exitRoomRunnable);
        }
        roomHandler = null;
        //RightMenu Static 배열 초기화
        userList.clear();
        myInfoList.clear();
        classUserList.clear();
        otherUserList.clear();
        chatList.clear();
        classChatList.clear();
        chatUserList.clear();
        classChatUserList.clear();
        commentList.clear();
        currentParent = -1;
        currentUserChild = -1;
        currentCommunityChild = -1;

        mSubscriber.unsubscribe();
        mDispatcher.onDestroy();
        super.onDestroy();
    }

    /**
     * CordovaWebView destroy..
     */
    private void destroyWebView() {
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.handleDestroy();
    }

    private void setOnPhoneClickRightSideMenu(View v) {
        switch (v.getId()){

            case R.id.popover_container:
                Log.d(this.getClass().getSimpleName(), "popover_container clicked..");
                closePopover(mPopupWindow);
                break;

            // drawing menu front
            case R.id.btn_rightmenu_pen_phone:
                roomTools.onPenTool();
                currentMode = GlobalConst.MODE_PEN;
                break;
            case R.id.btn_rightmenu_eraser_phone:
                roomTools.onEraserTool();
                currentMode = GlobalConst.MODE_ERASER;
                break;
            case R.id.btn_rightmenu_shape_phone:
                roomTools.onShapeTool();
                currentMode = GlobalConst.MODE_SHAPE;
                break;
//            case R.id.btn_rightmenu_text_phone:
//                roomTools.onOpenTextTool();
//                break;
//            case R.id.btn_rightmenu_document_phone:
//                roomTools.onOpenDocumentTools();
//                break;
            case R.id.btn_rightmenu_undo_phone:
                roomTools.onUndoTool();
                break;
            case R.id.btn_rightmenu_redo_phone:
                roomTools.onRedoTool();
                break;
            case R.id.btn_rightmenu_trash_phone:
                setAlertDialog("1", getResources().getString(R.string.global_delete), getResources().getString(R.string.canvas_delete_all));
                closeSubMenuLayer();
                break;

            // drawing menu end

            // presentation menu front
            case R.id.btn_rightmenu_hand_phone:
                roomTools.onHandMode("toggle");
                currentMode = GlobalConst.MODE_HAND;
                break;
            case R.id.btn_rightmenu_multipage_phone:
                roomTools.onMultiPage();
                break;
            case R.id.btn_rightmenu_pointer_phone:
                roomTools.onLaserTool();
                currentMode = GlobalConst.MODE_POINTER;
                break;
            case R.id.btn_rightmenu_zoomout_phone:
                roomTools.onZoomOut();
                break;
            case R.id.btn_rightmenu_zoomin_phone:
                roomTools.onZoomIn();
                break;
//            case R.id.btn_rightmenu_poll_phone:
//                //ToDo 폴서브메뉴 간격조정 후 open, close
//                clearRightBtnSelected();
//                closeSubMenuLayer("layer_poll_submenu");
//
//                if (guestFlag) {
//                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
//                    break;
//                }
//                if (!masterFlag) {
//                    setAlertDialog("5", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_authority_take) +"|get_authority|" + userId);
//                    break;
//                }
//
//                toggleSubMenuLayer("layer_poll_submenu", "btn_rightmenu_poll_phone", true);
//                roomTools.onHandMode();
//                break;
            //presentation menu end
            //communication menu front


            //Todo 창하쓰
            case R.id.btn_rightmenu_friends_phone:
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                    isOpenRightMenu = false;
                } else {
                    isOpenRightMenu = true;
//                    if(prefManager.getReqjoinNotiBadgeCount(roomCode) !=0){
//                        roomTools.onNotiTool();
//                    }else {

//                        menuIdx = 0;
//                        mDrawerLayout.openDrawer(Gravity.RIGHT);
//                        mSlidingFragment.setTabIndex(0);  // 기존 버전..
//                        closeSubMenuLayer();

//                    }
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                    mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW, true);
                    closeSubMenuLayer();
                }
                break;
            //Todo 창하쓰
            case R.id.btn_rightmenu_cam_phone :
                try {
                    if (TextUtils.equals(authInfo.getString("vcamopt"), "1") && (!creatorFlag && !TextUtils.equals(userType, "2"))) {
                        Toast.makeText(getContext(), getString(R.string.toast_authority_no), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {

                }

                View videoView = getWindow().findViewById(R.id.user_video_container);
                if (videoView.isShown()) {
                    ((ImageView)findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam);
                    videoView.setVisibility(View.GONE);
//                    videoCallback.setVideoStreamOnOff(false);
                } else {
                    ((ImageView)findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam_on);
                    videoView.setVisibility(View.VISIBLE);
//                    videoCallback.setVideoStreamOnOff(true);
                }
                closeSubMenuLayer();
                break;
            case R.id.btn_rightmenu_chat_phone :
                roomTools.onChatTool();
                break;
            case R.id.btn_rightmenu_comment_phone :
                roomTools.onCommentTool();
                break;
            case R.id.btn_rightmenu_setting_phone :
//                Intent configIntent = new Intent(RoomActivity.activity, ConfigDrawerActivity.class);
//                configIntent.putExtra("roomCode", roomCode);
//                configIntent.putExtra("roomTitle", roomTitle);
//                startActivity(configIntent);
                onRoomSetting();
                break;
            //communication menu end
        }
    }


    public void registBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(GcmRegistStatePreference.REGISTRATION_COMPLETE)) {
                    // 액션이 COMPLETE일 경우
                    final String token = intent.getStringExtra("registrationId");
                    Log.d(TAG, token);

                } else if (action.equals(GcmRegistStatePreference.PUSH_RECEIVED)) {

                    Bundle pushExtras = intent.getBundleExtra("push");
                    final String title = pushExtras.getString("title");
                    final String msg = pushExtras.getString("message");
                    final String roomCode = ((Integer) Integer.parseInt(pushExtras.getString("code"))).toString();
                    final String category = pushExtras.getString("category");

                    Log.d(TAG, "<registBroadcastReceiver / Knowlounge> title : " + title);
                    Log.d(TAG, "<registBroadcastReceiver / Knowlounge> msg : " + msg);
                    Log.d(TAG, "<registBroadcastReceiver / Knowlounge> roomCode : " + roomCode);

                    JSONArray pushArr = new JSONArray();
                    JSONObject pushJson = new JSONObject();
                    Set<String> keys = pushExtras.keySet();
                    for (String key : keys) {
                        try {
                            pushJson.put(key, pushExtras.get(key));
                        } catch (JSONException e) {

                        }
                    }

                    pushArr.put(pushJson);

                    //MyInviteListFragment._instance.addMyInviteListHandler(pushArr);

                    if(category.equals("invite")) {
                        Log.d(TAG, "<registBroadcastReceiver / Knowlounge> PUSH_RECEIVED - category : invite");
                        if (!activity.isFinishing()) {
                            Log.d(TAG, "<registBroadcastReceiver / Knowlounge> IndexActivity가 보이지 않네요.. Room에 메세지를 띄웁니다.");
                            if(isExitProcessing){return;}
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(isExitProcessing){return;}
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_apply_receive), Toast.LENGTH_SHORT).show();
//                                    setAlertDialog("6", title, msg + "|" + roomCode);
                                }
                            });
                        } else {
                            return;
                        }
                    } else if(category.equals("reqjoin")) {
                        Log.d(TAG, "<registBroadcastReceiver / Knowlounge> PUSH_RECEIVED - category : reqjoin");
                        // TODO : 참여제한 신청 수신시 알림처리..
                        if(roomCode.equals(RoomActivity.roomCode))
                            showNotification();
                        else
                            Toast.makeText(activity, getResources().getString(R.string.toast_apply_receive), Toast.LENGTH_LONG).show();

                    } else if (category.equals("extendroom")) {
                        // TODO : 참여제한 해제시 알림처리..
                        //openProgressLayout(title, msg, roomCode, false);
                    }
                    //Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                }

                checkNotiBadgeCount();
            }
        };
    }


    /**
     * 우측 메뉴 버튼들에 대해서 선택표시가 된 버튼을 초기화 시킴..
     */
    private void clearRightBtnSelected() {
        View parentView = isDevicePhone ? (View) mPhoneRightMenuArea : (View) mRightMenuArea;
        ArrayList<View> childViews = parentView.getTouchables();

        for (View child : childViews) {
            String tagStr = (String) child.getTag();

            if (tagStr.indexOf("right_btn") > -1) {
                String idStr = activity.getResources().getResourceName(child.getId());
                idStr = idStr.split("/")[1];
//                Log.d(TAG, "idStr : " + idStr);

                if(isDevicePhone) {
                    idStr = idStr.substring(0, idStr.length()-6);
                }
//                Log.d(TAG, "idStr : " + idStr);
                if(idStr.equals("btn_rightmenu_multipage"))
                    continue;
                ((ImageView) child).setImageResource(activity.getResources().getIdentifier(idStr, "drawable", getPackageName()));
            }
        }

        if (!isDevicePhone) {
            ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_poll)).setImageResource(R.drawable.btn_header_poll);
        }
    }




    // 이전 필기 다시 불러오기 모드 on / off 메서드
    public void setRedrawHistoryMode(final boolean enable) {
        Log.d(TAG, "<setRedrawHistoryMode / Knowlounge>");

        final LinearLayout btn_redraw_history_layout = (LinearLayout) findViewById(R.id.btn_redraw_history_layout);
        final TextView btn_redraw_history_cancel = (TextView) findViewById(R.id.btn_redraw_history_cancel);
        final TextView btn_redraw_history_ok = (TextView) findViewById(R.id.btn_redraw_history_ok);
        final View progress = (View) findViewById(R.id.progress);

        final ViewGroup.LayoutParams LoadingParams = (ViewGroup.LayoutParams) btn_redraw_history_layout.getLayoutParams();

        if(isExitProcessing){return;}
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //테블릿과 폰 가로세로에 따라 dialog 크기 변경
                if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
                    if (prefManager.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                        LoadingParams.width = (int) (300 * prefManager.getDensity());
                    } else {
                        LoadingParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    btn_redraw_history_layout.setLayoutParams(LoadingParams);
                } else {
                    LoadingParams.width = (int) (300 * prefManager.getDensity());
                    btn_redraw_history_layout.setLayoutParams(LoadingParams);
                }

                // RoomLoading Layout Show Hide
                if (enable) {
                    btn_redraw_history_layout.setVisibility(View.VISIBLE);
                    Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    Animation slide_left = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_to_right_duration8s);

                    btn_redraw_history_layout.startAnimation(slide_up);
                    progress.startAnimation(slide_left);

                    btn_redraw_history_layout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(btn_redraw_history_layout.isShown()) {
                                Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                                btn_redraw_history_layout.startAnimation(slide_down);
                                btn_redraw_history_layout.setVisibility(View.GONE);
                                classLoading = false;
                            }
                        }
                    }, 8000);
                    btn_redraw_history_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            classLoading = true;
                            CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                            webView.sendJavascript("CanvasApp.redrawHistory();");

                            Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            btn_redraw_history_layout.startAnimation(slide_down);
                            btn_redraw_history_layout.setVisibility(View.GONE);

                        }
                    });
                    btn_redraw_history_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CordovaWebView webView = mWebViewFragment.getCordovaWebView();

                            webView.sendJavascript("CanvasApp.hideDrawPacketModal();");

                            Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            btn_redraw_history_layout.startAnimation(slide_down);
                            btn_redraw_history_layout.setVisibility(View.GONE);
                            classLoading = false;
                        }
                    });
                } else {
                    if(btn_redraw_history_layout.isShown()) {
                        btn_redraw_history_layout.setVisibility(View.GONE);
                        btn_redraw_history_ok.setOnClickListener(null);
                        btn_redraw_history_cancel.setOnClickListener(null);
                    }
                }
            }
        });

    }


    public void closeRightDrawer() {
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }

    /**
     * 룸 배경 이미지 설정
     *
     * @param idx 룸 배경 이미지 인덱스 (1~4)
     */
    public void setRoomBgImageJavascript(int idx) {
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("Ctrl.Background._setImg(" + idx + ");");
    }


    /**
     * 룸 배경 색상 설정
     *
     * @param idx 룸 배경 색상 인덱스 (1~8)
     */
    public void setRoomBgColorJavascript(int idx) {
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        if (idx == 8) {
            webView.sendJavascript("Ctrl.Background._clear();");
        } else {
            webView.sendJavascript("Ctrl.Background._setColor(" + idx + ");");
        }
    }

    /**
     * 룸 배경 색상 커스텀 설정 (ColorPicker)
     *
     */
    public void setRoomBgColorCustomJavascript(int red, int green, int blue) {
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("Ctrl.Background._setRgbColor(" + red + ", " +green+", " + blue + ");");
    }


    /**
     * 룸 환경설정 저장
     *
     * @param obj
     */
    public void setRoomConfigJavascript(JSONObject obj, boolean showToast) {
        Log.d(TAG, "setRoomConfigJavascript");
        authInfo = obj;
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("Ctrl.Room.update('" + obj.toString() + "', " + showToast + ")");
        webView.sendJavascript("Ctrl.Background.save()");
    }


    // 최초 룸 정보 초기화 메서드
    public void initRoomInfo(JSONObject obj) {
        try {
            boolean creatorFlag  = obj.getBoolean("creatorflag");
            boolean masterFlag  = obj.getBoolean("masterflag");
            boolean guestFlag  = obj.getBoolean("guestflag");
            boolean isBookmark = obj.getBoolean("bookmark");

            JSONObject authInfo = obj.getJSONObject("auth");
            JSONObject bgInfo = obj.getJSONObject("bg");

            String roomId = obj.getString("roomid");
            String roomTitle = obj.getString("roomtitle");
            String userId = obj.getString("userid");
            String userNo = obj.getString("userno");
            String userNm = obj.getString("usernm");
            String snsType = obj.getString("snstype");
            String masterId = obj.getString("masterid");

            String classMasterUserNo = obj.getString("masterno");
            String masterRoomSeqNo = obj.getString("masterseqno");

            this.creatorFlag = creatorFlag;
            this.masterFlag = masterFlag;
            this.guestFlag = guestFlag;

            this.isFavoriteOn = isBookmark;


        } catch(JSONException e) {

        }
    }

    /**
     * 북마크 Flag 값 초기화
     * @param isOn
     */
    public void setBookmarkFlag(final boolean isOn) {
        isFavoriteOn = isOn;
    }


    /**
     * 타이틀 변경 UIHandler
     *
     * @param roomTitleParam
     **/
    public void setRoomTitleHandler(final String roomTitleParam) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
//                actionBar.setTitle(roomTitleParam);
                roomTitle = roomTitleParam;
                prefManager.setRoomTitle(roomTitleParam);

            }
        });
    }


    public JSONObject getAuthInfo() {
        return authInfo;
    }


    public void setAuthInfo(JSONObject obj) {
        try {
            Log.d(TAG, "<setAuthInfo / Knowlounge> obj : " + obj.toString());
            authInfo = obj;
            //userLimitCnt = obj.getInt("userlimitcnt");

            //initMasterUI(masterFlag, authInfo.getString("authtype"), userNm);

            // 선생님만 캠 허용 상태일 때 학생은 본인 캠을 제외한 나머지 캠을 볼 수 없고, 선생님은 학생 캠만 볼 수 없다.
            final String vCamOpt = authInfo.getString("vcamopt");
//            if (TextUtils.equals(vCamOpt, "1")) {
//                if(!creatorFlag && !TextUtils.equals(userType, "2")) {
//                    if(videoSource != null) {
//                        videoSource.stop();
//                    }
//
//                    videoCallback.destroyVideoUserAll(false);
//
////                if (mAppRTCAudioManager != null) {
////                    Log.d(TAG, "[WebRtcLifeCycle] RtcBaseActivity.onDestroy() - AppRTCAudioManager.close()");
////                    mAppRTCAudioManager.close();
////                    mAppRTCAudioManager = null;
////                }
////                mRootEglBase.release();
////                mRootEglBase = null;
//
//                } else {
//                    for(User user : userList) {
//                        boolean creatorFlag = TextUtils.equals(user.getCreator(), "1") ? true : false;
//                        String userId = user.getUserId();
//                        String userNm = user.getUserNm();
//                        String userType = user.getUserType();
//                        if(!creatorFlag && !TextUtils.equals(userType, "2")) {
//                            Log.d(TAG, "선생님만 캠 허용 모드 입니다. 학생 유저의 캠을 제거합니다.");
//                            videoCallback.destroyVideoUser(userId, false);
//                        } else if (TextUtils.equals(userNo, creatorNo) || TextUtils.equals(userType, "2")) {
//                            Log.d(TAG, "선생님만 캠 허용 모드 입니다. 영상 송출 대기중이었던 개설자와 선생님 유저의 캠을 활성화 시킵니다.");
//                            PeerConnectionClient client = VideoChatFragment._instance.getPcMap().get(userId);
//                            if (client != null) {
//                                Log.d(TAG, userNm + "의 캠을 활성화 시킵니다.");
//                                if (!client.getIsVideoAllow()) {
//                                    client.setIsVideoAllow(true);
//                                    client.displayAllowVideo();
//                                }
//                            }
//                        }
//                    }
//                }
//            }

//            videoCallback.changeOpt(vCamOpt);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.equals(vCamOpt, "1") && (!creatorFlag && !TextUtils.equals(userType, "2"))) {
                        getWindow().findViewById(R.id.user_video_container).setVisibility(View.GONE);
                        if(prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
                            ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video);
                        } else {
                            ((ImageView) findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam);
                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setBgInfo(JSONObject obj) {
        Log.d(TAG, "setBgInfo : " + obj.toString());
        bgInfo = obj;
    }

    public void setRoomId(final String param) {
        roomId = param;
        prefManager.setCurrentRoomId(roomId);
    }

    public void setCreatorFlag(final boolean isCreator) {
        // 개설자 설정
        creatorFlag = isCreator;
        if(isDevicePhone) {
            if (creatorFlag) {
                findViewById(R.id.btn_rightmenu_setting_phone).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.btn_rightmenu_setting_phone).setVisibility(View.GONE);
            }
        } else {
            if (creatorFlag) {
                findViewById(R.id.btn_header_setting).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.btn_header_setting).setVisibility(View.GONE);
            }
        }
    }

    public void showTeacherCallDialog(String roomCode, String teacherNm) {
        TeacherCallDialogFragment dialogFragment = new TeacherCallDialogFragment();
        Bundle args = new Bundle();
        args.putString("roomcode", roomCode);
        args.putString("teachernm", teacherNm);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "teacher_call_dialog");
    }


    /**
     * 영상 설정 팝업창이 뜨는 진입점..
     * @param obj
     */
    public void initializeRoom(JSONObject obj) {
        try {

            Log.d(TAG, "<initializeRoom / ZICO> obj : " + obj.toString());


            JSONObject authInfo = obj.getJSONObject("auth");
            JSONObject bgInfo = obj.getJSONObject("bg");

            JSONArray classUserList = obj.getJSONArray("onlineClassList");

            String roomId = obj.getString("roomid");
            String roomTitle = obj.getString("roomtitle");
            String userId = obj.getString("userid");
            String userNo = obj.getString("userno");
            String userNm = obj.getString("usernm");
            String snsType = obj.getString("snstype");
            String masterId = obj.getString("masterid");
            String masterNo = obj.getString("masterno");
            String creatorNo = obj.getString("creatorno");
            String creatorId = obj.getString("creatorid");
            String currentpageId = obj.getString("currentpageid");

            boolean creatorFlag = userId.equals(creatorId) ? true : false;
            boolean guestFlag = obj.getBoolean("isGuest");
            boolean isBookmark = obj.getBoolean("bookmark");

            String videoCtrlMode = obj.getString("videoctrl");
            String soundOnlyMode = obj.getString("soundonly");

            String userType = obj.getString("usertype");

            // 선생님 수업 정보
            String masterRoomSeqNo = obj.getString("masterseqno");
            String teacherUserNo = obj.getString("parentcreatorno");

            int userLimitCnt = obj.getInt("userLimitCnt");
            String userMaxCnt = authInfo.getString("usermaxcnt");

            String videoLimit = obj.getString("videoLimit");

            String separate = obj.getString("separate");

            this.isFavoriteOn = isBookmark;
            this.roomTitle = roomTitle;
            this.roomId = roomId;
            this.parentRoomId = roomId.indexOf("_") > -1 ? roomId.substring(0, roomId.indexOf("_")) : roomId;
            this.userNo = userNo;
            this.userId = userId;
            this.userNm = userNm;
            this.userType = userType;
            this.snsType = snsType;
            this.creatorFlag = creatorFlag;
            this.masterFlag = masterNo.equals(userNo) ? true : false;
            this.masterUserId = masterId;
            this.masterUserNo = masterNo;
            this.creatorNo = creatorNo;
            this.guestFlag = guestFlag;
            this.classMasterUserNo = teacherUserNo;    // 수업 개설자 유저의 유저번호값 설정
            this.masterRoomSeqNo = masterRoomSeqNo;  // 수업 마스터 룸의 seqNo값 설정
            if (userNo.equals(teacherUserNo)) {
                teacherFlag = true;
            } else {
                teacherFlag = false;
            }

            this.authInfo = authInfo;
            this.bgInfo = bgInfo;

            this.userLimitCnt = userLimitCnt;
            this.userMaxCnt = userMaxCnt;
            this.currentPage = currentpageId;

            this.videoLimit = Integer.parseInt(videoLimit);


            isVideoControl = "1".equals(videoCtrlMode) ? true : false;
            isVideoSeparate = "1".equals(separate) ? true : false;

            Log.d(TAG, "<initializeRoom / ZICO> isVideoSeparate : " + isVideoSeparate);

            // 클래스 유저 추가 - 추후 iOS와 정리되면 반영할 예정임.
//            for (int i=0; i<classUserList.length(); i++) {
//                JSONObject user = classUserList.getJSONObject(i);
//                user.put("master", masterUserNo.equals(user.getString("userno")) ? true : false);
//                mRoomUserPresenter.onAddUser(user);
//                this.classUserList.add(classUserItem(user));
//            }

            // 내 정보를 클래스 유저 목록에 추가
            JSONObject myInfo = new JSONObject();
            myInfo.put("userno", userNo);
            myInfo.put("userid", userId);
            myInfo.put("usernm", userNm);
            myInfo.put("thumbnail", obj.get("thumbnail"));
            myInfo.put("usertype", userType);
            myInfo.put("master", masterFlag);
            myInfo.put("connected_roomid", roomId);
            myInfo.put("connected_roomtitle", roomTitle);
            myInfo.put("connected_roomseparate", separate);
            mRoomUserPresenter.onAddUser(myInfo);
            this.myInfoList.add(classUserItem(myInfo));

            prefManager.setCurrentRoomId(roomId);
            prefManager.setRoomTitle(roomTitle);
            prefManager.setCurrentTeacherUserNo(teacherUserNo);
            prefManager.setUserNo(userNo);
            prefManager.setUserId(userId);
            prefManager.setUserNm(userNm);
            prefManager.setSnsType(snsType);

            mRoomSpec.setUserNo(userNo);
            mRoomSpec.setUserNm(userNm);

            // 서브 룸의 룸아이디가 올 경우, 처리 루틴..
            int specialCharPosition = roomId.indexOf("_");
            String parentRoomId = specialCharPosition < 0 ? roomId : roomId.substring(0, specialCharPosition);

            mRoomSpec.setRoomId(isVideoSeparate ? roomId : parentRoomId);   // 영상 그룹 분리 기능 추가 - 2017.04.04
            mRoomSpec.setIsParentMaster(teacherFlag);
            mRoomSpec.setIsCreator(creatorFlag);
            mRoomSpec.setIsVideoControlEnable("1".equals(videoCtrlMode) ? true : false);
            mRoomSpec.setIsWhiteboardMode("1".equals(soundOnlyMode) ? true : false);
            mRoomSpec.setIsSeparate(isVideoSeparate);

            ((MultiVideoChatFragment) getSupportFragmentManager().findFragmentByTag(MultiVideoChatFragment.class.getSimpleName())).openRtcSettingDialog(mRoomSpec);

            // UI 처리 부분
            String authType = authInfo.has("authtype") ? authInfo.getString("authtype") : "0";

            setUpView();

            mRoomEvents.initializeRoom(mRoomSpec);

            if(isExitProcessing)
                return;

            if(TextUtils.equals(authInfo.getString("vcamopt"), "1") && (!creatorFlag && !TextUtils.equals(userType, "2"))) {
                getWindow().findViewById(R.id.user_video_container).setVisibility(View.GONE);
                if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
                    ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video);
                } else {
//                    ((ImageView) findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam);
                }
            } else {
                getWindow().findViewById(R.id.user_video_container).setVisibility(View.VISIBLE);
                if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
                    ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video_on);
                } else {
//                    ((ImageView) findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam_on);
                }
            }

            // 내가 개설자 일때는 펜 모드 활성화..
            if (masterFlag) {
                isHandMode = false;
                isTextMode = false;
                currentMenu = GlobalConst.MENU_DRAWING;
            }



        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
//            RoomLeftmenuFragment roomLeftMenuFragment = new RoomLeftmenuFragment();
//            Bundle leftNavParams = new Bundle();
//            leftNavParams.putString("roomcode", roomCode);
//            leftNavParams.putString("roomtitle", roomTitle);
//            roomLeftMenuFragment.setArguments(leftNavParams);
//            getSupportFragmentManager().beginTransaction().replace(R.id.left_drawer_panel_room, roomLeftMenuFragment, "RoomLeftmenuFragment").commit();  // 좌측 슬라이딩 메뉴
            Log.d(TAG, "RoomLeftmenuFragment roomTitle initialize");
            ((RoomLeftmenuFragment)getSupportFragmentManager().findFragmentByTag("RoomLeftmenuFragment")).initializeLeftMenuUI(roomTitle);
        }
    }


    /**
     * 뷰 초기화 프로세스를 진행하는 메서드이다.
     */
    private void setUpView() {
        if (isDevicePhone) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d(TAG, "<setUpView> 폰 가로모드");
                mRightMenuLand.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "<setUpView> 폰 세로모드");
                mRightMenuPhone.setVisibility(View.VISIBLE);
            }
        } else {
            mRightMenuTablet.setVisibility(View.VISIBLE);
        }

        if (!isDevicePhone) {
            if (creatorFlag) {
                mCustomActionBar.findViewById(R.id.btn_header_setting).setVisibility(View.VISIBLE);
            } else {
                mCustomActionBar.findViewById(R.id.btn_header_setting).setVisibility(View.GONE);
            }
        }

        if (creatorFlag && !teacherFlag)
            // 서브룸의 개설자에게만 비디오 분리 버튼을 노출시킨다.
            mVideoSeperateBtn.setVisibility(View.VISIBLE);
        else
            mVideoSeperateBtn.setVisibility(View.GONE);


        if (masterFlag) {
            // 내가 진행자일 경우는 초기에 펜 모드로 UI 처리한다.
            ImageButton penBtn;
            if (isDevicePhone) {
                penBtn = (ImageButton) findViewById(R.id.btn_rightmenu_pen_phone);
                clearRightBtnSelected();
            } else {
                penBtn = (ImageButton) findViewById(R.id.btn_rightmenu_pen);
            }
            penBtn.setImageResource(R.drawable.btn_rightmenu_pen_on);
        }
    }



    /**
     *  masterFlag 값 변경.. (changeMaster일 때 호출함..)
     **/
    public void setMasterFlag(final boolean isMaster, final String newMasterId) {
        Log.d(TAG, "setMasterFlag : " + isMaster + ", newMaster : " + newMasterId);
        masterFlag = isMaster;
        masterUserId = newMasterId;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                videoCallback.updateVideoMaster(masterUserId);
                EventBus.get().post(new MultiPageEvent(MultiPageEvent.CHANGE_AUTH,""));
            }
        });

    }



    public void setGuestFlag(boolean param) {
        guestFlag = param;
    }

    public void setUserId(final String userIdParam) {
        userId = userIdParam;
//        FrameLayout videoViewFrame = (FrameLayout) activity.findViewById(R.id.video_frame);
//        if(videoViewFrame != null) {
//            videoViewFrame.setTag(userIdParam);
//        }
    }

    public void setUserNo(final String userNoParam) {
        userNo = userNoParam;
    }

    public void setUserNm(final String userNmParam) {
        userNm = userNmParam;
        prefManager.setUserNm(userNm);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                TextView videoUserTextView = (TextView) activity.findViewById(R.id.local_video_user_nm);
//                videoUserTextView.setText(userNm);
//            }
//        });
    }

    public String getClassMasterUserNo() {
        return classMasterUserNo;
    }

    public boolean getTeacherFlag() {
        return teacherFlag;
    }


    public void updateChattingBadge() {
        if(isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW) {
            clearChatBadgeCnt();
        }else {
            chatBadgeCnt++;
        }
        checkNotiBadgeCount();
    }
    public void updateClassChattingBadge() {
        if(isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW) {
            clearClassChatBadgeCnt();
        }else {
            classChatBadgeCnt++;
        }
        checkNotiBadgeCount();
    }


    public void updateCommentBadge() {
        if(isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW) {
            clearChatBadgeCnt();
        }else {
            commentBadgeCnt++;
        }
        checkNotiBadgeCount();
    }


    /**
     * 하단 보드 이동 버튼 이벤트 정의
     */
    public void setMoveRoomBtn() {

        layoutBottom = (LinearLayout) findViewById(R.id.layout_move_room);

        containerMoveRoom = (LinearLayout) findViewById(R.id.container_move_room);
        txtMoveRoom = (TextView) findViewById(R.id.txt_move_room);
        icoMoveRoom = (ImageView) findViewById(R.id.ico_move_room);

        if (classMasterUserNo.equals(userNo)) {  // 내가 선생님일 때
            containerMoveRoom.setVisibility(View.GONE);
            //txtMoveRoom.setVisibility(View.GONE);
            if (!masterRoomSeqNo.equals(roomCode)) {  // 선생님이 서브룸에 들어왔을 때
                containerMoveRoom.setVisibility(View.VISIBLE);
                containerMoveRoom.setBackground(getResources().getDrawable(R.drawable.btn_move_my_room));

                icoMoveRoom.setImageResource(R.drawable.ico_canvas_myboard);
                txtMoveRoom.setVisibility(View.VISIBLE);
                txtMoveRoom.setText(getResources().getString(R.string.canvas_navi_sub));
                txtMoveRoom.setTextColor(Color.parseColor("#19d7ff"));
                View.OnClickListener evtListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveRoom(masterRoomSeqNo, null);
                    }
                };
                txtMoveRoom.setOnClickListener(evtListener);
            }
        } else {
            containerMoveRoom.setVisibility(View.VISIBLE);
            //txtMoveRoom.setVisibility(View.VISIBLE);
            Log.d(TAG, "roomCode : " + roomCode + ", masterRoomCode : " + masterRoomSeqNo);

            if(!TextUtils.isEmpty(roomCode) && !TextUtils.isEmpty(masterRoomSeqNo)) {
                if (Integer.parseInt(roomCode) != Integer.parseInt(masterRoomSeqNo)) {
                    containerMoveRoom.setBackground(getResources().getDrawable(R.drawable.btn_move_teacher_room));
                    icoMoveRoom.setImageResource(R.drawable.ico_canvas_teacherboard);
                    txtMoveRoom.setText(getResources().getString(R.string.canvas_navi_master));
                    txtMoveRoom.setTextColor(getResources().getColor(R.color.app_base_color));
                    View.OnClickListener evtListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPollProgress) {
                                Toast.makeText(getContext(), getString(R.string.poll_draw_cant_mine_ansr), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            moveRoom(masterRoomSeqNo, null);
                        }
                    };
                    txtMoveRoom.setOnClickListener(evtListener);
                } else {
                    containerMoveRoom.setBackground(getResources().getDrawable(R.drawable.btn_move_my_room));
                    icoMoveRoom.setImageResource(R.drawable.ico_canvas_myboard);
                    txtMoveRoom.setText(getResources().getString(R.string.canvas_navi_sub));
                    txtMoveRoom.setTextColor(Color.parseColor("#19d7ff"));
                    View.OnClickListener evtListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            moveMyRoom(null);
//                        throw new RuntimeException();
//                        final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
//                        String cookieStr = "FBMMC=" + prefManager.getUserCookie() + "; " + "FBMCS=" + prefManager.getChecksumCookie() + ";";
//                        webView.loadUrl("javascript:Ctrl.Member.moveMyRoom('" + cookieStr + "');");
                        }
                    };
                    txtMoveRoom.setOnClickListener(evtListener);
                }
            }
            findViewById(R.id.btn_open_userlist).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roomTools.onOpenUserList();
                }
            });
        }
    }


    /**
     * 하단 폴 답변 버튼 제어
     */
    public void setAnswerPollBtn() {
        containerAnswerPoll = (LinearLayout) findViewById(R.id.container_poll);
        tooltipAnswerPollGuide = (TextView) findViewById(R.id.txt_answer_poll_guide);
        txtAnswerPoll = (TextView) findViewById(R.id.txt_poll);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isPollProgress) {
                    containerAnswerPoll.setVisibility(View.VISIBLE);
                    if (isQuestioner) {
                        containerAnswerPoll.setBackground(getResources().getDrawable(R.drawable.btn_wait_answer_poll));
                        txtAnswerPoll.setText(getResources().getString(R.string.poll_btn_submit_ing));
                        txtAnswerPoll.setTextColor(Color.parseColor("#c8c8c8"));

                        containerAnswerPoll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openPollDialogHandler(pollAnswerDataTmp, GlobalConst.ACTION_SHOW_TIMER_PANEL);
                            }
                        });
                    } else {
                        tooltipAnswerPollGuide.setVisibility(View.VISIBLE);
                        containerAnswerPoll.setBackground(getResources().getDrawable(R.drawable.btn_answer_poll));
                        txtAnswerPoll.setText(getResources().getString(R.string.poll_btn_submit));
                        txtAnswerPoll.setTextColor(Color.parseColor("#ffffff"));
                        containerAnswerPoll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tooltipAnswerPollGuide.setVisibility(View.GONE);
                                pollPopupDialog(pollAnswerDataTmp, GlobalConst.ACTION_MAKE_POLL_SHEET);
                            }
                        });
                        tooltipAnswerPollGuide.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tooltipAnswerPollGuide.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    tooltipAnswerPollGuide.setVisibility(View.GONE);
                    containerAnswerPoll.setVisibility(View.GONE);
                    containerAnswerPoll.setOnClickListener(null);
                }
            }
        });
    }

    public void allowVideoPermission(String toUserNo) {
        Log.d(TAG, "<allowVideoPermission> fromUserNo : " + userNo + ", toUserNo : " + toUserNo);
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("PacketMgr.Master.BroadCast.videoNoti('all', '" + roomId + "', 'connect', '" + userNo + "', '" + toUserNo + "');");
        Toast.makeText(getContext(), "상대방의 미디어를 허용하였습니다.", Toast.LENGTH_SHORT).show();
    }

    public void denyVideoPermission(String toUserNo) {
        Log.d(TAG, "<denyVideoPermission> fromUserNo : " + userNo + ", toUserNo : " + toUserNo);
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("PacketMgr.Master.BroadCast.videoNoti('all', '" + roomId + "', 'disconnect', '" + userNo + "', '" + toUserNo + "');");
        Toast.makeText(getContext(), "상대방의 미디어를 차단하였습니다.", Toast.LENGTH_SHORT).show();
    }

    public void requestVideoPermission(String fromUserNo) {
        Log.d(TAG, "<requestVideoPermission> fromUserNo : " + fromUserNo);
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("PacketMgr.Master.BroadCast.videoNoti('all', '" + roomId + "', 'request', '" + fromUserNo + "', '" + classMasterUserNo + "');");
        Toast.makeText(RoomActivity.activity.getApplicationContext(), "영상 초대 신청이 발송되었습니다_다국어 필요", Toast.LENGTH_SHORT).show();
    }

    public void callAllStudent() {
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("Ctrl.Member.callAllStudent()");
    }

    public boolean getCreatorFlag() {
        // 개설자 여부 설정..
        return creatorFlag;
    }

    public boolean getMasterFlag() {
        // 진행자 여부 설정..
        return masterFlag;
    }
    public String getMasterUserId(){
        return masterUserId;
    }

    public String getCreatorNo() {
        return creatorNo;
    }

    public boolean getGuestFlag() {
        // 게스트 여부 설정..
        return guestFlag;
    }


    public boolean getIsHandMode() {
        // 선택 모드 활성화 여부
        return isHandMode;
    }


    public boolean getIsTextMode() {
        // 텍스트 입력 모드 활성화 여부
        return isTextMode;
    }


    public boolean getIsSelectorMode() {
        // 판서폴 영역 선택 모드 활성화 여부
        return isSelectorMode;
    }


    public boolean getIsPollProgress() {
        // 폴 진행 여부
        return isPollProgress;
    }

    public void setIsPollProgress(boolean flag) {
        // 폴 진행 여부
        isPollProgress = flag;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public String getUserNm() {
        return userNm;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getParentRoomId() {
        return parentRoomId;
    }

    public String getRoomCode() { return roomCode; }

    public String getRoomNm() { return  roomTitle; }

    public String getSnsType() {
        return snsType;
    }

    public String getUserType() {
        return userType;
    }
    public String getMasterSeqNo() {return masterRoomSeqNo;}

    public boolean isTeacher() {
        return teacherFlag;
    }

    public boolean isVideoControl() {
        return isVideoControl;
    }

    public boolean isVideoSeparate() {
        return isVideoSeparate;
    }

    public void setSnsType(String snsType) {
        this.snsType = snsType;
    }

    public void setUserLimitCnt(int userLimitCnt) {
        Log.d(TAG, "setUserLimitCnt");
        this.userLimitCnt = userLimitCnt;
    }

    public void updateUserLimitCnt(int userLimitCnt) {
        Log.d(TAG, "updateUserLimitCnt");
        this.userLimitCnt = userLimitCnt;
        try {
            authInfo.putOpt("userlimitcnt", "30");
            //Toast.makeText(getContext(), getResources().getString(R.string.toast_remove), Toast.LENGTH_SHORT).show();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public int getUserLimitCnt() {
        // 유저 제한 인원 정보 리턴
        return userLimitCnt;
    }

    public void setZoomVal(final int zoom) {
        zoomVal = zoom;
    }

    public int getZoomVal() {
        return this.zoomVal;
    }

    public void resetZoom() {
        zoomVal = 100;
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.loadUrl("javascript:Ctrl.zoomControl(" + zoomVal + ");");
    }


    public void openRemoveCommentDialog(String commentNo, String title, String msg) {
        msg = msg + "|" + commentNo;
        setAlertDialog("4", title, msg);
        //webView.sendJavascript("Ctrl.Comment.remove('" + commentNo + "')");

    }


    public void sendChat(final String chatMsg, final String targetUserNo, final String targetUserNm, final String chatType) {

        final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isExitProcessing){return;}
                webView.sendJavascript("Ctrl.Chat.sendChat('" + chatMsg + "', '" + targetUserNo + "', '" + targetUserNm + "', '" + chatType + "')");
            }
        });
    }

    public void sendComment(final String commentMsg) {
        try {
            // 댓글 작성 옵션값에 따라 예외처리..
            String cmtOpt = authInfo.getString("cmtopt");
            if(TextUtils.equals(cmtOpt, "0")) {
                Toast.makeText(context, getResources().getString(R.string.canvas_authority_no), Toast.LENGTH_SHORT).show();
                return;
            }
            final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isExitProcessing) {
                        return;
                    }
                    webView.sendJavascript("Ctrl.Comment.add('" + commentMsg + "')");
                }
            });
        } catch (JSONException e) {

        }

    }


    public void sendInvite(final String receiverNoList, final String receiverIdList) {
        final String escapeRoomTitle = roomTitle.replace("'", "\\'");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isExitProcessing){return;}
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Notify.Invite.invite('" + roomId + "', '" + escapeRoomTitle + "', '" + userNo + "', '" + userNm + "', '" + receiverNoList + "', '" + receiverIdList + "')");
            }
        });
    }

    public void searchInviteUser(final String searchKey, final String searchTxt) {
        Log.d(TAG, "searchInviteUser.. searchTxt : " + searchTxt);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isExitProcessing){return;}
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();

                String callbackStr = "function(list){cordova.exec(function(result){},function(result){},'myPlugin', 'searchInviteUserResult', list);}";

                webView.sendJavascript("Notify.Invite.search('" + userNo + "', '" + searchKey + "', '" + searchTxt + "', " + callbackStr + ")");
            }
        });
    }

    public void sendInviteToSnsFriends(final String snsType, final String receiverId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isExitProcessing){return;}
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Notify.Invite.fbDialog('" + receiverId + "', '" + roomCode + "', '" + roomId + "', '" + roomTitle + "', '" + userNo + "', '" + userNm + "')");
            }
        });
    }

    public View.OnClickListener setChattingWisperEvent(final PopupWindow popup, final String userId){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popup != null)
                    popup.dismiss();
                if (!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                    mDrawerLayout.openDrawer(Gravity.RIGHT);

                EventBus.get().post(new ChattingEvent(userId));
                mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW, GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW, true);
            }
        };

        return listener;
    }


    public View.OnClickListener setAuthChangeEvent(final PopupWindow popup, final String authType, final String userId) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "[setAuthChangeEvent] authInfo : " + authInfo.toString());
                    String roomAuthType = authInfo.getString("authtype");
                    if(popup != null)
                        popup.dismiss();
                    if("1".equals(roomAuthType)) {
                        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
//                        webView.sendJavascript("Ctrl.Member.requestAuthChange('" + authType + "', '" + userId + "')");
                        String msgStr = "opener".equals(authType)  ? getResources().getString(R.string.canvas_authority_take) : getResources().getString(R.string.canvas_authority_give);

                        JSONObject authConfirmParam = new JSONObject();
                        authConfirmParam.put("mServiceType", "5");
                        authConfirmParam.put("title", getResources().getString(R.string.global_popup_title));
                        authConfirmParam.put("msg", msgStr + "|" + authType + "|" + userId);
                        openConfirmDialog(authConfirmParam);

                    } else {
                        //todo 권한이 허용되지 않은 class 입니다. toast
                        Toast.makeText(activity.getContext(), getResources().getString(R.string.canvas_auth_deny), Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        return listener;
    }

    // 유저 강제퇴장 이벤트 리스너
    public View.OnClickListener setDeportUserEvent(final PopupWindow popup, final String userNo, final String userId, final String userNm) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    CordovaWebView webView = mWebViewFragment.getCordovaWebView();
//                    webView.sendJavascript("Ctrl.Member.kick('" + userNo + "', '" + userId + "','" + userNm + "')");
                    if(popup.isShowing())
                        popup.dismiss();

                    JSONObject authConfirmParam = new JSONObject();
                    authConfirmParam.put("mServiceType", "7");
                    authConfirmParam.put("title", getResources().getString(R.string.global_popup_title));
                    authConfirmParam.put("msg", userNo + "|" + userId + "|" + userNm);
                    openConfirmDialog(authConfirmParam);
                } catch (JSONException e) {

                }
            }
        };

        return listener;
    }


    // 북마크 아이콘 변경
    //Todo 이거 뭔가요??
//    private static void setFavoriteIcon(Menu menu, int idx, boolean isOn) {
//        isFavoriteOn = isOn;
//        if (isOn) {
//            menu.getItem(idx).setIcon(R.drawable.btn_topmenu_favorite_on);
//        } else {
//            menu.getItem(idx).setIcon(R.drawable.btn_topmenu_favorite_off);
//        }
//    }

    // 댓글(코멘트) 추가 핸들러
//    public void addCommentListHandler(final JSONArray arr) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                addCommentList(arr);
//            }
//        });
//    }
//
//    // 댓글(코멘트) 추가
//    private static void addCommentList(JSONArray arr) {
//        try {
//            int len = arr.length();
//            for (int i = 0; i < len; i++) {
//                JSONObject obj = arr.getJSONObject(i);
//                String commentNo = obj.has("commentno") ? obj.getString("commentno") : "0";
//                String userId = obj.has("userid") ? obj.getString("userid") : "0";
//                String userNm = obj.has("usernm") ? obj.getString("usernm") : "0";
//                String userNo = obj.has("userno") ? obj.getString("userno") : "0";
//                String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : "0";
//                String cdatetime = obj.has("cdatetime") ? obj.getString("cdatetime") : "0";
//                String content = obj.has("content") ? obj.getString("content") : "0";
//                CommentItem newComment = new CommentItem(commentNo, userId, userNm, userNo, thumbnail, cdatetime, content);
//                if (len == 1) {
//                    commentList.add(0, newComment);
//                } else {
//                    commentList.add(newComment);
//                }
//
//            }
//            commentAdapter.notifyDataSetChanged();
//            commentAdapter.setListViewLock(false);
//
//        } catch (JSONException e) {
//            Log.d("setCommentList", e.getMessage());
//        }
//    }


    // 댓글(코멘트) 삭제 핸들러
//    public static void removeCommentListHandler(final String commentNo) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                removeCommentList(commentNo);
//            }
//        });
//    }
//
//
//    // 댓글(코멘트) 삭제
//    private static void removeCommentList(String commentNo) {
//        for (CommentItem comment : commentList) {
//
//            if (commentNo.equals(comment.getCommentNo())) {
//                commentList.remove(comment);
//                break;
//            }
//        }
//        commentAdapter.notifyDataSetChanged();
//        commentAdapter.setListViewLock(false);
//    }

    private JSONObject pollAnswerDataTmp = null;

    public void setPollAnswerData(JSONObject obj) {
        pollAnswerDataTmp = obj;
    }

    // 폴 추가 핸들러
    public void openPollDialogHandler(final JSONObject obj, final int tag) {
        switch (tag) {
            case GlobalConst.ACTION_MAKE_POLL_SHEET :
                isQuestioner = false;
                break;
            case GlobalConst.ACTION_SHOW_TIMER_PANEL :
                isQuestioner = true;
                break;
        }
        if (tag == GlobalConst.ACTION_MAKE_POLL_SHEET) {
            try {
                String pollType = obj.getJSONObject("polldata").getJSONObject("map").getString("polltype");
//                if (!TextUtils.equals(pollType, Integer.toString(PollCreateData.POLL_TYPE_DRAWING))) {
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d(TAG, "openPollDialogHandler");
//                            pollPopupDialog(obj, tag);
//                        }
//                    });
//                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "openPollDialogHandler");
                        pollPopupDialog(obj, tag);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "openPollDialogHandler");
                    pollPopupDialog(obj, tag);
                }
            });
        }
    }

    // 폴 엑티비티 시작하기
    private void pollPopupDialog(JSONObject obj, int tag) {
        try {
            Intent intent = null;
            if(isDevicePhone) {
                intent = new Intent(activity, PollPopupDialogPhone.class);
            }else{
                intent = new Intent(activity, PollPopupDialog.class);
            }
            intent.putExtra("plugin_type", tag);
            if (tag == GlobalConst.ACTION_SHOW_TIMER_PANEL) {
                intent.putExtra("pollno", obj.getString("pollno"));
                intent.putExtra("target", obj.getString("target"));
            } else {
                intent.putExtra("obj", obj.toString());
            }
            activity.startActivityForResult(intent, 8888);
        } catch (JSONException j) {
            j.printStackTrace();

        }
    }

    // 폴 엑티비티 종료하기
    private void closePollPopup() {
        if(isDevicePhone) {
            if (PollPopupDialogPhone._instance != null)
                PollPopupDialogPhone._instance.finish();
        }else{
            if (PollPopupDialog._instance != null)
                PollPopupDialog._instance.finish();
        }
    }


//    public void answerDeployHandler(final JSONObject obj){
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("answerDeployHandler", obj.toString());
//                answerDeploy(obj);
//            }
//        });
//    }
//    private static void answerDeploy(JSONObject obj) {
//        try {
//
//            JSONObject polldata_obj = obj.getJSONObject("polldata");
//            JSONObject map_obj = polldata_obj.getJSONObject("map");
//
//            String pollno = map_obj.getString("pollno");
//            String title = map_obj.getString("title");
//            String polltype = map_obj.getString("polltype");
//            JSONArray arr = map_obj.getJSONArray("itemlist");
//
////            String polltempno = temp_obj.getString("polltempno");
//            String shutdown_time = obj.getString("timelimit");
//            String iscount = obj.getString("iscount");
//            ArrayList<AnswerQuestion_Data> itemstr = new ArrayList<AnswerQuestion_Data>();
//            int len = arr.length();
//
//            for(int i=0; i<len; i++)
//            {
//                JSONObject item_obj = arr.getJSONObject(i);
//                String pollitemno = item_obj.getString("pollitemno");
//                String itemnm = item_obj.getString("itemnm");
//                itemstr.add(new AnswerQuestion_Data(pollitemno,itemnm));
//            }
//            setDeployAnswer(context, pollno, title, polltype, "", shutdown_time, iscount, itemstr);
//
//        } catch(JSONException e) {
//            Log.d("addPollList", e.getMessage());
//        }
//    }
//    public static void deployPollDetailHandler(final JSONObject obj) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("deployPollDetailHandler", "in");
//                depolyPollDetail(obj);
//            }
//        });
//    }
//    private static void depolyPollDetail(JSONObject obj) {
//        try {
//            isThroughHandle = true;
//            Log.d("parsing", "json");
//            JSONObject temp_obj = obj.getJSONObject("map");
//            JSONArray arr = temp_obj.getJSONArray("itemlist");
//            String polltype = temp_obj.getString("polltype");
//            String polltitle = temp_obj.getString("title");
//            String polltempno = temp_obj.getString("polltempno");
//            ArrayList<String> itemidx_arr = new ArrayList<String>();
//            ArrayList<String> itemnm_arr = new ArrayList<String>();
//
//            Log.d("depolyPoll_pollType",polltype);
//            Log.d("depolyPoll_pollTitle",polltitle);
//
//
//            Log.d("arr_string",arr.toString());
//            int len = arr.length();
//            Log.d("arr_length",Integer.toString(len));
//            for(int i=0; i<len; i++) {
//                JSONObject _obj = arr.getJSONObject(i);
//                String itemidx = _obj.has("itemidx") ? _obj.getString("itemidx") : "0";
//                String itemnm = _obj.has("itemnm") ? _obj.getString("itemnm") : "0";
//                itemidx_arr.add(itemidx);
//                itemnm_arr.add(itemnm);
//            }
//            poll_through_handler_depoly(polltitle, polltype, itemidx_arr, itemnm_arr, polltempno);
//
//        } catch(JSONException e) {
//            Log.d("addPollList", e.getMessage());
//        }
//    }
//
//    public static void addWaitPollViewHandler(final JSONObject obj) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Log.d("addWaitPollViewHandler", "in");
//                    PollWatingTimer(obj.getString("pollno"));
//                }catch(JSONException j){
//                    Log.d("addWaitPollViewHandler",j.getMessage());
//                }
//            }
//        });
//
//    }
//    // 폴 추가 핸들러
//    public static void addPollListHandler(final JSONObject obj) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("addPollListHandler", "in");
//                addPollList(obj);
//            }
//        });
//
//    }
//    // 폴 추가
//    private static void addPollList(JSONObject obj) {
//        try {
//            Log.d("parsing","json");
//            final int totalnumber = obj.getInt("totalcount");
//            JSONArray arr = obj.getJSONArray("list");
//            Log.d("arr_string",arr.toString());
//            int len = arr.length();
//            Log.d("arr_length",Integer.toString(len));
//            for(int i=0; i<len; i++) {
//                JSONObject _obj = arr.getJSONObject(i);
//                String polltempno = _obj.has("polltempno") ? _obj.getString("polltempno") : "0";
//                String title = _obj.has("title") ? _obj.getString("title") : "0";
//                int num = i+1;
//                addPoll(totalnumber, polltempno, title,num);
//            }
//
//        } catch(JSONException e) {
//            Log.d("addPollList", e.getMessage());
//        }
//    }


    public void openDeportUserDialog(JSONObject obj) throws JSONException {
        String userNo = obj.getString("userno");
        String userId = obj.getString("userid");
        String userNm = obj.getString("usernm");
        String payloadStr = userNo + "|" + userId + "|" + userNm;
        activity.setAlertDialog("7", getResources().getString(R.string.global_popup_title), payloadStr);
    }

    // Activity 종료
    public void finishActivity(JSONObject obj) throws JSONException {
        if (obj.length() == 0) {
            activity.finish();
        } else {
//            String dialogType = obj.getString("mServiceType");
//            String dialogTitle = obj.getString("title");
//            String dialogMsg = obj.getString("msg");
//
//            activity.setAlertDialog(dialogType, dialogTitle, dialogMsg);
            Toast.makeText(activity.getContext(), getResources().getString(R.string.toast_otherdevice), Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }


    // Activity 종료
    public void forcefinishActivity() throws JSONException {
        activity.finish();
    }


    public void openConfirmDialog(JSONObject obj) throws JSONException {
        String dialogType = obj.getString("mServiceType");
        String dialogTitle = obj.getString("title");
        String dialogMsg = obj.getString("msg");

        activity.setAlertDialog(dialogType, dialogTitle, dialogMsg);
    }


//    public void openRoomTitleDialog() {
//        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_title, null);
//        final EditText roomTitleInput = (EditText) dialogView.findViewById(R.id.room_title_input);
//        roomTitleInput.setText(roomTitle);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this, R.style.AlertDialogCustom);
//
//        final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
//
//        if (creatorFlag) {
//            builder.setMessage(getResources().getString(R.string.dialog_update_class_message)).setCancelable(true)
//                    .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            String newRoomTitle = roomTitleInput.getText().toString();
//                            if (TextUtils.isEmpty(newRoomTitle)) {
//                                Toast.makeText(activity.getContext(), getResources().getString(R.string.canvas_edittitle_guide), Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                            roomTitle = newRoomTitle;
//                            webView.sendJavascript("Ctrl.Modal.updateTitle('" + newRoomTitle + "');");
//                        }
//                    })
//                    .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//
//            AlertDialog confirm = builder.create();
//            confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//            confirm.setCanceledOnTouchOutside(true);
//            confirm.setTitle(getResources().getString(R.string.dialog_update_class_title));
//            confirm.setView(dialogView);
//
//            //confirm.setIcon(android.R.drawable.sym_def_app_icon);
//            //confirm.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
//            confirm.show();
//
//        }
//    }


    /**
     * Native confirm 창 생성
     * mServiceType
     * 0 : exit confirm dialog
     * 1 : erase all confirm dialog
     * 2 : bookmark confirm dialog
     * 3 : kick user exit dialog (deprecated)
     * 4 : comment remove confirm dialog
     * 5 : get / send authority confirm dialog
     * 7 : kick user
     */
    private void setAlertDialog(String type, String title, String msg) {

        //if(builder == null)
        if(dialogIsShowing == Integer.parseInt(type)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this, R.style.AlertDialogCustom);
        dialogIsShowing = Integer.parseInt(type);

        final CordovaWebView webView = mWebViewFragment.getCordovaWebView();

        switch (type) {
            case "0":  // 방 나가기
                if (creatorFlag) {
                    msg = getContext().getString(R.string.canvas_exit_body1);
                    builder.setMessage(msg).setCancelable(true)
                            .setPositiveButton(getResources().getString(R.string.canvas_close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    webView.sendJavascript("Ctrl.exit(true);");
                                    isExitRoom = true;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (TextUtils.isEmpty(prefManager.getUserCookie())) {
                                                Intent exitIntent = new Intent(RoomActivity.activity, LoginActivity.class);
                                                startActivity(exitIntent);
                                            } else {
                                                setResult(GlobalCode.CODE_EXIT_ROOM);
                                            }
                                            activity.finish();

                                        }
                                    }, 1500);

                                    dialogIsShowing = -1;
                                }
                            })
                            .setNeutralButton(getResources().getString(R.string.canvas_exit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    webView.sendJavascript("Ctrl.exit(false);");
                                    isExitRoom = true;
                                    dialogIsShowing = -1;
                                    setResult(GlobalCode.CODE_EXIT_ROOM);
                                    activity.finish();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogIsShowing = -1;
                                }
                            });

                } else {
                    msg = getContext().getString(R.string.canvas_exit_body2);
                    builder.setMessage(msg).setCancelable(true)
                            .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    webView.sendJavascript("Ctrl.exit(false);");
                                    dialogIsShowing = -1;
                                    activity.finish();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogIsShowing = -1;
                                }
                            });
                }
                break;
            case "1":  // 모두 지우기
                builder.setMessage(msg).setCancelable(true)
                        .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                webView.sendJavascript("Ctrl._eraserAllClear()");
                                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                                    closePopover(mPopupWindow);
                                }
                                dialogIsShowing = -1;
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogIsShowing = -1;
                            }
                        });
                break;
//            case "2":  // 북마크
//                final MenuItem item = headerMenu.findItem(R.id.menu_favorite);
//                builder.setMessage(msg).setCancelable(true)
//                        .setPositiveButton(getResources().getString(R.string.confirm), new android.content.DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                webView.sendJavascript("Ctrl.setBookmark(" + isFavoriteOn + ")");
//                                if (isFavoriteOn) {
//                                    item.setIcon(R.drawable.btn_topmenu_favorite_off);
//                                    isFavoriteOn = false;
//                                } else {
//                                    item.setIcon(R.drawable.btn_topmenu_favorite_on);
//                                    isFavoriteOn = true;
//                                }
//                            }
//                        })
//                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        });
//                break;
            case "3": // 강제퇴장
                builder.setMessage(msg).setCancelable(false)
                        .setPositiveButton(getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialogIsShowing = -1;
                                activity.finish();
                            }
                        });
                break;
            case "4": // 댓글삭제
                String[] msgArr = msg.split("\\|");

                String dialogMsg = msgArr[0];
                final String commentNo = msgArr[1];

                builder.setMessage(dialogMsg).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialogIsShowing = -1;
                                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                                Log.d(TAG, commentNo);
                                webView.sendJavascript("Ctrl.Comment.remove('" + commentNo + "')");
                            }
                        }).setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            case "5": // 권한
                String[] msgAuthArr = msg.split("\\|");

                String msgStr = msgAuthArr[0];
                final String authType = msgAuthArr[1];
                final String userIdParam = msgAuthArr[2];

                if ("chairman".equals(authType)) break;

                builder.setMessage(msgStr).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                                dialogIsShowing = -1;
                                // TODO : _checkAuth로 바꾸기..
                                webView.sendJavascript("Ctrl.Member.authChange('" + authType + "', '" + userIdParam + "', true)");
                            }
                        }).setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogIsShowing = -1;
                            }
                        });
                break;
            case "6":
                String[] msgPushArr = msg.split("\\|");

                String msgPushStr = msgPushArr[0];
                final String roomCode = msgPushArr[1];
                builder.setMessage(msgPushStr).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialogIsShowing = -1;
                                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                                webView.loadUrl("javascript:Ctrl.moveRoom('" + roomCode + "');");

                            }
                        }).setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogIsShowing = -1;
                            }
                        });
                break;
            case "7" :  //강퇴
                String[] payloadArr = msg.split("\\|");
                final String userNo = payloadArr[0];
                final String userId = payloadArr[1];
                final String userNm = payloadArr[2];
                final String bodyMessage = String.format(getResources().getString(R.string.canvas_popup_kick), userNm);
                builder.setMessage(bodyMessage).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialogIsShowing = -1;
                                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                                webView.sendJavascript("PacketMgr.Master.kickUser('" + roomId + "', '" + userNo + "', '" + userId + "', '" + userNm + "');");

                            }
                        }).setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogIsShowing = -1;
                            }
                        });
                break;
            case "8" : //
            // 허용 유도 alert
                builder.setMessage(msg).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialogIsShowing = -1;
                                try {
                                    authInfo.put("authtype", "1");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    setRoomConfigJavascript(authInfo, true);
                                }

                            }
                        }).setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogIsShowing = -1;
                    }
                });
                break;
            default:
                break;
        }

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    dialogIsShowing = -1;
                    dialog.dismiss();
                    return false;
                }
                return true;
            }
        });

        AlertDialog confirm = builder.create();

        confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        confirm.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        confirm.setCanceledOnTouchOutside(true);
        confirm.setTitle(title);

        //confirm.setIcon(android.R.drawable.sym_def_app_icon);
        //confirm.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        confirm.show();

    }

    AlertDialog alertDialog = null;
    public AlertDialog createAlertDialog(AlertDialog.Builder builder) {
        //if (alertDialog == null) {
            alertDialog = builder.create();
        //}
        return alertDialog;
    }

    public void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }





    // 버튼 이벤트 선언부
    private void setButtonEvent(int deviceType) {

        findViewById(R.id.popover_container).setOnClickListener(this);

        if (deviceType == GlobalConst.DEVICE_PHONE) {

        } else {

        }
        setSubMenuButtonEvent();

    }

    private void setSubMenuButtonEvent() {
        findViewById(R.id.layer_rightmenu_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomTools.onTextTool();
                closeSubMenuLayer();
            }
        });

        findViewById(R.id.layer_rightmenu_memo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomTools.onMemoTool();
            }
        });

        findViewById(R.id.layer_rightmenu_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomTools.onPhotoTool();
            }
        });

        findViewById(R.id.layer_rightmenu_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomTools.onImageTool();
            }
        });

        findViewById(R.id.layer_rightmenu_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomTools.onPdfTool();
            }
        });

        findViewById(R.id.layer_rightmenu_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomTools.onVideoShareTool();
            }
        });

        findViewById(R.id.right_submenu_poll_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isGuest()) return;

                try {
                    CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                    webView.sendJavascript("Ctrl._checkAuth(true)");

                    Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                    if (!masterFlag) return;

                    clearRightBtnSelected();
                    Intent intent;
                    if (isDevicePhone) {
                        intent = new Intent(RoomActivity.activity, PollPopupDialogPhone.class);
                    } else {
                        intent = new Intent(RoomActivity.activity, PollPopupDialog.class);
                    }
                    intent.putExtra("mServiceType", GlobalConst.VIEW_CREATE_FRAGMENT);
                    startActivity(intent);
                    mPollSubMenu.setVisibility(View.GONE);
                    isSubMenuCheck = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.right_submenu_poll_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGuest()) return;

                try {
                    CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                    webView.sendJavascript("Ctrl._checkAuth(true)");

                    Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                    if(!masterFlag){return;}

                    clearRightBtnSelected();
                    Intent intent;
                    if (isDevicePhone) {
                        intent = new Intent(RoomActivity.activity, PollPopupDialogPhone.class);
                    } else {
                        intent = new Intent(RoomActivity.activity, PollPopupDialog.class);
                    }
                    intent.putExtra("mServiceType", GlobalConst.VIEW_POLLLIST_FRAGMENT);
                    startActivity(intent);
                    mPollSubMenu.setVisibility(View.GONE);
                    isSubMenuCheck = true;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        findViewById(R.id.right_submenu_complet_poll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isGuest()) return;

                try {
                    CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                    webView.sendJavascript("Ctrl._checkAuth(true)");

                    Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                    if (!masterFlag) return;

                    clearRightBtnSelected();
                    Intent intent;
                    if (isDevicePhone) {
                        intent = new Intent(RoomActivity.activity, PollPopupDialogPhone.class);
                    } else {
                        intent = new Intent(RoomActivity.activity, PollPopupDialog.class);
                    }
                    intent.putExtra("mServiceType", GlobalConst.VIEW_COMPLETE_POLLLIST_FRAGMENT);
                    startActivity(intent);
                    mPollSubMenu.setVisibility(View.GONE);
                    isSubMenuCheck = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // OnClick 이벤트 설정..
    private void setOnTabletClickRightSideMenu(View v) {

        final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.popover_container);

        int popoverContainer = 0;

        switch (v.getId()) {

            case R.id.popover_container :
                Log.d(this.getClass().getSimpleName(), "popover_container clicked..");
                closePopover(mPopupWindow);
                break;

            case R.id.btn_rightmenu_fold : // 우측 아이콘 메뉴 최소화 / 최대화
                Log.d(TAG, "RightMenu fold button..");
                closeSubMenuLayer();

                if (isRightMenuFold) {
                    ((LinearLayout)rightMenu.findViewById(R.id.rightmenu_area)).setVisibility(View.VISIBLE);
                    isRightMenuFold = false;
                } else {
                    ((LinearLayout)rightMenu.findViewById(R.id.rightmenu_area)).setVisibility(View.GONE);
                    isRightMenuFold = true;
                }

                break;

            case R.id.btn_rightmenu_pen :  // 펜
                roomTools.onPenTool();
                break;

            case R.id.btn_rightmenu_eraser :  // 지우개
                roomTools.onEraserTool();
                break;

            case R.id.btn_rightmenu_shape :  // 도형
                roomTools.onShapeTool();
                break;

            case R.id.btn_rightmenu_pointer :  // 레이저 포인터
                roomTools.onLaserTool();
                break;

            case R.id.btn_rightmenu_chat :
                roomTools.onChatTool();
                break;

            case R.id.btn_rightmenu_comment :
                roomTools.onCommentTool();
                break;

            default :
                break;
        }
        Log.d("popover_container", rootView.getWidth() + "/" + rootView.getHeight());
    }


    /**
     * Pen color button event..
     *
     * @param popoverView PopupWindow container
     * @param webView     CordovaWebView (For calling javascript)
     * @param colorIdx    Index number about color
     */
    private void setEventBtnPenColor(final View popoverView, final CordovaWebView webView, final int colorIdx) {

        popoverView.findViewWithTag("pen_color_" + colorIdx).setOnClickListener(new View.OnClickListener() {
            PreviewCanvas previewCanvas = (PreviewCanvas) popoverView.findViewById(R.id.pen_preview);

            @Override
            public void onClick(final View v) {
                int savedPenAlpha = (int) prefManager.getPenAlpha();
                final int penAlpha = savedPenAlpha < 30 ? 30 : savedPenAlpha;
                if (colorIdx == 10) {
                    AmbilWarnaDialog dialog = new AmbilWarnaDialog(RoomActivity.this, 0xffffff00, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                        }

                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {

                            String selectedColor = String.format("0x%08x", color);
                            String penColor = selectedColor.substring(4, 10);
                            previewCanvas.setPenColor("#" + penColor);
                            previewCanvas.setPenAlpha(penAlpha);

                            ImageButton imgBtn = (ImageButton) v;

                            clearColorButton(popoverView, "pen_color");
                            imgBtn.setImageResource(R.drawable.btn_option_colorselect);
                            webView.sendJavascript("Ctrl.__setPenCustomColor(10, 1, '0', '" + penColor + "');");

                            prefManager.setPenColor("#" + penColor);
                            prefManager.setPenColorIdx(colorIdx);
                        }
                    });
                    dialog.show();

                } else {
                    String penColor = getResources().getString(getResources().getIdentifier("pen_color_" + colorIdx, "color", getPackageName()));
                    previewCanvas.setPenColor(penColor);
                    previewCanvas.setPenAlpha(penAlpha);

                    ImageButton imgBtn = (ImageButton) v;
                    clearColorButton(popoverView, "pen_color");
                    imgBtn.setImageResource(R.drawable.btn_option_colorselect);
                    webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", 1, '0');");

                    prefManager.setPenColor(penColor);
                    prefManager.setPenColorIdx(colorIdx);
                }
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        float x, y;
        x = event.getX();
        y = event.getY();
        Log.d(TAG, Float.toString(x) + "," + Float.toHexString(y));
        return super.onTouchEvent(event);
    }

    /**
     * 도형 유형 버튼 이벤트 설정
     *
     * @param popoverView PopupWindow container View
     * @param webView     CordovaWebView (For calling javascript)
     * @param shapeIdx    6 - Rectangle , 7 - Circle , 5 - Line
     */
    private void setOnClickShapeTypeBtn(final View popoverView, final CordovaWebView webView, final int shapeIdx) {

        final PreviewCanvas shapePreviewCanvas = (PreviewCanvas) popoverView.findViewById(R.id.shape_preview);
        final ImageButton btnTransparentColor = (ImageButton)popoverView.findViewById(R.id.shape_color_11);

        popoverView.findViewWithTag("shape_type_" + shapeIdx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setShapeTypeBtnEvent", "shapeIdx : " + shapeIdx);

                clearShapeTypeButtonOn(popoverView);   // 도형 유형 버튼 클리어
                String shapeTypeStr = shapeIdx == 6 ? "square" : shapeIdx == 7 ? "circle" : shapeIdx == 5 ? "line" : "";
                Drawable btnDrawable = ContextCompat.getDrawable(activity, getResources().getIdentifier("btn_shape_" + shapeTypeStr + "_on", "drawable", getPackageName()));
                ((ImageButton) v).setBackground(btnDrawable);  // 클릭한 버튼 배경에 선택모드 이미지 적용

                SeekBar shapeWidthBar = (SeekBar) popoverView.findViewById(R.id.shape_width_bar);
                SeekBar shapeAlphaBar = (SeekBar) popoverView.findViewById(R.id.shape_opacity_bar);

                prefManager.setShapeTypeIdx(shapeIdx);
                shapePreviewCanvas.setShapeType(shapeIdx); // Preview 캔버스에 도형 타입 정보 전달..

                int toggleRcIdx = shapeIdx == 5 ? 1 : shapeIdx == 6 ? 2 : shapeIdx == 7 ? 3 : 0;
                webView.sendJavascript("Ctrl.toggleRC(5, " + toggleRcIdx + ", true);");

                int fillIdx = (Integer) prefManager.getFillTypeIdx();

                if (shapeIdx == 5) {
                    setShapeFillBorderColor(popoverView, shapePreviewCanvas);
                    btnTransparentColor.setBackgroundResource(R.drawable.btn_option_transparent_dis);

                    int lPenWidth = prefManager.getLpenWidth();
                    int lPenAlpha = prefManager.getLpenAlpha();
                    shapeWidthBar.setProgress(lPenWidth);
                    shapeAlphaBar.setProgress(lPenAlpha);

                    clearShapeButtonBackground(popoverView, "fill_type");
                    ImageButton fillBtn = (ImageButton) popoverView.findViewById(R.id.shape_fill);
                    fillBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_plane_on));

                    ImageButton borderBtn = (ImageButton) popoverView.findViewById(R.id.shape_border);
                    borderBtn.setVisibility(View.INVISIBLE);

                    int lPenColorIdx = prefManager.getLpenColorIdx();
//                    int lPenColorIdx = getPref("lpen_color_idx") == null ? 1 : (Integer) getPref("lpen_color_idx");
                    //clearColorButton(popoverView, "shape_fill_color");


                    webView.sendJavascript("Ctrl.__setPenColor(" + lPenColorIdx + ", " + shapeIdx + ", '0');");

                } else if (shapeIdx == 6) {  // 사각형
                    setShapeFillBorderColor(popoverView, shapePreviewCanvas);
                    btnTransparentColor.setBackgroundResource(R.drawable.btn_option_transparent);
                    int sPenWidth = prefManager.getSpenWidth();
                    shapeWidthBar.setProgress(sPenWidth);
                    int sPenAlpha = prefManager.getSpenAlpha();
                    shapeAlphaBar.setProgress(sPenAlpha);

                    ImageButton fillBtn = (ImageButton) popoverView.findViewById(R.id.shape_fill);
                    ImageButton borderBtn = (ImageButton) popoverView.findViewById(R.id.shape_border);
                    borderBtn.setVisibility(View.VISIBLE);


                    clearShapeButtonBackground(popoverView, "fill_type");
                    switch (fillIdx) {
                        case 0:
                            fillBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_plane_on));
//                            fillBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_option_select));
                            break;
                        case 1:
                            borderBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_line_on));
//                            borderBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_option_select));
                            break;
                        default:
                            break;
                    }

                    int sPenColorIdx = prefManager.getSpenColorIdx();
                    int sPenBorderColorIdx = prefManager.getSpenBorderColorIdx();

                    webView.sendJavascript("Ctrl.__setPenColor(" + sPenColorIdx + ", " + shapeIdx + ", '0');");
                    webView.sendJavascript("Ctrl.__setPenColor(" + sPenBorderColorIdx + ", " + shapeIdx + ", '1');");

                } else if (shapeIdx == 7) {  // 원
                    setShapeFillBorderColor(popoverView, shapePreviewCanvas);
                    btnTransparentColor.setBackgroundResource(R.drawable.btn_option_transparent);


                    int cPenWidth = prefManager.getCpenWidth();
                    shapeWidthBar.setProgress(cPenWidth);
                    int cPenAlpha = prefManager.getCpenAlpha();
                    shapeAlphaBar.setProgress(cPenAlpha);

                    // Fill / Border 버튼 선택 세팅
                    ImageButton fillBtn = (ImageButton) popoverView.findViewById(R.id.shape_fill);
                    ImageButton borderBtn = (ImageButton) popoverView.findViewById(R.id.shape_border);
                    borderBtn.setVisibility(View.VISIBLE);

                    clearShapeButtonBackground(popoverView, "fill_type");
                    switch (fillIdx) {
                        case 0:
                            fillBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_plane_on));
//                            fillBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_option_select));
                            break;
                        case 1:
                            borderBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_line_on));
//                            borderBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_option_select));
                            break;
                        default:
                            break;
                    }

                    int cPenColorIdx = prefManager.getCpenColorIdx();
                    int cPenBorderColorIdx = prefManager.getCpenBorderColorIdx();
//                    int cPenColorIdx = getPref("cpen_color_idx") == null ? 1 : (Integer) getPref("cpen_color_idx");
//                    int cPenBorderColorIdx = getPref("cpen_border_color_idx") == null ? 1 : (Integer) getPref("cpen_border_color_idx");

                    webView.sendJavascript("Ctrl.__setPenColor(" + cPenColorIdx + ", " + shapeIdx + ", '0');");
                    webView.sendJavascript("Ctrl.__setPenColor(" + cPenBorderColorIdx + ", " + shapeIdx + ", '1');");
                }
            }
        });
    }


    /**
     * Fill and Border button event set..
     *
     * @param popoverView PopupWindow container View
     * @param webView     CordovaWebView (For calling javascript)
     * @param fillIdx     0 : Fill , 1 : Stroke
     */
    private void setShapeFillBtnEvent(final View popoverView, final CordovaWebView webView, final int fillIdx) {

        final PreviewCanvas shapePreviewCanvas = (PreviewCanvas) popoverView.findViewById(R.id.shape_preview);
        popoverView.findViewWithTag("fill_type_" + fillIdx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fillIdx == 0) {
                    popoverView.findViewById(R.id.fill_color).setVisibility(View.VISIBLE);
                    popoverView.findViewById(R.id.border_color).setVisibility(View.GONE);
                } else if (fillIdx == 1) {
                    popoverView.findViewById(R.id.fill_color).setVisibility(View.GONE);
                    popoverView.findViewById(R.id.border_color).setVisibility(View.VISIBLE);
                }

                clearColorButton(popoverView, "shape_fill_color");
                clearColorButton(popoverView, "shape_border_color");
//                clearShapeButtonBackground(popoverView, "fill_type");
                clearFillTypeButtonOn(popoverView);

                ImageButton imgBtn = (ImageButton) v;
                if (fillIdx == 0) {
                    imgBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_plane_on));

                } else if (fillIdx == 1) {
                    imgBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_line_on));
                }
//                imgBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_option_select));

                shapePreviewCanvas.setShapeFillType(fillIdx);
                prefManager.setFillTypeIdx(fillIdx);
                setShapeFillBorderColor(popoverView, shapePreviewCanvas);

                //webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", 1, '0');");
            }
        });
    }


    /**
     * setShapeColorBtnEvent
     * 도형 색상 버튼 이벤트 설정
     *
     * @param popoverView PopupWindow container
     * @param webView     CordovaWebView (For calling javascript)
     * @param colorIdx    index number about color
     */
    private void setShapeColorBtnEvent(final View popoverView, final CordovaWebView webView, final int colorIdx, final int fillIdx) {
        final String btnTag = fillIdx == 0 ? "shape_fill_color" : fillIdx == 1 ? "shape_border_color" : "";
        Log.d("setShapeColorBtnEvent", btnTag + "_" + colorIdx + " button Click event set.....");

        popoverView.findViewWithTag(btnTag + "_" + colorIdx).setOnClickListener(new View.OnClickListener() {
            PreviewCanvas previewCanvas = (PreviewCanvas) popoverView.findViewById(R.id.shape_preview);

            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Shape color button Click event set.....");
                if (fillIdx == 1 && colorIdx == 11) return;  // 테두리 색상은 투명색 옵션이 없으므로 return 시킴..
                final int shapeTypeIdx = (Integer) prefManager.getShapeTypeIdx();

                String shapeColor = "";
                String colorRgb = getResources().getString(getResources().getIdentifier("pen_color_" + colorIdx, "color", getPackageName()));

                if (colorIdx == 10) {   // 커스텀 컬러일 경우..
                    AmbilWarnaDialog dialog = new AmbilWarnaDialog(RoomActivity.this, 0xffffff00, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                        }

                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {
                            String selectedColor = String.format("0x%08x", color);
                            String colorStr = selectedColor.substring(4, 10);

                            setShapeInfo(popoverView, previewCanvas, colorIdx, fillIdx, "#" + colorStr);
                            saveShapeInfo(shapeTypeIdx, fillIdx, colorIdx, "#" + colorStr);
                            webView.sendJavascript("Ctrl.__setPenCustomColor(10, " + shapeTypeIdx + ", '" + fillIdx + "', '" + colorStr + "');");  // 커스텀 컬러는 항상 10으로..
                        }
                    });
                    dialog.show();

                } else if (colorIdx == 11) {  // 투명 컬러.. (Fill에만 해당함)
                    int currentShapeType = (int) prefManager.getShapeTypeIdx();
                    if (currentShapeType == 5) return;

                    shapeColor = colorRgb.equals("#0") ? "#00000000" : colorRgb;
                    webView.sendJavascript("Ctrl.__setFillClear(11, " + shapeTypeIdx + ");");

                } else {  // 컬러인덱스 1~9까지..
                    shapeColor = colorRgb;
                    webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", " + shapeTypeIdx + ", '" + fillIdx + "');");
                }

                Log.d("setShapeColorBtnEvent", "shapeType : " + shapeTypeIdx + ", fillIdx : " + fillIdx + ", colorIdx : " + colorIdx + ", shapeColor : " + shapeColor);

                setShapeInfo(popoverView, previewCanvas, colorIdx, fillIdx, shapeColor);
                clearColorButton(popoverView, btnTag);
                ImageButton imgBtn = (ImageButton) v;
                imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                /*
                if(colorIdx == 0) {
                    webView.sendJavascript("Ctrl.__setFillClear(12, " + shapeTypeIdx + ");");
                } else if(colorIdx != 99) {
                    webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", " + shapeTypeIdx + ", '" + fillIdx + "');");
                }*/

                saveShapeInfo(shapeTypeIdx, fillIdx, colorIdx, shapeColor);
            }
        });
    }


    private void setShapeInfo(View popoverView, PreviewCanvas previewCanvas, int colorIdx, int fillIdx, String shapeColor) {
        Log.d(TAG, "setShapeInfo - colorIdx : " + colorIdx);
        Log.d(TAG, "setShapeInfo - fillIdx : " + fillIdx);
        Log.d(TAG, "setShapeInfo - shapeColor : " + shapeColor);

        previewCanvas.setShapeFillType(fillIdx);
        previewCanvas.setShapeColor(shapeColor);

        if (fillIdx == 0) {  // Fill 색상을 누르면 Border 색상도 동일하게...
            if (colorIdx != 11) {   // 투명이 아니면..
                previewCanvas.setShapeFillType(1);
                previewCanvas.setShapeColor(shapeColor);

                clearColorButton(popoverView, "shape_border_color");
                ImageButton btn = (ImageButton) popoverView.findViewWithTag("shape_border_color_" + colorIdx);
                btn.setImageResource(R.drawable.btn_option_colorselect);
            }
        }
    }

    // SharedPreference에 도형 정보를 저장..
    private void saveShapeInfo(int shapeTypeIdx, int fillIdx, int colorIdx, String shapeColor) {
        Log.d(TAG, "[RoomActivity.saveShapeInfo] shapeType : " + shapeTypeIdx + ", fillIdx : " + fillIdx + ", colorIdx : " + colorIdx + ", shapeColor : " + shapeColor);

        if (shapeTypeIdx == 5) {
            prefManager.setLpenColor(shapeColor);
            prefManager.setLpenColorIdx(colorIdx);
        } else if (shapeTypeIdx == 6) {
            if (fillIdx == 0) {
                prefManager.setSpenColor(shapeColor);
                prefManager.setSpenColorIdx(colorIdx);
                if (colorIdx != 11) {
                    prefManager.setSpenBorderColor(shapeColor);
                    prefManager.setSpenBorderColorIdx(colorIdx);
                }
            } else if (fillIdx == 1) {
                prefManager.setSpenBorderColor(shapeColor);
                prefManager.setSpenBorderColorIdx(colorIdx);
            }
        } else if (shapeTypeIdx == 7) {
            if (fillIdx == 0) {
                prefManager.setCpenColor(shapeColor);
                prefManager.setCpenColorIdx(colorIdx);
                if (colorIdx != 11) {
                    prefManager.setCpenBorderColor(shapeColor);
                    prefManager.setCpenBorderColorIdx(colorIdx);
                }
            } else if (fillIdx == 1) {
                prefManager.setCpenBorderColor(shapeColor);
                prefManager.setCpenBorderColorIdx(colorIdx);
            }
        }
    }


    /**
     * UI에 lpen_color, spen_color, cpen_color, lpen_border_color, spen_border_color, cpen_border_color 값들을 세팅
     *
     * @param popoverView
     * @param shapePreviewCanvas
     */
    private void setShapeFillBorderColor(View popoverView, PreviewCanvas shapePreviewCanvas) {

        int shapeIdx = (Integer) prefManager.getShapeTypeIdx();
        int fillIdx = (Integer) prefManager.getFillTypeIdx();


        switch (shapeIdx) {
            case 5 :  // 선
                String lPenColor = prefManager.getLpenColor();
                int lPenColorIdx = prefManager.getLpenColorIdx();

                if (fillIdx == 0) {
//                    int lPenColorIdx = getPref("lpen_color_idx") == null ? 1 : (Integer) getPref("lpen_color_idx");
                    clearColorButton(popoverView, "shape_fill_color");
                    ImageButton colorBtn = (ImageButton) popoverView.findViewWithTag("shape_fill_color_" + lPenColorIdx);
                    colorBtn.setImageResource(R.drawable.btn_option_colorselect);
                }

                // PreviewCanvas에 지정된 색상값 적용시키기..
                shapePreviewCanvas.setShapeFillType(0);
                shapePreviewCanvas.setShapeColor(lPenColor);
                break;

            case 6 :  // 사각형
                String sPenColorRgb = prefManager.getSpenColor();
                String sPenBorderColorRgb = prefManager.getSpenBorderColor();
                int sPenColorIdx = prefManager.getSpenColorIdx();
                int sPenBorderColorIdx = prefManager.getSpenBorderColorIdx();

                if (fillIdx == 0) {  // 채우기 색상타입 일 때..
                    clearColorButton(popoverView, "shape_fill_color");
                    ImageButton colorBtn = (ImageButton) popoverView.findViewWithTag("shape_fill_color_" + sPenColorIdx);
                    colorBtn.setImageResource(R.drawable.btn_option_colorselect);
                } else if (fillIdx == 1) {  // 테두리 색상타입 일 때..
                    clearColorButton(popoverView, "shape_border_color");
                    ImageButton colorBtn = (ImageButton) popoverView.findViewWithTag("shape_border_color_" + sPenBorderColorIdx);
                    colorBtn.setImageResource(R.drawable.btn_option_colorselect);
                }

                shapePreviewCanvas.setShapeFillType(0);
                shapePreviewCanvas.setShapeColor(sPenColorRgb);
                shapePreviewCanvas.setShapeFillType(1);
                shapePreviewCanvas.setShapeColor(sPenBorderColorRgb);
                break;

            case 7 :  // 원
                String cPenColorRgb = prefManager.getCpenColor();
                String cPenBorderColorRgb = prefManager.getCpenBorderColor();
                int cPenColorIdx = prefManager.getCpenColorIdx();
                int cPenBorderColorIdx = prefManager.getCpenBorderColorIdx();

                if (fillIdx == 0) {
                    clearColorButton(popoverView, "shape_fill_color");
                    ImageButton colorBtn = (ImageButton) popoverView.findViewWithTag("shape_fill_color_" + cPenColorIdx);
                    colorBtn.setImageResource(R.drawable.btn_option_colorselect);
                } else if (fillIdx == 1) {
                    clearColorButton(popoverView, "shape_border_color");
                    ImageButton colorBtn = (ImageButton) popoverView.findViewWithTag("shape_border_color_" + cPenBorderColorIdx);
                    colorBtn.setImageResource(R.drawable.btn_option_colorselect);
                }

                shapePreviewCanvas.setShapeFillType(0);
                shapePreviewCanvas.setShapeColor(cPenColorRgb);
                shapePreviewCanvas.setShapeFillType(1);
                shapePreviewCanvas.setShapeColor(cPenBorderColorRgb);
                break;

            default:
                break;
        }

    }


    /**
     * 레이저 포인터 관련...
     * toggleRC(4, 1, true);  pointerIdx = 0
     * toggleRC(4, 2, true);  pointerIdx = 1
     * toggleRC(4, 3, true);  pointerIdx = 2
     */
    private void setLaserTypeBtnEvent(final View popoverView, final CordovaWebView webView, final int laserIdx) {
        popoverView.findViewWithTag("laser_type_" + laserIdx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int laserColorIdx = prefManager.getLaserColorIdx();
//                int laserColorIdx = getPref("laser_color_idx") == null ? 1 : (Integer) getPref("laser_color_idx");
                prefManager.setLaserType(laserIdx);
//                setPref("laser_type", laserIdx);

                clearLaserTypeButton(popoverView, "laser_type");  // 레이저 포인터 타입 버튼의 UI에 대한 초기화
                clearColorButton(popoverView, "laser_pointer_color");

                ImageButton imgBtn = (ImageButton) popoverView.findViewWithTag("laser_pointer_color_" + laserColorIdx);
                imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                ImageButton ltypeBtn = (ImageButton) popoverView.findViewWithTag("laser_type_" + laserIdx);

                String laserIdxStr = laserIdx < 10 ? "0" + laserIdx : laserIdx + "";
                String colorIdxStr = laserColorIdx < 10 ? "0" + laserColorIdx : laserColorIdx + "";

                int imgResource = getResources().getIdentifier("drawable/btn_laser_type" + laserIdxStr + "_color" + colorIdxStr, null, getPackageName());
                ltypeBtn.setBackgroundDrawable(getResources().getDrawable(imgResource));

                webView.sendJavascript("Ctrl.toggleRC(4, " + laserIdx + ", true);");


            }
        });
    }

    /**
     * Set laser pointer color button events..
     *
     * @param popoverView Parent view object
     * @param webView     CordovaWebView object
     * @param colorIdx    laser pointer color index value (1~7)
     *                    Ctrl.__setPenColor(1, 4, "0");
     *                    Ctrl.__setPenColor(2, 4, "0");
     *                    Ctrl.__setPenColor(3, 4, "0");
     *                    Ctrl.__setPenColor(4, 4, "0");
     *                    Ctrl.__setPenColor(5, 4, "0");
     *                    Ctrl.__setPenColor(6, 4, "0");
     *                    Ctrl.__setPenColor(7, 4, "0");
     */
    private void setLaserColorBtnEvent(final View popoverView, final CordovaWebView webView, final int colorIdx) {
        popoverView.findViewWithTag("laser_pointer_color_" + colorIdx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prefManager.setLaserColorIdx(colorIdx);
//                setPref("laser_color_idx", colorIdx);
                int laserIdx = prefManager.getLaserType();
//                int laserIdx = (Integer) getPref("laser_type");

                setLaserPointerPreviewBtn(popoverView);  // 레이저포인터 버튼 UI 세팅..

                clearColorButton(popoverView, "laser_pointer_color");
                ImageButton imgBtn = (ImageButton) v;
                imgBtn.setImageResource(R.drawable.btn_option_colorselect);
                //webView.sendJavascript("Ctrl.toggleRC(4, " + laserIdx + ", true);");
                webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", 4, '0');");
            }
        });
    }


    /**
     * Apply custom Thumb image in SeekBar
     *
     * @param sb       : target SeekBar object
     * @param resource : Thumb image resource
     */
    private void setCustomSeekBarThumb(SeekBar sb, int resource) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas c = new Canvas(bmp);
        String text = Integer.toString(sb.getProgress());
        Paint p = new Paint();
        //float scale = getResources().getDisplayMetrics().density;
        int scaledSize = getResources().getDimensionPixelSize(R.dimen.seek_bar_progress_text);

        p.setTypeface(Typeface.SANS_SERIF);
        p.setAntiAlias(true);
        p.setTextSize(scaledSize);
        p.setColor(0xFF000000);

        int width = (int) p.measureText(text);
        int yPos = (int) ((c.getHeight() / 2) - ((p.descent() + p.ascent()) / 2));

        c.drawText(text, (bmp.getWidth() - width) / 2, yPos, p);

        sb.setThumb(new BitmapDrawable(getResources(), bmp));
    }


    /**
     * Disable checked image in ImageButton source
     *
     * @param parentView Parent view containing ImageButton
     * @param btnType    : "pen_color" / "shape_fill_color" / "shape_border_color"
     */
    private void clearColorButton(View parentView, String btnType) {

        ArrayList<View> childViews = parentView.getTouchables();
        for (View child : childViews) {
            if (child instanceof ImageButton) {
                String tagStr = (String) child.getTag();

                if (tagStr.indexOf(btnType) > -1) {
                    ((ImageButton) child).setImageResource(android.R.color.transparent);
                }
            }
        }
    }


    /**
     * Disable selected image in shape button background
     *
     * @param parentView Parent view containing shape buttons
     * @param tag        Button's tag string ("shape_type", "fill_type"). See popover_shape.xml file.
     */
    private void clearShapeButtonBackground(View parentView, String tag) {
//        ArrayList<View> childViews = parentView.getTouchables();
//        for (View child : childViews) {
//            String tagStr = (String) child.getTag();
//
//            if (tagStr.indexOf(tag) > -1) {
//                ((ImageButton) child).setBackgroundColor(getResources().getColor(android.R.color.transparent));
//
//            }
//        }


//        ((ImageButton)parentView.findViewById(R.id.shape_square)).setBackgroundResource(R.drawable.btn_shape_square);
//        ((ImageButton)parentView.findViewById(R.id.shape_circle)).setBackgroundResource(R.drawable.btn_shape_circle);
//        ((ImageButton)parentView.findViewById(R.id.shape_line)).setBackgroundResource(R.drawable.btn_shape_line);
    }


    private void clearShapeTypeButtonOn(View parentView) {
        ((ImageButton)parentView.findViewById(R.id.shape_square)).setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_shape_square));
        ((ImageButton)parentView.findViewById(R.id.shape_circle)).setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_shape_circle));
        ((ImageButton)parentView.findViewById(R.id.shape_line)).setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_shape_line));
    }


    private void clearFillTypeButtonOn(View parentView) {
        ((ImageButton)parentView.findViewById(R.id.shape_fill)).setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_plane));
        ((ImageButton)parentView.findViewById(R.id.shape_border)).setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_line));
    }


    /**
     * Clear laser mServiceType button background
     *
     * @param parentView Parent view containing laser buttons
     * @param tag        Button's tag string ("laser_type")
     */
    private void clearLaserTypeButton(View parentView, String tag) {
        ArrayList<View> childViews = parentView.getTouchables();
        for (View child : childViews) {
            String tagStr = (String) child.getTag();

            if (tagStr.indexOf(tag) > -1) {
                int idx = Integer.parseInt(tagStr.split("_")[2] == null ? "1" : tagStr.split("_")[2]);
                String idxStr = idx < 10 ? "0" + idx : idx + "";
                int imgResource = getResources().getIdentifier("drawable/btn_laser_type" + idxStr, null, getPackageName());

                //child.setBackground(getResources().getDrawable(imgResource));
                child.setBackground(ResourcesCompat.getDrawable(getResources(), imgResource, context.getTheme()));
            }
        }
    }


    /**
     * Set laser pointer preview button background
     *
     * @param popoverView Parent view containing laser buttons
     */
    private void setLaserPointerPreviewBtn(View popoverView) {
        int laserIdx = prefManager.getLaserType();
        int laserColorIdx = prefManager.getLaserColorIdx();
//        int laserIdx = (Integer) getPref("laser_type");
//        int laserColorIdx = (Integer) getPref("laser_color_idx");

        String laserIdxStr = laserIdx < 10 ? "0" + laserIdx : laserIdx + "";
        String colorIdxStr = laserColorIdx < 10 ? "0" + laserColorIdx : laserColorIdx + "";

        ImageButton ltypeBtn = (ImageButton) popoverView.findViewWithTag("laser_type_" + laserIdx);
        int imgResource = getResources().getIdentifier("drawable/btn_laser_type" + laserIdxStr + "_color" + colorIdxStr, null, getPackageName());
        ltypeBtn.setBackground(getResources().getDrawable(imgResource));

    }


    public void moveRoom(final String roomCode, @Nullable final Bundle extraParam) {
        try {
            String passwd = authInfo.has("passwd") ? authInfo.getString("passwd") : "";
            String tokenStr = "roomcode=" + roomCode + "&passwd=" + passwd;

            AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
            String encryptToken = aesUtilObj.encrypt(tokenStr);

            final RequestParams params = new RequestParams();
            params.put("token", encryptToken);

            String masterCookie = prefManager.getUserCookie();
            String checksumCookie = prefManager.getChecksumCookie();
            RestClient.postWithCookie("room/check.json", masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.d(TAG, response.toString());
                        int result = response.getInt("result");
                        if (result == 0) {
                            isExitProcessing = true;


                            final Intent moveIntent = new Intent(RoomActivity.this, RoomSwitchActivity.class);
                            String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;

                            //moveIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            moveIntent.putExtra("roomurl", roomUrl);
                            moveIntent.putExtra("roomcode", roomCode);
                            if (extraParam != null)
                                moveIntent.putExtra("extra", extraParam);
                            moveIntent.putExtra("deviceid", prefManager.getDeviceId());

                            JsonObject roomSpecParams = new JsonObject();
                            roomSpecParams.addProperty("roomid", roomId);
                            roomSpecParams.addProperty("userno", userNo);
                            roomSpecParams.addProperty("usernm", prefManager.getUserNm());
                            roomSpecParams.addProperty("name", "");
                            roomSpecParams.addProperty("host", "");
                            roomSpecParams.addProperty("port", "");
                            roomSpecParams.addProperty("video", true);
                            roomSpecParams.addProperty("audio", true);
                            roomSpecParams.addProperty("volume", true);
                            roomSpecParams.addProperty("token", prefManager.getZicoAccessToken());

                            moveIntent.putExtra("arguments", new RoomSpec.Builder()
                                    .host(roomSpecParams.get("host").getAsString())
                                    .name(roomSpecParams.get("name").getAsString())
                                    .port(roomSpecParams.get("port").getAsString())
                                    .userNo(roomSpecParams.get("userno").getAsString())
                                    .userNm(roomSpecParams.get("usernm").getAsString())
                                    .roomId(roomSpecParams.get("roomid").getAsString())
                                    .accessToken(roomSpecParams.get("token").getAsString())
                                    .enableVideo(roomSpecParams.get("video").getAsBoolean())
                                    .enableAudio(roomSpecParams.get("audio").getAsBoolean())
                                    .enableAudio(roomSpecParams.get("volume").getAsBoolean())
                                    .build());

                            activity.startActivity(moveIntent);
                            finish();
                        } else if (result == -201 | result == -8001) {
                            // Invalid room
                            Toast.makeText(getContext(), getResources().getString(R.string.login_join_invalidcode), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {

        }
    }


    public void moveMyRoom(@Nullable final Bundle extraParam) {
        isExitProcessing = true;
        String url = "room/createSubRoom.json";
        RequestParams params = new RequestParams();
        params.put("roomid", roomId);
        params.put("deviceid", prefManager.getDeviceId());
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RestClient.postWithCookie(url, masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if ("0".equals(response.getString("result"))) {
                        String roomCode = response.getString("code");

//                        moveRoom(roomCode);

                        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                        Intent moveRoomIntent = new Intent(RoomActivity.this, RoomSwitchActivity.class);
                        //moveRoomIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        moveRoomIntent.putExtra("roomurl", roomUrl);
                        moveRoomIntent.putExtra("roomcode", roomCode);
                        if (extraParam != null)
                            moveRoomIntent.putExtra("extra", extraParam);

                        JsonObject roomSpecParams = new JsonObject();
                        roomSpecParams.addProperty("roomid", roomId);
                        roomSpecParams.addProperty("userno", userNo);
                        roomSpecParams.addProperty("usernm", prefManager.getUserNm());
                        roomSpecParams.addProperty("name", "");
                        roomSpecParams.addProperty("host", "");
                        roomSpecParams.addProperty("port", "");
                        roomSpecParams.addProperty("video", true);
                        roomSpecParams.addProperty("audio", true);
                        roomSpecParams.addProperty("volume", true);
                        roomSpecParams.addProperty("token", prefManager.getZicoAccessToken());

                        moveRoomIntent.putExtra("arguments", new RoomSpec.Builder()
                                .host(roomSpecParams.get("host").getAsString())
                                .name(roomSpecParams.get("name").getAsString())
                                .port(roomSpecParams.get("port").getAsString())
                                .userNo(roomSpecParams.get("userno").getAsString())
                                .userNm(roomSpecParams.get("usernm").getAsString())
                                .roomId(roomSpecParams.get("roomid").getAsString())
                                .accessToken(roomSpecParams.get("token").getAsString())
                                .enableVideo(roomSpecParams.get("video").getAsBoolean())
                                .enableAudio(roomSpecParams.get("audio").getAsBoolean())
                                .enableVolume(roomSpecParams.get("volume").getAsBoolean())
                                .build());

                        activity.startActivity(moveRoomIntent);
                        finish();

                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getContext(), getResources().getString(R.string.login_join_invalidcode), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void removeInviteRoom(final String seqNo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isExitProcessing) return;
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.loadUrl("javascript:Notify.Invite.remove('" + userNo + "', " + seqNo + ");");
            }
        });
    }




    /**
     * 룸 로딩 시작 처리.. (이전 필기 불러오기 기능에서 호출됨)
     * @param type
     */
    public void startRoomLoading(final String type) {
        Log.d(TAG, "startRoomLoading");
        isLoad = true;
        //bookLoading.start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isExitProcessing) return;
                View loadingLayer = activity.findViewById(R.id.loading_layer);

                TextView loadingTxt = (TextView) loadingLayer.findViewById(R.id.txt_canvas_loading);
                Button cancelLoadingBtn = (Button) loadingLayer.findViewById(R.id.cancel_room_loading);
                if(TextUtils.equals(type, "page")) {
                    Glide.with(activity).load(R.drawable.gif_multi_page).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView) findViewById(R.id.room_loading_img));
                    loadingTxt.setText(getString(R.string.splash_page));
                    cancelLoadingBtn.setVisibility(View.GONE);
                } else {
                    Glide.with(activity).load(R.drawable.gif_create_class).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView) findViewById(R.id.room_loading_img));
                    loadingTxt.setText(getString(R.string.splash_load));
                    cancelLoadingBtn.setVisibility(View.VISIBLE);
                }
                loadingLayer.setVisibility(View.VISIBLE);


            }
        });
    }


    /**
     * 룸 로딩 종료 처리.. (블러 뷰 제거.. Orientation 고정 모드 해제..)
     */
    public void finishRoomLoading() {
        Log.d(TAG, "finishRoomLoading");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMoveRoomBtn();
            }
        });


        isLoad = false;
        //bookLoading.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                View loadingLayer = activity.findViewById(R.id.loading_layer);
                if (loadingLayer.isShown()) {
                    Animation loaderAni = new AlphaAnimation(1.0f, 0.0f);
                    loaderAni.setDuration(300);
                    loadingLayer.setAnimation(loaderAni);
                    loadingLayer.setVisibility(View.GONE);
                    Glide.clear((ImageView) findViewById(R.id.room_loading_img));
                    //actionBar.show();
                }
            }
        });

        Intent intent = getIntent();

        // 판서형 질문을 받고 자신의 보드로 이동했을 때 화면구성에 필요한 데이터를 꺼내어 문제 답안 창을 출력함 - 2016.12.23
        Bundle pollExtra = intent.hasExtra("extra") ? intent.getBundleExtra("extra") : null;
        if (pollExtra != null) {
            CordovaWebView webview = mWebViewFragment.getCordovaWebView();
            String pollNo = pollExtra.getString("pollno");
            String timeLimit = pollExtra.getString("timelimit");
            String isCountdown = pollExtra.getString("iscountdown");
            String imageMap = pollExtra.getString("image");
            if(TextUtils.equals(roomCode, masterRoomSeqNo)) {   // 내 방에다가 폴 파일을 룸파일로 등록함
                Log.d(TAG, "readyToAnswerDrawingQuestion 호출..");
                webview.sendJavascript("PollCtrl.Action.Attender.readyToAnswerDrawingQuestion('" + pollNo + "');");
            }

            webview.sendJavascript("PollCtrl.startDrawingPollAnswer('" + pollNo + "', '" + timeLimit + "', " + isCountdown + ", '" + imageMap + "');");
        }
    }


    public void closePopover(PopupWindow pw) {
        if(pw != null) {
            pw.dismiss();
        }
        mPopupWindow = null;
        ((RelativeLayout)findViewById(R.id.popover_container)).setVisibility(View.GONE);
    }


    public void openPopover(){
        ((RelativeLayout)findViewById(R.id.popover_container)).setVisibility(View.VISIBLE);
    }


    public JSONObject getUserInfo() {
        JSONObject resultObj = null;
        try {
            if (TextUtils.equals(mServiceType, "knowlounge")) {
                String masterCookie = prefManager.getUserCookie();
                if (!TextUtils.isEmpty(masterCookie)) {
                    // 로그인한 유저일 때..
                    AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
                    String result = aesUtilObj.decrypt(masterCookie);
                    result = URLDecoder.decode(result, "utf-8");
                    Log.d(TAG, "decrypt result : " + result);
                    resultObj = new JSONObject(result);
                    resultObj.put("cookie", "FBMMC=" + prefManager.getUserCookie() + "&" + "FBMCS=" + prefManager.getChecksumCookie());
                } else {
                    // 게스트 일 때..
                    resultObj = new JSONObject();
                    resultObj.put("userno", deviceIdForGuest);
                    resultObj.put("userid", deviceIdForGuest);
                    resultObj.put("email", "");
                    resultObj.put("guest", guestNm);
                }
            } else if (TextUtils.equals(mServiceType, "premium")) {
                // 프리미엄 유저일 때..
                String masterCookie = prefManager.getPremiumMasterCookie();
                String checksumCookie = prefManager.getPremiumChecksumCookie();

                // 쿠키 Decrypt 하기..
                AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
                String result = aesUtilObj.decrypt(masterCookie);
                result = URLDecoder.decode(result, "utf-8");
                Log.d(TAG, "Premium Cookie decrypt result : " + result);
                resultObj = new JSONObject(result);
                resultObj.put("cookie", "FBMMC=" + masterCookie + "&" + "FBMCS=" + checksumCookie);
            }

            resultObj.put("locale", AndroidUtils.getLanguageCode());  // locale 정보 추가..
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObj;
    }


    /**
     * @param sValue 내용
     * @param nSize  맞출 길이
     * @return
     * @description 5자리 이하인 roomCode는 0 을 앞붙어 붙인다.
     */
    private String makeFixedSizeInt(String sValue, int nSize) {
        String strValue = sValue;

        int nCurSize = strValue.length();
        if (nCurSize >= nSize)
            return sValue;

        int diff = nSize - nCurSize;
        StringBuffer sb = new StringBuffer(nSize);

        for (int i = 0; i < diff; i++)
            sb.append("0");

        sb.append(strValue);

        String rtn = sb.toString().trim();
        sb.setLength(0);
        sb = null;
        return rtn;
    }


    // RightMenu --> User --> UserList 관련 ----start----
    // 참여자 리스트 관리
    @Override
    public void addRoomUserListHandler(final JSONArray userArr) {
        Log.d(TAG, "addRoomUserList");

        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int len = userArr.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject obj = userArr.getJSONObject(i);
                        addUserList(obj);
                    }
                    if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                        mNotiChangeCallBack.onNotiChangeData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void addUserList(final JSONObject obj)  throws JSONException {
        String userNoItem = obj.getString("userno");

//        // 중복 유저 체크
//        if (userList.size() > 0) {  // 접속중인 유저가 있을 때..
//            for (User user : userList) {
//                String existUserNo = user.getUserNo();
//                if (existUserNo.equals(userNo)) {  // 유저 리스트에 중복되어 추가되지 않도록 예외처리..
//                    return;
//                }
//            }
//            if ("1".equals(creatorFlag)) {
//                userList.add(0, userItem(obj));
//            } else {
//                userList.add(userItem(obj));
//            }
//            return;
//        }
//        obj.put("scope", "board");
        if (userNo.equals(userNoItem))
            userList.add(userItem(obj));
//        mRoomUserPresenter.onAddUser(obj);
    }


    private User userItem(final JSONObject obj) throws JSONException {
        String userId = obj.getString("userid");
        String userNm = obj.getString("usernm");
        String userNo = obj.getString("userno");
        String userType = (obj.has("usertype") || !obj.isNull("usertype")) ? obj.getString("usertype") : "0";

        String creatorFlag = obj.has("creator") ? obj.getString("creator") : "0";
        String masterFlag  = obj.has("master") ? obj.getString("master") : "0";
        String guestFlag   = obj.has("guest") ? obj.getString("guest") : "0";
        String thumbnail   = obj.has("thumbnail") ? obj.getString("thumbnail") : "";

        Log.d(TAG, "[addUserList] userNm : " + userNm + ", creatorFlag : " + creatorFlag + ", masterFlag : " + masterFlag + ", guestFlag : " + guestFlag);

        User newUser = new User(userId, userNm, userNo, userType, creatorFlag, masterFlag, guestFlag, thumbnail, android.R.drawable.ic_menu_search);
        return newUser;
    }


    @Override
    public void removeUserListHandler(final JSONObject obj) {
        Log.d(TAG, "<removeRoomUserList / ZICO>");
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    removeUserList(obj);
                    if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                        mNotiChangeCallBack.onNotiChangeData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void removeUserList(final JSONObject obj) throws JSONException {
        String userNo = obj.getString("userno");
        User removeUser = null;
        for (User user : userList) {
            if (userNo.equals(user.getUserNo())) {
                removeUser = user;
                break;
            }
        }
        userList.remove(removeUser);
    }


    //클래스 참여자 리스트 관리
    @Override
    public void addClassUserListHandler(final JSONArray arr) {
        Log.d(TAG, "<addClassUserList> size : " + arr.length());
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int len = arr.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        addClassUser(obj);
                    }
                    if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                        mNotiChangeCallBack.onNotiChangeData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void addClassUser(JSONObject obj) throws JSONException {
        Log.d(TAG, "<addClassUser / ZICO> obj : " + obj.toString());

        String userNo = obj.getString("userno");
        String userId = obj.getString("userid");
        boolean isMaster = masterUserNo.equals(userNo) ? true : false;

        if (!obj.has("master"))
            obj.put("master", isMaster);
        obj.put("creator", userNo.equals(creatorNo) ? 1 : 0);
        obj.put("guest", userNo.equals(userId));

        for (RoomUser user : classUserList) {
            String existUserNo = user.getUserNo();
            if (existUserNo.equals(userNo)) {  // 수업 유저 리스트에 중복되어 추가되지 않도록 예외처리..
                return;
            }
        }

        String teacherUserNo = getClassMasterUserNo();
        obj.put("scope", "class");

        mRoomUserPresenter.onAddUser(obj);

        if (teacherUserNo.equals(userNo)) {
            ArrayList<RoomUser> tmpList = new ArrayList<RoomUser>();
            if (classUserList.size() > 0) {
                tmpList = (ArrayList<RoomUser>)classUserList.clone();
                classUserList.clear();
                classUserList.add(0, classUserItem(obj));
                classUserList.addAll(tmpList);
                tmpList.clear();
            } else {
                classUserList.add(classUserItem(obj));
//                mRoomUserPresenter.onAddUser(obj);
            }
            return;
        }
        classUserList.add(classUserItem(obj));
    }


    private RoomUser classUserItem(JSONObject userObj) throws JSONException {
        Log.d(TAG, "<classUserItem> userObj : " + userObj.toString());
        int videoIndex;
        //int videoIndex = userObj.getInt("video_index");
        //int videoIndex = userObj.getInt("video_index");
        String userNo = userObj.getString("userno");
        String userId = userObj.getString("userid");
        String userNm = userObj.getString("usernm");
        String userType = (userObj.has("usertype") || !userObj.isNull("usertype")) ? userObj.getString("usertype") : "0";
        String thumbnail = userObj.getString("thumbnail");

        String userScope = "class";

        String connectedRoomId = userObj.has("connected_roomid") ? userObj.getString("connected_roomid") : "";
        String connectedRoomTitle = userObj.has("connected_roomtitle") ? userObj.getString("connected_roomtitle") : "";
        String connectedRoomSeparate = userObj.has("connected_roomseparate") ? userObj.getString("connected_roomseparate") : "";

        boolean isSeparateRoom = "1".equals(connectedRoomSeparate) ? true : false;

        int creator = userObj.has("creator") ? userObj.getInt("creator") : 0;
        boolean isCreator = creator == 1 ? true : false;
        boolean isMaster  = userObj.has("master") ? userObj.getBoolean("master") : masterUserNo.equals(userNo) ? true : false;
        boolean isGuest   = userObj.has("guest") ? userObj.getBoolean("guest") : false;

        String userRoomid = (userObj.has("roomid") || !userObj.isNull("roomid")) ? userObj.getString("roomid") : "";
        String userRoomSeqNo = userObj.has("seqno") ? userObj.getString("seqno") : "";

        Log.d(TAG, "<classUserItem> userRoomSeqNo : " + userRoomSeqNo);

        //guestFlag = userObj.has("isguest") ? userObj.getString("isguest") : userNo.equals(userId) ? "1" : "0";
        if (isGuest) {
            thumbnail = "";
            userType = "0";
        }

        if (isCreator) {
            videoIndex = 0;
        } else {
            videoIndex = classUserList.size();
        }

        RoomUser newUser = new RoomUser(videoIndex, userId, userNo, userNm, userType, thumbnail, userScope, userRoomid, userRoomSeqNo, isCreator, isMaster, isGuest, connectedRoomId, connectedRoomTitle, isSeparateRoom);
        //ClassUser newUser = new ClassUser(userId, userNm, userNo, userType, thumbnail, userRoomid, userRoomSeqNo);
        return newUser;
    }


    @Override
    public void removeClassUserListHandler(final JSONObject obj) {
        Log.d(TAG, "<removeClassUserList> obj : " + obj.toString());
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    removeClassUser(obj);
                    if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                        mNotiChangeCallBack.onNotiChangeData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void removeClassUser(final JSONObject obj) throws JSONException {
        String userNo = obj.getString("userno");
        RoomUser removeUser = null;
        for (RoomUser user : classUserList) {
            if (userNo.equals(user.getUserNo())) {
                removeUser = user;
                break;
            }
        }
        classUserList.remove(removeUser);
        mRoomUserPresenter.onRemoveUser(userNo);
    }


    // 미참여자 리스트 관리
    @Override
    public void addOtherUserListHandler(final JSONArray arr) {
        Log.d("아좀","addOtherUserList");
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int len = arr.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        addOtherUser(obj);
                    }
                    if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                        mNotiChangeCallBack.onNotiChangeData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void addOtherUser(JSONObject obj) throws JSONException {
        String userNo = obj.getString("userno");
        String userId = obj.getString("userid");
        String userNm = obj.getString("usernm");
        String userType = obj.getString("usertype");
        String email = obj.getString("email");
        String thumbnail = obj.getString("thumbnail");

        //참여자 리스트 중복체크
        for (User userData: userList) {
            if(userData.getUserNo().equals(userNo))
                return;
        }

        //중복체크
        for (OtherUser data: otherUserList) {
            if (data.getUserNo().equals(userNo))
                return;
        }

        OtherUser newOtherUser = new OtherUser(userNo, userId, userNm, userType, email, thumbnail);
        otherUserList.add(newOtherUser);
    }


    @Override
    public void onRemoveNotAttendee(final String userNo) {
        Log.d("아좀", "removeOtherList");
        try {
            OtherUser removeNotAttendeeUser = null;
            for (OtherUser user : otherUserList) {
                if (userNo.equals(user.getUserNo())) {
                    removeNotAttendeeUser = user;
                    break;
                }
            }
            otherUserList.remove(removeNotAttendeeUser);
            if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW  && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                mNotiChangeCallBack.onNotiChangeData();
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }


    //진행자 권한 변경 관리
    @Override
    public void changeMasterHandler(final JSONObject obj) {
        Log.d(TAG, "<changeMasterHandler> obj : " + obj.toString());

        try {
            String prevMasterUserId = mRoomUserPresenter.getCurrentMasterId();
            String newMasterUserId = obj.getString("userid");


            // 내가 진행자였다가 변경되는 케이스이다.
            if (masterFlag && !userId.equals(newMasterUserId)) {
                masterFlag = false;
                masterUserId = newMasterUserId;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearRightBtnSelected();
                        closePopover(mPopupWindow);
                    }
                });
            }

            // 내가 진행자가 되는 케이스이다.
            if (!masterFlag && userId.equals(newMasterUserId)) {
                masterFlag = true;
                masterUserId = newMasterUserId;
            }

            mRoomUserPresenter.setMasterUser(prevMasterUserId, newMasterUserId);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVideoEvents.onMasterChange();
                }
            }, 500);



            if (userList.size() == 0)
                return;

            // 현재 마스터 유저 찾기..
            for (User user : userList) {
                if("1".equals(user.getMaster())) {
                    user.setMaster("0");
                }
            }

            // 새로운 마스터 유저 찾기..
            for (User user : userList) {
                if (newMasterUserId.equals(user.getUserId())) {
                    user.setMaster("1");

                    if (user.getUserId().equals(userId)) {
                        setMasterFlag(true, user.getUserId());
                    } else {
                        setMasterFlag(false, user.getUserId());
                    }
                }
            }

            if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW  && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
                mNotiChangeCallBack.onNotiChangeData();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //TODO RightMenu --> User --> UserList 관련 ----end----


    //TODO RightMenu --> Community --> Chatting && Commnet 관련 ----start----
    //채팅 유저 리스트
    private void addDefaultChatUserItem() {
        ChatUser user = new ChatUser("", getResources().getString(R.string.canvas_chat_all), "", "", true);
        boolean isExist = false;
        if (chatUserList != null) {
            Iterator<ChatUser> iter = chatUserList.iterator();
            while (iter.hasNext()) {
                ChatUser chat = iter.next();
                String chatUserNo = chat.getUserNo();

                if (TextUtils.isEmpty(chatUserNo)) {
                    isExist = true;
                }
            }
        }

        if (!isExist) {
            chatUserList.add(0, user);
            classChatUserList.add(0, user);
        }
    }


    @Override
    public void addChatUserListHandler(final JSONArray arr, final String myUserNo) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                addChatUserList(arr, myUserNo, "room");
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }


    @Override
    public void addClassChatUserListHandler(final JSONArray arr, final String myUserNo) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                addChatUserList(arr, myUserNo, "class");
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }


    private void addChatUserList(JSONArray arr, String myUserNo, String chatType) {
        try {
            boolean isChanged = false;
            boolean isExisted = false;
            int arrSize = arr == null ? 0 : arr.length();
            for (int i=0; i<arrSize; i++) {

                JSONObject obj = arr.getJSONObject(i);
                String userId    = obj.has("userid") ? obj.getString("userid") : "0";
                String userNm    = obj.has("usernm") ? obj.getString("usernm") : "0";
                String userNo    = obj.has("userno") ? obj.getString("userno") : "0";
                String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : "0";

                ArrayList<ChatUser> list = TextUtils.equals(chatType, "room") ? chatUserList : classChatUserList;
                // ChatUser user = new ChatUser(userId, userNm, userNo, thumbnail);
                if (list != null) {
                    Iterator<ChatUser> iter = list.iterator();
                    while (iter.hasNext()) {
                        ChatUser chat = iter.next();
                        String chatUserNo = chat.getUserNo();
// Log.d("addChatUserList", "userNo : " + userNo + ", myUserNo : " + myUserNo + ", chatUserNo : " + chatUserNo);
                        // 이미 있는 유저이거나 나인경우 skip 한다.
                        if (userNo.equals(chatUserNo) || userNo.equals(myUserNo) || TextUtils.isEmpty(userNo)){
                            isExisted = true;
                        }
                    }

                    if (!isExisted) {
                        ChatUser user = new ChatUser(userId, userNm, userNo, thumbnail,false);
                        list.add(user);
                        isChanged = true;
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("addChatUserList", e.getMessage());
        }
    }


    @Override
    public void removeChatUserListHandler(final JSONArray arr) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                removeChatUserList(arr, "room");
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }



    @Override
    public void removeClassChatUserListHandler(final JSONArray arr) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                removeChatUserList(arr, "class");
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }

    private void removeChatUserList(JSONArray arr, String chatType) {
        try {
            int removeIdx = -1;
            // 삭제는 무조건 한명씩 온다.
            if (arr != null && arr.length() > 0){
                String userNo = arr.getJSONObject(0).getString("userno");

                ArrayList<ChatUser> list = TextUtils.equals(chatType, "room") ? chatUserList : classChatUserList;
                if (list != null){
                    int size = list == null ? 0 : list.size();
                    for (int i=0; i<size; i++){
                        ChatUser chat = list.get(i);
                        String chatUserNo = chat.getUserNo();
                        if (userNo.equals(chatUserNo)) {
                            removeIdx = i;
                        }
                    }
                }

                if (removeIdx > -1) {
                    list.remove(removeIdx);
                }
            }

        } catch(Exception e) {
            Log.d("removeChatUserList", e.getMessage());
        }
    }


    @Override
    public void addChatDataHandler(final JSONArray arr) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                addChatData(arr, "room");
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }


    @Override
    public void addClassChatDataHandler(final JSONArray arr) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                addChatData(arr, "class");
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW  && currentCommunityChild == GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }


    public void addChatData(JSONArray arr, String chatType) {
        try {
            int len = arr.length();
            for (int i=0; i<len; i++) {
                JSONObject obj = arr.getJSONObject(i);
                Log.d(TAG, "Chatting Data is " + obj.toString());

                String type      = obj.has("mServiceType") ? obj.getString("mServiceType") : "0";               // 0-전체 채팅 : 1-귓속 채팅
                String mode      = obj.has("mode") ? obj.getString("mode") : "0";               // 0-보낸 메세지 : 1-받은 메세지
                String sender    = obj.has("sender") ? obj.getString("sender") : "0";           // 보낸 사람
                String receiver  = obj.has("receiver") ? obj.getString("receiver") : "0";       // 받는 사람 type이 0이면 공백
                String senderNm  = obj.has("usernm") ? obj.getString("usernm") : "0";           // 보낸유저 이름
                String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : "0";     // 보낸유저 썸네일
                String content   = obj.has("msg") ? obj.getString("msg") : "0";                 // 보낸 메세지 내용
                String cDatetime = obj.has("cdatetime") ? obj.getString("cdatetime") : "0";     // 메세지 받은 시간

                Chat newChat = new Chat(type, mode, sender, receiver, senderNm, thumbnail, content, cDatetime);

                if (TextUtils.equals(chatType, "room")) {
                    RoomActivity.chatList.add(newChat);
                    if ("1".equals(mode)) {
                        updateChattingBadge();
                    }
                } else if (TextUtils.equals(chatType, "class")) {
                    RoomActivity.classChatList.add(newChat);
                    if ("1".equals(mode)) {
                        updateClassChattingBadge();
                    }
                }

            }
        } catch(JSONException e) {
            Log.e("addChatUserList", e.getMessage());
        }
    }

    @Override
    public void addCommentListHandler(final JSONArray arr, final boolean isInit) {
        rightMenuHandler.post(new Runnable() {
            @Override
            public void run() {
                addComment(arr, isInit);
                if(isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW ) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }


    public void addComment(JSONArray arr, boolean isInit) {
        try {
            Log.d("addCommentList", arr.toString());
            int len = arr.length();
            for (int i = 0; i < len; i++) {
                JSONObject obj = arr.getJSONObject(i);
                String commentNo = obj.has("commentno") ? obj.getString("commentno") : "0";
                String userId = obj.has("userid") ? obj.getString("userid") : "0";
                String userNm = obj.has("usernm") ? obj.getString("usernm") : "0";
                String userNo = obj.has("userno") ? obj.getString("userno") : "0";
                String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : "0";
                String cdatetime = obj.has("cdatetime") ? obj.getString("cdatetime") : "0";
                String content = obj.has("content") ? obj.getString("content") : "0";
                CommentItem newComment = new CommentItem(commentNo, userId, userNm, userNo, thumbnail, cdatetime, content);
                if (len == 1) {
                    commentList.add(0, newComment);
                } else {
                    commentList.add(newComment);
                }
                if (!isInit) {
                    updateCommentBadge();
                }
            }

        } catch (JSONException e) {
            Log.d("setCommentList", e.getMessage());
        }
    }

    /**
     * Commu
     * @param commentNo
     */
    @Override
    public void removeCommentListHandler(final String commentNo) {
//        rightMenuHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                removeComment(commentNo);
//                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW) {
//                    mNotiChangeCallBack.onNotiChangeData();
//                }
//            }
//        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeComment(commentNo);
                if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW) {
                    mNotiChangeCallBack.onNotiChangeData();
                }
            }
        });
    }


    private void removeComment(String commentNo) {
        for (CommentItem comment : commentList) {

            if (commentNo.equals(comment.getCommentNo())) {
                commentList.remove(comment);
                break;
            }
        }
    }
    //TODO RightMenu --> Community --> Chatting && Commnet 관련 ----end----


    @Override
    public void setIsBookmark(final boolean isEnable) {
        roomTools.onBookmark(isEnable);
    }

    @Override
    public boolean getIsBookmark() {
        return isFavoriteOn;
    }

    @Override
    public String getRoomTitle() {
        return roomTitle;
    }

    @Override
    public JSONObject getBgInfo() {
        return bgInfo;
    }


    /**
     * 수업 제목 수정 - RoomInfoDialogFragment에서 호출함..
     * 현재 방의 설정값 정보도 같이 보내야 함..
     * @param roomTitleParam
     */
    @Override
    public void setRoomTitle(final String roomTitleParam) {
        if (creatorFlag) {
            if (roomTitle.equals(roomTitleParam)) {  // 수업 제목이 동일하면 업데이트 로직을 호출하지 않음..
               return;
            } else {
                roomTitle = roomTitleParam;
                prefManager.setRoomTitle(roomTitleParam);
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (isExitProcessing) return;
                        String convertStr = roomTitleParam.replaceAll("'", "\\\\'");
                        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                        webView.sendJavascript("Ctrl.Modal.updateTitle('" + authInfo.toString() + "', '" + convertStr + "');");
                    }
                });
            }
        } else {
            if (!roomTitle.equals(roomTitleParam)) {
                Toast.makeText(getContext(), getString(R.string.toast_authority_no), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }


    @Override
    public void importCanvas() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isExitProcessing) return;
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.saveRoomCanvas();");
            }
        });
    }

    @Override
    public void shareRoom() {
        String sendMsg = getResources().getString(R.string.share_message);
        String shareUri = getResources().getString(R.string.share_uri) + roomCode;
        //Todo URL 넣어야함
        Intent msg = new Intent(Intent.ACTION_SEND);
        msg.addCategory(Intent.CATEGORY_DEFAULT);
        msg.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title));
        msg.putExtra(Intent.EXTRA_TEXT, sendMsg + shareUri);
        msg.putExtra(Intent.EXTRA_TITLE, getResources().getString(R.string.global_share_title));
        msg.setType("text/plain");

        startActivity(Intent.createChooser(msg, getResources().getString(R.string.canvas_share)));
    }

    @Override
    public void openInviteView() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            closePopover(mPopupWindow);
        }
        isOpenRightMenu = true;
        if (!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_INVITE_VIEW,0, false);
    }

    /**
     * Left Drawer 인터페이스 구현
     **/
    @Override
    public void setRoomTitleFromLeftDrawer(final String roomTitleParam) {
        if (creatorFlag) {
            if (TextUtils.isEmpty(roomTitleParam)) {
                Toast.makeText(getContext(), getResources().getString(R.string.toast_room_nulltitle), Toast.LENGTH_SHORT).show();
                return;
            }
            if (roomTitle.equals(roomTitleParam)) {
                return;
            } else {
                roomTitle = roomTitleParam;
                prefManager.setRoomTitle(roomTitleParam);
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        if(isExitProcessing){return;}
//                        String convertStr = roomTitleParam.replaceAll("'", "\\\\'");
//                        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
//                        webView.sendJavascript("Ctrl.Modal.updateTitle('" + convertStr + "');");
//                    }
//                });

                if (isExitProcessing) return;
                String convertStr = roomTitleParam.replaceAll("'", "\\\\'");
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.Modal.updateTitle('" + authInfo.toString() + "', '" + convertStr + "');");
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_authority_no), Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    public void onPenTool() {
        // 펜 툴
        roomTools.onPenTool();
    }


    @Override
    public void onEraserTool() {
        // 지우개 툴
        roomTools.onEraserTool();
    }


    @Override
    public void onShapeTool() {
        // 도형 툴
        roomTools.onShapeTool();
    }


    @Override
    public void onTextTool() {
        // 텍스트 툴
        roomTools.onTextTool();
    }


    @Override
    public void onPhotoTool() {
        // 사진 툴
        roomTools.onPhotoTool();
    }

    @Override
    public void onImageTool() {
        // 이미지 툴
        roomTools.onImageTool();
    }


    @Override
    public void onPdfTool() {
        // PDF 툴
        roomTools.onPdfTool();
    }


    @Override
    public void onUndoTool() {
        // Undo 툴
        roomTools.onUndoTool();
    }


    @Override
    public void onRedoTool() {
        // Redo 툴
        roomTools.onRedoTool();
    }


    @Override
    public void onDeleteAll() {
        roomTools.onDeleteAll();
    }


    @Override
    public void onMemoTool() {
        roomTools.onMemoTool();
    }


    @Override
    public void onHandMode() {
        // 선택모드 툴
        roomTools.onHandMode();
    }

    @Override
    public void onLaserTool() {
        // 레이저 포인터 툴
        roomTools.onLaserTool();
    }


    @Override
    public void onZoomIn() {
        // 확대 툴
        roomTools.onZoomIn();
    }


    @Override
    public void onZoomOut() {
        // 축소 툴
        roomTools.onZoomOut();
    }


    @Override
    public void onPoll() {
        // 폴 생성하기
        roomTools.onPoll(null);
    }


    @Override
    public void onPollTmpList(){
        // 폴 템플릿 리스트
        roomTools.onPollTmpList();
    }


    @Override
    public void onPollCompletedList() {
        // 완료된 폴 리스트
        roomTools.onPollCompletedList();
    }


    //Todo 창하쓰
    @Override
    public void onUserList() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            closePopover(mPopupWindow);
            //break;
        }
        isOpenRightMenu = true;
        if (!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW, false);
    }


    //Todo 창하쓰
    @Override
    public void onUserVideo() {
        try {
            if (TextUtils.equals(authInfo.getString("vcamopt"), "1") && (!creatorFlag && !TextUtils.equals(userType, "2"))) {
                Toast.makeText(getContext(), getString(R.string.toast_authority_no), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {

        }
        View videoView = getWindow().findViewById(R.id.user_video_container);
        if (videoView.isShown()) {
            ((ImageView)mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video);
            videoView.setVisibility(View.GONE);
//            videoCallback.setVideoStreamOnOff(false);
        } else {
            ((ImageView)mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video_on);
            videoView.setVisibility(View.VISIBLE);
//            videoCallback.setVideoStreamOnOff(true);
        }
    }



    @Override
    public void onChatting() {
        Log.d(TAG, "onChatting");
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            closePopover(mPopupWindow);
            //break;
        }
        isOpenRightMenu = true;
        if(!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW, GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW, false);
    }



    @Override
    public void onComment() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            closePopover(mPopupWindow);
            //break;
        }
        isOpenRightMenu = true;
        if(!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW, 0, false);
    }



    @Override
    public void onInvite() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            closePopover(mPopupWindow);
        }
        isOpenRightMenu = true;
        if(!mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_INVITE_VIEW, 0, false);

    }
    //Todo 창하쓰

    @Override
    public void onBookmark(boolean bookmarkFlag) {
        // 북마크 (즐겨찾기)
        roomTools.onBookmark(bookmarkFlag);
    }

    @Override
    public void onSaveCanvas(){
        roomTools.onSaveCanvasTool();
    }

    @Override
    public void onShare() {
        // 공유하기
        roomTools.onShare();
    }


    /**
     * 수업 나가기
     */
    @Override
    public void onExitRoom() {
        setAlertDialog("0", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_exit_body1));
    }


    /**
     * 룸 좌메뉴에 룸 환경설정 화면 띄우기
     */
    @Override
    public void onRoomSetting() {
        if(!mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.openDrawer(mLeftDrawerContent);
            findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);
        }

        AndroidUtils.keyboardHide(this);

        Bundle bundle = new Bundle();
        bundle.putString("roomCode", roomCode);

        ConfigFragment configFragment = new ConfigFragment();
        configFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.left_drawer_panel_room, configFragment, "ConfigFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }
    /**
     * 룸 좌메뉴에 룸 설정 수정화면 띄우기
     */
    public void openRoomSettingDetail(int type, String paramStr) {
        Bundle argument = new Bundle();
        if (type == GlobalConst.VIEW_CLASSCONFIG_TITLE) {
            argument.putInt("mServiceType", GlobalConst.VIEW_CLASSCONFIG_TITLE);
            argument.putString("title", paramStr);
        } else {
            argument.putInt("mServiceType", GlobalConst.VIEW_CLASSCONFIG_DESC);
            argument.putString("desc", paramStr);
        }

        ConfigEditFragment configEditFragment = new ConfigEditFragment();
        configEditFragment.setArguments(argument);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.left_drawer_panel_room, configEditFragment, "ConfigEditFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    /**
     * 룸 좌메뉴에 스타샵 화면 띄우기
     */
    public void openStarShop() {

        if(!mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.openDrawer(mLeftDrawerContent);
        }
        findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);

        StarShopFragment starShopFragment = new StarShopFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.left_drawer_panel_room, starShopFragment, "StarShopFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void pollONOFF() {
        mPollSubMenu.setVisibility(View.GONE);
        isSubMenuCheck = false;
    }


    /**
     * toggleSubMenuLayer - 서브메뉴가 존재하는 우측 버튼에 대한 토글러
     * @param layerName : 서브메뉴 레이어 이름
     * @param btnName : 메뉴 버튼 이름
     * @param isExistOnOff : 메뉴 버튼의 on / off 모드 존재 여부
     */
    private void toggleSubMenuLayer(String layerName, String btnName, boolean isExistOnOff) {
        //closeSubMenuLayer();

        ScrollView subMenuLayout = (ScrollView)findViewById(getResources().getIdentifier(layerName, "id", getPackageName()));
        ImageView menuBtn = (ImageView) findViewById(getResources().getIdentifier(btnName, "id", getPackageName()));

        if (subMenuLayout.isShown()) {
            if (isDevicePhone)
                menuBtn.setImageResource(getResources().getIdentifier(btnName.substring(0,btnName.length()-6), "drawable", getPackageName()));
            else
                menuBtn.setImageResource(getResources().getIdentifier(btnName, "drawable", getPackageName()));
            subMenuLayout.setVisibility(View.GONE);
        } else {
            if (isExistOnOff) {
                if (isDevicePhone)
                    menuBtn.setImageResource(getResources().getIdentifier(btnName.substring(0,btnName.length()-6) + "_on", "drawable", getPackageName()));
                else
                    menuBtn.setImageResource(getResources().getIdentifier(btnName + "_on", "drawable", getPackageName()));
            }
            subMenuLayout.setVisibility(View.VISIBLE);
        }
    }


    private void enableRightBtn(String btnName) {
        ImageButton menuBtn = (ImageButton)findViewById(getResources().getIdentifier(btnName, "id", getPackageName()));
        if (isDevicePhone)
            menuBtn.setImageResource(getResources().getIdentifier(btnName.substring(0,btnName.length()-6) + "_on", "drawable", getPackageName()));
        else
            menuBtn.setImageResource(getResources().getIdentifier(btnName + "_on", "drawable", getPackageName()));
    }

    private void disableRightBtn(String btnName) {
        ImageButton menuBtn = (ImageButton)findViewById(getResources().getIdentifier(btnName, "id", getPackageName()));
        if (isDevicePhone)
            menuBtn.setImageResource(getResources().getIdentifier(btnName.substring(0,btnName.length()-6), "drawable", getPackageName()));
        else
            menuBtn.setImageResource(getResources().getIdentifier(btnName, "drawable", getPackageName()));
    }


    /**
     * 서브 메뉴 레이아웃 클리어..
     */
    private void closeSubMenuLayer() {
        mTextSubMenu.setVisibility(View.GONE);
        findViewById(R.id.layer_document_submenu).setVisibility(View.GONE);
        mPollSubMenu.setVisibility(View.GONE);
    }


    private void closeSubMenuLayer(String currentLayer) {
        if (currentLayer.equals("layer_text_submenu")) {
            findViewById(R.id.layer_document_submenu).setVisibility(View.GONE);
            mPollSubMenu.setVisibility(View.GONE);
        } else if (currentLayer.equals("layer_document_submenu")) {
            findViewById(R.id.layer_text_submenu).setVisibility(View.GONE);
            mPollSubMenu.setVisibility(View.GONE);
        } else if (currentLayer.equals("layer_poll_submenu")) {
            mTextSubMenu.setVisibility(View.GONE);
            findViewById(R.id.layer_document_submenu).setVisibility(View.GONE);
        }
    }


    @Override
    public void onUpdateMasterName(String userNm) {
        prefManager.setCurrentMasterNm(userNm);
    }

    public int getReqBadgeCount(){
        return this.reqBadgeCount;
    }
    public int getChatBadgeCnt(){ return this.chatBadgeCnt; }
    public void clearChatBadgeCnt(){this.chatBadgeCnt = 0;}
    public int getClassChatBadgeCnt(){ return this.classChatBadgeCnt ; }
    public void clearClassChatBadgeCnt(){ this.classChatBadgeCnt = 0;}
    public int getCommentBadgeCnt(){ return this.commentBadgeCnt ; }
    public void clearCommentBadgeCnt(){this.commentBadgeCnt = 0;}


    // 캔버스 기능 관리용 내부클래스
    private class RoomTools {

        private final WeakReference<RoomActivity> mActivity;

        public RoomTools(RoomActivity activity) {
            mActivity = new WeakReference<RoomActivity>(activity);
        }
        public void onDeleteAll() {      //여기
            if(isGuest()){return;}

            setAlertDialog("1", getResources().getString(R.string.global_delete), getResources().getString(R.string.canvas_delete_all));
            closeSubMenuLayer();
        }

        public void onDefaultPenTool(){
            try {
                clearRightBtnSelected();
                closeSubMenuLayer();   // 서브메뉴 레이아웃을 GONE 처리..

                if(isGuest()){return;}

                final CordovaWebView webView = mWebViewFragment.getCordovaWebView();

                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }
                webView.sendJavascript("Ctrl.toggleRC(1, -1, true);");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                isHandMode = false;
                isTextMode = false;
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        public void onPenTool() {
            try {
                clearRightBtnSelected();
                closeSubMenuLayer();   // 서브메뉴 레이아웃을 GONE 처리..

                if(isGuest()){return;}

                final CordovaWebView webView = mWebViewFragment.getCordovaWebView();

                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

                isHandMode = false;
                isTextMode = false;

                webView.sendJavascript("Ctrl.toggleRC(1, -1, true);");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                // 네이티브 쪽에서 한번 더 권한체크.. 스크립트 toggleRC에서 권한체크가 한번 더 수행됨..
                if (!masterFlag) {
                    return;
                }

                // 펜 디폴트 세팅
                if (prefManager.getPenAlpha() == null) {
                    prefManager.setPenAlpha(100);
                }
                if (prefManager.getPenWidth() == null) {
                    prefManager.setPenWidth(50);

                }
                if (prefManager.getPenColorIdx() == null) {
                    prefManager.setPenColorIdx(1);
                }

                ImageView penBtn;
                if (isDevicePhone)
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        penBtn = mBtnMenuPenLand;
                    } else {
                        penBtn = mBtnMenuPenPhone;
                    }
                else
                    penBtn = mBtnMenuPen;

                penBtn.setImageResource(R.drawable.btn_rightmenu_pen_on);
                offHandMode();

                // PopupWindow 버전..
                final View popoverView = getLayoutInflater().inflate(R.layout.popover_pen, null);
                mPopupWindow = new PopupWindow(popoverView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, false);
                mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                mPopupWindow.showAtLocation(popoverView, Gravity.CENTER, 0, 0);
                mPopupWindow.setTouchable(true);
                mPopupWindow.setOutsideTouchable(true);
                openPopover();

                final PreviewCanvas previewCanvas = (PreviewCanvas) popoverView.findViewById(R.id.pen_preview);

                // SeekBar 이벤트 정의..
                final SeekBar penWidthBar = (SeekBar) popoverView.findViewById(R.id.pen_width_bar);
                final SeekBar penAlphaBar = (SeekBar) popoverView.findViewById(R.id.pen_opacity_bar);

                // 펜 너비를 이미 선택했다면 선택했던 너비값으로 설정해줌
                if (prefManager.getPenWidth() != null) {
                    int previousPenWidth = (int) prefManager.getPenWidth();
                    penWidthBar.setProgress(previousPenWidth);
                    previewCanvas.setPenWidth(previousPenWidth);
                    int widthVal = previewCanvas.getPenWidth();
                    webView.sendJavascript("Ctrl._setPenWidthSlide('" + widthVal + "');");
                }


                // 펜 컬러를 이미 선택했다면 선택했던 컬러로 설정해줌
                if (prefManager.getPenColor() != null && prefManager.getPenColorIdx() != null) {
                    String penColor = (String) prefManager.getPenColor();
                    int colorIdx = (int) prefManager.getPenColorIdx();

                    clearColorButton(popoverView, "pen_color");
                    previewCanvas.setPenColor(penColor);

                    ImageButton imgBtn = (ImageButton) popoverView.findViewWithTag("pen_color_" + colorIdx);
                    imgBtn.setImageResource(R.drawable.btn_option_colorselect);
                }


                // 펜 알파를 이미 선택했다면 선택했던 알파값으로 설정해줌
                // [Notice] PreviewCanvas에 반영할 때는 알파값을 마지막으로 지정하도록 함.. 알파값을 먼저 지정하면 지정한 알파값이 초기화되어 보여지므로...
                if (prefManager.getPenAlpha() != null) {
                    int previousPenAlpha = (int) prefManager.getPenAlpha();
                    Log.d("previousPenAlpha", previousPenAlpha + "");
                    penAlphaBar.setProgress(previousPenAlpha);   // 프로그래스 설정
                    previewCanvas.setPenAlpha(previousPenAlpha < 30 ? 30 : previousPenAlpha); // 미리보기 캔버스 설정
                    int alphaVal = previewCanvas.getPenAlpha();
                    webView.sendJavascript("Ctrl._setPenAlphaSlide('" + alphaVal + "');");
                }


                penWidthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setCustomSeekBarThumb(penWidthBar, R.drawable.btn_option_slide);
                        if (progress < 1) progress = 1;
                        prefManager.setPenWidth(progress);
                        previewCanvas.setPenWidth(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int widthVal = previewCanvas.getPenWidth();
                        webView.sendJavascript("Ctrl._setPenWidthSlide('" + widthVal + "');");
                    }

                });

                // 펜 알파 SeekBar
                penAlphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setCustomSeekBarThumb(penAlphaBar, R.drawable.btn_option_slide);
                        prefManager.setPenAlpha(progress);
                        if (progress < 30) progress = 30;
                        previewCanvas.setPenAlpha(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int alphaVal = previewCanvas.getPenAlpha();
                        webView.sendJavascript("Ctrl._setPenAlphaSlide('" + alphaVal + "');");
                    }
                });
                setCustomSeekBarThumb(penWidthBar, R.drawable.btn_option_slide);
                setCustomSeekBarThumb(penAlphaBar, R.drawable.btn_option_slide);

                // 펜 색상 버튼 이벤트 정의
                setEventBtnPenColor(popoverView, webView, 1);
                setEventBtnPenColor(popoverView, webView, 2);
                setEventBtnPenColor(popoverView, webView, 3);
                setEventBtnPenColor(popoverView, webView, 4);
                setEventBtnPenColor(popoverView, webView, 5);
                setEventBtnPenColor(popoverView, webView, 6);
                setEventBtnPenColor(popoverView, webView, 7);
                setEventBtnPenColor(popoverView, webView, 8);
                setEventBtnPenColor(popoverView, webView, 9);
                setEventBtnPenColor(popoverView, webView, 10);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 지우개 툴
         */
        public void onEraserTool() {
            try {
                clearRightBtnSelected();
                closeSubMenuLayer();   // 서브메뉴 레이아웃을 GONE 처리..

                if(isGuest()){return;}


                final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

                webView.sendJavascript("Ctrl.toggleRC(3, -1, true);");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                // 펜 디폴트 세팅
                if (prefManager.getEraserWidth() == null) {
                    prefManager.setEraserWidth(50);
                }


                ImageView eraserBtn;
                if (isDevicePhone)
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        eraserBtn = mBtnMenuEraserLand;
                    } else {
                        eraserBtn = mBtnMenuEraserPhone;
                    }
                else
                    eraserBtn = mBtnMenuEraser;

                eraserBtn.setImageResource(R.drawable.btn_rightmenu_eraser_on);

                if (!isDevicePhone) {
//                    ImageButton handmodeBtn = (ImageButton) mCustomActionBar.findViewById(R.id.btn_header_handmode);
//                    handmodeBtn.setImageDrawable(getResources().getDrawable(R.drawable.btn_header_handmode));
                    isHandMode = false;
                    isTextMode = false;
                } else {
//                    ImageButton handmodeBtn = (ImageButton) findViewById(R.id.btn_rightmenu_hand_phone);
//                    handmodeBtn.setImageDrawable(getResources().getDrawable(R.drawable.btn_header_handmode));
                    isHandMode = false;
                    isTextMode = false;
                }


                // PopupWindow 버전..
                final View popoverViewEraser = getLayoutInflater().inflate(R.layout.popover_eraser, null);
                mPopupWindow = new PopupWindow(popoverViewEraser, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, false);
                mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                mPopupWindow.showAtLocation(popoverViewEraser, Gravity.CENTER, 0, 0);

                mPopupWindow.setTouchable(true);
                mPopupWindow.setOutsideTouchable(false);
                openPopover();

                final SeekBar eraserWidthBar = (SeekBar) popoverViewEraser.findViewById(R.id.eraser_width_bar);
                final PreviewCanvas previewCanvas = (PreviewCanvas) popoverViewEraser.findViewById(R.id.eraser_preview);

                // 지우개 너비를 이미 선택했다면 선택했던 너비값으로 설정해줌
                if (prefManager.getPenWidth() != null) {
                    int prevEraserWidth = (int) prefManager.getEraserWidth();
                    eraserWidthBar.setProgress(prevEraserWidth);
                    previewCanvas.setEraserWidth(prevEraserWidth);
                    int widthVal = previewCanvas.getPenWidth();
                    webView.sendJavascript("Ctrl._setEraserWidthSlide('" + widthVal + "');");
                }

                // 지우개 SeekBar 이벤트 정의..
                eraserWidthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setCustomSeekBarThumb(eraserWidthBar, R.drawable.btn_option_slide);
                        if (progress < 1) progress = 1;
                        prefManager.setEraserWidth(progress);
                        previewCanvas.setEraserWidth(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int widthVal = previewCanvas.getEraserWidth();
                        webView.sendJavascript("Ctrl._setEraserWidthSlide('" + widthVal + "');");
                    }

                });

                setCustomSeekBarThumb(eraserWidthBar, R.drawable.btn_option_slide);  // 지우개 seekbar 초기화

                // 버튼 이벤트
                popoverViewEraser.findViewById(R.id.eraser_undo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.sendJavascript("Ctrl.setRedoUndoEvent('undo');");
                    }
                });

                popoverViewEraser.findViewById(R.id.eraser_redo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.sendJavascript("Ctrl.setRedoUndoEvent('redo');");
                    }
                });

                popoverViewEraser.findViewById(R.id.eraser_clear).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAlertDialog("1", getResources().getString(R.string.global_delete), getResources().getString(R.string.canvas_delete_all));
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        /**
         * 도형 툴
         **/
        public void onShapeTool() {
            try {
                clearRightBtnSelected();
                closeSubMenuLayer();   // 서브메뉴 레이아웃을 GONE 처리..

                // 네이티브 쪽에서 한번 더 권한체크.. 스크립트 toggleRC에서 권한체크가 한번 더 수행됨..
                if(isGuest())
                    return;

                final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

                // 도형 디폴트 세팅
                if (prefManager.getShapeTypeIdx() == null) {
                    prefManager.setShapeTypeIdx(6);
                }
                if (prefManager.getFillTypeIdx() == null) {
                    prefManager.setFillTypeIdx(0);
                }

                final int shapeTypeIdx = (Integer) prefManager.getShapeTypeIdx();
                final int fillTypeIdx = (Integer) prefManager.getFillTypeIdx();

                // toggleRC 호출하기 직전에 권한체크.. 스크립트에 권한 체크 toast 띄우는 처리가 있음..
                int toggleRcIdx = shapeTypeIdx == 5 ? 1 : shapeTypeIdx == 6 ? 2 : shapeTypeIdx == 7 ? 3 : 0;  //  선 - 1 , 사각형 - 2 , 원 - 3
                webView.sendJavascript("Ctrl.toggleRC(5, " + toggleRcIdx + ", true);");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                ImageButton shapeBtn = isDevicePhone ? (ImageButton) findViewById(R.id.btn_rightmenu_shape_phone) : (ImageButton) findViewById(R.id.btn_rightmenu_shape);
                shapeBtn.setImageResource(R.drawable.btn_rightmenu_shape_on);

//                if (!isDevicePhone) {
////                    ImageButton handmodeBtn = (ImageButton) mCustomActionBar.findViewById(R.id.btn_header_handmode);
////                    handmodeBtn.setImageDrawable(getResources().getDrawable(R.drawable.btn_header_handmode));
//                } else {
//                    ImageButton handmodeBtn = (ImageButton) findViewById(R.id.btn_rightmenu_hand_phone);
//                    handmodeBtn.setImageDrawable(getResources().getDrawable(R.drawable.btn_header_handmode));
//                }

                isHandMode = false;
                isTextMode = false;

                // PopupWindow 버전..
                final View popoverViewShape = getLayoutInflater().inflate(R.layout.popover_shape, null);
                mPopupWindow = new PopupWindow(popoverViewShape, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, false);
                mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                mPopupWindow.showAtLocation(popoverViewShape, Gravity.CENTER, 0, 0);

                mPopupWindow.setTouchable(true);
                mPopupWindow.setOutsideTouchable(false);
                openPopover();

                final PreviewCanvas shapePreviewCanvas = (PreviewCanvas) popoverViewShape.findViewById(R.id.shape_preview);
                shapePreviewCanvas.setShapeType(shapeTypeIdx);

                setShapeFillBorderColor(popoverViewShape, shapePreviewCanvas);


                /**
                 *  도형 너비 SeekBar
                 */
                final SeekBar widthSeekBar = (SeekBar) popoverViewShape.findViewById(R.id.shape_width_bar);

                int previousPenWidth = shapeTypeIdx == 5 ? prefManager.getLpenWidth() :
                        shapeTypeIdx == 6  ? prefManager.getSpenWidth() :
                                shapeTypeIdx == 7 ? prefManager.getCpenWidth() : 50;

                widthSeekBar.setProgress(previousPenWidth);
                shapePreviewCanvas.setShapeWidth(previousPenWidth, 0);
                int widthVal = shapePreviewCanvas.getShapeWidth();
                webView.sendJavascript("Ctrl._setShapeWidthSlide(" + shapeTypeIdx + ", '" + widthVal + "');");

                // 도형 width SeekBar 이벤트 정의..
                widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setCustomSeekBarThumb(widthSeekBar, R.drawable.btn_option_slide);

                        if (progress < 1) progress = 1;

                        int shapeIdx = (Integer) prefManager.getShapeTypeIdx();
                        if (shapeIdx == 5) {
                            prefManager.setLpenWidth(progress);
                        } else if (shapeIdx == 6) {
                            prefManager.setSpenWidth(progress);
                        } else if (shapeIdx == 7) {
                            prefManager.setCpenWidth(progress);
                        }

                        int fillType = (Integer) prefManager.getFillTypeIdx();

                        shapePreviewCanvas.setShapeWidth(progress, fillType);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int shapeIdx = (Integer) prefManager.getShapeTypeIdx();
                        int widthVal = shapePreviewCanvas.getShapeWidth();
                        webView.sendJavascript("Ctrl._setShapeWidthSlide(" + shapeIdx + ", '" + widthVal + "');");
                    }
                });


                /**
                 *  도형 알파 SeekBar
                 */
                final SeekBar shapeAlphaBar = (SeekBar) popoverViewShape.findViewById(R.id.shape_opacity_bar);

                int previousShapeAlpha = shapeTypeIdx == 5 ? prefManager.getLpenAlpha() :
                        shapeTypeIdx == 6 ? prefManager.getSpenAlpha() :
                                shapeTypeIdx == 7 ? prefManager.getCpenAlpha() : 50;

                // 도형 알파값을 이미 설정했다면 설정했던 알파값으로 반영함.
                shapeAlphaBar.setProgress(previousShapeAlpha);

                int shapeAlpha = previousShapeAlpha < 30 ? 30 : previousShapeAlpha;
                prefManager.setCpenAlpha(shapeAlpha);
                shapePreviewCanvas.setShapeAlpha(shapeAlpha);

                int alphaVal = shapePreviewCanvas.getShapeAlpha();
                webView.sendJavascript("Ctrl._setShapeAlphaSlide(" + shapeTypeIdx + ", '" + alphaVal + "');");

                // 도형 alpha SeekBar 이벤트 정의..
                shapeAlphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setCustomSeekBarThumb(shapeAlphaBar, R.drawable.btn_option_slide);

                        int shapeIdx = (Integer) prefManager.getShapeTypeIdx();
                        if (shapeIdx == 5) {
                            prefManager.setLpenAlpha(progress);
                        } else if (shapeIdx == 6) {
                            prefManager.setSpenAlpha(progress);
                        } else if (shapeIdx == 7) {
                            prefManager.setCpenAlpha(progress);
                        }
                        if (progress < 30) {
                            progress = 30;
                        }
                        shapePreviewCanvas.setShapeAlpha(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int shapeIdx = (Integer) prefManager.getShapeTypeIdx();
                        int alphaVal = shapePreviewCanvas.getShapeAlpha();
                        webView.sendJavascript("Ctrl._setShapeAlphaSlide(" + shapeIdx + ", '" + alphaVal + "');");
                    }

                });

                setCustomSeekBarThumb(widthSeekBar, R.drawable.btn_option_slide);  // 도형 너비 seekbar 초기화
                setCustomSeekBarThumb(shapeAlphaBar, R.drawable.btn_option_slide);  // 도형 알파 seekbar 초기화

                // UI에 도형 타입 선택 세팅
                clearShapeTypeButtonOn(popoverViewShape);
                if (prefManager.getShapeTypeIdx() != null) {
                    int shapeIdx = (Integer) prefManager.getShapeTypeIdx();
                    String shapeTypeStr = shapeIdx == 6 ? "square" : shapeIdx == 7 ? "circle" : shapeIdx == 5 ? "line" : "";

                    ImageButton imgBtnShapeType = (ImageButton) popoverViewShape.findViewWithTag("shape_type_" + shapeIdx);
                    imgBtnShapeType.setBackground(ContextCompat.getDrawable(activity, getResources().getIdentifier("drawable/btn_shape_" + shapeTypeStr + "_on", null, getPackageName())));

                    if (shapeIdx == 5) {
                        ImageButton borderBtn = (ImageButton) popoverViewShape.findViewWithTag("fill_type_1");
                        borderBtn.setVisibility(View.GONE);
                    }

                    webView.sendJavascript("Ctrl.toggleRC(5, " + toggleRcIdx + ", true);");

                    shapePreviewCanvas.setShapeType(shapeIdx);
                }

                clearFillTypeButtonOn(popoverViewShape);
                // UI에 채움 유형 선택 세팅

                int fillType = (Integer) prefManager.getFillTypeIdx();
//            clearShapeButtonBackground(popoverViewShape, "fill_type");  // 예전 버전..

                ImageButton imgBtnFillType = (ImageButton) popoverViewShape.findViewWithTag("fill_type_" + fillType);
                if (fillType == 0) {
                    imgBtnFillType.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_plane_on));
                    popoverViewShape.findViewById(R.id.fill_color).setVisibility(View.VISIBLE);
                    popoverViewShape.findViewById(R.id.border_color).setVisibility(View.GONE);
                } else if (fillType == 1) { // Border
                    imgBtnFillType.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_option_line_on));
                    popoverViewShape.findViewById(R.id.fill_color).setVisibility(View.GONE);
                    popoverViewShape.findViewById(R.id.border_color).setVisibility(View.VISIBLE);
                }
                shapePreviewCanvas.setShapeFillType(fillType);

                // UI에 도형 색상 세팅
                if (shapeTypeIdx == 5) {
                    if (fillTypeIdx == 0) {
                        String lPenColor = prefManager.getLpenColor();
                        int colorIdx = prefManager.getLpenColorIdx();

                        clearColorButton(popoverViewShape, "shape_fill_color");
                        ImageButton imgBtn = (ImageButton) popoverViewShape.findViewWithTag("shape_fill_color_" + colorIdx);
                        imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                        shapePreviewCanvas.setShapeFillType(0);
                        shapePreviewCanvas.setShapeColor(lPenColor);

                        webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", " + shapeTypeIdx + ", '" + fillTypeIdx + "');");
                    }

                } else if (shapeTypeIdx == 6) {
                    int colorIdx = 1;
                    if (fillTypeIdx == 0) {
                        String sPenColor = prefManager.getSpenColor();
                        colorIdx = prefManager.getSpenColorIdx();

                        clearColorButton(popoverViewShape, "shape_fill_color");
                        ImageButton imgBtn = (ImageButton) popoverViewShape.findViewWithTag("shape_fill_color_" + colorIdx);
                        imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                        shapePreviewCanvas.setShapeFillType(0);
                        shapePreviewCanvas.setShapeColor(sPenColor);

                        if (sPenColor.equals("#00000000")) {
                            shapePreviewCanvas.setShapeFillType(1);
                            shapePreviewCanvas.setShapeColor(prefManager.getSpenBorderColor());
                            shapePreviewCanvas.setShapeAlpha(prefManager.getSpenAlpha());
                        } else {
                            shapePreviewCanvas.setShapeAlpha(prefManager.getSpenAlpha());
                            shapePreviewCanvas.setShapeFillType(1);
                            shapePreviewCanvas.setShapeColor(sPenColor);
                            shapePreviewCanvas.setShapeAlpha(prefManager.getSpenAlpha());
                        }
                    } else if (fillTypeIdx == 1) {
                        String sPenColor = prefManager.getSpenColor();
                        String sPenBorderColor = prefManager.getSpenBorderColor();
                        colorIdx = prefManager.getSpenBorderColorIdx();

                        clearColorButton(popoverViewShape, "shape_border_color");
                        ImageButton imgBtn = (ImageButton) popoverViewShape.findViewWithTag("shape_border_color_" + colorIdx);
                        imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                        shapePreviewCanvas.setShapeFillType(0);
                        shapePreviewCanvas.setShapeColor(sPenColor);
                        shapePreviewCanvas.setShapeFillType(1);
                        shapePreviewCanvas.setShapeColor(sPenBorderColor);
                    }

                    webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", " + shapeTypeIdx + ", '" + fillTypeIdx + "');");

                } else if (shapeTypeIdx == 7) {
                    int colorIdx = 1;
                    if (fillTypeIdx == 0) {
                        String cPenColor = prefManager.getCpenColor();
                        colorIdx = prefManager.getCpenColorIdx();

                        clearColorButton(popoverViewShape, "shape_fill_color");
                        ImageButton imgBtn = (ImageButton) popoverViewShape.findViewWithTag("shape_fill_color_" + colorIdx);
                        imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                        shapePreviewCanvas.setShapeFillType(0);
                        shapePreviewCanvas.setShapeColor(cPenColor);

                        if (cPenColor.equals("#00000000")) {
                            shapePreviewCanvas.setShapeFillType(1);
                            shapePreviewCanvas.setShapeColor(prefManager.getCpenBorderColor());
                            shapePreviewCanvas.setShapeAlpha(prefManager.getCpenAlpha());
                        } else {
                            shapePreviewCanvas.setShapeAlpha(prefManager.getCpenAlpha());
                            shapePreviewCanvas.setShapeFillType(1);
                            shapePreviewCanvas.setShapeColor(cPenColor);
                            shapePreviewCanvas.setShapeAlpha(prefManager.getCpenAlpha());
                        }
                    } else if (fillTypeIdx == 1) {
                        String cPenColor = prefManager.getCpenColor();
                        String cPenBorderColor = prefManager.getCpenBorderColor();
                        colorIdx = prefManager.getCpenBorderColorIdx();

                        clearColorButton(popoverViewShape, "shape_border_color");
                        ImageButton imgBtn = (ImageButton) popoverViewShape.findViewWithTag("shape_border_color_" + colorIdx);
                        imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                        shapePreviewCanvas.setShapeFillType(0);
                        shapePreviewCanvas.setShapeColor(cPenColor);
                        shapePreviewCanvas.setShapeFillType(1);
                        shapePreviewCanvas.setShapeColor(cPenBorderColor);
                    }

                    webView.sendJavascript("Ctrl.__setPenColor(" + colorIdx + ", " + shapeTypeIdx + ", '" + fillTypeIdx + "');");
                }

                // Square, Circle, Line 도형 버튼 이벤트 등록
                setOnClickShapeTypeBtn(popoverViewShape, webView, 5);  // 선 버튼
                setOnClickShapeTypeBtn(popoverViewShape, webView, 6);  // 사각형 버튼
                setOnClickShapeTypeBtn(popoverViewShape, webView, 7);  // 원 버튼

                // Fill, Border 도형 버튼 이벤트 등록
                setShapeFillBtnEvent(popoverViewShape, webView, 0);  // 채우기 색상 모드 버튼
                setShapeFillBtnEvent(popoverViewShape, webView, 1);  // 테두리 색상 모드 버튼

                setShapeColorBtnEvent(popoverViewShape, webView, 1, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 2, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 3, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 4, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 6, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 7, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 8, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 9, 0);
                setShapeColorBtnEvent(popoverViewShape, webView, 10, 0);  // color picker
                setShapeColorBtnEvent(popoverViewShape, webView, 11, 0);  // transparent color

                setShapeColorBtnEvent(popoverViewShape, webView, 1, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 2, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 3, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 4, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 6, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 7, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 8, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 9, 1);
                setShapeColorBtnEvent(popoverViewShape, webView, 10, 1);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void onLaserTool() {
            try {
                clearRightBtnSelected();
                closeSubMenuLayer();   // 서브메뉴 레이아웃을 GONE 처리..

                // 네이티브 쪽에서 한번 더 권한체크.. 스크립트 toggleRC에서 권한체크가 한번 더 수행됨..
                if(isGuest()){return;}

                final CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

//            if (getPref("laser_type") == null) {
//                setPref("laser_type", 1);
//            }
//
//            if (getPref("laser_color_idx") == null) {
//                setPref("laser_color_idx", 1);
//            }

                int laserIdx = prefManager.getLaserType();
                int laserColorIdx = prefManager.getLaserColorIdx();
//            int laserIdx = (Integer) getPref("laser_type");
//            int laserColorIdx = (Integer) getPref("laser_color_idx");

                /*
                if(getPref("laser_type") == null && getPref("laser_color_idx") == null) {
                    webView.sendJavascript("Ctrl.toggleRC(4, 1, true);");
                }*/

                // 초기 스크립트에 레이저포인터 세팅하기..
                webView.sendJavascript("Ctrl.toggleRC(4, " + laserIdx + ", true);");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                ImageButton imgBtnPointer;

                if (isDevicePhone)
                    imgBtnPointer = (ImageButton) findViewById(R.id.btn_rightmenu_laser_phone);
                else
                    imgBtnPointer = (ImageButton) findViewById(R.id.btn_rightmenu_pointer);

                imgBtnPointer.setImageResource(R.drawable.btn_rightmenu_laser_on);
                offHandMode();

                webView.sendJavascript("Ctrl.__setPenColor(" + laserColorIdx + ", 4, '0');");

                // PopupWindow 버전..
                final View popoverViewLaser = getLayoutInflater().inflate(R.layout.popover_pointer, null);
                mPopupWindow = new PopupWindow(popoverViewLaser, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, false);
                mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                mPopupWindow.showAtLocation(popoverViewLaser, Gravity.CENTER, 0, 0);

                mPopupWindow.setTouchable(true);
                mPopupWindow.setOutsideTouchable(false);
                openPopover();


                setLaserPointerPreviewBtn(popoverViewLaser);  // 레이저포인터 버튼 UI 세팅..

                // 레이저포인터 컬러 버튼 UI 세팅..

                clearColorButton(popoverViewLaser, "laser_pointer_color");
                ImageButton imgBtn = (ImageButton) popoverViewLaser.findViewWithTag("laser_pointer_color_" + laserColorIdx);
                imgBtn.setImageResource(R.drawable.btn_option_colorselect);

                setLaserTypeBtnEvent(popoverViewLaser, webView, 1);
                setLaserTypeBtnEvent(popoverViewLaser, webView, 2);
                setLaserTypeBtnEvent(popoverViewLaser, webView, 3);

                setLaserColorBtnEvent(popoverViewLaser, webView, 1);
                setLaserColorBtnEvent(popoverViewLaser, webView, 2);
                setLaserColorBtnEvent(popoverViewLaser, webView, 3);
                setLaserColorBtnEvent(popoverViewLaser, webView, 4);
                setLaserColorBtnEvent(popoverViewLaser, webView, 5);
                setLaserColorBtnEvent(popoverViewLaser, webView, 6);
                setLaserColorBtnEvent(popoverViewLaser, webView, 7);
                setLaserColorBtnEvent(popoverViewLaser, webView, 8);
                setLaserColorBtnEvent(popoverViewLaser, webView, 9);
                setLaserColorBtnEvent(popoverViewLaser, webView, 10);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onOpenTextTool() {
            clearRightBtnSelected();
            closeSubMenuLayer("layer_text_submenu");
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                closePopover(mPopupWindow);
                //break;
            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(ALIGN_PARENT_RIGHT);
            if (isDevicePhone) {
                lp.setMargins(0, AndroidUtils.getPxFromDp(context, 170), AndroidUtils.getPxFromDp(context, 50), 0);
            } else {
                lp.setMargins(0, AndroidUtils.getPxFromDp(context, 210), AndroidUtils.getPxFromDp(context, 50), 0);
            }
            mTextSubMenu.setLayoutParams(lp);

            if(isDevicePhone)
                toggleSubMenuLayer("layer_text_submenu", "btn_rightmenu_text_phone", true);
            else
                toggleSubMenuLayer("layer_text_submenu", "btn_rightmenu_text", true);

        }

        public void onTextTool() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if (isGuest()) return;

            try {
                if (isExitProcessing) {
                    return;
                }

                if (isTextMode) {
                    return;
                }

                if (!masterFlag) {
                    return;
                }

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl._setTextAnnotation();");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                clearRightBtnSelected();

                ImageView imgBtnText;
                if (isDevicePhone)
                    imgBtnText = (ImageView) findViewById(R.id.btn_rightmenu_text_phone);
                else
                    imgBtnText = (ImageView) findViewById(R.id.btn_rightmenu_text);
                imgBtnText.setImageResource(R.drawable.btn_rightmenu_text_on);

                offHandMode();
                isTextMode = true;
//                onHandMode();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void onMemoTool() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest()){return;}

            try {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                }
                closeSubMenuLayer();

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                if (isExitProcessing) {
                    return;
                }
                webView.sendJavascript("Ctrl.addMemo();");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }
                onHandMode();
                clearRightBtnSelected();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onOpenDocumentTools() {
            clearRightBtnSelected();
            closeSubMenuLayer("layer_document_submenu");
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                closePopover(mPopupWindow);
                //break;
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(ALIGN_PARENT_RIGHT);
            if(isDevicePhone) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    lp.setMargins(0, AndroidUtils.getPxFromDp(context, 45), AndroidUtils.getPxFromDp(context, 50), 0);
                } else {
                    lp.setMargins(0, AndroidUtils.getPxFromDp(context, 210), AndroidUtils.getPxFromDp(context, 50), 0);
                }
                mDocumentSubMenu.setLayoutParams(lp);
//                toggleSubMenuLayer("layer_document_submenu", "btn_rightmenu_document_phone", true);
            } else {
                lp.setMargins(0, AndroidUtils.getPxFromDp(context, 290), AndroidUtils.getPxFromDp(context, 50), 0);
                mDocumentSubMenu.setLayoutParams(lp);
//                toggleSubMenuLayer("layer_document_submenu", "btn_rightmenu_document", true);
            }

            if (mDocumentSubMenu.isShown()) {
                if (isDevicePhone) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mBtnMenuDocumentLand.setImageResource(R.drawable.btn_rightmenu_document);
                    } else {
                        mBtnMenuDocumentPhone.setImageResource(R.drawable.btn_rightmenu_document);
                    }
                } else {
                    mBtnMenuDocument.setImageResource(R.drawable.btn_rightmenu_document);
                }
                mDocumentSubMenu.setVisibility(View.GONE);
            } else {
                if (isDevicePhone) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mBtnMenuDocumentLand.setImageResource(R.drawable.btn_rightmenu_document_on);
                    } else {
                        mBtnMenuDocumentPhone.setImageResource(R.drawable.btn_rightmenu_document_on);
                    }
                } else {
                    mBtnMenuDocument.setImageResource(R.drawable.btn_rightmenu_document_on);
                }
                mDocumentSubMenu.setVisibility(View.VISIBLE);
            }

        }

        public void onOpenPollTools() {
            //ToDo 폴서브메뉴 간격조정 후 open, close
            clearRightBtnSelected();
            closeSubMenuLayer("layer_poll_submenu");

            if (guestFlag) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!masterFlag) {
                setAlertDialog("5", getResources().getString(R.string.global_popup_title), getResources().getString(R.string.canvas_authority_take) +"|get_authority|" + userId);
                return;
            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (isDevicePhone) {
                lp.addRule(ALIGN_PARENT_RIGHT);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    lp.setMargins(0, AndroidUtils.getPxFromDp(context, 85), AndroidUtils.getPxFromDp(context, 50), 0);
                } else {
                    lp.setMargins(0, AndroidUtils.getPxFromDp(context, 300), AndroidUtils.getPxFromDp(context, 50), 0);
                }

                mPollSubMenu.setBackgroundResource(R.drawable.img_canvas_option_003);
            } else {
                lp.addRule(ALIGN_PARENT_TOP);
                lp.setMargins(AndroidUtils.getPxFromDp(context, 500), 0, 0, 0);
                mPollSubMenu.setBackgroundResource(R.drawable.img_canvas_option_001);
            }
            mPollSubMenu.setLayoutParams(lp);

            if (mPollSubMenu.isShown()) {
                if (isDevicePhone) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mBtnMenuPollLand.setImageResource(R.drawable.btn_header_poll);
                    } else {
                        mBtnMenuPollPhone.setImageResource(R.drawable.btn_header_poll);
                    }
                } else
                    ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_poll)).setImageResource(R.drawable.btn_header_poll);
                mPollSubMenu.setVisibility(View.GONE);
            } else {
                if (isDevicePhone)
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mBtnMenuPollLand.setImageResource(R.drawable.btn_header_poll_on);
                    } else {
                        mBtnMenuPollPhone.setImageResource(R.drawable.btn_header_poll_on);
                    }
                else
                    ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_poll)).setImageResource(R.drawable.btn_header_poll_on);
                mPollSubMenu.setVisibility(View.VISIBLE);
            }

            roomTools.onHandMode();
        }


        public void onPhotoTool() {
            try {
                if (isGuest())
                    return;
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl._checkAuth(true)");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }
                isUseCamera = true;

                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                String filePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera/" + url;
                //mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera", url));
                mImageCaptureUri = FileProvider.getUriForFile(RoomActivity.this, "com.knowlounge.provider", new File(filePath));

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                startActivityForResult(takePictureIntent, 9001);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onImageTool() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }
                closeSubMenuLayer();

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.toggleRC(0, -1, false);");
                webView.sendJavascript("Ctrl.Uploader.setImageUploadEvent();");
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                if (isExitProcessing) {
                    return;
                }
                onHandMode();
                clearRightBtnSelected();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onPdfTool() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }
                closeSubMenuLayer();
                if (isExitProcessing) {
                    return;
                }

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.Uploader.setPdfUploadEvent();");
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                onHandMode();
                clearRightBtnSelected();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void onVideoShareTool() {
            if (isGuest())
                return;

            try {

                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }
                closeSubMenuLayer();
                if (isExitProcessing) {
                    return;
                }

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl._checkAuth(true)");

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }
                FragmentManager fm = getSupportFragmentManager();
                YouTubeSearchDialogFragment testDialogFragment = new YouTubeSearchDialogFragment();
                testDialogFragment.show(fm, "testing");

                onHandMode();
                clearRightBtnSelected();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void onUndoTool() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                if (isExitProcessing) {
                    return;
                }
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.setRedoUndoEvent('undo');");
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                closeSubMenuLayer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onRedoTool() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                if (isExitProcessing) {
                    return;
                }
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.setRedoUndoEvent('redo');");
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                closeSubMenuLayer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onHandMode() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.toggleRC(0, -1, true);");   // 핸드 모드도 권한 가져올 수 있도록 수정 - 2016.10.14

                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                currentMode = GlobalConst.MODE_HAND;
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

                isHandMode = true;
//                if (isDevicePhone) {
//                    ((ImageView) findViewById(R.id.btn_rightmenu_hand_phone)).setImageResource(R.drawable.btn_rightmenu_hand_on);
//                } else {
//                    ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_handmode)).setImageResource(R.drawable.btn_header_handmode_on);
//                }

            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void onHandMode(String type) {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.sendJavascript("Ctrl.toggleRC(0, -1, true);");   // 핸드 모드도 권한 가져올 수 있도록 수정 - 2016.10.14
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }

                currentMode = GlobalConst.MODE_HAND;
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }
                clearRightBtnSelected();
                isHandMode = true;
//                if (isDevicePhone) {
//                    ((ImageView) findViewById(R.id.btn_rightmenu_hand_phone)).setImageResource(R.drawable.btn_rightmenu_hand_on);
//                } else {
//                    ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_handmode)).setImageResource(R.drawable.btn_header_handmode_on);
//                }
            }catch(InterruptedException e){
                e.printStackTrace();

            }
        }

        public void onZoomIn() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if(isGuest())
                return;

            try {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.loadUrl("javascript:Ctrl.zoomControl(" + zoomVal + ");");
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                if (!masterFlag) {
                    return;
                }
                closeSubMenuLayer();

                zoomVal += 25;
                if (zoomVal >= 400) {
                    zoomVal = 400;
                }
                if (isExitProcessing) {
                    return;
                }

            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onZoomOut() {
            // 네이티브 쪽 권한체크.. 스크립트에서 권한체크가 한번 더 수행됨..
            if (isGuest())
                return;

            try {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }

                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                webView.loadUrl("javascript:Ctrl.zoomControl(" + zoomVal + ");");
                Thread.sleep(GET_MASTER_AUTH_INTERVAL);

                closeSubMenuLayer();

                if (!masterFlag) {
                    return;
                }

                zoomVal -= 25;
                if (zoomVal <= 100) {
                    zoomVal = 100;
                }
                if (isExitProcessing) {
                    return;
                }

            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void onPoll(@Nullable Bundle param) {
            Intent intent;
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                closePopover(mPopupWindow);
                //break;
            }
            if (param == null) {
                if (isPollProgress) {
                    Toast.makeText(getContext(), getString(R.string.poll_draw_in_prgs), Toast.LENGTH_LONG).show();
                    return;
                }
                clearRightBtnSelected();
            }
            if (isDevicePhone){
                intent = new Intent(RoomActivity.activity, PollPopupDialogPhone.class);
            } else {
                intent = new Intent(RoomActivity.activity, PollPopupDialog.class);
            }
            intent.putExtra("param", param);
            intent.putExtra("mServiceType", GlobalConst.VIEW_CREATE_FRAGMENT);
            startActivity(intent);
            if (mPollSubMenu.isShown())
                mPollSubMenu.setVisibility(View.GONE);
        }


        /**
         * 저장한 질문 보기
         */
        public void onPollTmpList() {
            Intent intent;
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                closePopover(mPopupWindow);
                //break;
            }
            clearRightBtnSelected();
            if (KnowloungeApplication.isPhone)
                intent = new Intent(RoomActivity.activity, PollPopupDialogPhone.class);
            else
                intent = new Intent(RoomActivity.activity, PollPopupDialog.class);
            intent.putExtra("mServiceType", GlobalConst.VIEW_POLLLIST_FRAGMENT);
            startActivity(intent);
            mPollSubMenu.setVisibility(View.GONE);
        }


        /**
         * 질문 결과 목록 보기
         */
        public void onPollCompletedList() {
            Intent intent;
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                closePopover(mPopupWindow);
            }

            clearRightBtnSelected();

            if (KnowloungeApplication.isPhone) {
                intent = new Intent(RoomActivity.activity, PollPopupDialogPhone.class);
            } else {
                intent = new Intent(RoomActivity.activity, PollPopupDialog.class);
            }
            intent.putExtra("mServiceType", GlobalConst.VIEW_COMPLETE_POLLLIST_FRAGMENT);
            startActivity(intent);
            mPollSubMenu.setVisibility(View.GONE);
        }


        public void onBookmark(final boolean bookmarkFlag) {
            if (isExitProcessing) return;
            CordovaWebView webView = mWebViewFragment.getCordovaWebView();
            webView.sendJavascript("Ctrl.setBookmark(" + bookmarkFlag + ")");
            runOnUiThread(new Runnable() {
                public void run() {
                    if (bookmarkFlag) {
                        isFavoriteOn = false;
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_bookmark_del), Toast.LENGTH_SHORT).show();
                    } else {
                        isFavoriteOn = true;
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_bookmark_add), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        public void onSaveCanvasTool() {
            if (isExitProcessing) return;
            if (mDrawerLayout.isDrawerOpen(mDrawerContent)) {
                mDrawerLayout.closeDrawer(mDrawerContent);
            }
            String title = getResources().getString(R.string.toast_import_start);
            String msg = getResources().getString(R.string.canvas_history2);
            openProgressLayout(title, msg, null, true);

            CordovaWebView webView = mWebViewFragment.getCordovaWebView();
            webView.sendJavascript("Ctrl.saveRoomCanvas();");
        }


        public void onShare() {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                closePopover(mPopupWindow);
                //break;
            }
            clearRightBtnSelected();
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title));
            msg.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
            msg.putExtra(Intent.EXTRA_TITLE, getResources().getString(R.string.global_share_title));
            msg.setType("text/plain");

            allowExitRoom = false;
            startActivity(Intent.createChooser(msg, getResources().getString(R.string.canvas_share)));
        }


        //Todo 창하쓰
        public void onOpenUserList() {
            if (isOpenRightMenu) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
                mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW, true);
                closeSubMenuLayer();
            }
        }
        //Todo 창하쓰


        public void onNotiTool(){
            if (!isOpenRightMenu) {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
            mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_ROOMNOTI_VIEW, true);
            closeSubMenuLayer();
        }

        //Todo 창하쓰
        //Todo 창하쓰
        public void onChatTool() {
//            mSlidingFragment.setTabIndex(2);
//            mSlidingFragment.setCommunicateTabIndex(0);
//            if(!mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
//                mDrawerLayout.openDrawer(Gravity.RIGHT);
//                // 채팅 뱃지 카운트 초기화
//                chatBadgeCnt = 0;
//                updateChattingBadge(false);
//            }
            if (!isOpenRightMenu) {
                isOpenRightMenu = true;
                mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW, GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW, true);
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
            closeSubMenuLayer();
        }


        //Todo 창하쓰
        //Todo 창하쓰
        public void onCommentTool() {
//            mSlidingFragment.setTabIndex(2);
//            mSlidingFragment.setCommunicateTabIndex(1);
//            if(!mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
//                mDrawerLayout.openDrawer(Gravity.RIGHT);
//                // 코멘트 뱃지 카운트 초기화
//                commentBadgeCnt = 0;
//                updateCommentBadge(false);
//            }

            if (!isOpenRightMenu) {
                isOpenRightMenu = true;
                mRightMenuCallBack.openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW, 0, true);
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }

            closeSubMenuLayer();
        }
        //Todo 창하쓰

        public void onMultiPage(){
            // 네이티브 쪽에서 한번 더 권한체크.. 스크립트 toggleRC에서 권한체크가 한번 더 수행됨..
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    closePopover(mPopupWindow);
                    //break;
                }
                closeSubMenuLayer();
                if (isExitProcessing) {
                    return;
                }

                //todo 멀티 페이지 open animation
                showHideMultipage(isMultiPageOpen);
        }

    }


    /*
    *----------------------------------------------------------------------------------------------
    *--<override : VideoPlugin.VideoPluginEvents>
    */

    /**
     * 수업 모드가 변경되었을 때 호출된다.
     * @param roomId
     * @param videoCtrl
     * @param soundOnly
     */
    @Override
    public void onVideoOptionChange(String roomId, boolean videoCtrl, boolean soundOnly) {
        Log.d(TAG, "<onVideoOptionChange / ZICO>");
        isVideoControl = videoCtrl;
        if (isOpenRightMenu && currentParent == GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW && currentUserChild == GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW) {
            mNotiChangeCallBack.onNotiChangeData();
        }
        mVideoEvents.onVideoOptionChange(roomId, videoCtrl, soundOnly);
    }


    @Override
    public void onVideoGroup(String roomId, boolean separate) {
        Log.d(TAG, "<onVideoGroup / ZICO> separated room : " + roomId + ", separate : " + separate);
        //mVideoEvents.onVideoGroup(separate);
    }

    /**
     * video_noti를 받았을 때 호출됨.
     * @param action
     * @param fromUserNo
     * @param toUserNo
     */
    @Override
    public void onVideoNoti(String action, final String fromUserNo, final String toUserNo) {
        Log.d(TAG, "<onVideoNoti / ZICO> action : " + action + ", fromUserNo : " + fromUserNo + ", toUserNo : " + toUserNo);
        if(action.equals("request")) {
            final FrameLayout container = (FrameLayout) findViewById(R.id.main_layout);
            final View requestNoti = getLayoutInflater().inflate(R.layout.layout_video_req, container, false);

            final ViewGroup.LayoutParams LoadingParams = (ViewGroup.LayoutParams) requestNoti.getLayoutParams();

            //테블릿과 폰 가로세로에 따라 dialog 크기 변경
            if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
                if (prefManager.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                    LoadingParams.width = (int) (300 * prefManager.getDensity());
                } else {
                    LoadingParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                requestNoti.setLayoutParams(LoadingParams);
            } else {
                LoadingParams.width = (int) (300 * prefManager.getDensity());
                requestNoti.setLayoutParams(LoadingParams);
            }

            RoomUser user = mRoomUserPresenter.getRoomUserList().get(fromUserNo);
            final String thumbnail = user.getThumbnail();
            final String userNm = user.getUserNm();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Animation slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    container.addView(requestNoti, 6);
                    requestNoti.startAnimation(slideUpAnimation);

                    ImageView userThumbImg = (ImageView) requestNoti.findViewById(R.id.video_req_user_thumb);
                    TextView txtVideoReqContent = (TextView) requestNoti.findViewById(R.id.video_req_content);
                    TextView btnAllow = (TextView) requestNoti.findViewById(R.id.btn_allow);
                    TextView btnDeny = (TextView) requestNoti.findViewById(R.id.btn_deny);

                    Glide.with(getContext())
                            .load(AndroidUtils.changeSizeThumbnail(thumbnail, 70))
                            .error(R.drawable.img_userlist_default01)
                            .bitmapTransform(new CircleTransformTemp(context))
                            .into(userThumbImg);

                    txtVideoReqContent.setText(String.format(getResources().getString(R.string.cam_join_alert_qstn), userNm));
                    btnAllow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.videoNoti('connect', '" + toUserNo + "', '" + fromUserNo + "');");
                            Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            requestNoti.startAnimation(slideDownAnimation);
                            requestNoti.setVisibility(View.GONE);
                            ((FrameLayout) findViewById(R.id.main_layout)).removeView(requestNoti);
                        }
                    });
                    btnDeny.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWebViewFragment.getCordovaWebView().sendJavascript("PacketMgr.Master.videoNoti('disconnect', '" + toUserNo + "', '" + fromUserNo + "');");
                            Animation slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                            requestNoti.startAnimation(slideDownAnimation);
                            requestNoti.setVisibility(View.GONE);
                            ((FrameLayout) findViewById(R.id.main_layout)).removeView(requestNoti);
                        }
                    });

                }
            });
        } else if (action.equals("connect")) {

        } else if (action.equals("disconnect")) {
            Toast.makeText(getApplicationContext(), "선생님이 영상 참여 신청을 거절하였습니다.", Toast.LENGTH_SHORT).show();
        }

        mVideoEvents.onVideoNoti(action, fromUserNo, toUserNo);
    }

    //==========================================================================================================================================================================


    /**
     * 스타 잔액을 조회한다.
     */
    private void getStarBalance() {
        Log.d(TAG, "getStarBalance");
        String url = "user/currency?userAccessToken=" + CommonUtils.urlEncode(prefManager.getSiAccessToken());
        RestClient.getSiPlatform(url, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int starCount = response.getJSONObject("balance").getJSONObject("currency").getJSONObject("knowlounge").getInt("value");
                    int savedStarCount = prefManager.getUserStarBalance();
                    if (starCount != savedStarCount) {
                        prefManager.setUserStarBalance(starCount);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "SI platform getBalance onFailure - " + statusCode + ", " + responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if (throwable instanceof IOException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public int getRoomUserCount(){
        return userList.size();
    }


    NetworkStateReceiver.OnChangeNetworkStatusListener networkStateListener = new NetworkStateReceiver.OnChangeNetworkStatusListener(){
        @Override
        public void onChange(int status) {
            switch(status) {
                case NetworkStateReceiver.NETWORK_CONNECTED :
                    Log.d(TAG,"<NetworkStateReceiver / Knowlounge> network connected");
                    if(noNetwork != null && noNetwork.isShowing())
                        noNetwork.dismiss();
                    mNetworkConnectionEvents.onConnected();
                    return;

                case NetworkStateReceiver.NETWORK_DISCONNECTED :
                    Log.d(TAG,"<NetworkStateReceiver / Knowlounge> network disconnected");
                    mNetworkConnectionEvents.onDisconnected();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if(!KnowloungeApplication.isNetworkConnected) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setNoNetworkAlertDialog();
                                    }
                                });
                            }
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(timerTask, 3000);

                    return;
            }
        }
    };


    public void setNoNetworkAlertDialog() {
        if (noNetwork != null && noNetwork.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogCustom);
        builder.setMessage(getResources().getString(R.string.network_disconnected)).setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.network_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noNetwork.dismiss();
                    }
                });
        noNetwork = builder.create();
        noNetwork.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        noNetwork.setCanceledOnTouchOutside(false);
        noNetwork.setTitle(getResources().getString(R.string.global_popup_title));

        noNetwork.show();
    }

    public int getLastChatMode() {
        return lastChatMode;
    }

    public void setLastChatMode(int mode) {
        lastChatMode = mode;
    }

    private boolean isGuest() {
        if(guestFlag)
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
        return guestFlag;
    }

    public String getCurrentPageId(){ return this.currentPage; }

    public void setInitMultiPage(JSONArray arr){
        multiPageDataProvider = new MultiPageDataProvider(arr);
        ((MultiPageFragment)getSupportFragmentManager().findFragmentByTag("MultiPageFragment")).setDataProvider(multiPageDataProvider);

//        MultiPageFragment multiPageFragment = new MultiPageFragment();
//        getSupportFragmentManager().beginTransaction().replace(KnowloungeApplication.isPhone ? R.id.multi_page_container_phone : R.id.multi_page_container, multiPageFragment, "MultiPageFragment").commit();  // 슬라이딩 메뉴
    }

    public void setAddMultiPage(JSONObject obj){
        try {
            Log.d(TAG, "setAddMultiPage");
            multiPageDataProvider.addItem(obj.getString("pageid"));
            EventBus.get().post(new MultiPageEvent(MultiPageEvent.ADD_PAGE, obj.getString("pageid")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setDelMultiPage(JSONObject obj){
        try {
            Log.d(TAG, "setDelMultiPage");
            multiPageDataProvider.delItem(obj.getString("pageid"));
            EventBus.get().post(new MultiPageEvent(MultiPageEvent.DEL_PAGE, obj.getString("pageid")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentPageId(JSONObject obj){
        try {
            this.currentPage = obj.getString("newPageId");
            EventBus.get().post(new MultiPageEvent(MultiPageEvent.CHANGE_PAGE, ""));
        }catch(JSONException j){
            j.printStackTrace();
        }
    }

    public void setOrderPageList(JSONArray arr){
            multiPageDataProvider.reOrder(arr);
            EventBus.get().post(new MultiPageEvent(MultiPageEvent.ORDER_PAGE, ""));
    }

    public void setOrderResult(JSONObject obj){
        try {
            multiPageDataProvider.orderSuccess(obj.getBoolean("result"));
        }catch (JSONException j){
            j.printStackTrace();
        }
    }

    public AbstractDataProvider getMultiPageData(){
        if(multiPageDataProvider == null) {
            Log.d(TAG, "multiPageDataProvider is null");
        } else {
            Log.d(TAG, "multiPageDataProvider is not null");
        }
        return multiPageDataProvider;
    }


    // AlertDialog 없이 페이지를 추가하기 - 페이지를 추가하면서 수업을 확장하는 프로세스에서 호출된다.
    public void addMultiPageNoDialog(){
        Log.d(TAG, "addMultiPageNoDialog");
        try {
            CordovaWebView webView = mWebViewFragment.getCordovaWebView();
            webView.sendJavascript("UI.Page.addProc();");

            authInfo.put("userlimitcnt", "30");
            userLimitCnt = 30;
            setRoomConfigJavascript(authInfo, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // 페이지를 추가하기
    public void addCanvasPage() {
        if(isGuest())
            return;
        if(!masterFlag){
            getAuth();
        }
        if(userLimitCnt == 3){
            if(multiPageDataProvider.getCount() > 3){
                Bundle argument = new Bundle();
                argument.putString("mServiceType", "room_multipage");
                argument.putString("roomid", prefManager.getCurrentRoomId());
                argument.putString("code", RoomActivity.activity.getRoomCode());
                argument.putString("masterno", prefManager.getCurrentTeacherUserNo());
                ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                dialogFragment.setArguments(argument);
                dialogFragment.show(getSupportFragmentManager(), "multi_page");
                return;
            }
        }
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("UI.Page.add();");
    }


    // 선택한 페이지를 삭제하기
    public void removeCanvasPage(String pageId){
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        if(!masterFlag){
            getAuth();
        }
        webView.sendJavascript("UI.Page.remove('" + pageId +"');");
    }

    // 선택한 페이지로 이동하기
    public void changeCanvasPage(String pageId){
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        if(!masterFlag){
            getAuth();
        }
        webView.sendJavascript("UI.Page.change('" + pageId +"');");
    }

    // 페이지를 드래그하여 정렬하기
    public void orderCanvasPage(String orderList, String pageId) {
        Log.d(TAG,"orderCanvasPage : " + orderList);
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        if(!masterFlag){
            getAuth();
        }
        webView.sendJavascript("UI.Page.order('" + orderList + "','" + pageId +"');");
    }

    public void getAuth(){
        Log.d(TAG, "getAuth()");
        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        webView.sendJavascript("Ctrl._checkAuth(true)");
    }


    public void showHideMultipage(boolean isShow) {
        if(!isShow) {
            multiPageSlidingLayout.setVisibility(View.VISIBLE);
            isMultiPageOpen = true;

            Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.slide_up : R.anim.slide_in_right );
            multiPageSlidingLayout.startAnimation(animation);

            if (isDevicePhone) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mBtnMultipageLand.setImageResource(R.drawable.btn_header_multipage_on);
                } else {
                    mBtnMultipagePhone.setImageResource(R.drawable.btn_header_multipage_on);
                }

            } else
                ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_multipage)).setImageResource(R.drawable.btn_header_multipage_on);

        }
        else{
            multiPageSlidingLayout.setVisibility(View.GONE);
            isMultiPageOpen = false;

            Animation animation = AnimationUtils.loadAnimation(RoomActivity.this, KnowloungeApplication.isPhone ? R.anim.slide_down : R.anim.slide_out_right);
            multiPageSlidingLayout.startAnimation(animation);

            if (isDevicePhone) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mBtnMultipageLand.setImageResource(R.drawable.btn_header_multipage);
                } else {
                    mBtnMultipagePhone.setImageResource(R.drawable.btn_header_multipage);
                }
            } else
                ((ImageView) mCustomActionBar.findViewById(R.id.btn_header_multipage)).setImageResource(R.drawable.btn_header_multipage);
        }
    }

    String drawingContainerMode = "";
    public void invokeAreaSelector(String mode) {

        CordovaWebView webView = mWebViewFragment.getCordovaWebView();
        Log.d(TAG, "WebView 영역 width : " + webView.getView().getWidth() + ", WebView 영역 height : " + webView.getView().getHeight());
        ViewGroup.LayoutParams lp = selectableView.getLayoutParams();
        Log.d(TAG, "Selectable 영역 width : " + lp.width + ", Selectable 영역 height : " + lp.height);

        layoutBottom = (LinearLayout) findViewById(R.id.layout_move_room);

        if (isSelectorMode) {
            Log.d(TAG, "[invokeAreaSelector] 선택모드 비활성화..");
            this.drawingContainerMode = "";
            isSelectorMode = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    selectableView.setVisibility(View.GONE);
//                    enableButtons();
                    disableEnableControls(true, (ViewGroup)findViewById(R.id.canvas_container));

                    // 상단 툴바 보이기..
                    if (!actionBar.isShowing()) {
                        actionBar.show();
                        mToolbar.animate().translationY(0).setDuration(600L).start();
                    }

                    // 우측 메뉴바 보이기..
                    if (!rightMenu.isShown()) {
                        rightMenu.setVisibility(View.VISIBLE);
                    }

                    // 하단의 방 이동 및 폴 버튼 보이기..
                    if (!layoutBottom.isShown()) {
                        layoutBottom.setVisibility(View.VISIBLE);
                    }

                    // 유저 캠 UI는 VISIBILITY = VISIBLE 처리 함..
                    View videoView = getWindow().findViewById(R.id.user_video_container);
                    if (!videoView.isShown()) {
                        videoView.setVisibility(View.VISIBLE);
                        if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
                            ((ImageView)mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video_on);
                        } else {
                            ((ImageView)findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam_on);
                        }
                    }
                }
            });

        } else {
            Log.d(TAG, "[invokeAreaSelector] 선택모드 활성화..");
            this.drawingContainerMode = mode;
            isSelectorMode = true;
            isHandMode = false;
            selectableView.setVisibility(View.VISIBLE);
//            disableButtons();
            disableEnableControls(false, (ViewGroup)findViewById(R.id.canvas_container));

            // 상단 툴바 숨김 처리..
            if (actionBar.isShowing()) {
                //actionBar.hide();
                mToolbar.animate().translationY(-200).setDuration(600L)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            actionBar.hide();
                        }
                    }).start();
            }

            // 우측 메뉴바 숨김 처리..
            if (rightMenu.isShown()) {
//                rightMenu.setVisibility(View.GONE);
            }

            closeSubMenuLayer();   // 열려있는 서브메뉴가 있다면 모두 닫기..

            // 하단의 방 이동 및 폴 버튼 숨김 처리..
            if (layoutBottom.isShown()) {
                layoutBottom.setVisibility(View.GONE);
            }

            // 유저 캠 UI는 VISIBILITY = GONE 처리 함..
            View videoView = getWindow().findViewById(R.id.user_video_container);
            if (videoView.isShown()) {
                videoView.setVisibility(View.GONE);
                if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
                    ((ImageView)mCustomActionBar.findViewById(R.id.btn_header_video)).setImageResource(R.drawable.btn_header_video);
                } else {
                    ((ImageView)findViewById(R.id.btn_rightmenu_cam_phone)).setImageResource(R.drawable.btn_rightmenu_cam);
                }
            }

            Toast.makeText(this, getString(R.string.poll_draw_sel_area_ansr), Toast.LENGTH_SHORT).show();
        }
    }

    float sx = 0f;
    float sy = 0f;
    float swidth = 0f;
    float sheight = 0f;

    public void adjustSelection(float startX, float startY, float endX, float endY) {
        selectableView.updateSelectorPaint(startX, startY, endX, endY);
    }


    public void setSelectionCoord(float sx, float sy, float swidth, float sheight) {
        this.sx = sx;
        this.sy = sy;
        this.swidth = swidth;
        this.sheight = sheight;
    }


    /**
     * 영역 선택시 노출되는 버튼의 위치값 지정..
     * @param x
     * @param y
     */
    public void adjustSelectorButtonPosition(float x, float y) {
        Log.d(TAG, "adjustSelectorButtonPosition");
        LinearLayout btnContainer = (LinearLayout) findViewById(R.id.btn_selector_container);
        btnContainer.setVisibility(View.VISIBLE);
        int containerWidth = btnContainer.getWidth();
        int containerHeight = btnContainer.getHeight();
        containerWidth = containerWidth == 0 ? (int) (90 * KnowloungeApplication.density) : containerWidth;
        containerHeight = containerHeight == 0 ? (int) (50 * KnowloungeApplication.density) : containerHeight;
//        btnContainer.setX(x - containerWidth);
//        btnContainer.setY(y + (10 * KnowloungeApplication.density));
        btnContainer.setX(x - containerWidth - (10 * KnowloungeApplication.density));
        btnContainer.setY(y - containerHeight - (10 * KnowloungeApplication.density));
        btnContainer.requestLayout();
    }


    public void clearSelectorButton() {
        LinearLayout selectorBtnContainer = (LinearLayout) findViewById(R.id.btn_selector_container);
        selectorBtnContainer.setVisibility(View.GONE);
    }

    public void clearSelectionArea() {
        selectableView.updateSelectorPaint(0.f, 0.f, 0.f, 0.f);
        LinearLayout selectorBtnContainer = (LinearLayout) findViewById(R.id.btn_selector_container);
        selectorBtnContainer.setVisibility(View.GONE);
    }

    public void cancelCapture(String mode) {
        this.drawingContainerMode = "";
        isSelectorMode = false;
        selectableView.setVisibility(View.GONE);
        disableEnableControls(true, (ViewGroup)findViewById(R.id.canvas_container));

        // 상단 툴바 보이기..
        if (!actionBar.isShowing()) {
            actionBar.show();
            mToolbar.animate().translationY(0).setDuration(600L).start();
        }

        // 우측 메뉴바 보이기..
        if (!rightMenu.isShown()) {
            rightMenu.setVisibility(View.VISIBLE);
        }


        // 하단의 방 이동 및 폴 버튼 보이기..
        if (!layoutBottom.isShown()) {
            layoutBottom.setVisibility(View.VISIBLE);
        }

        clearSelectionArea();   // 선택된 영역 초기화
        clearSelectorButton();  // 영역 선택시 노출되는 버튼들을 숨김
        Bundle param = new Bundle();
        param.putString("img", "");
        param.putString("mode", mode);
        param.putInt("polltype", PollCreateData.POLL_TYPE_DRAWING);
        param.putInt("method", PollCreateData.SELECTION_CAPTURE);
        roomTools.onPoll(param);
    }

    public void confirmCapture(String mode, String imgBinary) {
        Log.d(TAG, "[confirmCapture] mode : " + mode);
        clearSelectionArea();   // 선택된 영역 초기화
        Bundle param = new Bundle();
        //param.putString("img", imgBinary);
        param.putString("mode", mode);
        param.putInt("polltype", PollCreateData.POLL_TYPE_DRAWING);
        param.putInt("method", PollCreateData.SELECTION_CAPTURE);

        pollData.setCheckedType(PollCreateData.POLL_TYPE_DRAWING);
        pollData.setDrawingMethod(PollCreateData.SELECTION_CAPTURE);
        pollData.setCapturedImgBinary(imgBinary);
        roomTools.onPoll(param);
    }


    public void openKnowloungePollDialog(String mode, String contentMsg, @Nullable final JSONObject data) {
        KnowloungeDialogFragment dialogFragment = KnowloungeDialogFragment.newInstance(mode, contentMsg);
        View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String pollNo = data.getString("pollno");
                            String timeLimit = data.getString("timelimit");
                            String isCountdown = data.getString("iscountdown");

                            CordovaWebView webView = mWebViewFragment.getCordovaWebView();
                            webView.sendJavascript("PollCtrl.Action.Attender.moveSubroomForDrawing('" + pollNo + "','" +timeLimit + "', " + isCountdown + ");");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
            }
        };
        dialogFragment.setOnConfirmClickListener(listener);

        FragmentManager fm = getSupportFragmentManager();
        dialogFragment.show(fm, "dialog");
    }


    public int pollAnswerTime = -1;
    public Thread mTimeThread;
    private int checkOneSecond = 0;
    public void startPollTimerThread() {
        mTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (pollAnswerTime != -1) {
                        if(checkOneSecond++ > 0) {
                            pollAnswerTime--;
                            if (PollAnswerFragment._instance != null && PollAnswerFragment._instance.isVisible()) {
                                Log.d(TAG, "답변 창의 타이머를 갱신합니다.");
                                PollAnswerFragment._instance.updateTime();
                            }
                            if (pollAnswerTime == 0) {
                                break;
                            }
                        }
                        Thread.sleep(1000);
                    }
                    stopPollTimerThread();

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        mTimeThread.start();
    }

    public void interruptPollTimerThread() {
        pollAnswerTime = -1;
        finishActivity(8888);
        setIsPollProgress(false);
        setAnswerPollBtn();
    }

    private void stopPollTimerThread() {
        // 제한시간 초과로 종료되었을 경우 처리..
        pollAnswerTime = -1;
        finishActivity(8888);
        setIsPollProgress(false);
        setAnswerPollBtn();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_poll_end), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableEnableControls(boolean enable, ViewGroup vg){
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            if (TextUtils.equals((String) child.getTag(), "webview_container"))
                continue;
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls(enable, (ViewGroup)child);
            }
        }
    }


    ProgressDialog mProgressDialog;
    public void showProgressDialog(String message) {
        mProgressDialog = ProgressDialog.show(this, "", message, true);
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    /**
     * 판서 질문을 다른 사람의 보드에서 받았을 때 다이얼로그를 띄워주는 메서드 - 2017.02.09
     * @param obj
     */
    public void openDrawingPollNotifyFragment(final JSONObject obj) {
        closePollPopup();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isMove = obj.getBoolean("ismove");
                    String roomCode = obj.getString("code");
                    String pollNo = obj.getString("pollno");
                    String timeLimit = obj.getString("timelimit");
                    String isCountDown = obj.getString("iscountdown");
                    String title = obj.getString("title");
                    String url = obj.getString("url");
                    String imageMap = obj.has("image") ? obj.getString("image") : "";


                    Bundle param = new Bundle();
                    param.putString("code", roomCode);
                    param.putString("pollno", pollNo);
                    param.putString("timelimit", timeLimit);
                    param.putString("iscountdown", isCountDown);
                    param.putString("title", title);
                    param.putString("url", url);
                    param.putBoolean("ismove", isMove);
                    param.putString("image", imageMap);

                    FragmentManager fm = getSupportFragmentManager();
                    DrawingPollNotifyFragment fragment = new DrawingPollNotifyFragment();
                    fragment.setArguments(param);
                    fragment.show(fm, "DrawingPollNotifyFragment");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 700);
    }

    public void closeDrawingPollNotifyFragment() {
        if (getSupportFragmentManager().findFragmentByTag("DrawingPollNotifyFragment") != null &&
                (getSupportFragmentManager().findFragmentByTag("DrawingPollNotifyFragment")).isVisible()) {
            Log.d(TAG, "기존 Fragment를 닫습니다.");
            DrawingPollNotifyFragment fragment = (DrawingPollNotifyFragment)getSupportFragmentManager().findFragmentByTag("DrawingPollNotifyFragment");
            fragment.dismiss();
        }
    }

    private void offHandMode() {
//        if (!isDevicePhone) {
//            ImageButton handmodeBtn = (ImageButton) mCustomActionBar.findViewById(R.id.btn_header_handmode);
//            handmodeBtn.setImageDrawable(getResources().getDrawable(R.drawable.btn_header_handmode));
//            isHandMode = false;
//            isTextMode = false;
//        } else {
//            ImageButton handmodeBtn = (ImageButton) findViewById(R.id.btn_rightmenu_hand_phone);
//            handmodeBtn.setImageDrawable(getResources().getDrawable(R.drawable.btn_header_handmode));
//            isHandMode = false;
//            isTextMode = false;
//        }
    }


    public RoomUserPresenter getRoomUserPresenter() {
        return mRoomUserPresenter;
    }
}