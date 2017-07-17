package com.knowlounge.model;

import java.io.Serializable;

/**
 * Created by Minsu on 2016-01-12.
 */
public class CommentItem implements Serializable {

    private String commentNo;
    private String userId;
    private String userNm;
    private String userNo;
    private String thumbnail;

    private String regDatetime;
    private String contents;


    public CommentItem(String commentNo, String userId, String userNm, String userNo, String thumbnail, String regDatetime, String contents) {
        this.commentNo = commentNo;
        this.userId = userId;
        this.userNm = userNm;
        this.userNo = userNo;
        this.thumbnail = thumbnail;
        this.regDatetime = regDatetime;
        this.contents = contents;
    }

    public String getCommentNo() {
        return this.commentNo;
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

    public String getThumbnail() {
        return this.thumbnail;
    }

    public String getRegDatetime() {
        return this.regDatetime;
    }

    public String getContents() {
        return this.contents;
    }
}
