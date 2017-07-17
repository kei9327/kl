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
import org.webrtc.SessionDescription;

/**
 * Created by Minsu on 2016-02-01.
 */
public class VideoPlugin extends CordovaPlugin {

    private String TAG = "VideoPlugin";


    // ZICO
    private final String ACTION_VIDEO_OPTIONS = "video_options";
    private final String ACTION_VIDEO_GROUP = "video_group";
    private final String ACTION_VIDEO_NOTI = "video_noti";

    private CallbackContext callbackContext = null;


    public interface VideoPluginEvents {
        void onVideoOptionChange(String roomId, boolean videoCtrl, boolean soundOnly);
        void onVideoGroup(String roomId, boolean separate);
        void onVideoNoti(String action, String fromUserNo, String toUserNo);
    }

    private VideoPluginEvents mEvents;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        //this.activity = (IndexActivity) cordova.getActivity();
        mEvents = (VideoPluginEvents) cordova.getActivity();
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equals(ACTION_VIDEO_OPTIONS)) {
            this.videoOptions(args.getJSONObject(0));
        } else if(action.equals(ACTION_VIDEO_GROUP)) {
            this.videoGroup(args.getJSONObject(0));
        } else if(action.equals(ACTION_VIDEO_NOTI)) {
            this.videoNoti(args.getJSONObject(0));
        }
        return true;
    }

    private void videoOptions(JSONObject obj) throws JSONException {
        String roomId = obj.getString("roomid");
        int videoCtrl = obj.getInt("videoctrl");
        int soundOnly = obj.getInt("soundonly");
        boolean isVideoCtrl = videoCtrl == 1 ? true : false;
        boolean isSoundOnly = soundOnly == 1 ? true : false;
        mEvents.onVideoOptionChange(roomId, isVideoCtrl, isSoundOnly);
    }

    private void videoGroup(JSONObject obj) throws JSONException {
        String roomId = obj.getString("roomid");
        int separate = obj.getInt("separate");
        boolean isSeparate = separate == 1 ? true : false;
        mEvents.onVideoGroup(roomId, isSeparate);
    }

    private void videoNoti(JSONObject obj) throws JSONException {
        String action = obj.getString("action");  // connect, disconnect, request
        String fromUserNo = obj.getString("from");
        String toUserNo = obj.getString("to");
        mEvents.onVideoNoti(action, fromUserNo, toUserNo);
    }
}