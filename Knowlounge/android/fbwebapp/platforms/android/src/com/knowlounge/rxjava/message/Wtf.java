package com.knowlounge.rxjava.message;

import com.wescan.alo.rtc.RtcMediaChannel;

/**
 * Created by Mansu on 2017-03-28.
 */

public class Wtf {
    private RtcMediaChannel localMediaChannel;

    public Wtf(RtcMediaChannel localMediaChannel) {
        this.localMediaChannel = localMediaChannel;
    }

    public RtcMediaChannel getLocalMediaChannel() {
        return localMediaChannel;
    }

    public void setLocalMediaChannel(RtcMediaChannel localMediaChannel) {
        this.localMediaChannel = localMediaChannel;
    }
}
