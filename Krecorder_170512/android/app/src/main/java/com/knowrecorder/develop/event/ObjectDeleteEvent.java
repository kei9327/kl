package com.knowrecorder.develop.event;

/**
 * Created by we160303 on 2017-02-07.
 */

public class ObjectDeleteEvent {
    long mid;

    public ObjectDeleteEvent(long mid) {
        this.mid = mid;
    }

    public long getMid() {
        return mid;
    }
}
