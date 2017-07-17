package com.knowlounge.login;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.gson.JsonObject;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.dialog.VersionCheckDialogFragment;
import com.knowlounge.gcm.GcmRegistStatePreference;
import com.knowlounge.manager.SharedPreferencesManager;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.network.restful.command.ApiCommand;
import com.knowlounge.network.restful.command.AuthApiCommand;
import com.knowlounge.receiver.NetworkStateReceiver;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Knowlounge Android가 동작하면 처음 실행되는 엑티비티이다.
 *
 * # 인증 정보 유무에 따른 처리
 *   - SharedPreference에 인증값을 관리하고 있는데, 이 값의 존재 유무를 체크하여 이미 로그인한 적이 있는 장비인지 아닌지를 판단한다.
 *   - 인증값이 존재하면 바로 메인 엑티비티로 이동하고, 인증값이 존재하지 않으면 로그인 엑티비티로 이동한다.
 *   - 인증값이 존재할 때의 Flow
 *     1) 인증값 갱신 : 'auth/reload.json' REST API를 호출한다. (response : 인증 쿠기값) - Knowlouge 모바일 API (www.knowlounge.com/mapi)
 *     2) 스타서버에서 토큰 발급 : 'auth/star/gettoken.json' REST API를 호출한다. - Knowlouge 모바일 API (www.knowlounge.com/mapi)
 *     3) AccessToken 발급 : 'user/accessToken' 을 호출한다.  - SI Platform 모바일 API (www.sayalo.me:30443/knowlounge)
 *     4) 3까지 성공하면 finishedReload 콜백을 호출하여 메인 엑티비티로 이동한다.
 *   - 인증값이 존재하지 않을 때의 Flow
 *     1) 현재 버전을 체크한다.
 *     2) 로그인 엑티비티로 이동한다.
 *
 * # 앱이 구동된 형태에 따른 처리
 *   - 푸시 메세지를 터치하여 앱이 구동된 케이스 : isCalledPush
 *   - 딥링크로 앱이 구동된 케이스 : isCalledDeepLink
 *
 */
public class SplashIntroActivity extends SnsOAuthActivity {

    private final String TAG = "SplashIntroActivity";

    // 필수 퍼미션 정의
    private String[] essentialPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isCalledDeepLink = false;   // 딥링크로 앱을 호출했는지 여부를 판단하는 값
    private boolean isCalledPush = false;       // 푸시로 앱에 진입했는지 여부를 판단하는 값
    private Uri urlData = null;

    private ImageView splashImg;

    private boolean permissionCheck = false;
    private NetworkStateReceiver networkStateReceiver = null;
    private AlertDialog noNetwork;
    private String roomCode;
    private SharedPreferencesManager pref;

    public WenotePreferenceManager prefManager;

    private Intent paramIntent;

    public GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        pref = SharedPreferencesManager.getInstance(this);
        prefManager = WenotePreferenceManager.getInstance(this);
        paramIntent = getIntent();

        //
        if (prefManager.contains("app_running_flag")) {
            Log.d(TAG, "RoomActivity is live..");
            if(RoomActivity.activity != null) {
                RoomActivity.activity.finish();
            }
        }

        Log.d(TAG, "<onCreate / Knowlounge> getIntent().getData() : " + getIntent().getData());
        Log.d(TAG, "<onCreate / Knowlounge> getIntent().getStringExtra() : " + getIntent().getStringExtra("code"));

        if (getIntent().hasExtra("code")) {
            isCalledPush = true;
            roomCode = getIntent().getStringExtra("code");
            Log.d(TAG, "<onCreate / Knowlounge> 노티를 통해 진입함.. roomCode : " + roomCode);
        }

        if (getIntent().getData() != null) {
            isCalledDeepLink = true;
            urlData = getIntent().getData();
            Log.d(TAG, "<onCreate / Knowlounge> 딥링크를 통해 진입함.. urlData : " + urlData.toString());
        }

        splashImg = (ImageView) findViewById(R.id.splash_img);
        setConfigurationSettingUI(this.getResources().getConfiguration());

        //폰인지 테블릿인지 판별
        int screenSizeType = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if (screenSizeType == Configuration.SCREENLAYOUT_SIZE_SMALL || screenSizeType == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            prefManager.setDeviceType(GlobalConst.DEVICE_PHONE);
        } else {
            prefManager.setDeviceType(GlobalConst.DEVICE_TABLET);
        }

        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.setOnChangeNetworkStatusListener(networkStateListener);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if(pref.getStartDay() == 0){
            pref.setStartDay(System.currentTimeMillis());
        }

        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(100);
        for( int i=0; i < taskList.size(); i++){
            Log.d(TAG, "base = " + taskList.get(i).baseActivity.getPackageName() + ", top = " + taskList.get(i).topActivity.getPackageName());
            Log.d(TAG, "base = " + taskList.get(i).baseActivity.getClassName() + ", top = " + taskList.get(i).topActivity.getClassName());
        }
        startApplication();
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
        if (mRegistrationBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.REGISTRATION_READY));
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.REGISTRATION_PROCESSING));
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.REGISTRATION_COMPLETE));
            //LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegistStatePreference.PUSH_RECEIVED));
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }


    @Override
    public void finishedReload() {

        Log.d(TAG, "<finishedReload / Knowlounge> isCalledPush : " + isCalledPush + ", isCalledDeepLink : " + isCalledDeepLink);

        Intent mainIntent = new Intent(this, MainActivity.class);
        if (isCalledPush) {
            mainIntent.putExtra("type", "knowlounge");
            mainIntent.putExtra("code", paramIntent.getStringExtra("code"));
            Log.d(TAG, "<finishedReload / Knowlounge> code : " + paramIntent.getStringExtra("code"));
        }
        if (isCalledDeepLink) {
            if (urlData.getQueryParameter("type") != null) {
                String type = urlData.getQueryParameter("type");
                if (TextUtils.equals(type, "premium")) {
                    String userNo = urlData.getQueryParameter("userno");
                    String code = urlData.getQueryParameter("code");
                    mainIntent.putExtra("type", type);
                }
            } else {
                mainIntent.putExtra("type", "knowlounge");
                if (urlData.getQueryParameter("code") != null)
                    mainIntent.putExtra("code", AndroidUtils.getRoomCode(urlData.getQueryParameter("code")));
                else {

                    // 프리미엄 딥링크 호출시 데이터 출력
                    // 테스트용 adb 쉘 명령
                    // adb shell am start -W -a android.intent.action.VIEW -d "knowlounge://premium?auth=b51c8a24b2df25db074fc61812db059713e27ca9cdff13a72af549d399cd9bed8c44d0c9e4827f3231d26d823482bf9719e352f4fe5cdf8575e9878ec9f6eea4884a8dcab58b0f0a5dc8757e8c125b5dfd994ad0208e02799a5f63e0d4ebb9997cab86839ca70442e402f266d721e248d7708389f430c3a3e9a711eded4461cf6914b8dd33d2fef658a4064d9b4486521e80604951b983f759a655beff84c309bd512a83a0f34b2e0bbc5683b55ee9fb6b321acaec01da86654ab6cb7ed8f0e36667e3fc81654420c815228330e21d1946948e84f06d9421d3693096fac4fc1501914fb93df65f8a069786c6691c08773718bf5c90e7387413c9040630f87cbc43597bf48f10567d54c69a23927c7e29f63efa3a111929c29f6e5522759d04aa" com.knowlounge
                    try {
                        AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());

                        String dataStr = urlData != null ? urlData.toString() : "데이터가 없습니다.";
                        String decryptStr = urlData != null ? aesUtilObj.decrypt(urlData.getQueryParameter("auth")) : "";

                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashIntroActivity.this, R.style.AlertDialogCustom);
                        builder.setMessage(dataStr + "\n\n" + decryptStr).setCancelable(true)
                                .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent authIntent = new Intent(SplashIntroActivity.this, MainActivity.class);
                                        startActivity(authIntent);
                                        finish();
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
                        confirm.setTitle(getResources().getString(R.string.global_popup_title));


                        confirm.show();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        startActivity(mainIntent);
        finish();
    }


    @Override
    public void finishSignInProcess() {

    }

    @Override
    public void goToLogin(ConnectionResult connectionResult) {
        Log.d(TAG, "<goToLogin / Knowlounge>");
        splashImg.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Animation hideAnimation = new AlphaAnimation(1.0f, 0.0f);
                hideAnimation.setDuration(500);
                splashImg.setVisibility(View.GONE);
                splashImg.setAnimation(hideAnimation);

                Intent intent = new Intent(SplashIntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setConfigurationSettingUI(newConfig);
    }


    /**
     * Knowlounge Android의 진입점
     */
    private void startApplication() {
        Log.d(TAG, "<startApplication / Knowlounge>");

        String userCookie = prefManager.getUserCookie() == null ? "" : prefManager.getUserCookie();
        if (!userCookie.equals("")) {
            if (mRegistrationBroadcastReceiver == null)
                registBroadcastReceiver();

            String snsType = prefManager.getSnsType();

            if ("0".equals(snsType)) {
                Log.d(TAG, "<startApplication / Knowlounge> 페이스북으로 이미 로그인 했었음.");
                reloadUserCredential();

            } else if ("1".equals(snsType)) {
                Log.d(TAG, "<startApplication / Knowlounge> 구글로 이미 로그인 했었음.");
                initGoogleAuth();
            }

            // TODO : SNS 토큰 만료여부 체크

//            reloadUserCredential();

        } else {
            getCurrentVersion();
            splashImg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Animation hideFade = new AlphaAnimation(1.0f, 0.0f);
                    hideFade.setDuration(500);
                    splashImg.setVisibility(View.GONE);
                    splashImg.setAnimation(hideFade);

                    Intent intent = new Intent(SplashIntroActivity.this, LoginActivity.class);
                    if (isCalledDeepLink) {
                        intent.putExtra("isCalledDeepLink", AndroidUtils.getRoomCode(urlData.getQueryParameter("code")));
                    }
                    startActivity(intent);
                    finish();
                }
            }, 1000);
        }
    }


    private void checkSnsToken() {
        AccessToken.refreshCurrentAccessTokenAsync();
    }

    /**
     * 버전 정보 조회하기
     */
    private void getCurrentVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);

            ApplicationInfo ai = getApplicationInfo();
            String sourceDir = ai.publicSourceDir ;

            String hash = AndroidUtils.getHash(sourceDir);
            String version = pi.versionName;
            String os = "android";

            chkAppVersion(os, version, hash);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * API로 버전 체크하기  (version/check.json)
     * @param os
     * @param version
     * @param hash
     * @throws Exception
     */
    private void chkAppVersion(String os, String version, String hash) throws Exception {
        String tokenStr = "os=" + os + "&version=" + version + "&hash=" + hash;
        AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
        String encryptToken = aesUtilObj.encrypt(tokenStr);
        Log.d(TAG, "IsCurrentVersion : " + encryptToken);

        RequestParams params = new RequestParams();
        params.put("token", encryptToken);
        RestClient.post("version/check.json", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "version/check.json result : " + response.toString());
                try {
                    String result = response.getString("result");
                    if (result.equals("0")) {
                        //Todo 성공일 때 islast == true
                        JSONObject map = response.getJSONObject("map");
                        JSONObject guide = map.getJSONObject("guide");

                        GlobalConst.MOVIE_CHANNEL = guide.getString("channel");
                        GlobalConst.MOVIE_INVITE = guide.getString("movie_invite");
                        GlobalConst.MOVIE_APPLY = guide.getString("movie_apply");
                        GlobalConst.MOVIE_CANVAS = guide.getString("movie_canvas");
                        GlobalConst.VIDEO_LIMIT = map.getString("videolimit");
                        Log.d(TAG, "VIDEO_LIMIT : " + GlobalConst.VIDEO_LIMIT);

                        long globalTimeTurm  = AndroidUtils.getGlobalTimeTurm(getApplicationContext(), map.getString("currenttime"));
                        prefManager.setGlobalTimeTurm(globalTimeTurm);

                        boolean isLast = map.getBoolean("islast");
                        String versionMode = map.getString("mode");
                        Log.d(TAG, "versionCheck isLast : " + isLast + ", mode : " + versionMode);
                        if (!isLast) {
                            if (!TextUtils.equals(versionMode, "")) {
                                Bundle argument = new Bundle();
                                argument.putString("type", map.getString("mode"));
                                argument.putString("msg", map.getString("msg"));
                                FragmentManager fm = getSupportFragmentManager();
                                VersionCheckDialogFragment dialogFragment = new VersionCheckDialogFragment();
                                dialogFragment.setArguments(argument);
                                dialogFragment.show(fm, "version");
                            } else {

                            }
                        }
                    } else if (result.equals("-602") || result.equals("-601")) {
                        //Todo 해쉬값이 달라짐 다이얼로그 띄우고 확인 누르고 gogole play 고고씽
                        Bundle argument = new Bundle();
                        argument.putString("type", "M");   // 강제 업데이트 모드
                        argument.putString("msg", "");
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
        });
    }


    private void setConfigurationSettingUI(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Glide.with(this).load(R.drawable.splash_screen_horizontal).centerCrop().into(splashImg);
        } else {
            Glide.with(this).load(R.drawable.splash_screen_vertical).centerCrop().into(splashImg);
        }
    }


    /**
     *  네트워크 상태를 감지하는 리스터를 정의한다.
     */
    NetworkStateReceiver.OnChangeNetworkStatusListener networkStateListener = new NetworkStateReceiver.OnChangeNetworkStatusListener(){
        @Override
        public void onChange(int status) {
            switch (status) {
                case NetworkStateReceiver.NETWORK_CONNECTED :
                    Log.d(TAG, "network connected");
                    if (noNetwork != null && noNetwork.isShowing())
                        noNetwork.dismiss();
//                    startApplication();
                    return;

                case NetworkStateReceiver.NETWORK_DISCONNECTED :
                    Log.d(TAG, "network disconnected");
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
                    timer.schedule(timerTask,3000);

                    return;
            }
        }
    };


    /**
     * 네트워크가 불안정할 때 띄우는 다이얼로그 설정하기
     */
    private void setNoNetworkAlertDialog() {
        if (noNetwork != null && noNetwork.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
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
                        finish();
                        noNetwork.dismiss();
                    }
                });
        noNetwork = builder.create();
        noNetwork.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        noNetwork.setCanceledOnTouchOutside(false);
        noNetwork.setTitle(getResources().getString(R.string.global_popup_title));
        noNetwork.show();
    }
}