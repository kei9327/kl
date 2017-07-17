package com.knowlounge.rxjava.message;

import com.knowlounge.common.GlobalConst;

/**
 * Created by we160303 on 2016-10-05.
 */

public class ChattingEvent {
    private int tag = GlobalConst.EVENT_TAG_VIDEO_SHARE;
    private String wisperId;

    public ChattingEvent(String wisperId){
        this.wisperId = wisperId;
    }
    public int getTag() { return this.tag ;}
    public String getWisperId(){ return this.wisperId ; }
}
