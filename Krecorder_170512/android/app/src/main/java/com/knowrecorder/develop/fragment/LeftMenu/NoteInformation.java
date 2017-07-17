package com.knowrecorder.develop.fragment.LeftMenu;

/**
 * Created by we160303 on 2017-03-24.
 */

public class NoteInformation {
    private String noteName;
    private String thumbNailPath;
    private String title;
    private String createDate;
    private long runTime;

    public NoteInformation(String noteName, String thumbNailPath, String title, String createDate, long runTime) {
        this.noteName = noteName;
        this.thumbNailPath = thumbNailPath;
        this.title = title;
        this.createDate = createDate;
        this.runTime = runTime;
    }

    public String getNoteName() {
        return noteName;
    }

    public String getThumbNailPath() {
        return thumbNailPath;
    }

    public String getTitle() {
        return title;
    }

    public String getCreateDate() {
        return createDate;
    }

    public long getRunTime() {
        return runTime;
    }
}
