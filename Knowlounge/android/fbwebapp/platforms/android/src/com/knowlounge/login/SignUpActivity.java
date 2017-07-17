package com.knowlounge.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
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
import com.knowlounge.R;
import com.knowlounge.model.FriendUser;
import com.knowlounge.network.restful.command.ApiCommand;
import com.knowlounge.network.restful.command.AuthApiCommand;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-07-21.
 */
public abstract class SignUpActivity extends GoogleAuthActivity {

    public String facebookAccessToken;
    public CallbackManager callbackManager;
    public AccessTokenTracker accessTokenTracker;

    private final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();
        initFacebookAuth();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void initFacebookAuth() {
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
                                            getUserCredential("0", facebookAccessToken);
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

    /**
     * 2016.11.28 - deprecated
     */
    @Deprecated
    public void getAuthReload() {
        AppLog.d(AppLog.TAG, "=========== reloadAuthInfo ===========");
        Log.d(TAG, "reloadAuthInfo");
        type = 3;

//        DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);
//        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);
//        String friendsIdStr = mDbOpenHelper.getAllFriendId();
//        mDbOpenHelper.close();
//
//        if(TextUtils.isEmpty(friendsIdStr)) {
//            getFriendList();
//        }

        getFriendList();

        /*
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
                                        try {
                                            int apiResult = object.get("result").getAsInt();
                                            if (apiResult == 0) {
                                                String reloadedMasterCookie = object.get("cookie").getAsJsonObject().get("FBMMC").getAsString();
                                                String reloadedCheckSumCookie = object.get("cookie").getAsJsonObject().get("FBMCS").getAsString();

                                                String newQueryStr = "FBMMC=" + reloadedMasterCookie + "&FBMCS=" + reloadedCheckSumCookie;

                                                prefManager.setUserCookie(reloadedMasterCookie);
                                                prefManager.setChecksumCookie(reloadedCheckSumCookie);
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
                                        }
                                    }
                                });
                    }
                }).execute();
            */

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
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        if(mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
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
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                AppLog.d(AppLog.TAG, "auth/reload.json failed.. statusCode : " + statusCode);
            }
        });
    }
}