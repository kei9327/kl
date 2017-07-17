package com.knowrecorder.develop.event;

/**
 * Created by we160303 on 2017-03-20.
 */

public class ExportSelectedDialogEvent {
    public static final int EXPORT_OPENCOURSE = 812;
    public static final int EXPORT_GALLARY = EXPORT_OPENCOURSE + 1;
    public static final int EXPORT_YOUTUBE = EXPORT_GALLARY + 1;

    private int type;

    public ExportSelectedDialogEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
