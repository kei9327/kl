package com.knowrecorder.OpenCourse;

public class ProgressMessage {
    public String msg;
    public boolean increase;
    public int progress;

    public ProgressMessage(String msg, boolean increase) {
        this.msg = msg;
        this.increase = increase;
    }

    public ProgressMessage(String msg, boolean increase, int progress) {
        this.msg = msg;
        this.increase = increase;
        this.progress = progress;
    }
}
