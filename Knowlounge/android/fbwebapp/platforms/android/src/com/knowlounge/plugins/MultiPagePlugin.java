package com.knowlounge.plugins;

import android.util.Log;

import com.knowlounge.view.room.RoomActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mansu on 2016-10-13.
 */

public class MultiPagePlugin extends CordovaPlugin {

    private CallbackContext callbackContext = null;

    private final String TAG = "MultiPagePlugin";

    private static String ACTION_ADD_PAGE = "addPage";
    private static String ACTION_CHANGE_PAGE = "changePage";
    private static String ACTION_REMOVE_PAGE = "removePage";
    private static String ACTION_ORDER_PAGE = "orderPage";
    private static String ACTION_INIT_PAGE_LIST = "initPageList";
    private static String ACTION_ORDER_RESULT = "orderResult";


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
//        roomListener = (RoomEventListener) RoomActivity.activity;
        //this.activity = (IndexActivity) cordova.getActivity();
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_ADD_PAGE)) {
            this.addPage(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_CHANGE_PAGE)) {
            this.changePage(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_REMOVE_PAGE)) {
            this.removePage(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_ORDER_PAGE)) {
            this.orderPage(callbackContext, args);
        } else if (action.equals(ACTION_INIT_PAGE_LIST)) {
            this.initPageList(callbackContext, args);
        } else if (action.equals(ACTION_ORDER_RESULT)) {
            this.orderResult(callbackContext, args.getJSONObject(0));
        }
        return true;
    }


    private void addPage(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        //RoomActivity.activity.setBgInfo(obj);
        Log.d(TAG, "addPage : "+ obj.toString());
        RoomActivity.activity.setAddMultiPage(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void changePage(CallbackContext callbackContext, JSONObject obj) throws JSONException {

        Log.d(TAG, "changePage : "+ obj.toString());
        RoomActivity.activity.setCurrentPageId(obj);

        //RoomActivity.activity.setAuthInfo(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void removePage(CallbackContext callbackContext, JSONObject obj) throws JSONException {

        Log.d(TAG, "removePage : "+ obj.toString());
        RoomActivity.activity.setDelMultiPage(obj);

        //RoomActivity.activity.updateUserLimitCnt(25);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void orderPage(CallbackContext callbackContext, JSONArray arr) throws JSONException {

        Log.d(TAG, "orderPage : "+ arr.toString());
        RoomActivity.activity.setOrderPageList(arr);

        //RoomActivity.activity.updateUserLimitCnt(25);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void initPageList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        // TODO : JSONArray arr로 멀티 페이지 리스트 UI 구성
        Log.d(TAG, "initPageList : "+ arr.toString());
        RoomActivity.activity.setInitMultiPage(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void orderResult(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        // TODO : JSONArray arr로 멀티 페이지 리스트 UI 구성

        Log.d(TAG, "orderResult : "+ obj.toString());
        RoomActivity.activity.setOrderResult(obj);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }
}