package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-14.
 */
public class PollResultDetailUser {


    private String thumbnail;
    private String user_name;


    public PollResultDetailUser(String thumbnail, String user_name) {
        this.thumbnail = thumbnail;
        this.user_name = user_name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUser_name() {
        return user_name;
    }
}
