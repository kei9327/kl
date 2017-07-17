package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-14.
 */
public class Chat {

    private String type;  // 0-normal, 1-whisper
    private String mode;  // 0-send, 1-receive
    private String sender;
    private String receiver;
    private String userNm;
    private String thumbnail;
    private String content;
    private String cdatetime;

    public Chat(String type, String mode, String sender, String receiver, String userNm, String thumbnail, String content, String cdatetime) {
        this.type = type;
        this.mode = mode;
        this.sender = sender;
        this.receiver = receiver;
        this.userNm = userNm;
        this.thumbnail = thumbnail;
        this.content = content;
        this.cdatetime = cdatetime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCdatetime(String cdatetime) {
        this.cdatetime = cdatetime;
    }


    public String getType() {
        return type;
    }

    public String getMode() {
        return mode;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getUserNm() {
        return userNm;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getContent() {
        return content;
    }

    public String getCdatetime() {
        return cdatetime;
    }
}
