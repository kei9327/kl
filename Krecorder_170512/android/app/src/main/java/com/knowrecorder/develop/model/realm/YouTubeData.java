package com.knowrecorder.develop.model.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by we160303 on 2017-02-03.
 */

public class YouTubeData extends RealmObject {

    @PrimaryKey
    private String youtubeId;
    private long noteid;
    private String createDate;
    private String title;
    private float totalTime;

    public String getYoutubeId() {
        return youtubeId;
    }

    public long getNoteid() {
        return noteid;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getTitle() {
        return title;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public void setNoteid(long noteid) {
        this.noteid = noteid;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }
}
