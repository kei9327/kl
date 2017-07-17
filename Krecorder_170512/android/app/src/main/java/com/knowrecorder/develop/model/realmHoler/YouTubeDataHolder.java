package com.knowrecorder.develop.model.realmHoler;

import java.util.Date;


/**
 * Created by we160303 on 2017-02-03.
 */

public class YouTubeDataHolder {

    private String youtubeid;
    private Date createDate;
    private String title;
    private long totalTime;

    public String getYoutubeid() {
        return youtubeid;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getTitle() {
        return title;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setYoutubeid(String youtubeid) {
        this.youtubeid = youtubeid;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
}
