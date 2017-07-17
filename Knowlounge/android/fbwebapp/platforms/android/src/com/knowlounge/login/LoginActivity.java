package com.knowlounge.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.gson.JsonObject;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.network.restful.zico.command.AuthRestCommand;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.dialog.RoomPasswdDialogFragment;
import com.knowlounge.gcm.GcmRegistStatePreference;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.GoogleAnalyticsService;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by we160303 on 2016-07-21.
 */
public class LoginActivity extends SnsOAuthActivity {

    private final String GOOGLE_ANALYTICS_TAG_FACEBOOK_SIGNIN = "FacebookSignUp";
    private final String GOOGLE_ANALYTICS_TAG_GOOGLE_SIGNIN = "GoogleSignUp";

    private final String TAG = "LoginActivity";

    public WenotePreferenceManager prefManager;

    // VIEWS
    @BindView(R.id.oauth_img_flipper) ViewFlipper mainImgFlipper;
    @BindView(R.id.direct_enter_room) TextView directEnterRoom;
    @BindView(R.id.oauthfacebook) ImageView facebookBtn;
    @BindView(R.id.oauthgoogle) ImageView googleBtn;
    @BindView(R.id.root_layout) RelativeLayout rootLayout;

    @BindView(R.id.oauth_img_layout1) LinearLayout oauthImgLayout1;
    @BindView(R.id.oauth_img_layout2) LinearLayout oauthImgLayout2;
    @BindView(R.id.oauth_img_layout3) LinearLayout oauthImgLayout3;
    @BindView(R.id.page1) ImageView page1;
    @BindView(R.id.page2) ImageView page2;
    @BindView(R.id.page3) ImageView page3;
    @BindView(R.id.oauth_guest_error_message) TextView oauthGuestErrorMessage;
    @BindView(R.id.oauth_guest_room_code_clear) ImageView oauthGuestRoomCodeClear;
    @BindView(R.id.input_guest_room_code) EditText inputGuestRoomCode;
    @BindView(R.id.input_guest_name) EditText inputGuestName;

    // Flags
    private boolean isFillGuestRoomCode = false;
    private boolean isFillGuestName = false;
    private boolean signBtnClicked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        prefManager = WenotePreferenceManager.getInstance(this);

        setUpView();

        int screenSizeType = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if(screenSizeType == Configuration.SCREENLAYOUT_SIZE_SMALL || screenSizeType == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            prefManager.setDeviceType(GlobalConst.DEVICE_PHONE);
        } else {
            prefManager.setDeviceType(GlobalConst.DEVICE_TABLET);
        }

        if (getIntent().hasExtra("urlStart")) {
            checkIntoGuestMode();
        }

        if (mRegistrationBroadcastReceiver == null) {
            //registBroadcastReceiver();
        }

        requestEssentialPermission(essentialPermissions, PERMISSION_REQUEST_CODE);
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
    public void finishSignInProcess() {
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        Intent authIntent = new Intent(this, MainActivity.class);
        startActivity(authIntent);
        finish();
    }


    @Override
    public void finishedReload() {

    }

    @Override
    public void goToLogin(ConnectionResult connectionResult) {
        Log.d(TAG, "<goToLogin / Knowlounge>");
        if (connectionResult != null)
            startResolution(connectionResult);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void setUpView() {
        Animation showIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        Animation showOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        mainImgFlipper.setInAnimation(showIn);
        mainImgFlipper.setOutAnimation(showOut);

        mainImgFlipper.setFlipInterval(10000);
        mainImgFlipper.setDisplayedChild(0);
        mainImgFlipper.startFlipping();

        mainImgFlipper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mainImgFlipper.getCurrentView() == oauthImgLayout1)
                    changePage(page1, page2, page3);
                else if (mainImgFlipper.getCurrentView() == oauthImgLayout2)
                    changePage(page2, page1, page3);
                else if (mainImgFlipper.getCurrentView() == oauthImgLayout3)
                    changePage(page3, page1, page2);
            }
        });

        inputGuestRoomCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0)
                    isFillGuestRoomCode = true;
                else
                    isFillGuestRoomCode = false;
                checkIntoGuestMode();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputGuestName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0)
                    isFillGuestName = true;
                else
                    isFillGuestName = false;
                checkIntoGuestMode();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <OnClick Events>
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.root_layout)
    void OnClickRootLayout(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootLayout.getWindowToken(), 0);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.oauthfacebook)
    void OnClickFacebookSignIn(){

        if(signBtnClicked)
            return;
        signBtnClicked = true;

        //authFacebook();

        type = 2;
        GoogleAnalyticsService.get().sendAnalyticsEvent(getClass().getSimpleName(), GOOGLE_ANALYTICS_TAG_FACEBOOK_SIGNIN);
        String cookieStr = prefManager.getUserCookie() == null ? "" : prefManager.getUserCookie();
        if (TextUtils.isEmpty(cookieStr)) {
            AppLog.d(AppLog.TAG, "onClick - accessToken null");
            LoginManager manager = LoginManager.getInstance();
            manager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
            manager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));
        }

        facebookBtn.postDelayed(new Runnable() {
            @Override
            public void run() {
                signBtnClicked = false;
            }
        }, 1000);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.oauthgoogle)
    void OnClickGoogleSignIn(){
        if(signBtnClicked)
            return;
        signBtnClicked = true;

        type = 1;
        GoogleAnalyticsService.get().sendAnalyticsEvent(getClass().getSimpleName(), GOOGLE_ANALYTICS_TAG_GOOGLE_SIGNIN);
        if(startPermissionCheck(new String[] {Manifest.permission.READ_CONTACTS} )) {
            initGoogleAuth();
            //startGoogleSignIn();  // 구글 OAuth 신규버전 테스트
        }
        googleBtn.postDelayed(new Runnable() {
            @Override
            public void run() {
                signBtnClicked = false;
            }
        }, 1000);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.direct_enter_room)
    void OnClickEnterRoom(){
        if (!startPermissionCheck(new String[] { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO } )) {
            return;
        }
        String roomCode = inputGuestRoomCode.getText().toString();
        String guestNm = inputGuestName.getText().toString();
        String passwd = "";

        if (TextUtils.isEmpty(roomCode)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_join_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(guestNm)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_join_invalidname), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String tokenStr = "roomcode=" + roomCode + "&passwd=" + passwd;

            AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
            String encryptToken = aesUtilObj.encrypt(tokenStr);
            enterRoomWithGuest(roomCode, guestNm, encryptToken);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.oauth_guest_room_code_clear)
    void OnClickRoomCodeClear(){
        oauthGuestRoomCodeClear.setVisibility(View.GONE);
        oauthGuestErrorMessage.setVisibility(View.GONE);
        inputGuestRoomCode.setText("");
        inputGuestRoomCode.requestFocus();
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(this.getCurrentFocus(), InputMethodManager.SHOW_FORCED);
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <methods>
     */
    private void changePage(ImageView check, ImageView noncheck1, ImageView noncheck2) {
        check.setImageResource(R.drawable.btn_main_content_indicater_on);
        noncheck1.setImageResource(R.drawable.btn_main_content_indicater_off);
        noncheck2.setImageResource(R.drawable.btn_main_content_indicater_off);
    }

    private void checkIntoGuestMode() {
        if (isFillGuestName && isFillGuestRoomCode){
            directEnterRoom.setBackground(getResources().getDrawable(R.drawable.bg_radius6_direct_btn_fill_state));
            directEnterRoom.setTextColor(Color.parseColor("#ffffff"));
        } else {
            directEnterRoom.setBackground(getResources().getDrawable(R.drawable.bg_radius6_direct_btn_empty_state));
            directEnterRoom.setTextColor(getResources().getColor(R.color.app_base_color));
        }
    }


    private void enterRoomWithGuest(final String roomCode, final String guestNm, String tokenStr) {
        final RequestParams params = new RequestParams();
        params.put("token", tokenStr);

        RestClient.post("room/check.json", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG, response.toString());
                    int result = response.getInt("result");
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                    if (result == 0) {
                        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                        String roomId = response.has("roomid") ? response.getString("roomid") : "";

                        JsonObject extraParams = new JsonObject();
                        extraParams.addProperty("type", "knowlounge");
                        extraParams.addProperty("roomurl", roomUrl);
                        extraParams.addProperty("guest", guestNm);
                        extraParams.addProperty("deviceid", deviceId);
                        extraParams.addProperty("mode", GlobalConst.ENTER_ROOM_MODE);

                        navigateRoomWithGuest(roomId, deviceId, extraParams);

//                        Intent mainIntent = new Intent(LoginActivity.this, RoomActivity.class);
//                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mainIntent.putExtra("roomurl", roomUrl);
//                        mainIntent.putExtra("guest", guestNm);
//                        mainIntent.putExtra("deviceid", deviceId);
//                        startActivity(mainIntent);

                        inputGuestRoomCode.setText("");
                        ((EditText) findViewById(R.id.input_guest_name)).setText("");

                        //_instance.finish();
                    } else if (result == -201) {
                        // Invalid room
                        AppLog.d(AppLog.TAG, "not found room");
                        oauthGuestErrorMessage.setVisibility(View.VISIBLE);
                        oauthGuestRoomCodeClear.setVisibility(View.VISIBLE);
                    } else if (result == -102) {
                        // Incorrect password
                        FragmentManager fm = getSupportFragmentManager();
                        RoomPasswdDialogFragment dialogFragment = new RoomPasswdDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("mode", "roomcode");
                        args.putString("roomcode", roomCode);
                        args.putString("guestnm", guestNm);
                        args.putString("deviceid", deviceId);
                        dialogFragment.setArguments(args);
                        dialogFragment.show(fm, "room_passwd");
                    } else if (result == -207) {   // room count limit (over 3)
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.global_popup_full) , Toast.LENGTH_SHORT).show();
                    } else if (result == -208) {
                        // room count limit (over 25)
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.global_popup_full) , Toast.LENGTH_SHORT).show();
                    } else if (result == -8001) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_join_invalidcode), Toast.LENGTH_SHORT).show();
                        oauthGuestErrorMessage.setVisibility(View.VISIBLE);
                        oauthGuestRoomCodeClear.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                AppLog.d(AppLog.TAG, "Create room onFailure " + statusCode);

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

    private void navigateRoomWithGuest(final String roomId, final String userNo, final JsonObject extraParams) {
        // Step 1. sns oauth token 인증 작업.
        new AuthRestCommand()
                .sns("guest")
                .token("")
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
                        Log.d(TAG, "onError");
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
                        Log.d(TAG, "<ZICO> getAccessToken.json result : " + jsonObject.toString());
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
                            params.addProperty("usernm", extraParams.get("guest").getAsString());
                            params.addProperty("name", "");
                            params.addProperty("host", "");
                            params.addProperty("port", "");
                            params.addProperty("video", true);
                            params.addProperty("audio", true);
                            params.addProperty("volume", true);
                            params.addProperty("token", zicoAccessToken);

                            getAppComponent().navigator().navigateToRoomActivityView(LoginActivity.this, params, extraParams);

//                            Intent intent = new Intent(LoginActivity.this, RoomActivity.class);

                            // 서브 룸의 룸아이디가 올 경우, 처리 루틴..
//                            String roomIdParam = params.get("roomid").getAsString();
//                            int specialCharPosition = roomIdParam.indexOf("_");
//                            String roomId = specialCharPosition < 0 ? roomIdParam : roomIdParam.substring(0, specialCharPosition);
//
//                            intent.putExtra("arguments",
//                                    new RoomSpec.Builder()
//                                            .host(params.get("host").getAsString())
//                                            .name(params.get("name").getAsString())
//                                            .port(params.get("port").getAsString())
//                                            .userNo(params.get("userno").getAsString())
//                                            .userNm(params.get("usernm").getAsString())
//                                            .roomId(roomId)
//                                            .accessToken(params.get("token").getAsString())
//                                            .enableVideo(params.get("video").getAsBoolean())
//                                            .enableAudio(params.get("audio").getAsBoolean())
//                                            .enableVolume(params.get("volume").getAsBoolean())
//                                            .build());
//                            intent.putExtra("type", extraParams.get("type").getAsString());
//                            intent.putExtra("roomurl", extraParams.get("roomurl").getAsString());
//                            intent.putExtra("deviceid", extraParams.get("deviceid").getAsString());
//                            intent.putExtra("mode", extraParams.get("mode").getAsInt());
//                            intent.putExtra("guest", extraParams.get("guest").getAsString());
//
//                            startActivity(intent);
                        } else if (resultCode == -8011) {
                            Log.d(TAG, "<ZICO> Expired token..");
                        } else if (resultCode == -9001) {
                            Log.d(TAG, "<ZICO> Invalid access token..");
                        }
                    }
                });
    }
}