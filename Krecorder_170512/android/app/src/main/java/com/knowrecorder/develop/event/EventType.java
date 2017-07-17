package com.knowrecorder.develop.event;

/**
 * Created by Changha on 2017-02-07.
 *
 * 한번에 이루어 지는 작업 모음
 * 예를 들어 PopupMenu 닫는다.
 */

public class EventType {
    public static final int CLOSE_POPUP = 0x000001;
    public static final int CLEAR_TEXT_FOCUS = CLOSE_POPUP + 1;
    public static final int PLAYER_END = CLEAR_TEXT_FOCUS + 1;

    public static final int OPEN_SERVICE_GUIDE = PLAYER_END + 1;
    public static final int OPEN_INFOMATION = OPEN_SERVICE_GUIDE + 1;
    public static final int OPEN_HELP = OPEN_INFOMATION + 1;
    public static final int CHANGE_TOOLTYPE = OPEN_HELP + 1;

    public static final int ALL_SAVE_PACKET = CHANGE_TOOLTYPE + 1;
    public static final int REFASH_PAGE_STATE = ALL_SAVE_PACKET + 1;

    public static final int REFASH_PAGE = REFASH_PAGE_STATE + 1;

    public static final int NEW_NOTE = REFASH_PAGE + 1;
    public static final int SET_NOTE = NEW_NOTE + 1;
    public static final int DEL_NOTE = SET_NOTE + 1;

    public static final int LOAD_COMPLETED = DEL_NOTE + 1;

    private int event;
    private String arg;
    public EventType(int event) {
        this.event = event;
    }

    public EventType(int event, String arg) {
        this.event = event;
        this.arg = arg;
    }

    public int getEvent() {
        return event;
    }

    public String getArgument(){ return arg;}
}
