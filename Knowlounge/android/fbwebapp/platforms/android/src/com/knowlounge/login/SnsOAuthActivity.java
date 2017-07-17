package com.knowlounge.login;

import android.Manifest;
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
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
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
import com.knowlounge.gcm.GcmRegistStatePreference;
import com.knowlounge.gcm.RegistrationIntentService;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.FriendUser;
import com.knowlounge.network.restful.APIUrl;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.CommonUtils;
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
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mansu on 2017-04-26.
 */

public abstract class SnsOAuthActivity extends NetworkStateActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private final String TAG = "SnsOAuthActivity";

    // const
    private final int CODE_SIGN_IN = 1001;
    private final int CODE_SIGN_OUT = 1002;
    private final int REQUEST_AUTHORIZATION = 9003;

    // UI
    public ProgressDialog mProgressDialog;

    // 페이스북 OAuth
    public String facebookAccessToken;
    public CallbackManager callbackManager;
    public AccessTokenTracker accessTokenTracker;

    // 구글 OAuth
    public GoogleApiClient mGoogleApiClient;
    GoogleAuthTask googleAuthTask = null;

    protected RuntimePermissionChecker mRuntimePermissionChecker;
    public final int PERMISSION_REQUEST_READ_CONTACTS = 1;
    public final int PERMISSION_REQUEST_CAMERA_AND_AUDIO = 0;

    // 필수 퍼미션 정의
    public String[] essentialPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public final int PERMISSION_REQUEST_CODE = 3;

    public DbOpenHelper mDbOpenHelper;

    public int type = -1;

    public String version;

    private WenotePreferenceManager prefManager;

    public BroadcastReceiver mRegistrationBroadcastReceiver = null;

    public boolean isSubscribeFirst = true;

    public boolean enableGoogleApi = false;   // Deprecated 된 Google Plus API 혹은 Google API 사용유무를 설정하는 값

    public int RC_SIGN_IN = 1111;

    public abstract void finishSignInProcess();  // 현재 사용하지 않음 - 2017.04.27
    public abstract void finishedReload();       // 인증값 갱신 절차가 완료되었을 때 호출
    public abstract void goToLogin(@Nullable ConnectionResult connectionResult);   // 로그인 페이지로 강제 이동시킬 때 호출


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = WenotePreferenceManager.getInstance(this);

        // Facebook SDK 초기화
        initFacebookAuth();

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
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // 신규 Google OAuth 버전
        if (enableGoogleApi) {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount account = result.getSignInAccount();
                Log.d(TAG, "<onActivityResult / GoogleSignIn / Knowlounge> displayName : " + account.getDisplayName());
                Log.d(TAG, "<onActivityResult / GoogleSignIn / Knowlounge> email : " + account.getEmail());
                Log.d(TAG, "<onActivityResult / GoogleSignIn / Knowlounge> Id : " + account.getId());
                Log.d(TAG, "<onActivityResult / GoogleSignIn / Knowlounge> IdToken : " + account.getIdToken());
                Log.d(TAG, "<onActivityResult / GoogleSignIn / Knowlounge> PhotoUrl : " + account.getPhotoUrl());

                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                googleAuthTask = new GoogleAuthTask();
                googleAuthTask.execute(account.getEmail());
            }
        }
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <implements : ActivityCompat.OnRequestPermissionsResultCallback>
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "<onRequestPermissionsResult>");

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


    /*
     *----------------------------------------------------------------------------------------------
     *-- <implements : GoogleApiClient.ConnectionCallbacks>
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "<onConnected / Knowlounge>");
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
        Log.d(TAG, "<onConnected / Knowlounge> mAccountName : " + mAccountName);

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
            @Override
            public void onResult(People.LoadPeopleResult loadPeopleResult) {
                Log.d(TAG, "<onConnected / Knowlounge> Result : " + Integer.toString(loadPeopleResult.getStatus().getStatusCode()));

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
//                if (TextUtils.isEmpty(userCookie)) {
                    googleAuthTask = new GoogleAuthTask();
                    googleAuthTask.execute(mAccountName);
//                }
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <implements : GoogleApiClient.OnConnectionFailedListener>
     */
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


                final int errorCode = connectionResult.getErrorCode();

                if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {

                } else {
                    // todo show sign-out
                }
            }

            // SIGN_IN_REQUIRED 에러가 발생하면 이미 인증정보가 존재해도 로그인 화면으로 이동한다.
            if (connectionResult.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                goToLogin(connectionResult);
            }
        } catch (Exception e) {
            Log.e(TAG, "<onConnectionFailed> connection failed.", e);
        }

    }

    public void startResolution(ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(this, CODE_SIGN_IN);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "<onConnectionFailed> connection failed.", e);
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
                Log.d(TAG, "<GoogleAuthTask / onPostExecute / Knowlounge> 최초로 로그인한 상태입니다.");
                prefManager.setSnsType("1");
                prefManager.setAccessToken(token);
                loadUserCredential("1", token);
            } else {
                if (!TextUtils.equals(prefManager.getAccessToken(), token))
                    prefManager.setAccessToken(token);
                Log.d(TAG, "<GoogleAuthTask / onPostExecute / Knowlounge> 이미 로그인한 상태입니다.");
                reloadUserCredential();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_network_error), Toast.LENGTH_LONG).show();
        }
    }


    /*
     *----------------------------------------------------------------------------------------------
     *-- <methods>
     */
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

    private void initFacebookAuth() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager manager = LoginManager.getInstance();
        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook LoginManager / onSuccess");
                AccessToken.setCurrentAccessToken(loginResult.getAccessToken());

                Log.d(TAG, "Facebook AccessToken : " + loginResult.getAccessToken());

                facebookAccessToken = loginResult.getAccessToken().getToken();
                if (facebookAccessToken == null || facebookAccessToken.length() == 0) {
                    return;
                }

                Log.d(TAG, "Facebook LoginManager / onSuccess - accessToken : " + facebookAccessToken);

                final Bundle fbParams = new Bundle();
                fbParams.putString("fields", "id,name,email,picture.type(small)");
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/friends",
                        fbParams,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.d(TAG, "Facebook /me/friends success");
                                try {
                                    if (response != null) {
                                        JSONObject responseJson = response.getJSONObject();
                                        if (responseJson != null) {
                                            //Todo 친구 추가
                                            AppLog.d(AppLog.TAG, "Facebook friend list : " + responseJson.getJSONArray("data").toString());
                                            JSONArray arr = responseJson.getJSONArray("data");

                                            for(int i=0; i< arr.length(); i++) {
                                                JSONObject obj = arr.getJSONObject(i);
                                                String userId = obj.getString("id");
                                                String userNm = obj.getString("name");
                                                String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : obj.getJSONObject("picture").getJSONObject("data").getString("url");
                                                mDbOpenHelper.insert(new FriendUser(userId, userNm, thumbnail));
                                            }
                                            prefManager.setSnsType("0");
                                            prefManager.setAccessToken(facebookAccessToken);
                                            loadUserCredential("0", facebookAccessToken);
                                        }
                                    } else {
                                        // TODO : 응답을 제대로 받지 않았을 때에 대한 예외처리 필요..
                                    }
                                } catch (JSONException e) {

                                }
                            }
                        }
                ).executeAsync();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "Facebook LoginManager / onError");
                if (exception instanceof FacebookAuthorizationException) {
                    // error
                    String msg = exception.getMessage();
                    Log.d(TAG, exception.getMessage());
                    //Log.d(TAG, exception.getCause().getMessage());
                    if (msg.indexOf("ERR_INTERNET_DISCONNECTED") > -1) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_network_error), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }
                AccessToken.setCurrentAccessToken(null);
                facebookAccessToken = null;
//				currentUserChanged();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook LoginManager / onCancel");
                AccessToken.setCurrentAccessToken(null);
                facebookAccessToken = null;
                //Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_LONG).show();
//				currentUserChanged();
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
    }

    public void initGoogleAuth() {
        Log.d(TAG, "initGoogleAuth");

        if(mGoogleApiClient == null) {

            mProgressDialog = null;
            mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.toast_account_retrieve), true);

            if (!enableGoogleApi) {
                // 이전 구글 플러스 OAuth 버전.. 구글 플러스 API는 deprecated 되었음.
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

                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "initGoogleAuth / init googleApiClient connect");
                    mGoogleApiClient.connect();
                } else {
                    Log.d(TAG, "initGoogleAuth / init googleApiClient reconnect");
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                }
            } else {
                // 신규 구글 OAuth 버전..
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(googleSignInIntent, RC_SIGN_IN);

                // TODO: 구글 친구리스트는 추후 구현 필요..

            }
        }
    }

    private void showAlertEssentialPermissions(String[] permissions) {
        final List<String> rationales = new ArrayList<>();

        Log.d(TAG, "displaying contacts permission rationale to provide additional context.");

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


    public void loadUserCredential(String snsType, String accessToken) {
        // auth.json 호출..
        AppLog.d(AppLog.TAG, "=========== loadUserCredential ===========");

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

        paramJson.addProperty("devicetoken", prefManager.getDeviceToken());
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


    /**
     * 이미 로그인한 장비에서는 Credential 정보를 갱신함.
     */
    public void reloadUserCredential() {
        Log.d(TAG, "<reloadUserCredential / Knowlounge>");
        type = 3;

        DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);
        String friendsIdStr = mDbOpenHelper.getAllFriendId();
        mDbOpenHelper.close();

        if(TextUtils.isEmpty(friendsIdStr)) {
            getFriendList();
        }

//        getFriendList();

        String masterCookie = prefManager.getUserCookie();
        String checkSumCookie = prefManager.getChecksumCookie();

        RestClient.postWithCookie("auth/reload.json", masterCookie, checkSumCookie, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int apiResult = response.getInt("result");
                    Log.d(TAG, response.toString());

                    if (apiResult == 0) {
                        String reloadedMC = response.getJSONObject("cookie").getString("FBMMC");
                        String reloadedCS = response.getJSONObject("cookie").getString("FBMCS");

                        String newQueryStr = "FBMMC=" + reloadedMC + "&FBMCS=" + reloadedCS;

                        prefManager.setUserCookie(reloadedMC);
                        prefManager.setChecksumCookie(reloadedCS);
                        prefManager.setCookieQueryStr(newQueryStr);

                        JSONObject userInfo = decryptUserCookie();

                        String userNo = userInfo.getString("userno");
                        String userId = userInfo.getString("userid");
                        String userNm = userInfo.getString("usernm");
                        String userEmail = userInfo.getString("email");
                        String userThumbnail = userInfo.has("thumbnail") ? userInfo.getString("thumbnail") : "";
                        String userThumbnailLarge = userInfo.has("thumbnail_large") ? userInfo.getString("thumbnail_large") : "";
                        String userSnstype = userInfo.getString("snstype");

                        // SharedPreference에 유저정보 저장..
                        prefManager.setUserNo(userNo);
                        prefManager.setUserId(userId);
                        prefManager.setUserNm(userNm);
                        prefManager.setEmail(userEmail);
                        prefManager.setUserThumbnail(userThumbnail);
                        prefManager.setUserThumbnailLargeCurrent(userThumbnailLarge);
                        prefManager.setSnsType(userSnstype);

                        // Star 서버에서 발급하는 토큰과 TTL값 얻어온 후 검증하기..
                        getSiPlatformUserToken();

                    } else if (apiResult == -8001) {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                    } else if (apiResult == -101) {
                        // TODO : 메세지 정의 필요 - 인증값이 잘 못되었을 때, 토스트 메세지
                        Toast.makeText(getApplicationContext(), "Authentication information is invalid. Please sign in agian.", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        if(mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        prefManager.clear();

                        goToLogin(null);
                    }
                } catch (JSONException e) {
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
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if (throwable instanceof IOException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
                if (throwable instanceof SocketTimeoutException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                AppLog.d(AppLog.TAG, "auth/reload.json failed.. statusCode : " + statusCode);
            }
        });
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


    /**
     * SNS 친구 리스트 불러오기..
     */
    public void getFriendList() {
        Log.d(TAG, "<getFriendList / Knowlounge>");
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
                                        AppLog.d(AppLog.TAG, "<getFriendList / Knowlounge> list : " + responseJson.getJSONArray("data").toString());
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
//                initGoogleAuth();
            } else {

            }
        }
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
                Log.e(TAG, "<getUserAccessToken / Knowlounge> user/accessToken fail.. statusCode : " + statusCode);
                prefManager.clear();
                Toast.makeText(getBaseContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JSONObject decryptUserCookie() {
        JSONObject resultObj = null;
        try {
            String masterCookie = prefManager.getUserCookie();
            AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
            String result = aesUtilObj.decrypt(masterCookie);
            result = URLDecoder.decode(result, "utf-8");
            Log.d(TAG, "<decryptUserCookie / Knowlounge> Decrypt result : " + result);
            resultObj = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObj;
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
        Log.d(TAG, "<subscribeSiPlatform / Knowlounge> url : " + url);

        RestClient.postSiPlatform(url, false, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "<subscribeSiPlatform / onSuccess / Knowlounge> statusCode : " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // TODO : 구독 설정 실패..
                Log.d(TAG, "<subscribeSiPlatform / onFailure / Knowlounge> statusCode : " + statusCode);
            }
        });
    }   //등록

}
