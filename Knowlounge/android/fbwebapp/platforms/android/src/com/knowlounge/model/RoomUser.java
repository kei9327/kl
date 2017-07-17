package com.knowlounge.model;

import android.util.Log;

/**
 * Created by Mansu on 2017-03-27.
 */

public class RoomUser {

    private int mVideoIndex;
    private String mUserId;
    private String mUserNo;
    private String mUserNm;
    private String mUserType;
    private String mThumbnail;

    private String mUserScope;

    private String mUserRoomId;
    private String mUserRoomCode;

    private String connectedRoomId;
    private String connectedRoomTitle;
    private boolean connectedRoomSeparate;

    private boolean isCreator;
    private boolean isMaster;
    private boolean isGuest;

    private boolean isExpanded;


    public RoomUser(int videoIndex, String userId, String userNo, String userNm, String userType, String thumbnail, String userScope, String userRoomId, String userRoomCode,
                    boolean isCreator, boolean isMaster, boolean isGuest, String connectedRoomId, String connectedRoomTitle, boolean isSeparateRoom) {
        Log.d("RoomUser", "<constructor> videoIndex : " + videoIndex + ", userNm : " + userNm + ", isCreator : " + isCreator + ", isMaster : " + isMaster);
        mVideoIndex = videoIndex;
        mUserId = userId;
        mUserNo = userNo;
        mUserNm = userNm;
        mUserType = userType;
        mThumbnail = thumbnail;
        mUserScope = userScope;
        mUserRoomId = userRoomId;
        mUserRoomCode = userRoomCode;
        this.isCreator = isCreator;
        this.isMaster = isMaster;
        this.isGuest = isGuest;
        this.connectedRoomId = connectedRoomId;
        this.connectedRoomTitle = connectedRoomTitle;
        this.connectedRoomSeparate = isSeparateRoom;
        isExpanded = false;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getUserNo() {
        return mUserNo;
    }

    public void setUserNo(String mUserNo) {
        this.mUserNo = mUserNo;
    }

    public String getUserNm() {
        return mUserNm;
    }

    public void setUserNm(String mUserNm) {
        this.mUserNm = mUserNm;
    }

    public String getUserType() {
        return mUserType;
    }

    public void setUserType(String mUserType) {
        this.mUserType = mUserType;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String mThumbnail) {
        this.mThumbnail = mThumbnail;
    }

    public String getUserRoomId() {
        return mUserRoomId;
    }

    public void setUserRoomId(String mUserRoomId) {
        this.mUserRoomId = mUserRoomId;
    }

    public String getUserRoomCode() {
        return mUserRoomCode;
    }

    public void setUserRoomCode(String mUserRoomCode) {
        this.mUserRoomCode = mUserRoomCode;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public void setGuest(boolean guest) {
        isGuest = guest;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean getIsExpanded() {
        return this.isExpanded;
    }

    public String getConnectedRoomId() {
        return connectedRoomId;
    }
    public String getConnectedRoomTitle() {
        return connectedRoomTitle;
    }
    public boolean isConnectedRoomSeparate() {
        return connectedRoomSeparate;
    }
}
