package com.knowlounge.model;

import java.io.Serializable;

/**
 * Created by Minsu on 2016-01-21.
 */
public class User implements Serializable {

    private String userId;
    private String userNm;
    private String userNo;
    private String userType;

    private String snsType;

    private String creator;
    private String master;
    private String guest;
    private String thumbnail;

    private boolean chatFlag;

    private int iconRes;

    public User(String userId, String userNm, String userNo, String userType, String creator, String master, String guest, String thumbnail, int iconRes) {
        this.userId = userId;
        this.userNm = userNm;
        this.userNo = userNo;
        this.userType = userType;
        this.creator = creator;
        this.master = master;
        this.guest = guest;
        this.thumbnail = thumbnail;
        this.iconRes = iconRes;
        this.chatFlag = true;
    }

    public String getUserId() {
        // 유저 아이디
        return this.userId;
    }

    public String getUserNm() {
        // 유저 명
        return this.userNm;
    }

    public String getUserNo() {
        // 유저 번호
        return this.userNo;
    }

    public String getThumbnail() {
        // 유저 썸네일 URL
        return this.thumbnail;
    }

    public String getCreator() {
        // 개설자 여부
        return this.creator;
    }

    public String getMaster() {
        // 진행자 여부
        return this.master;
    }

    public String getGuest() {
        // 게스트 유저 여부
        return this.guest;
    }

    public String getSnsType() {
        // SNS 로그인 타입
        return this.snsType;
    }

    public String getUserType() {
        // 유저 타입 (학생, 선생님)
        return this.userType;
    }

    public void setMaster(String masterFlag) {
        // 진행자 여부
        this.master = masterFlag;
    }

    public void setSnsType(String snsType) {
        // SNS 로그인 타입
        this.snsType = snsType;
    }

    public void setUserNm(String userNm) {
        // 유저명
        this.userNm = userNm;
    }

    public void setChatFlag(boolean chatFlag) {
        // 채팅 플래그
        this.chatFlag = chatFlag;
    }

    public boolean getChatFlag() {
        // 채팅 플래그
        return this.chatFlag;
    }
}
