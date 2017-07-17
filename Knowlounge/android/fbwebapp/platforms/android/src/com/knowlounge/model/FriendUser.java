package com.knowlounge.model;

import java.io.Serializable;

/**
 * Created by we160303 on 2016-04-27.
 */
public class FriendUser implements Serializable {

    public String id;
    public String userNm;
    public String userThumbnail;
    public boolean ischecked;

    public String inviteMessage;
    public String cdatetime;
    public String roomThumbnail;

    public FriendUser(String id, String name, String userThumbnail) {
        this.id = id;
        this.userNm = name;
        this.userThumbnail = userThumbnail;
        this.ischecked = false;

    }

    public String getId() { return this.id; }
    public String getUserNm() { return this.userNm; }
    public String getUserThumbnail() { return this.userThumbnail; }

    public boolean IsChecked() { return this.ischecked; }
    public void setIschecked(){
        if(ischecked)
            ischecked = false;
        else
            ischecked = true;
    }
    public void clear(){
        ischecked = false;
    }
}