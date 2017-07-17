package com.knowlounge;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.knowlounge.base.BaseActivity;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.customview.CustomSpinner;
import com.knowlounge.fragment.AppConfigFragment;
import com.knowlounge.fragment.HelpDeskFragment;
import com.knowlounge.fragment.MainLeftNavFragment;
import com.knowlounge.fragment.ProfileEditFragment;
import com.knowlounge.fragment.ProfileEditMultiFragment;
import com.knowlounge.fragment.ProfileFragment;
import com.knowlounge.fragment.SNSFriendListDepthOneFragment;
import com.knowlounge.fragment.StarShopFragment;
import com.knowlounge.fragment.dialog.DirectEnterDialogFragment;
import com.knowlounge.fragment.dialog.ExtendReqDialogFragment;
import com.knowlounge.fragment.dialog.RoomPasswdDialogFragment;
import com.knowlounge.fragment.dialog.VersionCheckDialogFragment;
import com.knowlounge.fragment.home.FriendClassFragment;
import com.knowlounge.fragment.home.HomeClassFragment;
import com.knowlounge.fragment.home.MyClassFragment;
import com.knowlounge.fragment.home.PublicClassFragment;
import com.knowlounge.fragment.home.SchoolClassFragment;
import com.knowlounge.gcm.GcmRegistStatePreference;
import com.knowlounge.gcm.RegistrationIntentService;
import com.knowlounge.inapp.InAppRootDispatcher;
import com.knowlounge.login.LoginActivity;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.network.restful.command.ApiCommand;
import com.knowlounge.network.restful.command.AuthApiCommand;
import com.knowlounge.network.restful.command.ProfileApiCommand;
import com.knowlounge.network.restful.zico.command.AuthRestCommand;
import com.knowlounge.premium.PremiumWebViewActivity;
import com.knowlounge.receiver.NetworkStateReceiver;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.GoogleAnalyticsService;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.RuntimePermissionChecker;
import com.knowlounge.view.room.RoomActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import cz.msebera.android.httpclient.Header;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Minsu on 2016-03-14.
 */
public class MainActivity extends BaseActivity implements
        ViewPager.OnPageChangeListener,
        MainLeftNavFragment.OnMainLeftNavEvent,
        ActivityCompat.OnRequestPermissionsResultCallback,
        StarShopFragment.InAppBillingListener {

    private final String TAG = "MainActivity";

    public static MainActivity _instance;
    private boolean isFirst = true;

    public ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    // 푸시 관련
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String deviceToken;

    private WenotePreferenceManager prefManager;

    private static int CODE_SIGN_IN = 1001;
    private static int CODE_SIGN_OUT = 1002;

    private Toolbar mToolbar;
    private ActionBar actionBar;

    private Handler handler = new Handler();
    int exitCount;

    private List<Fragment> fragments;
    private int pagerIdx = 0;

    private float density;

    public DrawerLayout mDrawerLayout;
    LinearLayout mLeftDrawerContent;
    FrameLayout mRightDrawerContent;
    LinearLayout mMainContainer;
    LinearLayout mTouchBlockLayout;

    CustomSpinner actionBarSpinner;
    SpinnerAdapter mSpinnerAdapter;

    View actionBarView;
    private String roomCode;
    private boolean isCalledExternal = false;

    private long mLastClickTime = 0;

    private NetworkStateReceiver networkStateReceiver = null;
    private AlertDialog noNetwork;
    private boolean isSettingsThrough = false;

    private ArrayList<reNetworkConnected> mNetworkReconnected;

    public interface reNetworkConnected {
        void reConnectedNetwork();
    }

    public void addNetworkReconnected(reNetworkConnected listener) {
        if (mNetworkReconnected == null) {
            mNetworkReconnected = new ArrayList<reNetworkConnected>();
            mNetworkReconnected.add(listener);
        } else {
            mNetworkReconnected.add(listener);
        }
    }

    public void removeNetworkReconnected(reNetworkConnected listener) {
        if (mNetworkReconnected == null)
            return;
        mNetworkReconnected.remove(listener);
    }


    private InAppRootDispatcher mDispatcher;
    private InAppRootDispatcher.InAppListener inAppListener = new InAppRootDispatcher.InAppListener() {
        @Override
        public void onFinished() {
            // 실제 구매가 완료가 되었을 때, UI 또는 기타 처리는 여기에서 하면 됩니다
            getStarBalance();
        }
    };

    protected RuntimePermissionChecker mRuntimePermissionChecker;
    private static final int PERMISSION_REQUEST_CODE = 3;

    // 필수 퍼미션 정의
    public String[] essentialPermissions = new String[]{
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public String[] mediaPermissions = new String[] {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isOpenedSetProfileActivity = false;
    public boolean reloadFlag = true;

    public ProgressDialog mProgressDialog;

    private Intent paramIntent;

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        // 구글 Analytics 통계
        GoogleAnalyticsService.get().sendAnalyticsEvent(getClass().getSimpleName(), "Main");
        GoogleAnalyticsService.get().sendAnalyticsScreen("Main");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);


        _instance = this;
        prefManager = WenotePreferenceManager.getInstance(this);
        paramIntent = getIntent();

        //InApp 처리 관련 위임
        mDispatcher = new InAppRootDispatcher(this);
        mDispatcher.setListener(inAppListener);
        mDispatcher.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FF2DCD55"));
        }

        if (prefManager.getMyUserType().equals("0") && !prefManager.isProfileSkip()) {
            Log.d(TAG, "프로필 창 띄움");a
            isOpenedSetProfileActivity = true;
            final Intent intent = new Intent(getApplicationContext(), ProfileInitActivity.class);
            // MainActivity의 onCreate()시에 바로 ProfileInitActivity가 열리면 액티비티 스택의 밑으로 내려가는 MainActivity는 onPause가 호출되지 않을 수 있는데 이런 현상은 에러를 유발시킴..
            // 때문에 강제로 딜레이를 주는 방어코드를 작성함..
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                }
            }, 1000);

        }

        setContentView(R.layout.activity_intro);
        initViewPager();   // Intialize ViewPager
        getCurrentVersion();
        getStarBalance();

        prefManager.setOrientation(getScreenOrientation());

        if (paramIntent != null) {
            isCalledExternal = true;

        } else {
            isCalledExternal = false;
        }

//        if (getIntent().hasExtra("code")) {
//            isCalledExternal = true;
//        } else {
//            isCalledExternal = false;
//        }

        mRuntimePermissionChecker = new RuntimePermissionChecker(this);


        // 디바이스 dpi 계산
//        DisplayMetrics metrics = new DisplayMetrics();
//        WindowManager mgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        mgr.getDefaultDisplay().getMetrics(metrics);
//
//        density = metrics.densityDpi / 160;

        density = KnowloungeApplication.density;

        prefManager.setDensity(density);

        // 액션바 설정
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        actionBar = getSupportActionBar();

        actionBarView = getLayoutInflater().inflate(R.layout.actionbar_intro_custom, null, false);
        ActionBar.LayoutParams actionbarParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        setConfigurationSettingUI(this.getResources().getConfiguration());


        String[] itemTitleArr = getResources().getStringArray(R.array.main_nav_item_title_array);
        int[] itemIconArr = {
                R.drawable.ico_maintop_left_sidemenu_home_off, R.drawable.ico_maintop_left_sidemenu_home_color,
                R.drawable.ico_maintop_left_sidemenu_myclass_off,  R.drawable.ico_maintop_left_sidemenu_myclass_color,
                R.drawable.ico_maintop_left_sidemenu_friendclass_off, R.drawable.ico_maintop_left_sidemenu_friendclass_color,
                R.drawable.ico_maintop_left_sidemenu_publicclass_off,  R.drawable.ico_maintop_left_sidemenu_publicclass_color,
                R.drawable.ico_maintop_left_sidemenu_schoolclass_off,  R.drawable.ico_maintop_left_sidemenu_schoolclass_color};

        actionBarSpinner = (CustomSpinner) actionBarView.findViewById(R.id.intro_spinner);
        mSpinnerAdapter = new SpinnerAdapter(getApplicationContext(), itemTitleArr, itemIconArr, prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET ? false : true);
        actionBarSpinner.setAdapter(mSpinnerAdapter);

        actionBarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewPager.setCurrentItem(position);
                mSpinnerAdapter.setCurrentPositon(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {  // 테블릿 장비일 때..
//            String[] itemTitleArr = getResources().getStringArray(R.array.main_nav_item_title_array);
//            int[] itemIconArr = {
//                    R.drawable.ico_maintop_left_sidemenu_home_off, R.drawable.ico_maintop_left_sidemenu_home_color,
//                    R.drawable.ico_maintop_left_sidemenu_myclass_off,  R.drawable.ico_maintop_left_sidemenu_myclass_color,
//                    R.drawable.ico_maintop_left_sidemenu_friendclass_off, R.drawable.ico_maintop_left_sidemenu_friendclass_color,
//                    R.drawable.ico_maintop_left_sidemenu_publicclass_off,  R.drawable.ico_maintop_left_sidemenu_publicclass_color,
//                    R.drawable.ico_maintop_left_sidemenu_schoolclass_off,  R.drawable.ico_maintop_left_sidemenu_schoolclass_color};
//
//            actionBarSpinner = (CustomSpinner) actionBarView.findViewById(R.id.intro_spinner);
//            mSpinnerAdapter = new SpinnerAdapter(getApplicationContext(), itemTitleArr, itemIconArr, false);
//            actionBarSpinner.setAdapter(mSpinnerAdapter);
//
//            actionBarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    mViewPager.setCurrentItem(position);
//                    mSpinnerAdapter.setCurrentPositon(position);
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//
//                }
//            });
//        } else {
//            String[] itemTitleArr = getResources().getStringArray(R.array.main_nav_item_title_array);
//            int[] itemIconArr = {
//                    R.drawable.ico_maintop_left_sidemenu_home_off, R.drawable.ico_maintop_left_sidemenu_home_color,
//                    R.drawable.ico_maintop_left_sidemenu_myclass_off,  R.drawable.ico_maintop_left_sidemenu_myclass_color,
//                    R.drawable.ico_maintop_left_sidemenu_friendclass_off, R.drawable.ico_maintop_left_sidemenu_friendclass_color,
//                    R.drawable.ico_maintop_left_sidemenu_publicclass_off,  R.drawable.ico_maintop_left_sidemenu_publicclass_color,
//                    R.drawable.ico_maintop_left_sidemenu_schoolclass_off,  R.drawable.ico_maintop_left_sidemenu_schoolclass_color};
//
//            actionBarSpinner = (CustomSpinner) actionBarView.findViewById(R.id.intro_spinner);
//            mSpinnerAdapter = new SpinnerAdapter(getApplicationContext(), itemTitleArr, itemIconArr, true);
//            actionBarSpinner.setAdapter(mSpinnerAdapter);
//
//            actionBarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    mViewPager.setCurrentItem(position);
//                    mSpinnerAdapter.setCurrentPositon(position);
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//
//                }
//            });
//        }

        actionBarView.findViewById(R.id.btn_left_nav_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!KnowloungeApplication.isNetworkConnected)
                    return;

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

//                Intent leftNavIntent = new Intent(MainActivity.this, MainLeftNavActivity.class);
//                startActivityForResult(leftNavIntent, GlobalCode.CODE_MAIN_NAV_OPEN);

                if (!mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
                    mDrawerLayout.openDrawer(mLeftDrawerContent);
                    findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);
                } else {
                    mDrawerLayout.closeDrawer(mLeftDrawerContent);
                }

                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                }

                MainLeftNavFragment mainNavFragment = new MainLeftNavFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_left_nav_content, mainNavFragment, "MainLeftNav").commit();

            }
        });

        actionBarView.findViewById(R.id.enter_room_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!KnowloungeApplication.isNetworkConnected)
                    return;

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                FragmentManager fm = getSupportFragmentManager();
                DirectEnterDialogFragment dialogFragment = new DirectEnterDialogFragment();
                dialogFragment.show(fm, "direct_enter");
            }
        });

        if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
            actionBarView.findViewById(R.id.btn_actionbar_create_room).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (!KnowloungeApplication.isNetworkConnected)
                    return;

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                createRoom();
                }
            });
        }

        actionBarView.findViewById(R.id.btn_actionbar_alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!KnowloungeApplication.isNetworkConnected)
                    return;

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                prefManager.clearNotiBadgeCount();
                AndroidUtils.setBadge(getApplicationContext(),prefManager.getNotiBadgeCount());
                checkNotiBadgeCount();

                if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
                    Intent intent = new Intent(getApplicationContext(), NoticeTabletActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), NoticeActivity.class);
                    startActivity(intent);
                }
            }
        });

        actionBarView.findViewById(R.id.btn_actionbar_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!KnowloungeApplication.isNetworkConnected)
                    return;

                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                    AndroidUtils.keyboardHide(MainActivity.this);
                } else {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);


                }
            }
        });

        if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
            ((ImageView)findViewById(R.id.btn_float_create_room)).setImageResource(R.drawable.btn_floatingaction_makeroom_phone);
        } else {
            ((ImageView)findViewById(R.id.btn_float_create_room)).setImageResource(R.drawable.btn_floatingaction_makeroom_tablet);
        }

        findViewById(R.id.btn_float_create_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!KnowloungeApplication.isNetworkConnected)
                    return;

                createRoom();
            }
        });

        actionBar.setCustomView(actionBarView, actionbarParams);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);  // Actionbar title 설정
        actionBar.setDisplayUseLogoEnabled(false);  // Actionbar logo 설정

        mToolbar.setContentInsetsAbsolute(0, 0);
        mToolbar.setPadding(0, 0, 0, 0);


        // GCM관련 Initialize
        registBroadcastReceiver();
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Log.d(TAG, "startService");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        Bundle paramBundle = getIntent().getExtras();
        if (paramBundle != null) {
            String queryStr = prefManager.getCookieQueryStr();

            Bundle params = new Bundle();
            params.putString("querystring", queryStr);
            mPagerAdapter.getItem(0).setArguments(params);
            mPagerAdapter.getItem(1).setArguments(params);

        } else {  // 비로그인 유저

        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.invite_drawer_in_main_activity);

        mLeftDrawerContent = (LinearLayout)findViewById(R.id.main_left_nav);
        mRightDrawerContent = (FrameLayout)findViewById(R.id.main_right_friend_list);

        mMainContainer = (LinearLayout)findViewById(R.id.main_activity_container);

        // InviteFragment 추가
        SNSFriendListDepthOneFragment fragment = new SNSFriendListDepthOneFragment();
        Bundle argument = new Bundle();
        argument.putInt("type", GlobalConst.THROUGHT_MAIIN);
        fragment.setArguments(argument);

        getSupportFragmentManager().beginTransaction().replace(mRightDrawerContent.getId(), fragment, "FriendList").commit();

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Log.d(TAG, "offset : " + slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Log.d(TAG, "onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportFragmentManager().popBackStack();
                //Log.d(TAG, "onDrawerClosed");
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                mMainContainer.setLayoutParams(lp);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        //DrawerLayout 속성값 설정
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mDrawerLayout.requestDisallowInterceptTouchEvent(true);
        mDrawerLayout.closeDrawer(Gravity.RIGHT);

        findViewById(R.id.deemed_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
                    closeLeftNavDrawer();
                }
            }
        });

        if (mRegistrationBroadcastReceiver != null) {
//            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.REGISTRATION_READY));
//            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.REGISTRATION_PROCESSING));
//            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.REGISTRATION_COMPLETE));
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.PUSH_RECEIVED));
        }

//        Context ctx = getApplicationContext();
//        Intent intent = new Intent(ctx, ProfileInitActivity.class);
//        startActivity(intent);

        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.setOnChangeNetworkStatusListener(networkStateListener);

        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        mTouchBlockLayout = (LinearLayout)findViewById(R.id.touch_block_layout);
        mTouchBlockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNoNetworkAlertDialog();
            }
        });
        mTouchBlockLayout.setVisibility(View.GONE);

        getProfile();

        requestEssentialPermission(essentialPermissions, PERMISSION_REQUEST_CODE);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        Bundle savedState = intent.getExtras();
        if(savedState != null) {
            Log.d(TAG, "leave_class : " + savedState.getBoolean("leave_class"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "<onResume / Knowlounge>");

        String type = paramIntent.getStringExtra("type");
        roomCode = paramIntent.getStringExtra("code");

        Log.d(TAG, "<onResume / Knowlounge> roomCode : " + roomCode);

        isSettingsThrough = false;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(pagerIdx);
        }
        checkNotiBadgeCount();
        //getProfileRestClient();

        if (isCalledExternal) {
            if (TextUtils.equals(type, "knowlounge")) {
                String passwd = "";
                String tokenStr = "roomcode=" + roomCode + "&passwd=" + passwd;
                Log.d(TAG, "<onResume / Knowlounge> roomCode : " + roomCode);
                enterRoom(roomCode);
            } else if (TextUtils.equals(type, "premium")) {
                String userNo = paramIntent.getStringExtra("userno");
                Intent premiumIntent = new Intent(this, PremiumWebViewActivity.class);
                premiumIntent.putExtra("userno", userNo);
                premiumIntent.putExtra("code", roomCode);
                startActivity(premiumIntent);

            }

        }
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "<onPause / Knowlounge>");
        super.onPause();
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "<onStop / Knowlounge>");
        //앱 실행 상태 체크
        //closeLeftNav();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "<onDestroy / Knowlounge>");
        super.onDestroy();
        mDispatcher.onDestroy();
        if (mRegistrationBroadcastReceiver !=null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        }
        if(networkStateReceiver != null)
            unregisterReceiver(networkStateReceiver);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
            } else {
                closeLeftNavDrawer();
            }
            return;
        }
        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            if (exitCount == 1) {
                _instance = null;
                ActivityCompat.finishAffinity(this);
            } else {
                exitCount++;
                if (_instance != null) {
                    Toast.makeText(this, getString(R.string.toast_turnoff), Toast.LENGTH_SHORT).show();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitCount = 0;
                        }
                    }, 2000);
                } else {
                    _instance = null;
                    ActivityCompat.finishAffinity(this);
                }

                return;
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState / outState : " + outState.toString());
        //super.onSaveInstanceState(outState);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setConfigurationSettingUI(newConfig);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mDispatcher.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        if (requestCode == GlobalCode.CODE_ENTER_ROOM) {
            Log.d(TAG, "enter room..");
        } else if (requestCode == GlobalCode.CODE_ENTER_ROOM_WITH_ROOM_CODE) {
            Log.d(TAG, "room code enter room..");
        }

        if (resultCode == GlobalCode.CODE_MAIN_NAV_CLOSE) {
            Log.d(TAG, "reloadFlag : " + reloadFlag);
            reloadFlag = false;
        } else if (resultCode == GlobalCode.CODE_EXIT_ROOM) {
            Log.d(TAG, "reloadFlag : " + reloadFlag);
            reloadFlag = true;
            if(reloadFlag && data != null) {
                String roomCode = data.getStringExtra("roomcode");
                enterRoom(roomCode);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<String> request = new ArrayList<String>();
                int idx = 0;
                for(int resultCode : grantResults) {
                    if (resultCode == PackageManager.PERMISSION_DENIED) {
                        request.add(permissions[idx++]);
                    }
                }

                if (!request.isEmpty()) {
                    showAlertEssentialPermissions(request.toArray(new String[request.size()]));

                } else {
                    Intent intent = getIntent();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    startActivity(intent);
                }
            }
        }
    }


    /** ViewPager.OnPageChangeListener Override +*/
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
        pagerIdx = position;
        actionBarSpinner.setSelection(position);
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub
    }


    /** MainLeftNavFragment.SetMainLeftDrawerListener 관련 **/
    @Override
    public void onCurrentPage(int page) {
        mViewPager.setCurrentItem(page);
    }


    @Override
    public void onLogOut() {
        signOut();
    }


    /**
     * StarShopFragment.InAppBillingListener 오버라이드..
     *
     *
     **/
    @Override
    public void onStarShopItemClicked(String productId) {
        mDispatcher.start(productId);
    }


    public void closeLeftNavDrawer() {
        Animation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(100);
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


        if (mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.closeDrawer(mLeftDrawerContent);
        }
    }


    private void initViewPager() {
        fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, HomeClassFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, MyClassFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, FriendClassFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, PublicClassFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, SchoolClassFragment.class.getName()));

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mViewPager.setAdapter(this.mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
    }


    private void setConfigurationSettingUI(Configuration newConfig){
        if (!KnowloungeApplication.isPhone) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                actionBarView.findViewById(R.id.enter_room_code_text).setVisibility(View.VISIBLE);
                actionBarView.findViewById(R.id.create_room_text).setVisibility(View.VISIBLE);
                actionBarView.findViewById(R.id.alert_text).setVisibility(View.VISIBLE);
                actionBarView.findViewById(R.id.invite_text).setVisibility(View.VISIBLE);
            } else {
                actionBarView.findViewById(R.id.enter_room_code_text).setVisibility(View.GONE);
                actionBarView.findViewById(R.id.create_room_text).setVisibility(View.GONE);
                actionBarView.findViewById(R.id.alert_text).setVisibility(View.GONE);
                actionBarView.findViewById(R.id.invite_text).setVisibility(View.GONE);
            }
        }
    }


    private void requestEssentialPermission(String[] permissions, int requestId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<String> needPermissions = new ArrayList<String>();
            needPermissions.clear();

            for (String permission : permissions) {
                if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    needPermissions.add(permission);
                }
            }

            if (!needPermissions.isEmpty()) {
                String[] requestArray = needPermissions.toArray(new String[needPermissions.size()]);
                this.requestPermissions(requestArray, requestId);
            }
        }
    }


    private void showAlertEssentialPermissions(String[] permissions) {
        final List<String> rationales = new ArrayList<>();
        for (String str : permissions) {
            rationales.add(str);
        }
        Log.d(TAG, "showAlertEssentialPermissions / 거부된 퍼미션 목록 : " + rationales.toString());

        if (!rationales.isEmpty()) {
            StringBuilder message = new StringBuilder(getString(R.string.permission_alert_head_optional));
            List<String> missing = mRuntimePermissionChecker.getPermissionDisplayName(rationales);
            for (String perm : missing) {
                message.append("- ");
                message.append(perm);
                message.append("\n");
            }
            message.append(getString(R.string.permission_alert_footer));

            // TODO AlertDialog는 모두 알로 테마 다이얼로그로 교체할 것.
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage(message.toString())
                    .setCancelable(false)
                    .setNegativeButton(getResources().getString(R.string.global_exit), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.finishAffinity(_instance);
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.global_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);

                        }
                    }).show();
        }
    }


    public void reloadAuthInfo() {
        Log.d(TAG, "reloadAuthInfo");

        new AuthApiCommand()
                .command("reloadAuth")
                .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
                .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                    @Override
                    public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                        observer.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "[RxJava / reloadAuth] onCompleted");

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "[RxJava / reloadAuth] onError");
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onNext(JsonObject object) {
                                        Log.d(TAG, "[RxJava / reloadAuth] onNext");

                                        int apiResult = object.get("result").getAsInt();
                                        if(apiResult == 0) {
                                            String reloadedMasterCookie = object.get("cookie").getAsJsonObject().get("FBMMC").getAsString();
                                            String reloadedCheckSumCookie = object.get("cookie").getAsJsonObject().get("FBMCS").getAsString();

                                            String newQueryStr = "FBMMC=" + reloadedMasterCookie + "&FBMCS=" + reloadedCheckSumCookie;

                                            prefManager.setUserCookie(reloadedMasterCookie);
                                            prefManager.setChecksumCookie(reloadedCheckSumCookie);
                                            prefManager.setCookieQueryStr(newQueryStr);
                                        }
                                    }
                                });
                    }
                }).execute();


//        String masterCookie = prefManager.getUserCookie();
//        String checkSumCookie = prefManager.getChecksumCookie();
//
//        RestClient.postWithCookie("auth/reload.json", masterCookie, checkSumCookie, new RequestParams(), new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                try {
//                    int apiResult = response.getInt("result");
//
//                    if (apiResult == 0) {
//                        String reloadedMasterCookie = response.getJSONObject("cookie").getString("FBMMC");
//                        String reloadedCheckSumCookie = response.getJSONObject("cookie").getString("FBMCS");
//
//                        String newQueryStr = "FBMMC=" + reloadedMasterCookie + "&FBMCS=" + reloadedCheckSumCookie;
//
//                        prefManager.setUserCookie(reloadedMasterCookie);
//                        prefManager.setChecksumCookie(reloadedCheckSumCookie);
//                        prefManager.setCookieQueryStr(newQueryStr);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d(TAG, "auth/reload.json failed.. statusCode : " + statusCode);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                Log.d(TAG, "onFailure - statusCode : " + statusCode);
//                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
//                if (throwable instanceof IOException) {
//                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }


    private void showProfileDialog() {
        String msg = getResources().getString(R.string.confirm_setprofile);
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance, R.style.AlertDialogCustom);
        builder.setMessage(msg).setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Context ctx = getApplicationContext();
                        Intent intent = new Intent(ctx, ProfileInitActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefManager.setProfileSkip(true);
                    }
                });
        AlertDialog confirm = builder.create();
        confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        confirm.setCanceledOnTouchOutside(true);
        confirm.show();
    }


    @Override
    public void openAppConfig() {
        AppConfigFragment appConfigFragment = new AppConfigFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_left_nav_content, appConfigFragment, "AppConfig");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void openHelpDesk() {
        HelpDeskFragment helpFragment = new HelpDeskFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_left_nav_content, helpFragment, "HelpDesk");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void openSharShop() {
        if(!mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.openDrawer(mLeftDrawerContent);
            findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);
        }
        StarShopFragment starShopFragment = new StarShopFragment();
        Bundle params = new Bundle();
        params.putString("parent_activity", "MainActivity");
        starShopFragment.setArguments(params);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_left_nav_content, starShopFragment, "StarShop");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void openUserProfile() {
        if(!mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.openDrawer(mLeftDrawerContent);
            findViewById(R.id.deemed_layer).setVisibility(View.VISIBLE);
        }
        ProfileFragment profileFragment = new ProfileFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_left_nav_content, profileFragment , "Profile");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void openUserProfieEdit(Bundle arguments) {
        ProfileEditFragment mainleftMenuProfileEditFragment = new ProfileEditFragment();
        mainleftMenuProfileEditFragment.setArguments(arguments);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_left_nav_content, mainleftMenuProfileEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void openUserProfileEditMulti(Bundle arguments) {
        ProfileEditMultiFragment mainleftMenuProfileMultiFragment = new ProfileEditMultiFragment();
        mainleftMenuProfileMultiFragment.setArguments(arguments);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_left_nav_content, mainleftMenuProfileMultiFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void popFragmentStack() {
        getSupportFragmentManager().popBackStack();
    }


    public void closeLeftNav() {
        if(mDrawerLayout.isDrawerOpen(mLeftDrawerContent)) {
            mDrawerLayout.closeDrawer(mLeftDrawerContent);
            findViewById(R.id.deemed_layer).setVisibility(View.GONE);
        }
    }


    /**
     * Retrofit + OkHttp + RxJava
     * ProfileApiCommand -> RestApiFactory에서 API 생성 ->
     */
    public void getProfile() {
        new ProfileApiCommand()
            .command("getProfile")
            .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
            .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                @Override
                public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                    observer.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JsonObject>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "[RxJava] onCompleted");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "[RxJava] onError");
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(JsonObject object) {
                                    Log.d(TAG, "[RxJava] onNext");

                                    JsonObject resultMap = object.getAsJsonObject("map");
                                    Log.d(TAG, "Retrofit & OkHttp & RxJava API Call result : " + resultMap.toString());

                                    initializeUserProfile(object);
                                }
                            });
                }
            }).execute();
    }


    private void initializeUserProfile(JsonObject obj) {
        Log.d(TAG, "initializeUserProfile");
        obj = obj.getAsJsonObject("map");
        String userType = obj.get("usertype").getAsString();
        prefManager.setMyUserType(userType);

        Log.d(TAG, "[initializeUserProfile] userType : " + userType);
        if (TextUtils.equals(userType, "0") && !prefManager.isProfileSkip()) {
            if (!isOpenedSetProfileActivity) {
                Log.d(TAG, "프로필 초기설정 엑티비티 띄움");
                Context ctx = getApplicationContext();
                Intent intent = new Intent(ctx, ProfileInitActivity.class);
                startActivity(intent);
            }
        } else if (userType.equals("0") && prefManager.isProfileSkip() && isFirst) {
            Log.d(TAG, "프로필 alert 띄움");
            showProfileDialog();
        } else {
            Log.d(TAG, "프로필 다음에 설정하기..");
            prefManager.setEmail(obj.get("email").getAsString());
            prefManager.setUserNm(obj.get("usernm").getAsString());
            prefManager.setMyeducation(covertJsonArrayToString("value", obj.get("school").getAsJsonArray()));
            prefManager.setUserThumbnailLargeCurrent(obj.get("thumbnail").getAsString());
            prefManager.setUserThumbnailLargeLast(obj.get("thumbnail").getAsString());
            prefManager.setMyGradeCode(covertJsonArrayToString("code", obj.get("grade").getAsJsonArray()));
            prefManager.setMyGradeName(covertJsonArrayToString("name", obj.get("grade").getAsJsonArray()));
            prefManager.setMySubjectCode(covertJsonArrayToString("code", obj.get("subject").getAsJsonArray()));
            prefManager.setMySubjectName(covertJsonArrayToString("name", obj.get("subject").getAsJsonArray()));
            prefManager.setMyLanguageCode(covertJsonArrayToString("code", obj.get("language").getAsJsonArray()));
            prefManager.setMyLanguageName(covertJsonArrayToString("name", obj.get("language").getAsJsonArray()));
            prefManager.setMyIntroduction(obj.get("introduction").getAsString());
            if (prefManager.isProfileChanged()) {
                Log.d(TAG, "프로필 업데이트 했음..");
                reloadAuthInfo();
                prefManager.setChangeProfile(false);
            }
            if (getSupportFragmentManager().findFragmentByTag("MainLeftNav") != null) {
                ((MainLeftNavFragment) getSupportFragmentManager().findFragmentByTag("MainLeftNav")).updateUI();
            }
        }
        isFirst = false;
    }


    public void getProfileRestClient() {
        Log.d(TAG, "getProfileRestClient");

        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RestClient.getWithCookie("profile/getProfile.json",masterCookie,checksumCookie, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "send message success.. response : " + obj.toString());
                try {
                    obj = obj.getJSONObject("map");
                    String usertype = obj.getString("usertype");
                    prefManager.setMyUserType(usertype);
                    if (usertype.equals("0") && !prefManager.isProfileSkip()) {
                        if (!isOpenedSetProfileActivity) {
                            Log.d(TAG, "프로필 창 띄움");
                            Context ctx = getApplicationContext();
                            Intent intent = new Intent(ctx, ProfileInitActivity.class);
                            startActivity(intent);
                        }
                    } else if (usertype.equals("0") && prefManager.isProfileSkip() && isFirst) {
                        Log.d(TAG, "프로필 alert 띄움");
                        showProfileDialog();
                    } else {
                        Log.d(TAG, "프로필 다음에 설정하기..");
                        if (prefManager.isProfileChanged()) {
                            Log.d(TAG, "프로필 업데이트 했음..");
                            prefManager.setEmail(obj.getString("email"));
                            prefManager.setUserNm(obj.getString("usernm"));
                            prefManager.setMyeducation(Listting(obj.getJSONArray("school"),"value"));
                            prefManager.setUserThumbnailLargeCurrent(obj.getString("thumbnail"));
                            prefManager.setUserThumbnailLargeLast(obj.getString("thumbnail"));
                            prefManager.setMyGradeCode(codeListting(obj.getJSONArray("grade")));
                            prefManager.setMyGradeName(Listting(obj.getJSONArray("grade"),"name"));
                            prefManager.setMySubjectCode(codeListting(obj.getJSONArray("subject")));
                            prefManager.setMySubjectName(Listting(obj.getJSONArray("subject"),"name"));
                            prefManager.setMyLanguageCode(codeListting(obj.getJSONArray("language")));
                            prefManager.setMyLanguageName(Listting(obj.getJSONArray("language"),"name"));
                            prefManager.setMyIntroduction(obj.getString("introduction"));
                            reloadAuthInfo();
                            prefManager.setChangeProfile(false);
                        }
                    }
                    isFirst = false;
                } catch(JSONException j) {
                    j.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if(throwable instanceof IOException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private String covertJsonArrayToString(String key, JsonArray arr) {
        String result = "";
        for (JsonElement obj : arr) {
            result += obj.getAsJsonObject().get(key).getAsString() + ",";
        }
        result = result.length() > 0 ? result.substring(0, result.length()-1) : "";
        return result;
    }


    private String codeListting(JSONArray arr) {
        String result = "";
        try {
            for (int i=0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                result += obj.getString("code")+",";
            }
            result = result.length() >0 ? result.substring(0, result.length()-1) : "";

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String Listting(JSONArray arr, String flag) {
        String result = "";
        try {
            for (int i=0; i<arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                result += obj.getString(flag) + ",";
            }
            result = result.length() > 0 ? result.substring(0, result.length()-1) : "";

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    private void checkNotiBadgeCount() {
        if (prefManager.getNotiBadgeCount() != 0) {
            actionBarView.findViewById(R.id.main_badge_count).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.main_badge_count)).setText(Integer.toString(prefManager.getNotiBadgeCount()));
        } else {
            actionBarView.findViewById(R.id.main_badge_count).setVisibility(View.GONE);
        }
    }


    public void registBroadcastReceiver() {
        Log.d(TAG, "registBroadcastReceiver");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkNotiBadgeCount();
                String action = intent.getAction();
                if (action.equals(GcmRegistStatePreference.REGISTRATION_COMPLETE)) {
                    // 액션이 COMPLETE일 경우
                    final String token = intent.getStringExtra("registrationId");
                    Log.d(TAG, "registrationId (GCM Token) : " + token);
                    deviceToken = token;

                } else if (action.equals(GcmRegistStatePreference.PUSH_RECEIVED)) {
                    Bundle pushExtras = intent.getBundleExtra("push");
                    String title = pushExtras.getString("title");
                    String msg = pushExtras.getString("message");
                    String roomCode = ((Integer) Integer.parseInt(pushExtras.getString("code"))).toString();
                    String category = pushExtras.getString("category");
                    Log.d(TAG, "PUSH_RECEIVED - category : " + category);
                    if (category.equals("invite")) {
                        Log.d(TAG, "PUSH_RECEIVED - category : invite");
                        if ((RoomActivity.activity != null && RoomActivity.activity.isFinishing()) || RoomActivity.activity == null) {
                            Log.d(TAG, "RoomActivityTemp가 보이지 않네요.. Index에 메세지를 띄웁니다.");
                            if (AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
                                setAlertDialog(title, msg, roomCode);
                            }
                        } else {
                            return;
                        }
                    } else if (category.equals("reqjoin")) {
                        Log.d(TAG, "PUSH_RECEIVED - category : reqjoin");
                        Log.d(TAG, "title : " + title);
                        Log.d(TAG, "msg : " + msg);
                        Log.d(TAG, "roomCode : " + roomCode);
                        if ((RoomActivity.activity != null && RoomActivity.activity.isFinishing()) || RoomActivity.activity == null) {
                            if (AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
                                setAlertDialog(title, msg, roomCode);
                            }
                        }

                    } else if (category.equals("extendroom")) {
                        if ((RoomActivity.activity != null && RoomActivity.activity.isFinishing()) || RoomActivity.activity == null) {
                            Log.d(TAG, "RoomActivityTemp가 보이지 않네요.. Index에 메세지를 띄웁니다.");
                            setAlertDialog(title, msg, roomCode);
                        } else {
                            return;
                        }
                    }
                }
            }
        };
    }


    public void setAlertDialog(String title, String msg, final String roomCode) {
        final String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
        AlertDialog.Builder builder = new AlertDialog.Builder(_instance, R.style.AlertDialogCustom);
        builder.setMessage(msg).setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        enterRoom(roomCode);

//                        Context ctx = getApplicationContext();
//                        Intent mainIntent = new Intent(ctx, RoomActivity.class);
//                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mainIntent.putExtra("roomurl", roomUrl);
//                        startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog confirm = builder.create();
        confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        confirm.setCanceledOnTouchOutside(true);
        confirm.setTitle(title);
        confirm.show();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    private void getStarBalance() {
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


    public void createRoom() {

        //requestEssentialPermission(mediaPermissions, PERMISSION_REQUEST_CODE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needPermissions = new ArrayList<String>();
            needPermissions.clear();
            for (String permission : mediaPermissions) {
                if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    needPermissions.add(permission);
                }
            }
            if (!needPermissions.isEmpty()) {
                String[] requestArray = needPermissions.toArray(new String[needPermissions.size()]);
                this.requestPermissions(requestArray, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.now_loading) , true);

        final String deviceId = prefManager.getDeviceId();
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RequestParams params = new RequestParams();
        params.put("deviceid", deviceId);
        params.put("openflag", prefManager.getEstablish() == GlobalConst.ESTABLISH_ALL_PUBLIC ? 1 : 0);

        String apiUrl = getResources().getString(R.string.api_create_room);
        RestClient.postWithCookie(apiUrl, masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "canvas/add.json success / response : " + response.toString());
                try {
                    String result = response.getString("result");
                    if ("0".equals(result)) {
                        String roomCode = response.getString("code");
                        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;

                        /*
                        Intent mainIntent = new Intent(_instance, RoomActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mainIntent.putExtra("roomurl", roomUrl);
                        mainIntent.putExtra("mode", GlobalConst.CREATE_ROOM_MODE);
                        startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM);
                        */

                        JsonObject extraParams = new JsonObject();
                        extraParams.addProperty("type", "knowlounge");
                        extraParams.addProperty("roomurl", roomUrl);
                        extraParams.addProperty("deviceid", deviceId);
                        extraParams.addProperty("mode", GlobalConst.CREATE_ROOM_MODE);

                        navigateRoom(roomCode, prefManager.getUserNo(), extraParams);



                        GlobalConst.reLoadingRoomList = true;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mProgressDialog.dismiss();
                Log.d(TAG, "Create room onFailure");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                mProgressDialog.dismiss();
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if (throwable instanceof IOException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void signOut() {
        String deviceId = prefManager.getDeviceId();
        String osType = "android";

        RequestParams params = new RequestParams();
        params.put("deviceid", deviceId);
        params.put("ostype", osType);

        String apiUrl = getResources().getString(R.string.api_signout);
        RestClient.postWithCookie(apiUrl, prefManager.getUserCookie(), prefManager.getChecksumCookie(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Sign out success.. response : " + response.toString());
                unsubscribeSiPlatform();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Sign out onFailure");
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


    private void unsubscribeSiPlatform() {
        String url = "user/push/public?"
                + "appId=" + "knowlounge"
                + "&pushPlatform=" + "gcm"
                + "&pushToken=" + prefManager.getDeviceToken();

        RestClient.deleteSiPlatform(url, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "SI Platform Sign out success.. response : " + response.toString());
                prefManager.clear();
                closeLeftNavDrawer();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent signOutIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(signOutIntent);
                        finish();

                    }
                }, 400);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // TODO : 구독 설정 실패..
                Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
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
    }    //해제



    public void getCurrentVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);

            ApplicationInfo ai = getApplicationInfo();
            String sourceDir = ai.publicSourceDir ;

            String hash = getHash(sourceDir).toString();
            String version = pi.versionName;
            String os = "android";

            IsCurrentVersion(os, version, hash);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void IsCurrentVersion(String os, String version, String hash) throws Exception {

        String tokenStr = "os=" + os + "&version=" + version + "&hash=" + hash;
        AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
        String encryptToken = aesUtilObj.encrypt(tokenStr);

        RequestParams params = new RequestParams();
        params.put("token", encryptToken);
        RestClient.post("version/check.json", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String result = response.getString("result");

                    if (result.equals("0")) {
                        //Todo 성공적일때 islast == true
                        JSONObject map = response.getJSONObject("map");
                        JSONObject guide = map.getJSONObject("guide");

                        GlobalConst.MOVIE_CHANNEL = guide.getString("channel");
                        GlobalConst.MOVIE_INVITE = guide.getString("movie_invite");
                        GlobalConst.MOVIE_APPLY = guide.getString("movie_apply");
                        GlobalConst.MOVIE_CANVAS = guide.getString("movie_canvas");
                        GlobalConst.VIDEO_LIMIT = map.getString("videolimit");

                        long globalTimeTurm  = AndroidUtils.getGlobalTimeTurm(getApplicationContext(),map.getString("currenttime"));
                        prefManager.setGlobalTimeTurm(globalTimeTurm);

                        boolean isLast = map.getBoolean("islast");
                        String versionMode = map.getString("mode");
                        if (!isLast) {
                            if (!TextUtils.equals(versionMode, "")) {
                                Bundle argument = new Bundle();
                                argument.putString("type", map.getString("mode"));
                                argument.putString("msg", map.getString("msg"));
                                FragmentManager fm = getSupportFragmentManager();
                                VersionCheckDialogFragment dialogFragment = new VersionCheckDialogFragment();
                                dialogFragment.setArguments(argument);
                                dialogFragment.show(fm, "version");
                            }
                        }
                    } else if (result.equals("-602") || result.equals("-601")) {
                        //Todo 해쉬값이 달라짐 다이얼로그 띄우고 확인 누르고 gogole play 고고씽
                        Bundle argument = new Bundle();
                        argument.putString("type","M");
                        argument.putString("msg","");
                        FragmentManager fm = getSupportFragmentManager();
                        VersionCheckDialogFragment dialogFragment = new VersionCheckDialogFragment();
                        dialogFragment.setArguments(argument);
                        dialogFragment.show(fm, "version");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
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


    private CharSequence getHash(String sourceDir)  {
        // TODO Auto-generated method stub

        File file = new File(getApplicationInfo().sourceDir);
        String outputTxt= "";
        String hashcode = null;

        try {
            FileInputStream input = new FileInputStream(file);

            ByteArrayOutputStream output = new ByteArrayOutputStream ();
            byte [] buffer = new byte [65536];
            int l;

            while ((l = input.read (buffer)) > 0)
                output.write (buffer, 0, l);

            input.close ();
            output.close ();

            byte [] data = output.toByteArray ();

            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );

            byte[] bytes = data;

            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append( String.format("%02X", b) );
            }

            hashcode = sb.toString();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hashcode;
    }


    public void enterRoom(final String roomCode) {
        try {

            //requestEssentialPermission(mediaPermissions, PERMISSION_REQUEST_CODE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<String> needPermissions = new ArrayList<String>();
                needPermissions.clear();
                for (String permission : mediaPermissions) {
                    if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                        needPermissions.add(permission);
                    }
                }
                if (!needPermissions.isEmpty()) {
                    String[] requestArray = needPermissions.toArray(new String[needPermissions.size()]);
                    this.requestPermissions(requestArray, PERMISSION_REQUEST_CODE);
                    return;
                }
            }


            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.now_loading) , true);

            String passwd = "";
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
                        isCalledExternal = false;
                        int result = response.getInt("result");
                        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (result == 0) {
                            Log.d(TAG, "<room/check.json> success - result : " + response.toString());
                            JSONObject responseMap = response.getJSONObject("map");
                            String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                            String roomId = responseMap.has("roomid") ? responseMap.getString("roomid") : "";

                            /*
                            Intent mainIntent = new Intent(getApplicationContext(), RoomActivity.class);
                            //mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mainIntent.putExtra("type", "knowlounge");
                            mainIntent.putExtra("roomurl", roomUrl);
                            mainIntent.putExtra("deviceid", deviceId);
                            mainIntent.putExtra("mode", GlobalConst.ENTER_ROOM_MODE);
                            startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM_WITH_ROOM_CODE);
                            */

                            JsonObject extraParams = new JsonObject();
                            extraParams.addProperty("type", "knowlounge");
                            extraParams.addProperty("roomurl", roomUrl);
                            extraParams.addProperty("deviceid", deviceId);
                            extraParams.addProperty("mode", GlobalConst.ENTER_ROOM_MODE);
                            navigateRoom(roomId, prefManager.getUserNo(), extraParams);

                        } else if (result == -201 | result == -8001) {   // Invalid room/
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_join_invalidcode), Toast.LENGTH_SHORT).show();
                        } else if (result == -102) {   // Incorrect password
                            FragmentManager fm = getSupportFragmentManager();
                            RoomPasswdDialogFragment dialogFragment = new RoomPasswdDialogFragment();
                            Bundle args = new Bundle();
                            args.putString("mode", "roomcode");
                            args.putString("roomcode", roomCode);
                            args.putString("deviceid", deviceId);
                            dialogFragment.setArguments(args);
                            dialogFragment.show(fm, "room_passwd");
                        } else if (result == -207) {  // room count limit
                            String roomId = response.getJSONObject("map").getString("roomid");
                            Bundle args = new Bundle();
                            args.putString("roomid", roomId);
                            args.putString("code", roomCode);
                            args.putString("deviceid", deviceId);
                            args.putString("masterno", response.getJSONObject("map").getString("masterno"));

                            ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getSupportFragmentManager(), "extend_user_limit");

//                            StarPayNotiDialogFragment dialogFragment = new StarPayNotiDialogFragment();
//                            dialogFragment.show(fm, "pay_star_noti");

                        } else if (result == -208) {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.global_popup_full), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mProgressDialog.dismiss();
                        closeLeftNav();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "Create room onFailure " + statusCode);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.d(TAG, "onFailure - statusCode : " + statusCode);
                    Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                    if(throwable instanceof IOException) {
                        Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                        if(mProgressDialog != null)
                            mProgressDialog.dismiss();
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateRoom(final String roomId, final String userNo, final JsonObject extraParams) {
        // Step 1. sns oauth token 인증 작업.
        String accessToken = prefManager.getZicoAccessToken();
//        if ("".equals(accessToken)) {
            new AuthRestCommand()
                    .sns(TextUtils.equals(prefManager.getSnsType(), "0") ? "fb" : "gl")
                    .token(prefManager.getAccessToken())
                    .service("knowlounge")
                    .ip(NetworkUtils.getIpAddress())
                    .buildApi()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JsonObject>() {

                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "onCompleted");

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "<getAccessToken / onError / ZICO>");
                            // TODO  인증 실패시 에러 처리
                        /*
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException) e;
                            int code = response.code();

                            if (code >= 200 && code < 300) {
                                // success
                            } else if (code == 401) {
                                // unauthenticated
                            } else if (code >= 400 && code < 500) {
                                // client error
                            } else if (code >= 500 && code < 600) {
                                // server error
                            } else {
                                // unexpected error
                            }

                            error += ": code " + code + " response : " + response.toString();
                        } else if (e instanceof IOException) {
                            // network error
                        } else {
                            // unexpected error
                            error += ": code " + e.toString();
                        }
                        */
                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            Log.d(TAG, "<getAccessToken / onNext / ZICO> result : " + jsonObject.toString());
                            int resultCode = jsonObject.get("result").getAsInt();
                            if (resultCode == 0) {
                                JsonObject data = jsonObject.get("accessToken").getAsJsonObject();
                                String zicoAccessToken = data.get("token").getAsString();
                                prefManager.setZicoAccessToken(zicoAccessToken);

                                int ttl = data.get("ttl").getAsInt();

                                /**
                                 * TODO 차후에 룸넘버 가져오는 루틴 정리할 것.
                                 */
//                            fetchRtcServer(roomNumberEdit.getText().toString());

                                JsonObject params = new JsonObject();
                                params.addProperty("roomid", roomId);
                                params.addProperty("userno", userNo);
                                params.addProperty("usernm", prefManager.getUserNm());
                                params.addProperty("name", "");
                                params.addProperty("host", "");
                                params.addProperty("port", "");
                                params.addProperty("video", true);
                                params.addProperty("audio", true);
                                params.addProperty("volume", true);
                                params.addProperty("token", zicoAccessToken);

                                getAppComponent().navigator().navigateToRoomActivityView(MainActivity.this, params, extraParams);

                            } else if (resultCode == -9001) {
                                Log.d(TAG, "<getAccessToken / ZICO> Invalid sns access token..");
                                //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
//        } else {
//            Log.d(TAG, "<navigateRoom / ZICO> access token already exist..");
//            JsonObject params = new JsonObject();
//            params.addProperty("roomid", roomId);
//            params.addProperty("userno", userNo);
//            params.addProperty("usernm", prefManager.getUserNm());
//            params.addProperty("name", "");
//            params.addProperty("host", "");
//            params.addProperty("port", "");
//            params.addProperty("video", true);
//            params.addProperty("audio", true);
//            params.addProperty("volume", true);
//            params.addProperty("token", accessToken);
//
//            getAppComponent().navigator().navigateToRoomActivityView(MainActivity.this, params, extraParams);
//        }
    }


    NetworkStateReceiver.OnChangeNetworkStatusListener networkStateListener = new NetworkStateReceiver.OnChangeNetworkStatusListener(){
        @Override
        public void onChange(int status) {
            switch (status) {
                case NetworkStateReceiver.NETWORK_CONNECTED :
                    Log.d(TAG,"network connected");
                    if(noNetwork != null && noNetwork.isShowing())
                        noNetwork.dismiss();

                    if(mNetworkReconnected != null) {
                        for (reNetworkConnected listener : mNetworkReconnected)
                            listener.reConnectedNetwork();
                    }
                    actionBarSpinner.setEnabled(true);
                    mTouchBlockLayout.setVisibility(View.GONE);
                    return;

                case NetworkStateReceiver.NETWORK_DISCONNECTED :
                    Log.d(TAG,"network disconnected");
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if(!KnowloungeApplication.isNetworkConnected) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTouchBlockLayout.setVisibility(View.VISIBLE);
                                        setNoNetworkAlertDialog();
                                    }
                                });
                            }
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(timerTask,3000);
                    
                    return;
        }
        }
    };


    public void setNoNetworkAlertDialog() {
        if (noNetwork != null && noNetwork.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(_instance, R.style.AlertDialogCustom);
        builder.setMessage(getResources().getString(R.string.network_disconnected)).setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.network_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        isSettingsThrough = true;
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
        actionBarSpinner.setEnabled(false);
    }


    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " + "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180 :
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270 :
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default :
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " + "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }
        return orientation;
    }


    class PagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;
        /**
         * @param fm
         * @param fragments
         */
        public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        /* (non-Javadoc)
         * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
         */
        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        /* (non-Javadoc)
         * @see android.support.v4.view.PagerAdapter#getCount()
         */
        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }


    public class SpinnerAdapter extends BaseAdapter {
        Context mContext ;
        String[] menuTitle ;
        int[] resourceId ;
        boolean isDevicePhone;
        int currentPosition;

        public SpinnerAdapter(Context context, String[] menuTitle, int[] resourceId, boolean isDevicePhone) {
            this.mContext = context;
            this.menuTitle = menuTitle;
            this.resourceId = resourceId;
            this.isDevicePhone = isDevicePhone;
            currentPosition = 0;
        }

        @Override
        public int getCount() {
            return menuTitle.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.spinner_row, parent, false);
            }
            TextView introSpinnerMenuText = (TextView) convertView.findViewById(R.id.intro_spinner_menu_text);
            ImageView introSpinnerMenuImg = (ImageView) convertView.findViewById(R.id.intro_spinner_menu_img);
            ImageView introSpinnerMenuSelectOn = (ImageView) convertView.findViewById(R.id.intro_spinner_menu_select_on);
            introSpinnerMenuText.setText(menuTitle[position]);

            if (isDevicePhone) {
                if (isDropdown) {
                    introSpinnerMenuText.setVisibility(View.VISIBLE);
                    introSpinnerMenuText.setTextColor(Color.parseColor("#646464"));
                    introSpinnerMenuImg.setImageResource(resourceId[position * 2 + 1]);

                    if (position == 0)
                        introSpinnerMenuSelectOn.setVisibility(View.VISIBLE);
                    else
                        introSpinnerMenuSelectOn.setVisibility(View.GONE);

                    if (position == currentPosition)
                        convertView.setBackgroundColor(Color.parseColor("#f0f0f0"));
                    else
                        convertView.setBackgroundColor(Color.parseColor("#ffffff"));

                } else {
                    introSpinnerMenuText.setVisibility(View.GONE);
                    introSpinnerMenuSelectOn.setVisibility(View.GONE);
                    convertView.setBackgroundColor(Color.parseColor("#00000000"));
                    introSpinnerMenuImg.setImageResource(resourceId[position * 2]);
                }
            } else {
                introSpinnerMenuText.setVisibility(View.VISIBLE);
                if (isDropdown) {
                    introSpinnerMenuText.setVisibility(View.VISIBLE);
                    introSpinnerMenuText.setTextColor(Color.parseColor("#646464"));
                    introSpinnerMenuImg.setImageResource(resourceId[position * 2 + 1]);

                    if (position == 0)
                        introSpinnerMenuSelectOn.setVisibility(View.VISIBLE);
                    else
                        introSpinnerMenuSelectOn.setVisibility(View.GONE);

                    if (position == currentPosition)
                        convertView.setBackgroundColor(Color.parseColor("#f0f0f0"));
                    else
                        convertView.setBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    introSpinnerMenuSelectOn.setVisibility(View.GONE);
                    introSpinnerMenuText.setTextColor(getResources().getColor(R.color.app_base_color));
                    convertView.setBackgroundColor(Color.parseColor("#00000000"));
                    introSpinnerMenuImg.setImageResource(resourceId[position * 2]);
                }
            }
            introSpinnerMenuSelectOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // actionBarSpinner.onDetachedFromWindow();
                    AndroidUtils.hideSpinnerDropDown(actionBarSpinner);
                    actionBarSpinner.setVisibility(View.VISIBLE);
                }
            });

            return convertView;
        }


        private void setCurrentPositon(int currentPosition) {
            this.currentPosition = currentPosition;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position,convertView,parent, true);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position,convertView,parent, false);
        }
    }
}