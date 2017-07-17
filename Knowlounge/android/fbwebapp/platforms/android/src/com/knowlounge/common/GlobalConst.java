package com.knowlounge.common;

import java.util.regex.Pattern;

/**
 * Created by Minsu on 2016-01-21.
 */
public class GlobalConst {
    public static boolean profileDialogFirst = true;
    public static Pattern VAILID_EMAIL_ADDRESS_REGETX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    // 폴 관련
    public static final int VIEW_CREATE_FRAGMENT = 1;
    public static  final int VIEW_POLLLIST_FRAGMENT = 2;
    public static final int VIEW_COMPLETE_POLLLIST_FRAGMENT = 3;
    public static  final int ACTION_POLL_SAVE = 4;
    public static  final int ACTION_MAKE_POLL_SHEET = 5;
    public static  final int ACTION_SHOW_TIMER_PANEL = 6;
    public static final int ACTION_SHOW_POLL_RESULT = 7;
    public static final int ACTION_POLL_LIST_REMOVE = 9;
    public static final int ACTION_COMPLETE_POLL_REMOVE = 8;
    public static final int ACTION_POLL_LIST_SEND = 10;

    // 메인 좌메뉴 셋팅 관련
    public static final int NOTIFICATION_SOUND = 11;
    public static final int NOTIFICATION_MESSAGE = 12;

    public static final int ESTABLISH_CLOSED = 13;
    public static final int ESTABLISH_ALL_PUBLIC = 14;

    public static final int VIEW_MAINLEFT_PROFILE_MYNAME = 16;
    public static final int VIEW_MAINLEFT_PROFILE_INTRODUCE = 17;
    public static final int VIEW_MAINLEFT_PROFILE_SCHOOL = 18;

    // 룸 우메뉴 셋팅 관련
    public static final int VIEW_CLASSCONFIG_TITLE = 19;
    public static final int VIEW_CLASSCONFIG_DESC = 20;
    public static final int VIEW_CLASSCONFIG_PARTICIPANT = 21;
    public static final int VIEW_CLASSCONFIG_PUBLIC = 22;


    //룸 채팅 관련
    public static final int VIEW_CHAT_SENDER = 23;
    public static final int VIEW_CHAT_SENDER_INSTANT = 24;
    public static final int VIEW_CHAT_RECEIVER = 25;
    public static final int VIEW_CHAT_RECEIVER_INSTANT = 26;
    public static final int TYPE_CIRCLE_THUMB = 27;
    public static final int TYPE_CIRCLE_SELECT_THUMB = 28;


    //친구 리스트 관련
    public static final int THROUGHT_MAIIN = 29;
    public static final int THROUGHT_ROOM = 30;

    //디바이스 타입 관련
    public static final int DEVICE_PHONE = 31;
    public static final int DEVICE_TABLET = 32;

    public static final int PHONE_PORTRAIT = 33;

    //폰 디바이스 룸 액션바 메뉴 관련
    public static final int MENU_DRAWING = 34;
    public static final int MENU_PRESENTATION = 35;
    public static final int MENU_COMMUNICATION = 36;

    //폰 디바이스 룸 오른쪽 메뉴 관련

    public static final int MODE_PEN = 37;
    public static final int MODE_ERASER = 38;
    public static final int MODE_SHAPE = 39;
    public static final int MODE_HAND = 40;
    public static final int MODE_POINTER = 41;
    public static final int MODE_CAM = 48;

    // 프로필 설정 관련
    public static final int TYPE_STUDENT_START = 42;
    public static final int TYPE_TEACHER_START = 43;
    public static final String CATEGORY_SCHOOL = "PT001";
    public static final String CATEGORY_GRADE = "PT002";
    public static final String CATEGORY_SUBJECT = "PT003";
    public static final String CATEGORY_LANGUAGE = "PT004";

    public static final int VIEW_MAINLEFT_PROFILE_GRADE = 44;
    public static final int VIEW_MAINLEFT_PROFILE_SUBJECT = 45;
    public static final int VIEW_MAINLEFT_PROFILE_LANGUAGE = 46;
    public static final int VIEW_MAINLEFT_PROFILE_ID = 47;


    // 네트워크
    public static final int NETWORK_DISCONNECTED = 100;
    public static final int NETWORK_CONNECTED = 101;

    public static String[] SUPPORT_LANGUAGES = new String[]{
        "ar", // Arabic 2016.07.08 언어 안정화 전까지 아직 지원 안함.
        "da", // Danish
        "de", // German
        "es", // Spanish (latin)
        "fi", // Finnish
        "fr", // French
        "it", // Italian
        "ja", // Japanese
        "ko", // korean
        "pl", // Polish
        "pt",
        "pt-BR", // Portuguese (brazil)
        "ru", // russian  - 2016.07.18 임시로 주석처리
        "tr", // Turkish
        "uk", // Ukrainian
        "zh", // Simplified Chinese
        "zh-TW", // Traditional Chinese
    };

    //도움말 동영상 Uri
    public static String MOVIE_CHANNEL;
    public static String MOVIE_INVITE;
    public static String MOVIE_APPLY;
    public static String MOVIE_CANVAS;
    public static String VIDEO_LIMIT;

    //CanvasRightMenu-parent 관련
    public static final int CANVAS_RIGHT_MENU_USER_VIEW = 48;
    public static final int CANVAS_RIGHT_MENU_INVITE_VIEW = 49;
    public static final int CANVAS_RIGHT_MENU_COMMUNITY_VIEW = 50;
    public static final int CANVAS_RIGHT_MENU_COMMENT_VIEW = 54;

    //CanvasRightMenu-child 관련
    public static final int CANVAS_RIGHT_MENU_USERLIST_VIEW = 51;
    public static final int CANVAS_RIGHT_MENU_ROOMNOTI_VIEW = 52;
    public static final int CANVAS_RIGHT_MENU_CHAT_VIEW = 53;
    public static final int CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW = 55;
    public static final int CANVAS_RIGHT_MENU_GUEST = 56;

    public static boolean reLoadingRoomList = false;

    public static final int EVENT_TAG_VIDEO_SHARE = 57;
    public static final int EVENT_TAG_CHATTING = 58;

    public static final int CREATE_ROOM_MODE = 60;
    public static final int ENTER_ROOM_MODE = 61;
    public static final int MOVE_ROOM_MODE = 62;

}