package com.knowlounge.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Minsu on 2016-09-12.
 */
public abstract class UserAuthActivity extends OAuthActivity {

    private static final String TAG = "UserAuthActivity";
    private WenotePreferenceManager prefManager;

    public ProgressDialog mProgressDialog;

    boolean hasAuth = false;


    public abstract void finishedReload();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "UserAuthActivity");
        super.onCreate(savedInstanceState);
        prefManager = WenotePreferenceManager.getInstance(this);
    }



    public void getAuthReload() {
        AppLog.d(AppLog.TAG, "=========== reloadAuthInfo ===========");
        Log.d(TAG, "reloadAuthInfo");
        hasAuth = true;
//        getFriendList();

        String masterCookie = prefManager.getUserCookie();
        String checkSumCookie = prefManager.getChecksumCookie();

        RestClient.postWithCookie("auth/reload.json", masterCookie, checkSumCookie, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int apiResult = response.getInt("result");
                    Log.d("AuthReload", response.toString());

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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
//                    closeGoogleApi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    Toast.makeText(getApplicationContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

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

                    if(hasAuth) {
                        finishedReload();
                    }

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
                Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


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


    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
