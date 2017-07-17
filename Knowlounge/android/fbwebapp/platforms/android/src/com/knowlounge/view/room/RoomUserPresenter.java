package com.knowlounge.view.room;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.base.BasePresenter;
import com.knowlounge.dagger.component.MultiVideoChatFragmentComponent;
import com.knowlounge.dagger.component.RoomActivityComponent;
import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.model.ClassUser;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.model.RoomUser;
import com.knowlounge.model.RoomUsers;
import com.knowlounge.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

/**
 * Created by Mansu on 2017-03-23.
 */

@PerActivity
public class RoomUserPresenter extends BasePresenter {

    private final static String TAG = RoomUserPresenter.class.getSimpleName();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private RoomUsers mRoomUsers;

    private RoomActivityComponent mComponent;

    private Set<RoomUserEvent> mRoomUserEvents = new HashSet<>();

    private boolean isMasterExist = false;

    @Inject
    public RoomUserPresenter(RoomActivityComponent component) {
        Log.d(TAG, "<RoomUserPresenter> RoomUserPresenter initialize..");

        mRoomUsers = new RoomUsers.Builder().build();
        mComponent = component;
    }

//    public void initialize(RoomActivityComponent component) {
//        mRoomUsers = new RoomUsers.Builder().build();
//        mComponent = component;
//    }


    void onAddUser(JSONObject user) {
        Log.d(TAG, "<onAddUser / ZICO> user : " + user.toString());
        try {
            RoomUser newUser = parseJsonToUser(user);
            if (newUser.isMaster())
                isMasterExist = true;
            mRoomUsers.addUser(newUser);
            notifyOnEnterUser(newUser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


//    void onAddUser(int index, JSONObject user) {
//        Log.d(TAG, "<onAddUser> user : " + user.toString());
//        try {
//            RoomUser newUser = parseJsonToUser(user);
//            mRoomUsers.addUser(index, newUser);
//            notifyOnEnterUser(newUser);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    void onRemoveUser(RoomUser removeUser) {
        Log.d(TAG, "<onRemoveUser> user : " + removeUser.getUserNm());
        mRoomUsers.removeUser(removeUser);
        notifyOnExitUser(removeUser);
    }

    void onRemoveUser(String userNo) {
        RoomUser removeUser = mRoomUsers.getUserMap().get(userNo);
        if (removeUser != null) {
            Log.d(TAG, "<onRemoveUser> user : " + removeUser.getUserNm());
            mRoomUsers.removeUser(removeUser);
            if (removeUser.isMaster())
                isMasterExist = false;
        }
    }

    void onChangeMaster(User newMasterUser) {
    }

    RoomUsers getRoomUsers() {
        return mRoomUsers;
    }

    public ConcurrentHashMap<String, RoomUser> getRoomUserList() {
        return mRoomUsers.getUserMap();
    }

    public RoomUser getRoomUser(String userNo) {
        return mRoomUsers.getUserMap().get(userNo);
    }

    public boolean isMasterExist() {
        return isMasterExist;
    }

    public void addRoomUserEventsListener(RoomUserEvent events) {
        Log.d(TAG, "<addRoomUserEventsListener> RoomUserEvents set complete.");
        mRoomUserEvents.add(events);
    }

    public String getCurrentMasterId() {
        String result = "";
        for (Map.Entry<String, RoomUser> entry : mRoomUsers.getUserMap().entrySet()) {
            if (entry.getValue().isMaster())
                result = entry.getValue().getUserId();
        }
        return result;
    }

    public void setMasterUser(String prevMaster, String newMaster) {
        for (Map.Entry<String, RoomUser> entry : mRoomUsers.getUserMap().entrySet()) {
            RoomUser roomUser = entry.getValue();
            if (prevMaster.equals(roomUser.getUserId()))
                entry.getValue().setMaster(false);
            if (newMaster.equals(roomUser.getUserId()))
                entry.getValue().setMaster(true);
        }
    }

    public void removeRoomUserEventsListener(RoomUserEvent events) {
        mRoomUserEvents.remove(events);
    }

    private void notifyOnEnterUser(RoomUser newUser) {
        for (RoomUserEvent event : mRoomUserEvents) {
            event.onEnterUser(newUser);
        }
    }

    private void notifyOnExitUser(RoomUser removeUser) {
        for (RoomUserEvent event : mRoomUserEvents) {
            event.onExitUser(removeUser);
        }
    }

    private RoomUser parseJsonToUser(JSONObject userObj) throws JSONException {

        int videoIndex;
        //int videoIndex = userObj.getInt("video_index");
        String userNo = userObj.getString("userno");
        String userId = userObj.getString("userid");
        String userNm = userObj.getString("usernm");
        String userType = (userObj.has("usertype") || !userObj.isNull("usertype")) ? userObj.getString("usertype") : "0";
        String thumbnail = userObj.getString("thumbnail");

        String userScope = "class";

        String connectedRoomId = userObj.has("connected_roomid") ? userObj.getString("connected_roomid") : "";
        String connectedRoomTitle = userObj.has("connected_roomtitle") ? userObj.getString("connected_roomtitle") : "";
        String connectedRoomSeparate = userObj.has("connected_roomseparate") ? userObj.getString("connected_roomseparate") : "";

        boolean isSeparateRoom = "1".equals(connectedRoomSeparate) ? true : false;

        int creator = userObj.has("creator") ? userObj.getInt("creator") : 0;
        boolean isCreator = creator == 1 ? true : false;

        boolean isMaster  = userObj.has("master") ? userObj.getBoolean("master") : false;
        boolean isGuest   = userObj.has("guest") ? userObj.getBoolean("guest") : false;

        String userRoomid = (userObj.has("roomid") || !userObj.isNull("roomid")) ? userObj.getString("roomid") : "";
        String userRoomSeqNo = (userObj.has("seqno") || !userObj.isNull("seqno")) ? userObj.getString("seqno") : "";

        //guestFlag = userObj.has("isguest") ? userObj.getString("isguest") : userNo.equals(userId) ? "1" : "0";
        if (isGuest) {
            thumbnail = "";
            userType = "0";
        }

        if (isCreator) {
            videoIndex = 0;
        } else {
            videoIndex = getRoomUserList().size();
        }

        RoomUser newUser = new RoomUser(videoIndex, userId, userNo, userNm, userType, thumbnail, userScope, userRoomid, userRoomSeqNo, isCreator, isMaster, isGuest, connectedRoomId, connectedRoomTitle, isSeparateRoom);
        //ClassUser newUser = new ClassUser(userId, userNm, userNo, userType, thumbnail, userRoomid, userRoomSeqNo);
        return newUser;
    }


    public interface RoomUserEvent {
        void onEnterUser(RoomUser newUser);
        void onExitUser(RoomUser removeUser);
    }

}
