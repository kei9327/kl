package com.knowrecorder.phone.rxevent;

/**
 * Created by we160303 on 2016-12-08.
 */

public class PlayVideo {
    private int videoId;
    private String videoTitle;

    public PlayVideo(int videoId, String videoTitle) {
        this.videoId = videoId;
        this.videoTitle = videoTitle;
    }

    public int getVideoId(){return this.videoId ;}
    public String getVideoTitle(){return this.videoTitle ;}
}
