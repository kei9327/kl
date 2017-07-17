package com.knowlounge.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-08.
 */

@SuppressWarnings("WeakerAccess")
public class RoomSpec implements Parcelable {

    private String mHost;
    private String mName;
    private String mPort;
    private String mUserNo;
    private String mUserNm;
    private String mRoomId;
    private String mAccessToken;
    private boolean isParentMaster;
    private boolean isCreator;
    private boolean mEnableVideo;
    private boolean mEnableAudio;
    private boolean mEnableVolume;
    private boolean isClassMode;
    private boolean isWhiteboardMode;
    private boolean isSeparate;
    private boolean isAllowCaller;

    public RoomSpec(RoomSpec.Builder builder) {
        mHost = builder.mHost;
        mName = builder.mName;
        mPort = builder.mPort;
        mUserNo = builder.mUserNo;
        mUserNm = builder.mUserNm;
        mRoomId = builder.mRoomId;
        mAccessToken = builder.mAccessToken;
        mEnableVideo = builder.mEnableVideo;
        mEnableAudio = builder.mEnableAudio;
        mEnableVolume = builder.mEnableVolume;
    }

    public String getHost() {
        return mHost;
    }

    public String getName() {
        return mName;
    }

    public String getPort() {
        return mPort;
    }

    public String getUserNo() {
        return mUserNo;
    }

    public String getUserNm() {
        return mUserNm;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public boolean isParentMaster() {
        return isParentMaster;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public boolean isVideoEnabled() {
        return mEnableVideo;
    }

    public boolean isAudioEnabled() {
        return mEnableAudio;
    }

    public boolean isVolumeEnabled() {
        return mEnableVolume;
    }

    public boolean isVideoControlEnable() {
        return isClassMode;
    }

    public boolean isWhiteboardMode() {
        return isWhiteboardMode;
    }

    public boolean isSeparate() {
        return isSeparate;
    }

    public boolean isAllowCaller() {
        return isAllowCaller;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public void setUserNo(String userNo) {
        mUserNo = userNo;
    }

    public void setUserNm(String userNm) {
        mUserNm = userNm;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPort(String port) {
        mPort = port;
    }

    public void setIsParentMaster(boolean isParentMaster) {
        this.isParentMaster = isParentMaster;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
    }

    public void setIsVideoEnable(boolean enableVideo) {
        mEnableVideo = enableVideo;
    }

    public void setIsAudioEnable(boolean enableAudio) {
        mEnableAudio = enableAudio;
    }

    public void setIsVolumeEnable(boolean enableVolume) {
        mEnableVolume = enableVolume;
    }

    public void setIsVideoControlEnable(boolean enableClassMode) {
        isClassMode = enableClassMode;
    }

    public void setIsWhiteboardMode(boolean isWhiteboardMode) {
        this.isWhiteboardMode = isWhiteboardMode;
    }

    public void setIsSeparate(boolean isSeparate) {
        this.isSeparate = isSeparate;
    }

    public void setIsAllowCaller(boolean isAllowCaller) {
        this.isAllowCaller = isAllowCaller;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mHost);
        dest.writeString(mName);
        dest.writeString(mPort);
        dest.writeString(mUserNo);
        dest.writeString(mUserNm);
        dest.writeString(mRoomId);
        dest.writeString(mAccessToken);
        dest.writeBooleanArray(new boolean[]{mEnableVideo, mEnableAudio, mEnableVolume});
    }

    public static final Creator<RoomSpec> CREATOR = new Creator<RoomSpec>() {
        public RoomSpec createFromParcel(Parcel in) {
            return new RoomSpec(in);
        }

        public RoomSpec[] newArray(int size) {
            return new RoomSpec[size];
        }
    };

    private RoomSpec(Parcel in) {
        mHost = in.readString();
        mName = in.readString();
        mPort = in.readString();
        mUserNo = in.readString();
        mUserNm = in.readString();
        mRoomId = in.readString();
        mAccessToken = in.readString();

        // video, audio
        boolean[] enables = new boolean[3];
        in.readBooleanArray(enables);

        mEnableVideo = enables[0];
        mEnableAudio = enables[0];
        mEnableVolume = enables[0];
    }


    public static class Builder {
        private String mHost;
        private String mName;
        private String mPort;
        private String mUserNo;
        private String mUserNm;
        private String mRoomId;
        private String mAccessToken;
        private boolean mEnableVideo;
        private boolean mEnableAudio;
        private boolean mEnableVolume;

        public Builder host(String host) {
            mHost = host;
            return this;
        }

        public Builder name(String name) {
            mName = name;
            return this;
        }

        public Builder port(String port) {
            mPort = port;
            return this;
        }

        public Builder userNo(String userId) {
            mUserNo = userId;
            return this;
        }

        public Builder userNm(String userNm) {
            mUserNm = userNm;
            return this;
        }

        public Builder roomId(String roomId) {
            mRoomId = roomId;
            return this;
        }

        public Builder accessToken(String accessToken) {
            mAccessToken = accessToken;
            return this;
        }

        public Builder enableVideo(boolean enableVideo) {
            mEnableVideo = enableVideo;
            return this;
        }

        public Builder enableAudio(boolean enableAudio) {
            mEnableAudio = enableAudio;
            return this;
        }

        public Builder enableVolume(boolean enableVolume) {
            mEnableVolume = enableVolume;
            return this;
        }

        public RoomSpec build() {
            return new RoomSpec(this);
        }
    }
}
