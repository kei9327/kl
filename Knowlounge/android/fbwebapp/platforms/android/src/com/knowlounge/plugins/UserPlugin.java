package com.knowlounge.plugins;

import android.util.Log;

import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.listener.UserPresenceListener;
import com.knowlounge.manager.WenotePreferenceManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Minsu on 2016-04-12.
 */
public class UserPlugin extends CordovaPlugin {

    private CallbackContext callbackContext = null;

    private final String TAG = "UserPlugin";

    private final String ACTION_ADD_ROOM_USER = "addRoomUser";
    private final String ACTION_REMOVE_ROOM_USER = "removeRoomUser";

    private final String ACTION_ADD_OTHER_USER_LIST = "addNotAttendee";
    private final String ACTION_REMOVE_NOT_ATTENDEE = "removeNotAttendee";

    private final String ACTION_UPDATE_MASTER_USER_NM = "updateMasterUserNm";

    private final String ACTION_CHANGE_MASTER = "changeMaster";

    private final String ACTION_ADD_CLASS_USER_LIST = "addClassUserList";
    private final String ACTION_REMOVE_CLASS_USER_LIST = "removeClassUserList";

    private final String ACTION_GET_USER_INFO = "getUserInfo";

    private static String ACTION_DEPORT_USER = "deportUser";

    private final String ACTION_INIT_GUEST_INFO = "initGuestInfo";

    private WenotePreferenceManager prefManager;

    public interface UserListEventListener {

        void addRoomUserListHandler(JSONArray userArr);
        void removeUserListHandler(JSONObject obj);

        void addClassUserListHandler(JSONArray arr);
        void removeClassUserListHandler(JSONObject obj);

        void addOtherUserListHandler(final JSONArray arr);
        void changeMasterHandler(final JSONObject obj);

        void onRemoveNotAttendee(String userNo);
    }
    public interface MasterChangeEventListener {
        void onUpdateMasterName(String userNm);
    }

    private static UserListEventListener mUserListCallback;
    private static MasterChangeEventListener mMasterChangeCallback;

    private static UserPresenceListener mUserPresenceListener;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mUserListCallback = (UserListEventListener) RoomActivity.activity;
        mMasterChangeCallback = (MasterChangeEventListener) RoomActivity.activity;
        prefManager = WenotePreferenceManager.getInstance(cordova.getActivity());
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals(ACTION_ADD_ROOM_USER)) {
            addRoomUser(callbackContext, args);
        } else if(action.equals(ACTION_REMOVE_ROOM_USER)) {
            this.removeRoomUser(callbackContext, args);
        } else if(action.equals(ACTION_ADD_OTHER_USER_LIST)) {
            this.addNotAttendee(callbackContext, args);
        } else if(action.equals(ACTION_REMOVE_NOT_ATTENDEE)) {
            removeNotAttendee(callbackContext, args);
        } else if(action.equals(ACTION_UPDATE_MASTER_USER_NM)) {
            updateMasterUserNm(callbackContext, args);
        } else if(action.equals(ACTION_CHANGE_MASTER)) {
            this.changeMaster(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_ADD_CLASS_USER_LIST)) {
            this.addClassUserList(callbackContext, args);
        } else if(action.equals(ACTION_REMOVE_CLASS_USER_LIST)) {
            this.removeClassUserList(callbackContext, args);
        } else if(action.equals(ACTION_GET_USER_INFO)) {
            this.getUserInfo(callbackContext, args);
        } else if (action.equals(ACTION_DEPORT_USER)) {
            this.deportUser(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_INIT_GUEST_INFO)) {
            this.initGuestInfo(callbackContext, args);
        }
        return true;
    }


    /**
     * 참여자 리스트에 추가 (newuser)
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addRoomUser(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(TAG, "addRoomUser");
        mUserListCallback.addRoomUserListHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 참여자 리스트에서 삭제 (leaveuser)
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void removeRoomUser(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        int len = arr.length();
        for(int i=0; i<len; i++) {
            JSONObject obj = arr.getJSONObject(i);
            mUserListCallback.removeUserListHandler(obj);
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 미참여자 리스트에 추가
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addNotAttendee(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(TAG, "addAttendList");
        mUserListCallback.addOtherUserListHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 미참여자 리스트에서 삭제
     * @param callbackContext
     * @param args
     * @throws JSONException
     */
    private void removeNotAttendee(CallbackContext callbackContext, JSONArray args) throws JSONException {
        String userNo = args.getString(0);
        mUserListCallback.onRemoveNotAttendee(userNo);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void updateMasterUserNm(CallbackContext callbackContext, JSONArray args) throws JSONException {
        String userNm = args.getString(0);
        mMasterChangeCallback.onUpdateMasterName(userNm);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));

    }


    /**
     * 진행자 변경
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void changeMaster(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(TAG, "changeMaster : " + obj.toString());
        mUserListCallback.changeMasterHandler(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 수업 참여자 추가
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addClassUserList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(TAG, "addClassUserList");
        mUserListCallback.addClassUserListHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }




    /**
     * 수업 참여자 삭제
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void removeClassUserList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(TAG, "removeClassUserList");
        int len = arr.length();
        for(int i=0; i<len; i++) {
            JSONObject obj = arr.getJSONObject(i);
            mUserListCallback.removeClassUserListHandler(obj);
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * Native에서 쿠키값 복호화하여 추출한 유저정보를 스크립트로 넘겨주기..
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void getUserInfo(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        JSONObject userInfo = RoomActivity.activity.getUserInfo();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, userInfo));
    }

    /**
     * 참여자 강제퇴장
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void deportUser(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        RoomActivity.activity.openDeportUserDialog(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void initGuestInfo(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        JSONObject obj = arr.getJSONObject(0);
        String guestNm = obj.getString("guestnm");
        String guestId = obj.getString("guestid");
        String guestNo = obj.getString("guestno");

        boolean guestFlag  = obj.getBoolean("guestflag");

        RoomActivity.activity.setUserNm(guestNm);
        RoomActivity.activity.setUserId(guestId);
        RoomActivity.activity.setUserNo(guestNo);
        RoomActivity.activity.setGuestFlag(guestFlag);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }
}
