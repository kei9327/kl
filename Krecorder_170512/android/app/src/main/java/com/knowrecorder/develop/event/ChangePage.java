package com.knowrecorder.develop.event;

/**
 * Created by we160303 on 2017-02-23.
 */

public class ChangePage {
    long pageid;
    float timeStamp;

    public ChangePage(long pageid) {
        this.pageid = pageid;
        this.timeStamp  = -1;
    }

    public ChangePage(float timeStamp) {
        this.pageid = -1;
        this.timeStamp = timeStamp;
    }

    public ChangePage(long pageid, float timeStamp) {
        this.pageid = pageid;
        this.timeStamp = timeStamp;
    }

    public long getPageid() {
        return pageid;
    }

    public float getTimeStamp() {
        return timeStamp;
    }
}

