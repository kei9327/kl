package com.knowrecorder.develop.model;

import android.content.Context;

import com.knowrecorder.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ChangHa on 2017-02-15.
 */

public class NewNote {
    private long id ;
    private String noteName;
    private String createDate;
    private String title;

    public NewNote(Context context) {
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        this.id = 1;
        noteName = Long.toString(currentTime);
        createDate = sdf.format(date);
        title = context.getResources().getString(R.string.export_default_text);
    }

    public NewNote(long id, String noteName, String createDate, String title){
        this.id = id;
        this.noteName = noteName;
        this.createDate = createDate;
        this.title = title;
    }
    public long getId() { return id ; }

    public String getNoteName() {
        return noteName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getTitle() {
        return title;
    }
}
