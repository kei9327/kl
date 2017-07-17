package com.knowlounge.model;

import java.io.Serializable;
import java.io.StringReader;

/**
 * Created by Minsu on 2016-01-14.
 */
public class ChatUser implements Serializable {

    private String userId;
    private String userNm;
    private String userNo;
    private String thumbnail;
    private boolean selected;

    public ChatUser(String userId, String userNm, String userNo, String thumbnail, boolean toogle) {
        this.userId = userId;
        this.userNm = userNm;
        this.userNo = userNo;
        this.thumbnail = thumbnail;
        this.selected = toogle;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setSelected(Boolean toggle) { this.selected = toggle;}

    public String getUserId() {
        return this.userId;
    }

    public String getUserNm() {
        return this.userNm;
    }

    public String getUserNo() {
        return this.userNo;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public Boolean isSelected() { return this.selected; }
}
