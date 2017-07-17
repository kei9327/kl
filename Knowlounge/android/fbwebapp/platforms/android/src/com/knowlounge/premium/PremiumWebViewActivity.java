package com.knowlounge.premium;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.dialog.ExtendReqDialogFragment;
import com.knowlounge.fragment.dialog.RoomPasswdDialogFragment;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mansu on 2016-12-26.
 */

public class PremiumWebViewActivity extends AppCompatActivity {
    private final String TAG = "PremiumWebViewActivity";

    private WebView mWebView;
    private WebView mWebviewPop;
    private Context mContext;
    private FrameLayout mContainer;
    private String url = "https://www.knowlounges.com";
    private String targetUrlPrefix = "edudev.knowlounges.com";

    ProgressDialog mProgressDialog;

    private WenotePreferenceManager prefManager;

    private Intent paramIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_webview);

        prefManager = WenotePreferenceManager.getInstance(getApplicationContext());
        paramIntent = getIntent();

        url = KnowloungeApplication.KNOWLOUNGE_PREMIUM_URL + "knowlounge/android/main";

        mWebView = (WebView) findViewById(R.id.webview);
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);

        mContext = this.getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }

//        Bundle param = getIntent().getExtras();
//        url = param.getString("url");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        url = url + "?tk=" + prefManager.getPremiumToken() + "&ptk=" +prefManager.getDeviceToken();

        Log.d(TAG, "Premium init url : " + url);

        mWebView.setWebViewClient(new WebViewClientClass());  // WebViewClient 지정
        mWebView.setWebChromeClient(new MyCustomChromeClient());
        mWebView.loadUrl(url);

        if (paramIntent != null) {
            String userNo = paramIntent.getStringExtra("userno");
            String roomCode = paramIntent.getStringExtra("code");
            if (roomCode != null)
                MainActivity._instance.enterRoom(roomCode);
        }
   }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String schema = uri.getScheme();
            Log.d("shouldOverrideUrl", host);
            //Toast.makeText(MainActivity.this, host,
            //Toast.LENGTH_SHORT).show();
            if (host.equals(targetUrlPrefix)) {
                // This is my web site, so do not override; let my WebView load
                // the page
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop = null;
                }
                return false;
            }

            if (host.contains("m.facebook.com") || host.contains("facebook.co")
                    || host.contains("google.co")
                    || host.contains("www.facebook.com")
                    || host.contains(".google.com")
                    || host.contains(".google.co")
                    || host.contains("accounts.google.com")
                    || host.contains("accounts.google.co.in")
                    || host.contains("www.accounts.google.com")
                    || host.contains("www.twitter.com")
                    || host.contains("secure.payu.in")
                    || host.contains("https://secure.payu.in")
                    || host.contains("oauth.googleusercontent.com")
                    || host.contains("content.googleapis.com")
                    || host.contains("ssl.gstatic.com")) {
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch
            // another Activity that handles URLs
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //startActivity(intent);
            //return true;

            // klounge 스키마 url에 대한 처리..
            if (TextUtils.equals(schema, "klounge")) {
Log.d(TAG, "##### klounge ##### : " + url);
                if (TextUtils.equals(host, "login")) {
                    String userNo = uri.getQueryParameter("user");
                    String accessToken = uri.getQueryParameter("tk");
                    String cookieStr = uri.getQueryParameter("cookie");
                    String groupListStr = uri.getQueryParameter("grouplist");

                    String[] cookieArr = cookieStr.split(",");
                    String masterCookie = cookieArr[0].split("\\|")[1];
                    String checksumCookie = cookieArr[1].split("\\|")[1];
Log.d(TAG, "### masterCookie : " + masterCookie);
Log.d(TAG, "### checksumCookie : " + checksumCookie);

                    prefManager.setPremiumUserNo(userNo);
                    prefManager.setPremiumToken(accessToken);
                    prefManager.setPremiumMasterCookie(masterCookie);
                    prefManager.setPremiumChecksumCookie(checksumCookie);
                    prefManager.setPremiumGroupList(groupListStr);

                } else if (TextUtils.equals(host, "logout")) {
Log.d(TAG, "##### logout #####");
                    prefManager.setPremiumUserNo("");
                    prefManager.setPremiumToken("");
                    prefManager.setPremiumMasterCookie("");
                    prefManager.setPremiumChecksumCookie("");
                    prefManager.setPremiumGroupList("");
                    finish();
                    // TODO: webview 창 닫거나, 초기 페이지로 이동...
                } else if (TextUtils.equals(host, "joinclass")) {
                    String userNo = uri.getQueryParameter("user");
                    String code = uri.getQueryParameter("code");

                    enterRoom(code);

                } else if (TextUtils.equals(host, "viewvideo")) {
                    // Don't care..
                } else if (TextUtils.equals(host, "gobrowser")) {
                    try {
                        String urlStr = URLDecoder.decode(uri.getQueryParameter("url"), "utf-8");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                        startActivity(browserIntent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            } else if(TextUtils.equals(schema, "intent")) {
Log.d(TAG, "##### intent ##### : " + url);
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if(existPackage != null) {
                        startActivity(intent);
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        startActivity(marketIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            return false;
        }
    }


    private class MyCustomChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(mContext);
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new WebViewClientClass());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            // TODO Auto-generated method stub
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }

    }


    private void enterRoom(final String roomCode) {
        try {
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.now_loading) , true);

            String passwd = "";
            String tokenStr = "roomcode=" + roomCode + "&passwd=" + passwd;

            AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
            String encryptToken = aesUtilObj.encrypt(tokenStr);

            final RequestParams params = new RequestParams();
            params.put("token", encryptToken);

            String masterCookie = prefManager.getPremiumMasterCookie();
            String checksumCookie = prefManager.getPremiumChecksumCookie();
            RestClient.postWithCookie("room/check.json", masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.d(TAG, response.toString());
                        int result = response.getInt("result");
                        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (result == 0) {
                            String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                            Intent mainIntent = new Intent(getApplicationContext(), RoomActivity.class);
                            //mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mainIntent.putExtra("type", "premium");
                            mainIntent.putExtra("roomurl", roomUrl);
                            mainIntent.putExtra("deviceid", deviceId);
                            mainIntent.putExtra("mode", GlobalConst.ENTER_ROOM_MODE);

                            startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM_WITH_ROOM_CODE);
                        } else if (result == -201 | result == -8001) {   // Invalid room
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
                            args.putString("masterno", response.getJSONObject("map").getString("masterno"));

                            ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getSupportFragmentManager(), "extend_user_limit");

                        } else if (result == -208) {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.global_popup_full), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        mProgressDialog.dismiss();
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
}
