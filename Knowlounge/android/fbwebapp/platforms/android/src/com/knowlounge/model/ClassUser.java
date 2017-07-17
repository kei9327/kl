package com.knowlounge.model;

import java.io.Serializable;

/**
 * Created by Minsu on 2016-05-06.
 */
public class ClassUser implements Serializable {

    private String userId;
    private String userNm;
    private String userNo;
    private String userType;
    private String userRoomId;
    private String userRoomSeqNo;
    private String email;
    private String snsType;
    private String creator;
    private String master;
    private String thumbnail;
    private int iconRes;



    private boolean isExpanded;

    public ClassUser(String userId, String userNm, String userNo, String creator, String master, String thumbnail, int iconRes) {
        this.userId = userId;
        this.userNm = userNm;
        this.userNo = userNo;
        this.creator = creator;
        this.master = master;
        this.thumbnail = thumbnail;
        this.iconRes = iconRes;
        this.isExpanded = false;
    }

    public ClassUser(String userId, String userNm, String userNo, String userType, String thumbnail, String userRoomId, String userRoomSeqNo) {
        this.userId = userId;
        this.userNm = userNm;
        this.userNo = userNo;
        this.userType = userType;
        this.thumbnail = thumbnail;
        this.userRoomId = userRoomId;
        this.userRoomSeqNo = userRoomSeqNo;
        this.isExpanded = false;
    }

    public String getUserId() {
        return this.userId;
    }
    public String getUserNm() {
        return this.userNm;
    }

    public String getUserNo() {
        return this.userNo;
    }

    public String getUserType() {
        return this.userType;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public String getCreator() {
        return this.creator;
    }

    public String getMaster() {
        return this.master;
    }

    public String getSnsType() {
        return this.snsType;
    }

    public String getUserRoomId() {
        return this.userRoomId;
    }

    public String getUserRoomSeqNo() {
        return this.userRoomSeqNo;
    }

    public void setMaster(String masterFlag) {
        this.master = masterFlag;
    }

    public void setSnsType(String snsType) {
        this.snsType = snsType;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean getIsExpanded() {
        return this.isExpanded;
    }

}
