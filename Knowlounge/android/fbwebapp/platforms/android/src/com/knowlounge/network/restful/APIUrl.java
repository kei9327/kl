package com.knowlounge.network.restful;

/**
 * Created by Minsu on 2016-05-31.
 */
public class APIUrl {

    public final static String MAPI_HOST = "https://dev.knowlounges.com/mapi/";
    public final static String SI_PLATFORM_HOST = "https://www.sayalo.me:30443/knowlounge/";


    // 인증관련
    public final static String SIGN_IN_FACEBOOK    = "fb/auth.json";
    public final static String SIGN_IN_GOOGLE      = "gl/auth.json";
    public final static String GET_STAR_AUTH_TOKEN = MAPI_HOST + "auth/star/gettoken.json";
    public final static String RELOAD_AUTH         = MAPI_HOST + "auth/reload.json";
    public final static String SIGN_OUT            = MAPI_HOST + "auth/signout.json";


    // 프로필 관련
    public final static String SET_USER_TYPE         = MAPI_HOST + "profile/setUserType.json";
    public final static String SET_PROFILE           = MAPI_HOST + "profile/setProfile.json";
    public final static String UPDATE_PROFILE        = MAPI_HOST + "profile/updateProfile.json";
    public final static String GET_PROFILE           = MAPI_HOST + "profile/getProfile.json";
    public final static String UPLOAD_PROFILE_IMAGE  = MAPI_HOST + "profile/uploadProfileImg.json";
    public final static String GET_PROFILE_CODE_LIST = MAPI_HOST + "profile/getProfileCodeList.json";

    // 룸 관련
    public final static String CREATE_SUB_ROOM        = MAPI_HOST + "room/createSubRoom.json";
    public final static String GET_MY_ROOM_LIST       = MAPI_HOST + "room/list/my.json";
    public final static String GET_RECENT_ROOM_LIST   = MAPI_HOST + "room/list/recent.json";
    public final static String GET_BOOKMARK_ROOM_LIST = MAPI_HOST + "room/list/bookmark.json";
    public final static String GET_FRIEND_ROOM_LIST   = MAPI_HOST + "room/list/friends.json";
    public final static String GET_SCHOOL_ROOM_LIST   = MAPI_HOST + "room/list/school.json";
    public final static String GET_PUBLIC_ROOM_LIST   = MAPI_HOST + "room/list/public.json";
    public final static String ROOM_CHECK             = MAPI_HOST + "room/check.json";
    public final static String REMOVE_ROOM            = MAPI_HOST + "room/remove.json";
    public final static String CREATE_ROOM            = MAPI_HOST + "canvas/add.json";

    // 메인 화면 관련
    public final static String GET_MAIN_HOME    = MAPI_HOST + "main/home.json";
    public final static String GET_MAIN_MYCLASS = MAPI_HOST + "main/myclass.json";
    public final static String GET_MAIN_FRIENDS = MAPI_HOST + "main/friends.json";
    public final static String GET_MAIN_PUBLIC  = MAPI_HOST + "main/public.json";
    public final static String GET_MAIN_SCHOOL  = MAPI_HOST + "main/school.json";

    // Invite 관련 (수업 초대, 참여 요청)
    public final static String INVITE_REQ_JOIN = MAPI_HOST + "invite/join/req.json";

    // 알림(Histroy) 관련
    public final static String GET_HISTORY_LIST      = MAPI_HOST + "history/list.json";
    public final static String REMOVE_HISTORY        = MAPI_HOST + "history/remove.json";
    public final static String UPDATE_HISTORY_STATUS = MAPI_HOST + "history/update.json";
    public final static String GET_ROOM_HISTORY_LIST = MAPI_HOST + "history/list/class.json";

    // SI Platform (스타 서버)
    public final static String HANDLE_SI_ACCESSTOKEN     = SI_PLATFORM_HOST + "user/accessToken";
    public final static String GET_STAR_BALANCE       = SI_PLATFORM_HOST + "user/currency";
    public final static String VERIFY_IN_APP_BILLING  = SI_PLATFORM_HOST + "user/currency/playStorePurchaseData";











}
