package com.knowrecorder.develop.model.realm;

import com.knowrecorder.develop.model.realmHoler.NotesHolder;

import io.realm.RealmObject;

/**
 * Created by we160303 on 2017-03-29.
 */

public class Notes extends RealmObject {

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

    public NotesHolder clone(){
        NotesHolder notesCopy = new NotesHolder();
        notesCopy.setNoteName(this.noteName);
        notesCopy.setTitle(this.title);
        notesCopy.setCreateDate(this.createDate);
        notesCopy.setTotaltime(this.totaltime);
        return notesCopy;
    }
}
