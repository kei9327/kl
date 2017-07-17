package com.knowlounge.util;

import android.content.Context;
import android.util.Log;

import com.knowlounge.KnowloungeApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Locale;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by Minsu on 2016-03-10.
 */
public class RestClient {
    private static final String TAG = "RestClient";

//    private static final String BASE_URL = "http://192.168.0.195/mapi/";
//    private static final String BASE_URL = "https://dev.knowlounges.com/mapi/";
//    private static final String BASE_URL = "https://www.knowlounges.com/mapi/";


    private static final String SI_BASE_URL = "https://www.sayalo.me:30443/knowlounge/";
    private static final String SI_BASE_URL_DEV = "http://sidev.wescan.com:9000/knowlounge/";
    private static final String UTUBE_URL = "https://www.googleapis.com/youtube/v3/";


    private static final String COOKIE_MASTER_KEY = "FBMMC";
    private static final String COOKIE_CHECKSUM_KEY = "FBMCS";

    private static AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getWithCookie(String url, String masterCookie, String checksumCookie, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String localeStr = Locale.getDefault().toString().replace("_", "-");
        Log.d("RestClient", localeStr);
        client.addHeader("Cookie", COOKIE_MASTER_KEY + "=" + masterCookie + "; " + COOKIE_CHECKSUM_KEY + "=" + checksumCookie + ";");
        client.addHeader("Accept-Language", localeStr);  // 다국어 처리를 위한 로케일값 전송..
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getSiPlatform(String url, boolean isDev, AsyncHttpResponseHandler responseHandler) {
        client.get(getSiAbsoluteUrl(url, isDev), responseHandler);
    }

    public static void postSiPlatform(String url, boolean isDev, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(30000);
        client.post(getSiAbsoluteUrl(url, isDev), null, responseHandler);
    }

    public static void deleteSiPlatform(String url, boolean isDev, AsyncHttpResponseHandler responseHandler) {
        client.delete(getSiAbsoluteUrl(url, isDev), new RequestParams(), responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postWithCookie(String url, String masterCookie, String checksumCookie, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String localeStr = Locale.getDefault().toString().replace("_", "-");
        client.addHeader("Cookie", COOKIE_MASTER_KEY + "=" + masterCookie + "; " + COOKIE_CHECKSUM_KEY + "=" + checksumCookie + ";");
        client.addHeader("Accept-Language", localeStr);  // 다국어 처리를 위한 로케일값 전송..
        //client.addHeader(COOKIE_MASTER_KEY, masterCookie);
        //client.addHeader(COOKIE_CHECKSUM_KEY, checksumCookie);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }


    public static void postWithJsonString(Context context, String url, String masterCookie, String checksumCookie, JSONObject jsonParam, AsyncHttpResponseHandler responseHandler) {
        try {
            StringEntity entity = new StringEntity(jsonParam.toString(), Charset.forName("utf-8"));
            String localeStr = Locale.getDefault().toString().replace("_", "-");
            client.addHeader("Cookie", COOKIE_MASTER_KEY + "=" + masterCookie + "; " + COOKIE_CHECKSUM_KEY + "=" + checksumCookie + ";");
            client.addHeader("Accept-Language", localeStr);  // 다국어 처리를 위한 로케일값 전송..
            client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public static void postWithBasicAuth(String url, String queryString, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", queryString);
        //client.setBasicAuth("Authorization", queryString);
        client.post(getAbsoluteUrl(url), params, responseHandler);

    }

    public static void requestHttpPostStringToJsonObject(String url, String body, String type) {
        client.addHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    private static String getAbsoluteUrl(String relativeUrl) {
//        Log.d(TAG, "HTTP Request url : " + BASE_URL + relativeUrl);
//        return BASE_URL + relativeUrl;
        Log.d(TAG, "HTTP Request url : " + KnowloungeApplication.KNOWLOUNGE_API_HOST + relativeUrl);
        return KnowloungeApplication.KNOWLOUNGE_API_HOST + relativeUrl;
    }

    private static String getSiAbsoluteUrl(String relativeUrl, boolean isDev) {
        String rootPath = isDev ? SI_BASE_URL_DEV : SI_BASE_URL;
        Log.d(TAG, "HTTP Request url : " + rootPath + relativeUrl);
        return rootPath + relativeUrl;
    }

    private static String getYouTubeListUrl(String relativeUrl){
        return UTUBE_URL + relativeUrl;
    }

    public static void getYouTubeList(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getYouTubeListUrl(url), params, responseHandler);
    }
}
