package com.knowlounge.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.RoomSwitchActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Minsu on 2016-06-07.
 */
public class RoomUtils {

    private static final String TAG = "RoomUtils";
    private String masterCookie;
    private String checksumCookie;


    public static void moveMyRoom(String roomId, String deviceId, String master, String checksum) {
        Log.d(TAG, "moveMyRoom");
        String url = "room/createSubRoom.json";
        RequestParams params = new RequestParams();
        params.put("roomid", roomId);
        params.put("deviceid", deviceId);

        RestClient.postWithCookie(url, master, checksum, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if ("0".equals(response.getString("result"))) {
                        String roomCode = response.getString("code");

//                        moveRoom(roomCode);

                        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                        Intent moveRoomIntent = new Intent(RoomActivity.activity, RoomSwitchActivity.class);
                        moveRoomIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        moveRoomIntent.putExtra("roomurl", roomUrl);
                        RoomActivity.activity.startActivity(moveRoomIntent);
                        RoomActivity.activity.finish();

                    } else {
                        Toast.makeText(RoomActivity.activity, "내 방으로 이동하는 중에 오류가 발생하였습니다..", Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(RoomActivity.activity, "내 방으로 이동하는 중에 오류가 발생하였습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void moveRoom(String roomCode) {
        Log.d(TAG, "moveRoom");
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                CordovaWebView webView = mWebViewFragment.getCordovaWebView();
//                webView.loadUrl("javascript:Ctrl.moveRoom('" + roomCode + "');");
//            }
//        });

        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
        Intent moveIntent = new Intent(RoomActivity.activity, RoomSwitchActivity.class);
        moveIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        moveIntent.putExtra("roomurl", roomUrl);
        RoomActivity.activity.startActivity(moveIntent);
        RoomActivity.activity.finish();

    }


    public static void presentStar(String userNo, String userId, int starAmount, AESUtil aesUtil, String master, String checksum) {

        try {
            String paramStr = "userno=" + userNo + "&userid=" + userId + "&star=" + starAmount;
            String encryptParam = aesUtil.encrypt(paramStr);

            RequestParams params = new RequestParams();
            params.put("token", encryptParam);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
