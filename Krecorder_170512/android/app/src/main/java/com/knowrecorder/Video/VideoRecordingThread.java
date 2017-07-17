package com.knowrecorder.Video;

/**
 * Created by ssyou on 2016-02-05.
 */
public class VideoRecordingThread {
    static private VideoRecordingThread mInstance = new VideoRecordingThread();
    public boolean doThread = true;
    private Thread mThread;

    public VideoRecordingThread() {
    }

    public static VideoRecordingThread getInstance() {
        return mInstance;
    }
}
