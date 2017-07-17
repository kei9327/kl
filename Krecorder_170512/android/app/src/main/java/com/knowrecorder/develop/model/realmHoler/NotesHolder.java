package com.knowrecorder.develop.model.realmHoler;

/**
 * Created by we160303 on 2017-03-29.
 */

public class NotesHolder {
    private String noteName ;
    private String title;
    private String createDate;
    private float totaltime;

    public String getNoteName() {
        return noteName;
    }

    public String getTitle() {
        return title;
    }

    public String getCreateDate() {
        return createDate;
    }

    public float getTotaltime() {
        return totaltime;
    }


    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setTotaltime(float totaltime) {
        this.totaltime = totaltime;
    }
}
