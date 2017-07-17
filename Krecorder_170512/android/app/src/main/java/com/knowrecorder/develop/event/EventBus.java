package com.knowrecorder.develop.event;

/**
 * Created by we160303 on 2017-03-02.
 */

public class EventBus {
    public static final int BRING_TO_FRONT_VIEW = -1124232;
    public static final int CHANGE_PAGE = BRING_TO_FRONT_VIEW + 1;
    public static final int EVENT_TYPE = CHANGE_PAGE + 1;
    public static final int OBJECT_DELETE = EVENT_TYPE + 1;
    public static final int EXPORT_SELECTED = OBJECT_DELETE + 1;
    public static final int OPEN_COURSE_EXPORT = EXPORT_SELECTED + 1;
}
