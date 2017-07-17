package com.knowlounge.model;

/**
 * Created by Minsu on 2016-04-05.
 */
public class OtherUser {

    private String userNo;
    private String userId;
    private String userNm;
    private String userType;
    private String email;
    private String thumbnail;

    public OtherUser(String userNo, String userId, String userNm, String userType, String email, String thumbnail) {

        this.userNo = userNo;
        this.userId = userId;
        this.userNm = userNm;
        this.userType = userType;
        this.email = email;
        this.thumbnail = thumbnail;
    }

    public String getUserNo() {
        return userNo;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserNm() {
        return userNm;
    }

    public String getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public String getThumbnail() {
        return thumbnail;
    }

}
