package com.knowrecorder.develop.model.realmHoler;

import java.util.Date;

/**
 * Created by we160303 on 2017-02-03.
 */

public class NoteHolder {
    private int id;
    private Date createDate;
    private String title;
    private long totaltime;
    private String info;

    public int getNoteId() {
        return id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getTitle() {
        return title;
    }

    public long getTotalTime() {
        return totaltime;
    }

    public String getInfo() {
        return info;
    }

    public void setNoteId(int noteId) {
        this.id = noteId;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotalTime(long totalTime) {
        this.totaltime = totalTime;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
