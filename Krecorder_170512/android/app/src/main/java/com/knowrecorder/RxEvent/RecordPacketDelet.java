package com.knowrecorder.RxEvent;

import com.knowrecorder.rxjava.RxEvent;

/**
 * Created by we160303 on 2016-12-22.
 */

public class RecordPacketDelet extends RxEvent {
    private long downTime;
    private long upTime;

    public RecordPacketDelet(long downTime, long upTime) {
        super("RecordPacketDelet");
        this.downTime = downTime;
        this.upTime = upTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public long getDownTime() {
        return downTime;
    }

    public long getUpTime() {
        return upTime;
    }
}
