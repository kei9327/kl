package com.knowlounge.rxjava.message;

import com.knowlounge.common.GlobalConst;

/**
 * Created by we160303 on 2016-10-05.
 */

public class VideoIdEvent {
    private int tag = GlobalConst.EVENT_TAG_VIDEO_SHARE;
    private String videoId;
    private String title;
    private String typeFlag;

    public VideoIdEvent(String videoId, String title, String typeFlag){
        this.videoId = videoId;
        this.title = title;
        this.typeFlag = typeFlag;
    }
    public int getTag() { return this.tag ;}
    public String getVideoId(){ return this.videoId ; }
    public String getTitle(){ return this.title ; }
    public String getTypeFlag(){ return  this.typeFlag ; }
}
