package com.knowrecorder.OpenCourse.API.Models;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Playtime {

    @SerializedName("playtime")
    @Expose
    private int playtime;

    /**
     *
     * @return
     * The playtime
     */
    public int getPlaytime() {
        return playtime;
    }

    /**
     *
     * @param playtime
     * The playtime
     */
    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

}