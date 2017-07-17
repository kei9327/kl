package com.knowlounge.plugins;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.fragment.ConfigFragment;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.MultiPageEvent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Minsu on 2016-04-06.
 */
public class RoomPlugin extends CordovaPlugin {

    private CallbackContext callbackContext = null;
    //private IndexActivity activity = null;


    private static String ACTION_UPDATE_ROOM_BG = "updateRoomBg";
    private static String ACTION_UPDATE_ROOM_AUTH = "updateRoomAuth";

    private static String ACTION_REMOVE_ROOM_USER_LIMIT = "removeRoomUserLimit";

    private static String ACTION_CALL_ALL_STUDENT = "callAllStudent";

    private static String ACTION_SET_REDRAW_HISTORY_MODE = "setRedrawHistoryMode";

    private final String ACTION_START_ROOM_LOADING = "startRoomLoading";
    private final String ACTION_FINISH_ROOM_LOADING = "finishRoomLoading";


    private final String ACTION_MOVE_ROOM = "moveRoom";
    private final String ACTION_EXIT_ROOM = "exitRoom";

    private final String ACTION_SET_ROOM_AUTH = "setRoomAuth";

    private final String ACTION_SET_ZOOM_VAL = "setZoomVal";

    private final String ACTION_SWITCH_ACTIVITY = "enterRoom";

    private final String ACTION_UPDATE_ROOM_TITLE = "updateRoomTitle";

    private final String ACTION_FORCE_FINISH_ACTITY = "forceFinishActivity";

    private final String ACTION_NATIVE_UI_INIT = "initializeRoom";

    private final String ACTION_SAVE_CANVAS = "saveCanvas";

    private final String ACTION_SHOW_UPLOAD_PROGRESS = "showUploadProgress";

    private final String ACTION_RELOAD_ROOM_THUMB = "reloadRoomThumb";

    public interface RoomEventListener {

    }

    private static RoomEventListener roomListener;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
//        roomListener = (RoomEventListener) RoomActivity.activity;
        //this.activity = (IndexActivity) cordova.getActivity();
    }


    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {


        if (action.equals(ACTION_UPDATE_ROOM_BG)) {
            this.updateRoomBg(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_UPDATE_ROOM_AUTH)) {
            this.updateRoomAuth(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_REMOVE_ROOM_USER_LIMIT)) {
            this.removeRoomUserLimit(callbackContext, args);
        } else if (action.equals(ACTION_CALL_ALL_STUDENT)) {
            this.callAllStudent(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_SET_REDRAW_HISTORY_MODE)) {
            this.setRedrawHistoryMode(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_START_ROOM_LOADING)) {
            this.startRoomLoading(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_FINISH_ROOM_LOADING)) {
            this.finishRoomLoading(callbackContext, args);
        }  else if(action.equals(ACTION_EXIT_ROOM)) {
            this.exitRoom(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_MOVE_ROOM)) {
            //this.moveRoom(callbackContext, args);
            this.moveRoom(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_SET_ROOM_AUTH)) {
            this.setRoomAuth(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_SET_ZOOM_VAL)) {
            this.setLastZoomVal(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_SWITCH_ACTIVITY)){
            this.enterMainActivity(callbackContext, args);
        } else if(action.equals(ACTION_UPDATE_ROOM_TITLE)) {
            this.updateRoomTitle(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_FORCE_FINISH_ACTITY)) {
            this.forceFinishActivity(callbackContext, args);
        } else if(action.equals(ACTION_NATIVE_UI_INIT)) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        initializeRoom(callbackContext, args.getJSONObject(0));
                    } catch (JSONException e) {

                    }
                }
            });
            //this.initializeRoom(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_SAVE_CANVAS)) {
            this.saveCanvas(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_SHOW_UPLOAD_PROGRESS)) {
            this.showUploadProgress(callbackContext, args.getJSONObject(0));
        } else if(action.equals(ACTION_RELOAD_ROOM_THUMB)) {
            this.reloadRoomThumb(callbackContext, args);
        }

        return true;
    }

    private void updateRoomBg(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        RoomActivity.activity.setBgInfo(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void updateRoomAuth(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        RoomActivity.activity.setAuthInfo(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void removeRoomUserLimit(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        // TODO : 참여제한 해제 UI 처리 메서드를 호출..
        RoomActivity.activity.updateUserLimitCnt(30);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void callAllStudent(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        String roomCode = obj.getString("roomcode");
        String teacherNm = obj.getString("usernm");
        RoomActivity.activity.showTeacherCallDialog(roomCode, teacherNm);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void setRedrawHistoryMode(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        boolean redrawEnable = obj.getBoolean("redraw_enable");
        RoomActivity.activity.setRedrawHistoryMode(redrawEnable);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void startRoomLoading(CallbackContext callbackContext, @Nullable JSONObject obj) throws JSONException {
        String type = obj.getString("type");
        RoomActivity.activity.startRoomLoading(type);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void finishRoomLoading(CallbackContext callbackContext, @Nullable JSONArray arr) throws JSONException {
        RoomActivity.activity.finishRoomLoading();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }




    /**
     * 룸 퇴장하기
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void exitRoom(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(this.getClass().getSimpleName(), "finishRoom");
        RoomActivity.activity.finishActivity(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 룸 이동하기
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void moveRoom(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(this.getClass().getSimpleName(), "finishRoom");
//        RoomActivity.activity.finishActivity(new JSONObject());
        ActivityCompat.finishAfterTransition(RoomActivity.activity);
        final String roomUrl = arr.getString(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Context ctx = cordova.getActivity().getApplicationContext();
                Intent mainIntent = new Intent(ctx, RoomActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.putExtra("roomurl", roomUrl);
                cordova.getActivity().startActivity(mainIntent);
            }
        }, 1500);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void moveRoom(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        final String roomCode = obj.getString("code");
        RoomActivity.activity.moveRoom(roomCode, null);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    /**
     * 룸 권한정보 업데이트
     * @param callbackContext
     * @param obj
     */
    private void setRoomAuth(CallbackContext callbackContext, JSONObject obj) {
        try {
            Log.d("RoomPlugin", "obj : " + obj.toString());
            if(ConfigFragment._instance == null) {
                RoomActivity.activity.setAuthInfo(obj);
            } else {
                ConfigFragment._instance.applyRoomConfigHandler(obj);
            }
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 마지막으로 설정한 줌 값 세팅
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void setLastZoomVal(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        int zoomVal = obj.getInt("zoom");
        RoomActivity.activity.setZoomVal(zoomVal);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 룸 입장
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void enterMainActivity(CallbackContext callbackContext, JSONArray arr) throws JSONException {

        String roomUrl = arr.getString(0);

        Context ctx = cordova.getActivity().getApplicationContext();
        Intent mainIntent = new Intent(ctx, RoomActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.putExtra("roomurl", roomUrl);
        cordova.getActivity().startActivity(mainIntent);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 룸 타이틀 수정
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void updateRoomTitle(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        String roomTitle = obj.getString("title");
        RoomActivity.activity.setRoomTitleHandler(roomTitle);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void forceFinishActivity(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        RoomActivity.activity.forcefinishActivity();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 룸 정보 초기화 - canvas_webapp.js의 getRoomInfo 시점에서 호출됨.
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void initializeRoom(CallbackContext callbackContext, JSONObject obj) throws JSONException {

        RoomActivity.activity.initializeRoom(obj);

//        boolean creatorFlag  = obj.getBoolean("creatorflag");
//        boolean masterFlag  = obj.getBoolean("masterflag");
//        boolean guestFlag  = obj.getBoolean("guestflag");
//        boolean isBookmark = obj.getBoolean("bookmark");
//
//        JSONObject authInfo = obj.getJSONObject("auth");
//        JSONObject bgInfo = obj.getJSONObject("bg");
//
//        String roomId = obj.getString("roomid");
//        String roomTitle = obj.getString("roomtitle");
//        String userId = obj.getString("userid");
//        String userNo = obj.getString("userno");
//        String userNm = obj.getString("usernm");
//        String snsType = obj.getString("snstype");
//        String masterId = obj.getString("masterid");
//
//        // 선생님 수업 정보
//        String masterRoomSeqNo = obj.getString("masterseqno");
//        String teacherUserNo = obj.getString("parentcreatorno");
//
//        int userLimitCnt = obj.getInt("userlimitcnt");
//
//        RoomActivity.activity.setBookmarkFlag(isBookmark);
//        RoomActivity.activity.setRoomTitleHandler(roomTitle);
//        RoomActivity.activity.setRoomId(roomId);
//        RoomActivity.activity.setCreatorFlag(creatorFlag);
//        RoomActivity.activity.initMasterFlag(masterFlag, masterId);
//        RoomActivity.activity.setMasterRoomInfo(teacherUserNo, masterRoomSeqNo);
//        RoomActivity.activity.setGuestFlag(guestFlag);
//        RoomActivity.activity.setUserId(userId);
//        RoomActivity.activity.setUserNo(userNo);
//        RoomActivity.activity.setUserNm(userNm);
//        RoomActivity.activity.setAuthInfo(authInfo);
//        RoomActivity.activity.setBgInfo(bgInfo);
//        RoomActivity.activity.setSnsType(snsType);
//        RoomActivity.activity.setUserLimitCnt(userLimitCnt);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void saveCanvas(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        String imgUrlStr = obj.getString("url");
        RoomActivity.activity.saveCanvasScreen(imgUrlStr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void showUploadProgress(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        int percent = obj.getInt("percent");
        RoomActivity.activity.uploadFileProgress(percent);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void reloadRoomThumb(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        // TODO : 프리뷰 썸네일과 페이지 썸네일 refresh..
        Log.d("RoomPlugin","reloadRoomThumb");
        EventBus.get().post(new MultiPageEvent(MultiPageEvent.RELOAD_PAGE, ""));
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


}
