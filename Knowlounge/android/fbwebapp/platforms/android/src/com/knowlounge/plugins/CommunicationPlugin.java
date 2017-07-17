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
 * Created by Minsu on 2016-07-22.
 */
public class CommunicationPlugin extends CordovaPlugin {

    private CallbackContext callbackContext = null;

    private final String TAG = "ChatPlugin";

    private final String ACTION_ADD_CHAT_DATA = "addChatData";
    private final String ACTION_ADD_CLASS_CHAT_DATA = "addClassChatData";

    private final String ACTION_ADD_CHAT_USER_LIST = "addChatUserList";
    private final String ACTION_REMOVE_CHAT_USER_LIST = "removeChatUserList";

    private final String ACTION_ADD_CLASS_CHAT_USER_LIST = "addClassChatUserList";
    private final String ACTION_REMOVE_CLASS_CHAT_USER_LIST = "removeClassChatUserList";

    private final String ACTION_INIT_COMMENT_LIST = "initCommentList";
    private final String ACTION_ADD_COMMENT_LIST = "addCommentList";
    private final String ACTION_REMOVE_COMMENT_LIST = "removeCommentList";

    public interface ChattingEventListener{
        void addChatUserListHandler(JSONArray arr, String myUserNo);
        void removeChatUserListHandler(JSONArray arr);

        void addClassChatUserListHandler(JSONArray arr, String myUserNo);
        void removeClassChatUserListHandler(JSONArray arr);

        void addChatDataHandler(JSONArray arr);
        void addClassChatDataHandler(JSONArray arr);
    }
    public interface CommentEventListener{
        void addCommentListHandler(JSONArray arr, boolean isInit);
        void removeCommentListHandler(String commentNo);
    }

    private static ChattingEventListener mChattingCallback;
    private static CommentEventListener mCommentCallback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        //this.activity = (IndexActivity) cordova.getActivity();
        mChattingCallback = (ChattingEventListener) RoomActivity.activity;
        mCommentCallback = (CommentEventListener) RoomActivity.activity;
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_ADD_CHAT_DATA)) {
            this.addChatData(callbackContext, args);
        } else if (action.equals(ACTION_ADD_CLASS_CHAT_DATA)) {
            this.addClassChatData(callbackContext, args);
        } else if(action.equals(ACTION_ADD_CHAT_USER_LIST)) {
            this.addChatUserList(callbackContext, args);
        } else if(action.equals(ACTION_REMOVE_CHAT_USER_LIST)) {
            this.removeChatUserList(callbackContext, args);
        } else if(action.equals(ACTION_ADD_CLASS_CHAT_USER_LIST)) {
            this.addClassChatUserList(callbackContext, args);
        } else if(action.equals(ACTION_REMOVE_CLASS_CHAT_USER_LIST)) {
            this.removeClassChatUserList(callbackContext, args);
        }  else if(action.equals(ACTION_INIT_COMMENT_LIST)) {
                this.initCommentList(callbackContext, args);
        } else if(action.equals(ACTION_ADD_COMMENT_LIST)) {
            this.addCommentList(callbackContext, args);
        } else if(action.equals(ACTION_REMOVE_COMMENT_LIST)) {
            this.removeCommentList(callbackContext, args.getJSONObject(0));
        }
        return true;
    }


    /**
     * 채팅 데이터 리스트뷰에 추가
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addChatData(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(TAG, "addChatData");
        //RoomActivity.addChatDataHandler(arr);
        mChattingCallback.addChatDataHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 클래스 채팅 데이터 리스트뷰에 추가
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addClassChatData(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(TAG, "addClassChatData");
        //RoomActivity.addChatDataHandler(arr);
        //ChattingFragment.getInstance().addChatData(arr);
        mChattingCallback.addClassChatDataHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 댓글(코멘트) 초기데이터 설정
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void initCommentList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        mCommentCallback.addCommentListHandler(arr, true);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 댓글(코멘트) 불러오기
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addCommentList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(this.getClass().getSimpleName(), "addCommentList");

        mCommentCallback.addCommentListHandler(arr, false);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 댓글(코멘트) 삭제하기
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void removeCommentList(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(this.getClass().getSimpleName(), "removeCommentList");
        String commentNo = obj.has("commentno") ? obj.getString("commentno") : "";
//        RoomActivity.removeCommentListHandler(commentNo);
        mCommentCallback.removeCommentListHandler(commentNo);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 채팅 유저를 리스트에 등록
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void addChatUserList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(getClass().getSimpleName(), "addChatUserList");

        mChattingCallback.addChatUserListHandler(arr, RoomActivity.activity.getUserNo());
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    /**
     *
     * @param callbackContext
     * @param arr
     * @throws JSONException
     */
    private void removeChatUserList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(getClass().getSimpleName(), "removeChatUserList");
        //RoomActivity.addChatUserListHandler(arr);
        mChattingCallback.removeChatUserListHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void addClassChatUserList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(getClass().getSimpleName(), "addClassChatUserList");

        mChattingCallback.addClassChatUserListHandler(arr, RoomActivity.activity.getUserNo());
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void removeClassChatUserList(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        Log.d(getClass().getSimpleName(), "removeClassChatUserList");
        //RoomActivity.addChatUserListHandler(arr);
        mChattingCallback.removeClassChatUserListHandler(arr);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }
}
