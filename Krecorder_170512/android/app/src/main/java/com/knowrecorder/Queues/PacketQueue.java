package com.knowrecorder.Queues;

import com.knowrecorder.Timeline.TimelineCarry;

import java.util.ArrayDeque;

/**
 * Created by ssyou on 2016-02-04.
 */
public class PacketQueue {

    private static PacketQueue mInstance = new PacketQueue();
    public ArrayDeque<TimelineCarry> timelineList = new ArrayDeque<>();

    static public PacketQueue getInstance() {
        if (mInstance == null)
            mInstance = new PacketQueue();

        return mInstance;
    }
}
