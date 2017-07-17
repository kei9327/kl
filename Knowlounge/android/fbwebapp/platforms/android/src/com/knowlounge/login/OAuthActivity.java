package com.knowlounge.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;

/**
 * Created by Minsu on 2016-08-24.
 */
public abstract class OAuthActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "OAuthActivity";

    private WenotePreferenceManager prefManager;

    private final int CODE_SIGN_IN = 1001;
    private final int CODE_SIGN_OUT = 1002;
    private final int REQUEST_GOOGLE_AUTHORIZATION = 9003;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private GoogleApiClient mGoogleApiClient;

//    GoogleAuthTask googleAuthTask = null;

    protected enum OAuthType {
        FACEBOOK, GOOGLE
    }

    private String facebookAccessToken;

    public DbOpenHelper mDbOpenHelper;

    private ProgressDialog mProgressDialog;


    public abstract void onOAuthSuccess(OAuthType type, String accessToken);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = WenotePreferenceManager.getInstance(this);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);
        mDbOpenHelper.dropAndReCreateTable(DataBases.CreateDB._TABLENAME);

        callbackManager = CallbackManager.Factory.create();
    }




//    private void initFacebook() {
//        callbackManager = CallbackManager.Factory.create();
//        LoginManager manager = LoginManager.getInstance();
//        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                AppLog.d(AppLog.TAG, "Facebook LoginManager / onSuccess");
//                AccessToken.setCurrentAccessToken(loginResult.getAccessToken());
//
//                facebookAccessToken = loginResult.getAccessToken().getToken();
//                if (facebookAccessToken == null || facebookAccessToken.length() == 0) {
//                    return;
//                }
//
//                AppLog.d(AppLog.TAG, "Facebook LoginManager / onSuccess - accessToken : " + facebookAccessToken);
//
//                final Bundle fbParams = new Bundle();
//                fbParams.putString("fields", "id,name,email,picture.type(small)");
//                new GraphRequest(
//                        AccessToken.getCurrentAccessToken(),
//                        "/me/friends",
//                        fbParams,
//                        HttpMethod.GET,
//                        new GraphRequest.Callback() {
//                            public void onCompleted(GraphResponse response) {
//                                AppLog.d(AppLog.TAG, "Facebook /me/friends success");
//                                try {
//                                    if (response != null) {
//                                        JSONObject responseJson = response.getJSONObject();
//                                        if (responseJson != null) {
//                                            //Todo 친구 추가
//                                            AppLog.d(AppLog.TAG, "Facebook friend list : " + responseJson.getJSONArray("data").toString());
//                                            JSONArray arr = responseJson.getJSONArray("data");
//                                            for(int i=0; i< arr.length(); i++)
//                                            {
//                                                JSONObject obj = arr.getJSONObject(i);
//                                                String userId = obj.getString("id");
//                                                String userNm = obj.getString("name");
//                                                String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : obj.getJSONObject("picture").getJSONObject("data").getString("url");
//
//                                                mDbOpenHelper.insert(userNm, userId, thumbnail);
//                                            }
//
//                                            prefManager.setSnsType("0");
//                                            prefManager.setAccessToken(facebookAccessToken);
//                                            //getUserCredential("0", facebookAccessToken);
//                                            onOAuthSuccess(OAuthType.FACEBOOK, facebookAccessToken);
//                                        }
//                                    } else {
//                                        // TODO : 응답을 제대로 받지 않았을 때에 대한 예외처리 필요..
//                                    }
//                                } catch (JSONException e) {
//
//                                }
//                            }
//                        }
//                ).executeAsync();
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                AppLog.d(AppLog.TAG, "Facebook LoginManager / onError");
//                if (exception instanceof FacebookAuthorizationException) {
//                    // error
//                    String msg = exception.getMessage();
//
//                    AppLog.d(AppLog.TAG, exception.getMessage());
//                    if (msg.indexOf("ERR_INTERNET_DISCONNECTED") > -1) {
//                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_network_error), Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_network_error), Toast.LENGTH_LONG).show();
//                    }
//                }
//                AccessToken.setCurrentAccessToken(null);
//                facebookAccessToken = null;
////				currentUserChanged();
//            }
//
//            @Override
//            public void onCancel() {
//                AppLog.d(AppLog.TAG, "Facebook LoginManager / onCancel");
//                AccessToken.setCurrentAccessToken(null);
//                facebookAccessToken = null;
////				currentUserChanged();
//            }
//        });
//
//        accessTokenTracker = new AccessTokenTracker() {
//            @Override
//            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
//                // Set the access token using
//                // currentAccessToken when it's loaded or set.
//            }
//        };
//    }
//
//    private void initGoogle() {
//        if (mGoogleApiClient == null) {
//            Plus.PlusOptions gpo = Plus.PlusOptions.builder().addActivityTypes(
//                    "http://schemas.google.com/AddActivity",
//                    "http://schemas.google.com/BuyActivity",
//                    "http://schemas.google.com/CreateActivity").build();
//
//            GoogleApiClient.Builder googleApiBuilder = new GoogleApiClient.Builder(this);
//            googleApiBuilder.addScope(new Scope(Scopes.PLUS_LOGIN));
//            googleApiBuilder.addScope(new Scope(Scopes.PLUS_ME));
//            googleApiBuilder.addScope(new Scope(Scopes.EMAIL));
//            googleApiBuilder.addScope(new Scope("https://www.googleapis.com/auth/plus.profile.emails.read"));
//
//
//            mGoogleApiClient = googleApiBuilder.addApi(Plus.API, gpo)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .build();
//
//        }
//
//        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.connect();
//        } else {
//            mGoogleApiClient.disconnect();
//            mGoogleApiClient.connect();
//        }
//    }
//
//
//    /** GoogleApiClient.ConnectionCallbacks implements **/
//    @Override
//    public void onConnected(Bundle bundle) {
//        AppLog.d(AppLog.TAG, "onConnected()");
//
//        final String mAccountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
//        AppLog.d(AppLog.TAG, "account name : " + mAccountName);
//
//        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
//            @Override
//            public void onResult(People.LoadPeopleResult loadPeopleResult) {
//                Log.d("onConnected Result:", Integer.toString(loadPeopleResult.getStatus().getStatusCode()));
//
//                //if (loadPeopleResult.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
//                if (loadPeopleResult.getStatus().isSuccess()) {
//                    PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();
//                    int count = personBuffer.getCount();
//
//                    //구글 플러스 친구 리스트
//                    for (int i = 0; i < count; i++) {
//                        String userNm = personBuffer.get(i).getDisplayName();
//                        String userId = personBuffer.get(i).getId();
//                        String thumbnail = personBuffer.get(i).getImage().getUrl();
//                        mDbOpenHelper.insert(userNm, userId, thumbnail);
//                    }
//                    personBuffer.release();
//
//                } else {
//                    AppLog.d(AppLog.TAG, "loadPeopleResult.getStatus() : " + loadPeopleResult.getStatus() + ", statusCode : " + loadPeopleResult.getStatus().getStatusCode());
//
//                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                        mProgressDialog.dismiss();
//                    }
//                }
//
//                String userCookie = prefManager.getUserCookie() == null ? "" : prefManager.getUserCookie();
//                if (TextUtils.isEmpty(userCookie)) {
//                    googleAuthTask = new GoogleAuthTask();
//                    googleAuthTask.execute(mAccountName);
//                }
//            }
//        });
//    }
//
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//
//    }
//
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        try {
//            if (!connectionResult.hasResolution()) {
//                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        finish();
//                    }
//                }).show();
//
//            } else {
//                AppLog.d(AppLog.TAG, "onConnectionFailed(): connection resolving.");
//                AppLog.d(AppLog.TAG, "Google login errCode : " + connectionResult.getErrorCode());
//                AppLog.d(AppLog.TAG, "Google login errMsg : " + connectionResult.getErrorMessage());
//
//                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                    mProgressDialog.dismiss();
//                    mProgressDialog = null;
//                }
//
//                connectionResult.startResolutionForResult(this, CODE_SIGN_IN);
//                final int errorCode = connectionResult.getErrorCode();
//                AppLog.d(AppLog.TAG, "Google login errCode : " + errorCode);
//
//                if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
//
//                } else {
//
//                    // todo show sign-out
//                }
//            }
//        } catch (IntentSender.SendIntentException e) {
//            AppLog.d(AppLog.TAG, "onConnectionFailed(): connection failed.", e);
//        }
//
//    }
//
//
//    public class GoogleAuthTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... params) {
//            String accessToken = null;
//            try {
//                String scopeStr = "oauth2:" + Scopes.PLUS_LOGIN + " " + Scopes.PLUS_ME + " " + Scopes.EMAIL + "  https://www.googleapis.com/auth/plus.profile.emails.read";
//                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), params[0], scopeStr);
//                AppLog.d(AppLog.TAG, "[doInBackground] Google Access Token : " + accessToken);
//            } catch (UserRecoverableAuthException e) {
//                //startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
//                e.printStackTrace();
//            } catch (GoogleAuthException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return accessToken;
//        }
//
//
//        @Override
//        protected void onPostExecute(String googleAccessToken) {
//            AppLog.d(AppLog.TAG, "[onPostExecute] Google Access Token : " + googleAccessToken);
//            prefManager.setSnsType("1");
//            prefManager.setAccessToken(googleAccessToken);
//            //getUserCredential("1", token);
//            onOAuthSuccess(OAuthType.GOOGLE, googleAccessToken);
//        }
//    }
//
//
//    public void closeGoogleApi() {
//        if (googleAuthTask != null) {
//            googleAuthTask.cancel(false);
//        }
//        if (mGoogleApiClient != null) {
//            if (mGoogleApiClient.isConnected()) {
//                mGoogleApiClient.disconnect();
//            }
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//
//    /**
//     * SNS 친구 리스트 불러오기..
//     */
//    public void getFriendList() {
//        AppLog.d(AppLog.TAG, "getFriendList");
//        Log.d(TAG, "getFriendList");
//        String snsType = prefManager.getSnsType();
//        if ("0".equals(snsType)) {
//            final Bundle fbParams = new Bundle();
//            fbParams.putString("fields", "id,name,email,picture.type(small)");
//            new GraphRequest(
//                    AccessToken.getCurrentAccessToken(),
//                    "/me/friends",
//                    fbParams,
//                    HttpMethod.GET,
//                    new GraphRequest.Callback() {
//                        public void onCompleted(GraphResponse response) {
//                            AppLog.d(AppLog.TAG, "Facebook /me/friends success");
//                            try {
//                                if (response != null) {
//                                    JSONObject responseJson = response.getJSONObject();
//                                    if (responseJson != null) {
//                                        //Todo 친구 추가
//                                        AppLog.d(AppLog.TAG, "Facebook friend list : " + responseJson.getJSONArray("data").toString());
//                                        JSONArray arr = responseJson.getJSONArray("data");
//                                        for(int i=0; i<arr.length(); i++) {
//                                            JSONObject obj = arr.getJSONObject(i);
//                                            String userId = obj.getString("id");
//                                            String userNm = obj.getString("name");
//                                            String thumbnail = obj.has("thumbnail") ? obj.getString("thumbnail") : obj.getJSONObject("picture").getJSONObject("data").getString("url");
//
//                                            mDbOpenHelper.insert(userNm, userId, thumbnail);
//                                        }
//                                    }
//                                } else {
//                                    // TODO : 응답을 제대로 받지 않았을 때에 대한 예외처리 필요..
//                                }
//                            } catch (JSONException e) {
//
//                            }
//
//                        }
//                    }
//            ).executeAsync();
//
//        } else if("1".equals(snsType)) {
//            if (startPermissionCheck(new String[] {Manifest.permission.GET_ACCOUNTS} )) {
//                startGoogleAuth();
//            } else {
//
//            }
//        }
//    }
//
//
//    protected boolean startPermissionCheck(String[] permissions) {
//        final List<String> rationales = new ArrayList<>();
//
//        if (!mRuntimePermissionChecker.checkSelfPermission(permissions, rationales)) {
//            AppLog.i(AppLog.TAG, "displaying contacts permission rationale to provide additional context.");
//            AppLog.i(AppLog.TAG, rationales.toString());
//
//            if (!rationales.isEmpty()) {
//                StringBuilder message = new StringBuilder(getString(R.string.permission_alert_head_optional));
//                List<String> missing = mRuntimePermissionChecker.getPermissionDisplayName(rationales);
//                for (String perm : missing) {
//                    message.append("- ");
//                    message.append(perm);
//                    message.append("\n");
//                }
//                message.append(getString(R.string.permission_alert_footer));
//
//                // TODO AlertDialog는 모두 알로 테마 다이얼로그로 교체할 것.
//                new AlertDialog.Builder(this)
//                        .setTitle("Notice")
//                        .setMessage(message.toString())
//                        .setCancelable(false)
//                        .setNegativeButton("Cancel", null)
//                        .setPositiveButton("Setting",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
//                                        startActivity(intent);
//
//                                    }
//                                }).show();
//            } else {
//                /**
//                 * Note
//                 * Fragment.onRequestPermissionsResult 콜백을 호출하여 처리하기 위해서는
//                 * ActivityCompat.requestPermissions를 호출하지 말고, Fragment.requestPermissions를
//                 * 호출하여 처리해야 한다.
//                 */
//                mRuntimePermissionChecker.requestPermissions(permissions, PERMISSION_REQUEST_READ_CONTACTS);
//            }
//            return false;
//        }
//        return true;
//    }
}
