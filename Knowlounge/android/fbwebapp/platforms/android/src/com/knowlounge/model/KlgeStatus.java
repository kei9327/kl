package com.knowlounge.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class KlgeStatus {

    @SerializedName("rtc")
    @Expose
    private Boolean rtc;
    @SerializedName("video")
    @Expose
    private String video;
    @SerializedName("audio")
    @Expose
    private String audio;

    public Boolean getRtc() {
        return rtc;
    }

    public void setRtc(Boolean rtc) {
        this.rtc = rtc;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

}
