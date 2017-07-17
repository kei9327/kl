package com.knowlounge.login;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.gson.JsonObject;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.gcm.GcmRegistStatePreference;
import com.knowlounge.gcm.RegistrationIntentService;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.FriendUser;
import com.knowlounge.network.restful.APIUrl;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.GoogleAnalyticsService;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.RuntimePermissionChecker;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-07-20.
 */
public abstract class GoogleAuthActivity extends NetworkStateActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private final String TAG = "GoogleAuthActivity";

    public abstract void finishSignInProcess();

    public abstract void finishedReload();

    // 필수 퍼미션 정의
    public String[] essentialPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public final int PERMISSION_REQUEST_CODE = 3;

    private final int CODE_SIGN_IN = 1001;
    private final int CODE_SIGN_OUT = 1002;
    private final int REQUEST_AUTHORIZATION = 9003;

    WenotePreferenceManager prefManager;

    // 구글 로그인 퍼미션 관련
    protected RuntimePermissionChecker mRuntimePermissionChecker;
    public final int PERMISSION_REQUEST_READ_CONTACTS = 1;
    public final int PERMISSION_REQUEST_CAMERA_AND_AUDIO = 0;

    public GoogleApiClient mGoogleApiClient;

    GoogleAuthTask googleAuthTask = null;
    public BroadcastReceiver mRegistrationBroadcastReceiver = null;

    public String deviceToken = "";
    public ProgressDialog mProgressDialog;

    public DbOpenHelper mDbOpenHelper;

    public int type = -1;

    public boolean isSubscribeFirst = true;

    public String version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = WenotePreferenceManager.getInstance(this);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);
        mDbOpenHelper.dropAndReCreateTable(DataBases.CreateDB._TABLENAME);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Log.d(TAG, "startService");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        mRuntimePermissionChecker = new RuntimePermissionChecker(this);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (mRegistrationBroadcastReceiver == null) {
            registBroadcastReceiver();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mDbOpenHelper.close();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult / requestCode : " + requestCode + ", resultCode : " + resultCode);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_SIGN_IN) {

            if (resultCode == Activity.RESULT_CANCELED) {   // 구글 계정 선택 창 닫았을 때..
                Log.d(TAG, "Google Login canceled..");

                if (mGoogleApiClient != null) {
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = null;
                }
            } else if (resultCode == Activity.RESULT_OK) {  // 구글 계정 선택 창에서 계정을 정상적으로 선택했을 때..
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                }
            }

        } else if (requestCode == CODE_SIGN_OUT) {
            if (resultCode == Activity.RESULT_OK) {
//                handleSignInResult(false, "", "");
            }
        } else {
            if (resultCode == Activity.RESULT_OK) {
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                }
            }
        }

    }

    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : GoogleApiClient.ConnectionCallbacks>
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "<onConnected / Google OAuth / Knowlounge>");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final String mAccountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Log.d(TAG, "<onConnected / Google OAuth / Knowlounge> account name : " + mAccountName);

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
            @Override
            public void onResult(People.LoadPeopleResult loadPeopleResult) {
                Log.d(TAG, "<onConnected / 구글 친구리스트 불러오기 / Knowlounge> loadPeopleResult:" + Integer.toString(loadPeopleResult.getStatus().getStatusCode()));

                //if (loadPeopleResult.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                if (loadPeopleResult.getStatus().isSuccess()) {
                    PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();
                    int count = personBuffer.getCount();

                    //구글 플러스 친구 리스트
                    for (int i = 0; i < count; i++) {
                        String userNm = personBuffer.get(i).getDisplayName();
                        String userId = personBuffer.get(i).getId();
                        String thumbnail = personBuffer.get(i).getImage().getUrl();
                        mDbOpenHelper.insert(new FriendUser(userId, userNm, thumbnail));
                    }
                    personBuffer.release();

                } else {
                    Log.d(TAG, "loadPeopleResult.getStatus() : " + loadPeopleResult.getStatus() + ", statusCode : " + loadPeopleResult.getStatus().getStatusCode());

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }

                String userCookie = prefManager.getUserCookie() == null ? "" : prefManager.getUserCookie();
                //if (TextUtils.isEmpty(userCookie)) {
                    Log.d(TAG, "onConnected() - mAccountName : " + mAccountName);
                    googleAuthTask = new GoogleAuthTask();
                    googleAuthTask.execute(mAccountName);
                //}
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "<onConnectionSuspended / Knowlounge>");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        try {
            if (!connectionResult.hasResolution()) {
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();

            } else {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                Log.d(TAG, "<onConnectionFailed / Knowlounge> connection resolving. errCode : " + connectionResult.getErrorCode() + ", errMsg : " + connectionResult.getErrorMessage());

                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

                connectionResult.startResolutionForResult(this, CODE_SIGN_IN);
                final int errorCode = connectionResult.getErrorCode();
                if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {

                } else {
                    // todo show sign-out
                }
            }

            if (connectionResult.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED) {
                Intent intent = new Intent(this, LoginActivity.class);
//                    if (isCalledDeepLink) {
//                        intent.putExtra("isCalledDeepLink", AndroidUtils.getRoomCode(urlData.getQueryParameter("code")));
//                    }
                startActivity(intent);
            }

        } catch (IntentSender.SendIntentException e) {
            Log.d(TAG, "onConnectionFailed(): connection failed.", e);
        }

    }


    public void authGoogle(){
        type = 1;
        GoogleAnalyticsService.get().sendAnalyticsEvent(getClass().getSimpleName(), "GoogleSignUp");
        if(startPermissionCheck(new String[] {Manifest.permission.READ_CONTACTS} )) {
            googleSignIn();
        }
    }


    public void googleSignIn() {
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);

        Log.d(TAG, "googleSignIn");

        if(getCurrentNetworkStatus() == GlobalConst.NETWORK_DISCONNECTED) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_network_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (mGoogleApiClient == null) {
            startGoogleAuth();  // GoogleApiClient Object 생성..
            //startGoogleAuthNew();
        } else {
            if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
        }
    }


    public void startGoogleAuthNew() {
        // 구글 로그인 다른 버전..
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestIdToken("544848250470-gg4885tcs5ib3o2fh237mtt339fvkkoe.apps.googleusercontent.com")
                .requestProfile()
                .requestScopes(
                        new Scope("https://www.googleapis.com/auth/plus.profile.emails.read")).build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, CODE_SIGN_IN);
    }


    public void startGoogleAuth() {
        Log.d(TAG, "startGoogleAuth");

        if(mGoogleApiClient == null) {

            mProgressDialog = null;
            mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.toast_account_retrieve), true);

            Plus.PlusOptions gpo = Plus.PlusOptions.builder().addActivityTypes(
                    "http://schemas.google.com/AddActivity",
                    "http://schemas.google.com/BuyActivity",
                    "http://schemas.google.com/CreateActivity").build();

            GoogleApiClient.Builder googleApiBuilder = new GoogleApiClient.Builder(this);
            googleApiBuilder.addScope(new Scope(Scopes.PLUS_LOGIN));
            googleApiBuilder.addScope(new Scope(Scopes.PLUS_ME));
            googleApiBuilder.addScope(new Scope(Scopes.EMAIL));
            googleApiBuilder.addScope(new Scope("https://www.googleapis.com/auth/plus.profile.emails.read"));

            mGoogleApiClient = googleApiBuilder.addApi(Plus.API, gpo)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }

        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
            Log.d(TAG, "startGoogleAuth / init googleApiClient connect");
            mGoogleApiClient.connect();
        } else {
            Log.d(TAG, "startGoogleAuth / init googleApiClient reconnect");
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }


    public void getUserCredential(String snsType, String accessToken) {
        // auth.json 호출..
        AppLog.d(AppLog.TAG, "=========== getUserCredential ===========");



        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.toast_signing_in), true);

        String url = "0".equals(snsType) ? APIUrl.SIGN_IN_FACEBOOK : APIUrl.SIGN_IN_GOOGLE;

        JsonObject paramJson = new JsonObject();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);  // 64-bit number (as a hex string) that is randomly (16자리)
        String osType = "android";
        String osVersion = Build.VERSION.RELEASE;
        String screenSize = displayMetrics.densityDpi / 160 + "|" + width + "x" + height;
        String deviceModel = Build.MODEL;

        paramJson.addProperty("devicetoken", TextUtils.isEmpty(deviceToken) ? prefManager.getDeviceToken() : deviceToken);
        paramJson.addProperty("deviceid", deviceId);  // 32자리
        paramJson.addProperty("deviceinfo", osType + "|" + osVersion + "|" + screenSize + "|" + deviceModel);
        paramJson.addProperty("ostype", osType);
        paramJson.addProperty("osversion", osVersion);
        paramJson.addProperty("screensize", screenSize);
        paramJson.addProperty("devicemodel", deviceModel);
        paramJson.addProperty("apptype", "freeboard");
        paramJson.addProperty("appversion", version);
        paramJson.addProperty("ipaddr", NetworkUtils.getIpAddress());

        Log.d(TAG, "paramJson : " + paramJson.toString());
        prefManager.setDeviceId(deviceId);

        RequestParams params = new RequestParams();
        params.put("token", accessToken);
        params.put("deviceinfo", paramJson.toString());

        AppLog.d(AppLog.TAG, "서버로 보낼 accessToken : " + accessToken);
        AppLog.d(AppLog.TAG, "서버로 보낼 deviceinfo : " + paramJson.toString());

        /*
        String cmd = "0".equals(snsType) ? "facebookSignIn" : "googleSignIn";
        new AuthApiCommand()
                .command(cmd)
                .params(accessToken, paramJson.toString())
                .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                    @Override
                    public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                        observer.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "[RxJava / getUserCredential] onCompleted");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "[RxJava / getUserCredential] onError");
                                        e.printStackTrace();
                                        prefManager.clear();
                                        Toast.makeText(getBaseContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;
                                        }
                                    }

                                    @Override
                                    public void onNext(JsonObject object) {
                                        Log.d(TAG, "[RxJava / getUserCredential] onNext");
                                        try {
                                            int apiResult = object.get("result").getAsInt();
                                            if (apiResult == 0) {
                                                String masterCookie = object.get("cookie").getAsJsonObject().get("FBMMC").getAsString();
                                                String checkSumCookie = object.get("cookie").getAsJsonObject().get("FBMCS").getAsString();

                                                String queryStr = "FBMMC=" + masterCookie + "&FBMCS=" + checkSumCookie;

                                                prefManager.setUserCookie(masterCookie);
                                                prefManager.setChecksumCookie(checkSumCookie);
                                                prefManager.setCookieQueryStr(queryStr);

                                                JSONObject userInfo = decryptUserCookie();

                                                String userNo = userInfo.getString("userno");
                                                String userId = userInfo.getString("userid");
                                                String userNm = userInfo.getString("usernm");
                                                String userEmail = userInfo.getString("email");
                                                String userThumbnail = userInfo.has("thumbnail") ? userInfo.getString("thumbnail") : "";
                                                String userThumbnailLarge = userInfo.has("thumbnail_large") ? userInfo.getString("thumbnail_large") : "";
                                                String userSnstype = userInfo.getString("snstype");
                                                String userType = userInfo.getString("usertype");

                                                // Star 서버에서 발급하는 토큰과 TTL값 얻어오기..
                                                JsonObject siPlatformAuth = object.get("star").getAsJsonObject();
                                                String userAccessToken = siPlatformAuth.get("token").getAsString();
                                                String userAccessTokenTTL = siPlatformAuth.get("ttl").getAsString();

                                                // SharedPreference에 유저정보 저장..
                                                prefManager.setUserNo(userNo);
                                                prefManager.setUserId(userId);
                                                prefManager.setUserNm(userNm);
                                                prefManager.setEmail(userEmail);
                                                prefManager.setUserThumbnail(userThumbnail);
                                                prefManager.setUserThumbnailLargeCurrent(userThumbnailLarge);
                                                prefManager.setUserThumbnailLargeLast(userThumbnailLarge);
                                                prefManager.setSnsType(userSnstype);
                                                prefManager.setMyUserType(userType);

                                                getUserAccessToken(userAccessToken);

                                            } else if (apiResult == -8001 || apiResult == -1) {
                                                Log.d(TAG, "apiResult : " + apiResult);
                                                Toast.makeText(getBaseContext(), "로그인 중에 오류가 발생하였습니다. 관리자에게 문의해주세요.", Toast.LENGTH_SHORT).show();
                                                prefManager.clear();
                                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                            }
                                        } catch (JSONException e) {
                                            prefManager.clear();
                                            e.printStackTrace();
                                            if (googleAuthTask != null) {
                                                googleAuthTask.cancel(false);
                                            }
                                            if (mGoogleApiClient != null) {
                                                if (mGoogleApiClient.isConnected()) {
                                                    mGoogleApiClient.disconnect();
                                                }
                                            }
                                        } catch (Exception e) {
                                            prefManager.clear();
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                }).execute();
        */

        RestClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(AppLog.TAG, "RestClient.post() / onSuccess / result : " + response.toString());

                try {
                    int apiResult = response.getInt("result");

                    if (apiResult == 0) {
                        String masterCookie = response.getJSONObject("cookie").getString("FBMMC");
                        String checkSumCookie = response.getJSONObject("cookie").getString("FBMCS");

                        String queryStr = "FBMMC=" + masterCookie + "&FBMCS=" + checkSumCookie;

                        prefManager.setUserCookie(masterCookie);
                        prefManager.setChecksumCookie(checkSumCookie);
                        prefManager.setCookieQueryStr(queryStr);

                        JSONObject userInfo = decryptUserCookie();

                        String userNo = userInfo.getString("userno");
                        String userId = userInfo.getString("userid");
                        String userNm = userInfo.getString("usernm");
                        String userEmail = userInfo.getString("email");
                        String userThumbnail = userInfo.has("thumbnail") ? userInfo.getString("thumbnail") : "";
                        String userThumbnailLarge = userInfo.has("thumbnail_large") ? userInfo.getString("thumbnail_large") : "";
                        String userSnstype = userInfo.getString("snstype");
                        String userType = userInfo.getString("usertype");

                        // Star 서버에서 발급하는 토큰과 TTL값 얻어오기..
                        JSONObject siPlatformAuth = response.getJSONObject("star");
                        String userAccessToken = siPlatformAuth.getString("token");
                        String userAccessTokenTTL = siPlatformAuth.getString("ttl");

                        // SharedPreference에 유저정보 저장..
                        prefManager.setUserNo(userNo);
                        prefManager.setUserId(userId);
                        prefManager.setUserNm(userNm);
                        prefManager.setEmail(userEmail);
                        prefManager.setUserThumbnail(userThumbnail);
                        prefManager.setUserThumbnailLargeCurrent(userThumbnailLarge);
                        prefManager.setUserThumbnailLargeLast(userThumbnailLarge);
                        prefManager.setSnsType(userSnstype);
                        prefManager.setMyUserType(userType);

                        getUserAccessToken(userAccessToken);

//                        Bundle bundleParam = new Bundle();
//                        bundleParam.putString("userno", userNo);
//                        bundleParam.putString("userid", userId);
//                        bundleParam.putString("usernm", userNm);
//                        bundleParam.putString("querystring", queryStr);
//
//                        onAuthComplete(bundleParam);
                    //} else if (apiResult == -8001 || apiResult == -1) {
                    } else {
                        Log.d(TAG, "apiResult : " + apiResult);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_fail_sign_in), Toast.LENGTH_SHORT).show();
                        prefManager.clear();
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                    }
                } catch (JSONException e) {
                    prefManager.clear();
                    e.printStackTrace();
                    if (googleAuthTask != null) {
                        googleAuthTask.cancel(false);
                    }
                    if (mGoogleApiClient != null) {
                        if (mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.disconnect();
                        }
                    }
                } catch (Exception e) {
                    prefManager.clear();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                AppLog.d(AppLog.TAG, "auth.json onFailure");
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


    private void getUserAccessToken(String userAccessToken) {

        String url = "user/accessToken?" + "userAccessToken=" + CommonUtils.urlEncode(userAccessToken);
        RestClient.postSiPlatform(url, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String accessToken = response.getString("accessToken");
                    prefManager.setSiAccessToken(accessToken);
                    if (type == 3)
                        finishedReload();
                    else
                        finishSignInProcess();

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                AppLog.d(AppLog.TAG, "user/accessToken fail.. statusCode : " + statusCode);
                prefManager.clear();
                Toast.makeText(getBaseContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void registBroadcastReceiver() {
        AppLog.d(AppLog.TAG, "[GoogleAuthActivity] registBroadcastReceiver");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(GcmRegistStatePreference.REGISTRATION_COMPLETE) && isSubscribeFirst) {
                    // 액션이 COMPLETE일 경우
                    String token = intent.getStringExtra("registrationId");
                    if (!TextUtils.isEmpty(token)) {
                        AppLog.d(AppLog.TAG, "[registrationId (GCM Token) : " + token);
                        prefManager.setDeviceToken(token);
                        subscribeSiPlatform(token);
                    }
                }
            }
        };
    }


    private void subscribeSiPlatform(String token) {
        isSubscribeFirst = false;
        String url = "user/push/public?"
                + "appId=" + "knowlounge"
                + "&pushPlatform=" + "gcm"
                + "&pushToken=" + token
                + "&timeZone=" + TimeZone.getDefault().getID().toString()
                + "&locale=" + Locale.getDefault().toString();
        AppLog.d(AppLog.TAG, "[GoogleAuthActivity / subscribeSiPlatform] url : " + url);

        RestClient.postSiPlatform(url, false, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                AppLog.d(AppLog.TAG, "[subscribeSiPlatform / onSuccess] statusCode : " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // TODO : 구독 설정 실패..
                AppLog.d(AppLog.TAG, "[subscribeSiPlatform / onFailure] statusCode : " + statusCode);
            }
        });
    }   //등록


    public JSONObject decryptUserCookie() {
        JSONObject resultObj = null;
        try {
            String masterCookie = prefManager.getUserCookie();
            AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
            String result = aesUtilObj.decrypt(masterCookie);
            result = URLDecoder.decode(result, "utf-8");
            AppLog.d(AppLog.TAG, "decrypt result : " + result);
            resultObj = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObj;
    }


    protected boolean startPermissionCheck(String[] permissions) {
        final List<String> rationales = new ArrayList<>();

        if (!mRuntimePermissionChecker.checkSelfPermission(permissions, rationales)) {
            AppLog.i(AppLog.TAG, "displaying contacts permission rationale to provide additional context.");
            AppLog.i(AppLog.TAG, rationales.toString());

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
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Setting",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                                        startActivity(intent);

                                    }
                                }).show();
            } else {
                /**
                 * Note
                 * Fragment.onRequestPermissionsResult 콜백을 호출하여 처리하기 위해서는
                 * ActivityCompat.requestPermissions를 호출하지 말고, Fragment.requestPermissions를
                 * 호출하여 처리해야 한다.
                 */
                mRuntimePermissionChecker.requestPermissions(permissions, PERMISSION_REQUEST_READ_CONTACTS);
            }
            return false;
        }
        return true;
    }


    public void getSiPlatformUserToken() {
        String url = "auth/star/gettoken.json";
        RestClient.postWithCookie(url, prefManager.getUserCookie(), prefManager.getChecksumCookie(), new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int apiResult = response.getInt("result");
                    if (apiResult == 0) {
                        String userToken = response.getJSONObject("map").getString("token");
                        getUserAccessToken(userToken);
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if(throwable instanceof IOException) {
                    Toast.makeText(getBaseContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                AppLog.i(AppLog.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * SNS 친구 리스트 불러오기..
     */
    public void getFriendList() {
        AppLog.d(AppLog.TAG, "getFriendList");
        Log.d(TAG, "getFriendList");
        String snsType = prefManager.getSnsType();
        if ("0".equals(snsType)) {
            final Bundle fbParams = new Bundle();
            fbParams.putString("fields", "id,name,email,picture.type(small)");
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    fbParams,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            AppLog.d(AppLog.TAG, "Facebook /me/friends success");
                            try {
                                if (response != null) {
                                    JSONObject responseJson = response.getJSONObject();
                                    if (responseJson != null) {
                                        //Todo 친구 추가
                                        AppLog.d(AppLog.TAG, "Facebook friend list : " + responseJson.getJSONArray("data").toString());
                                        JSONArray arr = responseJson.getJSONArray("data");
                                        for(int i=0; i<arr.length(); i++) {
                                            JSONObject obj = arr.getJSONObject(i);
                                            String userId = obj.getString("id");
                                            String userNm = obj.getString("name");
                                            String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : obj.getJSONObject("picture").getJSONObject("data").getString("url");

                                            mDbOpenHelper.insert(new FriendUser(userId, userNm, thumbnail));
                                        }
                                    }
                                } else {
                                    // TODO : 응답을 제대로 받지 않았을 때에 대한 예외처리 필요..
                                }
                            } catch (JSONException e) {

                            }

                        }
                    }
            ).executeAsync();

        } else if("1".equals(snsType)) {
            if (startPermissionCheck(new String[] {Manifest.permission.GET_ACCOUNTS} )) {
                startGoogleAuth();
            } else {

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppLog.i(AppLog.TAG, "[onRequestPermissionsResult]");

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<String> request = new ArrayList<String>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        request.add(permissions[i]);
                    }
                }

                if (!request.isEmpty()) {
                    showAlertEssentialPermissions(request.toArray(new String[request.size()]));

                }
            }
        }
    }


    public void requestEssentialPermission(String[] permissions, int requestId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<String> needPermissions = new ArrayList<String>();
            needPermissions.clear();

            for(int i=0; i< permissions.length; i++) {
                if (this.checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    needPermissions.add(permissions[i]);
                    // 거부된 퍼미션을 추가함..
                }
            }

            if(!needPermissions.isEmpty()) {
                String[] requestArray = needPermissions.toArray(new String[needPermissions.size()]);
                this.requestPermissions(requestArray, requestId);
            }
        }
    }


    public void showAlertEssentialPermissions(String[] permissions) {
        final List<String> rationales = new ArrayList<>();

        Log.d(TAG, "<showAlertEssentialPermissions / Knowlounge> displaying contacts permission rationale to provide additional context.");

        for(String str : permissions) {
            rationales.add(str);
        }
        AppLog.d(AppLog.TAG, rationales.toString());

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
                            android.os.Process.killProcess(android.os.Process.myPid());
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

    public class GoogleAuthTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String accessToken = null;
            try {
                String scopeStr = "oauth2:" + Scopes.PLUS_LOGIN + " " + Scopes.PLUS_ME + " " + Scopes.EMAIL + "  https://www.googleapis.com/auth/plus.profile.emails.read";
                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), params[0], scopeStr);

                Log.d(TAG, "<GoogleAuthTask / doInBackground / Knowlounge> Google Access Token : " + accessToken);
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            }
            return accessToken;
        }

        @Override
        protected void onPostExecute(String token) {
            Log.d(TAG, "<GoogleAuthTask / onPostExecute / Knowlounge> Google Access Token : " + token);
            if (TextUtils.isEmpty(prefManager.getAccessToken())) {
                prefManager.setSnsType("1");
                prefManager.setAccessToken(token);
                getUserCredential("1", token);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_network_error), Toast.LENGTH_LONG).show();
        }
    }
}
