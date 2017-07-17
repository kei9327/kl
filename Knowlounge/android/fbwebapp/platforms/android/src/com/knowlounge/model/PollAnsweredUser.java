package com.knowlounge.model;

/**
 * Created by Mansu on 2016-12-15.
 */

public class PollAnsweredUser {
    private String userNo;
    private String userId;
    private String userNm;
    private String userThumb;
    private boolean isAnswered;

    public PollAnsweredUser(String userNo, String userId, String userNm, String userThumb, boolean isAnswered) {
        this.userNo = userNo;
        this.userId = userId;
        this.userNm = userNm;
        this.userThumb = userThumb;
        this.isAnswered = isAnswered;
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

    public String getUserThumb() {
        return userThumb;
    }

    public boolean isAnswered() {
        return isAnswered;
    }


    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }


    public void setUserThumb(String thumb) {
        this.userThumb = thumb;
    }

    public void setAnswered(boolean isAnswered) {
        this.isAnswered = isAnswered;
    }
}
