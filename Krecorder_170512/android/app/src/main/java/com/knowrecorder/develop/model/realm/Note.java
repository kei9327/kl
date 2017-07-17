package com.knowrecorder.develop.model.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by we160303 on 2017-02-03.
 */

public class Note extends RealmObject {
    @PrimaryKey
    private long id;
    private String createDate;
    private String title;
    private float totaltime;
    private boolean isCurrentUsing; //todo 지워도 상관없는 컬럼
    private String info;

    public long getNoteId() {
        return id;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getTitle() {
        return title;
    }

    public float getTotalTime() {
        return totaltime;
    }

    public boolean getIsCurrentUsing() { return isCurrentUsing; }

    public String getInfo() {
        return info;
    }

    public void setNoteId(long noteId) {
        this.id = noteId;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotalTime(float totalTime) {
        this.totaltime = totalTime;
    }

    public void setIsCurrentUsing(boolean isCurrentUsing) { this.isCurrentUsing = isCurrentUsing; }

    public void setInfo(String info) {
        this.info = info;
    }
}
