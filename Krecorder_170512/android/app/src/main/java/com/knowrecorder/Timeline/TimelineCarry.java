package com.knowrecorder.Timeline;

/**
 * Created by ssyou on 2016-03-29.
 */
public class TimelineCarry {
    private long timestamp;
    private boolean isBackward;

    public TimelineCarry(long timestamp, boolean isBackward) {
        this.timestamp = timestamp;
        this.isBackward = isBackward;
    }

    public TimelineCarry(long timestamp, boolean isBackward, long lastPosition) {
        this.timestamp = timestamp;
        this.isBackward = isBackward;
    }

    public boolean isBackward() {
        return isBackward;
    }

    public void setIsBackward(boolean isBackward) {
        this.isBackward = isBackward;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
