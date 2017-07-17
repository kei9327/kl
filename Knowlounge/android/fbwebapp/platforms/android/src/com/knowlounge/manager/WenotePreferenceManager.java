package com.knowlounge.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.knowlounge.common.GlobalConst;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Minsu on 2016-03-08.
 */
public class WenotePreferenceManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "WenotePreferenceManager";

    private static WenotePreferenceManager _instance = null;
    private static SharedPreferences preferences;
    private static Context mContext;
    private String KEY = "fboardeoqkr!@#!!";
    private String VECTOR = "fboardEoqkRtkn%!";
    private String CHARSET = "UTF-8";

    private ArrayList<OnPreferenceChangeListener> mOnPreferenceChangeListeners = new ArrayList<>();

    public interface OnPreferenceChangeListener {
        void onPreferenceChanged(SharedPreferences preferences, String key);
    }

    public static WenotePreferenceManager getInstance(Context context){
        mContext = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        _instance = new WenotePreferenceManager();
        return _instance;
    }

    public long getGlobalTimeTurm(){ return preferences.getLong("global_time_turm",0);}

    public boolean getAppOnOff() { return preferences.getBoolean("app_on_off",false);}

    public String getKEY() {
        return this.KEY;
    }

    public String getVECTOR() {
        return this.VECTOR;
    }

    public String getCHARSET() { return this.CHARSET; }

    public float getDensity(){
        return preferences.getFloat("density", 1.0f);
    }

    public String getUserId() {
        return preferences.getString("userid", "");
    }

    public String getUserNo() {
        return preferences.getString("userno", "");
    }

    public String getUserNm() {
        return preferences.getString("usernm", "");
    }

    public String getEmail() {
        return preferences.getString("email", "");
    }

    public String getUserThumbnail() {
        return preferences.getString("thumbnail", "");
    }

    public String getUserThumbnailLargeCurrent() {
        return preferences.getString("large_thumbnail_current", "");
    }
    public String getUserThumbnailLargeLast() {
        return preferences.getString("large_thumbnail_last", "");
    }

    public String getSnsType() {
        return preferences.getString("snstype", "");
    }

    public String getAccessToken() {
        return preferences.getString("access_token", "");
    }

    public String getZicoAccessToken() {
        return preferences.getString("zico_access_token", "");
    }

    public String getUserCookie() {
        Log.d(TAG, "getUserCookie : " + preferences.getString("user_cookie", ""));
        return preferences.getString("user_cookie", "");
    }

    public String getChecksumCookie() {
        return preferences.getString("checksum_cookie", "");
    }

    public String getCookieQueryStr() {
        Log.d(TAG, "getCookieQueryStr : " + preferences.getString("cookie_query_string", ""));
        return preferences.getString("cookie_query_string", "");
    }

    public String getDeviceToken() {
        return preferences.getString("device_token", "");
    }

    public String getDeviceId() {
        return preferences.getString("device_id", "");
    }

    public String getCurrentRoomId() {
        return preferences.getString("roomid", "");
    }

    public String getRoomTitle() {
        return preferences.getString("roomtitle", "");
    }


    public String getCurrentRoomAuthInfo() { return preferences.getString("current_room_authinfo","") ; }

    public String getCurrentMasterNm() {
        return preferences.getString("master_user_nm", "");
    }

    public String getCurrentTeacherUserNo() {
        return preferences.getString("teacher_userno", "");
    }

    public Object getPenAlpha() {
        Map<String, ?> map = preferences.getAll();
        return map.get("pen_alpha");
    }

    public Object getPenWidth() {
        Map<String, ?> map = preferences.getAll();
        return map.get("pen_width");
    }

    public Object getPenColor() {
        Map<String, ?> map = preferences.getAll();
        return map.get("pen_color");
    }

    public Object getPenColorIdx() {
        Map<String, ?> map = preferences.getAll();
        return map.get("pen_color_idx");
    }

    public Object getEraserWidth() {
        Map<String, ?> map = preferences.getAll();
        return map.get("eraser_width");
    }

    public Object getShapeTypeIdx() {
        Map<String, ?> map = preferences.getAll();
        return map.get("shape_type_idx");
    }

    public Object getFillTypeIdx() {
        return preferences.getInt("fill_type_idx", 0);
    }

    public String getLpenColor() {
        // 선 색상 코드값 (디폴트 : 공백)
        return preferences.getString("lpen_color", "");
    }

    public String getSpenColor() {
        // 사각형 색상 코드값 (디폴트 : 공백)
        return preferences.getString("spen_color", "");
    }

    public String getCpenColor() {
        // 원 색상 코드값 (디폴트 : 공백)
        return preferences.getString("cpen_color", "");
    }


    public int getLpenColorIdx() {
        return preferences.getInt("lpen_color_idx", 1);
    }

    public int getSpenColorIdx() {
        return preferences.getInt("spen_color_idx", 1);
    }

    public int getCpenColorIdx() {
        return preferences.getInt("cpen_color_idx", 1);
    }

    public String getSpenBorderColor() {
        // 사각형 테두리 색상 코드값 (디폴트 : 공백)
        return preferences.getString("spen_border_color", "");
    }

    public String getCpenBorderColor() {
        // 원 테두리 색상 코드값 (디폴트 : 공백)
        return preferences.getString("cpen_border_color", "");
    }

    public int getSpenBorderColorIdx() {
        return preferences.getInt("spen_border_color_idx", 1);
    }

    public int getCpenBorderColorIdx() {
        return preferences.getInt("cpen_border_color_idx", 1);
    }

    public int getLpenWidth() {
        // 선 너비값 (디폴트 : 50)
        return preferences.getInt("lpen_width", 50);
    }

    public int getSpenWidth() {
        // 사각형 너비값 (디폴트 : 50)
        return preferences.getInt("spen_width", 50);
    }

    public int getCpenWidth() {
        // 원 너비값 (디폴트 : 50)
        return preferences.getInt("cpen_width", 50);
    }

    public int getLpenAlpha() {
        // 선 알파값 (디폴트 : 100)
        return preferences.getInt("lpen_alpha", 100);
    }

    public int getSpenAlpha() {
        // 사각형 알파값 (디폴트 : 100)
        return preferences.getInt("spen_alpha", 100);
    }

    public int getCpenAlpha() {
        // 원 알파값 (디폴트 : 100)
        return preferences.getInt("cpen_alpha", 100);
    }

    public int getLaserType() {
        return preferences.getInt("laser_type", 1);
    }

    public int getLaserColorIdx() {
        return preferences.getInt("laser_color_idx", 1);
    }

    // MainLeftMenu 관련
    public int getEstablish() { return preferences.getInt("establish", GlobalConst.ESTABLISH_ALL_PUBLIC);}

    public boolean getNotification_message() { return preferences.getBoolean("notification_message", true);}

    public String getSelfIntroduce(){ return preferences.getString("self_introduce", ""); }

    // DropBox AccessToken 관련
    public String getAccessKey(String tag){ return preferences.getString(tag, null);}

    public String getAccessSecret(String tag){ return preferences.getString(tag,null);}

    public Boolean getDropboxLoggedIn(){ return preferences.getBoolean("dropbox_loggedin", false);}

    public int getDeviceType(){ return  preferences.getInt("device_type", -1); }

    public int getOrientation(){ return preferences.getInt("orientation", -1); }

    public String getFriendsIdStr() {
        return preferences.getString("friends", "");
    }

    public String getSiAccessToken() {
        return preferences.getString("si_access_token", "");
    }

    public int getUserStarBalance() {
        return preferences.getInt("star_balance", 0);
    }

    //Profile 관련
    public boolean isProfileSkip(){ return preferences.getBoolean("profile_skip", false); }

    public String getMyUserType(){return preferences.getString("my_user_type","0"); }

    public String getMyGradeCode(){ return preferences.getString("grade_code", ""); }
    public String getMyGradeName(){ return preferences.getString("grade_name", ""); }

    public String getMySubjectCode(){ return preferences.getString("subject_code","") ;}
    public String getMySubjectName(){ return preferences.getString("subject_name","") ;}

    public String getMyLanguageCode(){ return  preferences.getString("language_code", ""); }
    public String getMyLanguageName(){ return  preferences.getString("language_name", ""); }

    public String getMyIntroduction(){ return preferences.getString("introduction", ""); }

    public String getMyeducation(){return  preferences.getString("education", ""); }

    public boolean isProfileChanged(){ return  preferences.getBoolean("is_profile_changed", false) ;}


    // 프리미엄
    public String getPremiumUserNo() {
        // 프리미엄 서비스에 로그인 한 유저의 유저번호 리턴
        return preferences.getString("premium_userno", "");
    }


    public String getPremiumToken() {
        // 프리미엄 서비스에 로그인 한 유저의 유저번호 리턴
        return preferences.getString("premium_token", "");
    }

    public String getPremiumMasterCookie() {
        // 프리미엄 서비스에 로그인 한 유저의 마스터 쿠키 리턴
        return preferences.getString("premium_master_cookie", "");
    }


    public String getPremiumChecksumCookie() {
        // 프리미엄 서비스에 로그인 한 유저의 체크썸 쿠키 리턴
        return preferences.getString("premium_checksum_cookie", "");
    }

    public String getPremiumGroupList() {
        // 프리미엄 서비스에 로그인 한 유저의 그룹 리스트 리턴
        return preferences.getString("premium_group_list", "");
    }


    public void setPremiumUserNo(String userNo) {
        // 프리미엄 서비스에 로그인 한 유저의 유저번호 저장
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("premium_userno", userNo);
        editor.commit();
    }


    public void setPremiumToken(String accessToken) {
        // 프리미엄 서비스에 로그인 한 유저의 Access Token 저장
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("premium_token", accessToken);
        editor.commit();
    }

    public void setPremiumMasterCookie(String cookieStr) {
        // 프리미엄 서비스에 로그인 한 유저의 마스터 쿠키 저장
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("premium_master_cookie", cookieStr);
        editor.commit();
    }

    public void setPremiumChecksumCookie(String cookieStr) {
        // 프리미엄 서비스에 로그인 한 유저의 체크썸 쿠키 저장
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("premium_checksum_cookie", cookieStr);
        editor.commit();
    }

    public void setPremiumGroupList(String groupListStr) {
        // 프리미엄 서비스에 로그인 한 유저의 유저번호 리턴
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("premium_group_list", groupListStr);
        editor.commit();
    }








    // Setter Start----------------------------------------------------------------------------------------------------------------
    public void setGlobalTimeTurm(long turm){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("global_time_turm", turm);
        editor.commit();
    }

    public void setAppOnOff(boolean onOff){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("app_on_off",onOff);
        editor.commit();
    }

    public void setDensity(float density) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("density", density);
        editor.commit();
    }

    public void setSnsType(String snsType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("snstype", snsType);
        editor.commit();
    }

    public void setUserNo(String userNo) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userno", userNo);
        editor.commit();
    }

    public void setUserId(String userId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userid", userId);
        editor.commit();
    }

    public void setUserNm(String userNm) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("usernm", userNm);
        editor.commit();
    }

    public void setEmail(String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.commit();
    }


    public void setUserThumbnail(String thumbnail) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("thumbnail", thumbnail);
        editor.commit();
    }

    public void setUserThumbnailLargeCurrent(String thumbnail) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("large_thumbnail_current", thumbnail);
        editor.commit();
    }
    public void setUserThumbnailLargeLast(String thumbnail) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("large_thumbnail_last", thumbnail);
        editor.commit();
    }

    public void setAccessToken(String accessToken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("access_token", accessToken);
        editor.commit();
    }

    public void setZicoAccessToken(String accessToken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("zico_access_token", accessToken);
        editor.commit();
    }


    public void setUserCookie(String cookie) {
        Log.d(TAG, "setUserCookie : " + cookie);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_cookie", cookie);
        editor.commit();
    }

    public void setChecksumCookie(String cookie) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("checksum_cookie", cookie);
        editor.commit();
    }

    public void setCookieQueryStr(String queryStr) {
        Log.d(TAG, "setCookieQueryStr : " + queryStr);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cookie_query_string", queryStr);
        editor.commit();
    }

    public void setDeviceToken(String deviceToken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("device_token", deviceToken);
        editor.commit();
    }

    public void setDeviceId(String deviceId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("device_id", deviceId);
        editor.commit();
    }

    public void setCurrentRoomId(String roomId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("roomid", roomId);
        editor.commit();
    }


    public void setRoomTitle(String roomTitle) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("roomtitle", roomTitle);
        editor.commit();
    }

    public void setCurrentRoomAuthInfo(String authinfo){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("current_room_authinfo",authinfo);
        editor.commit();
    }

    public void setCurrentMasterNm(String userNm) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("master_user_nm", userNm);
        editor.commit();
    }

    public void setCurrentTeacherUserNo(String userNo) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("teacher_userno", userNo);
        editor.commit();
    }

    public void setPenAlpha(int penAlpha) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("pen_alpha", penAlpha);
        editor.commit();
    }

    public void setPenWidth(int penWidth) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("pen_width", penWidth);
        editor.commit();
    }

    public void setPenColor(String penColor) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pen_color", penColor);
        editor.commit();
    }

    public void setPenColorIdx(int penColorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("pen_color_idx", penColorIdx);
        editor.commit();
    }

    public void setEraserWidth(int eraserWidth) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("eraser_width", eraserWidth);
        editor.commit();
    }

    public void setShapeTypeIdx(int shapeTypeIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("shape_type_idx", shapeTypeIdx);
        editor.commit();
    }


    public void setFillTypeIdx(int fillTypeIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("fill_type_idx", fillTypeIdx);
        editor.commit();
    }

    public void setLpenColor(String colorCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lpen_color", colorCode);
        editor.commit();
    }

    public void setSpenColor(String colorCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("spen_color", colorCode);
        editor.commit();
    }

    public void setCpenColor(String colorCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cpen_color", colorCode);
        editor.commit();
    }


    public void setLpenColorIdx(int lpenColorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lpen_color_idx", lpenColorIdx);
        editor.commit();
    }

    public void setSpenColorIdx(int spenColorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("spen_color_idx", spenColorIdx);
        editor.commit();
    }

    public void setCpenColorIdx(int cpenColorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cpen_color_idx", cpenColorIdx);
        editor.commit();
    }



    public void setSpenBorderColor(String colorCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("spen_border_color", colorCode);
        editor.commit();
    }

    public void setCpenBorderColor(String colorCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cpen_border_color", colorCode);
        editor.commit();
    }


    public void setSpenBorderColorIdx(int colorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("spen_border_color_idx", colorIdx);
        editor.commit();
    }

    public void setCpenBorderColorIdx(int colorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cpen_border_color_idx", colorIdx);
        editor.commit();
    }


    public void setLpenWidth(int width) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lpen_width", width);
        editor.commit();
    }

    public void setSpenWidth(int width) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("spen_width", width);
        editor.commit();
    }

    public void setCpenWidth(int width) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cpen_width", width);
        editor.commit();
    }

    public void setLpenAlpha(int alpha) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lpen_alpha", alpha);
        editor.commit();
    }

    public void setSpenAlpha(int alpha) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("spen_alpha", alpha);
        editor.commit();
    }

    public void setCpenAlpha(int alpha) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cpen_alpha", alpha);
        editor.commit();
    }

    public void setLaserType(int laserType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("laser_type", laserType);
        editor.commit();
    }

    public void setLaserColorIdx(int laserColorIdx) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("laser_color_idx", laserColorIdx);
        editor.commit();
    }



    //MainLeftMenu 관련
    public void setEstablish(int establish_position) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("establish", establish_position);
        editor.commit();
    }
    public void setNotification(int type){
        if(type == GlobalConst.NOTIFICATION_SOUND) {
            SharedPreferences.Editor editor = preferences.edit();
            if(preferences.getBoolean("notification_sound",false))
                editor.putBoolean("notification_sound",false);
            else
                editor.putBoolean("notification_sound", true);
            editor.commit();
        }else if(type == GlobalConst.NOTIFICATION_MESSAGE){
            SharedPreferences.Editor editor = preferences.edit();
            if(preferences.getBoolean("notification_message",true))
                editor.putBoolean("notification_message",false);
            else
                editor.putBoolean("notification_message", true);
            editor.commit();
        }
    }

    public void setSelfIntroduce(String data){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("self_introduce",data);
        editor.commit();
    }

    //DropBox AcessToken
    public void setAccessKey(String tag, String data){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(tag,data);
        editor.commit();
    }
    public void setAccessSecret(String tag, String data){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(tag, data);
        editor.commit();
    }
    public void setDropboxLoggedIn(boolean toggle) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dropbox_loggedin", toggle);
        editor.commit();
    }

    public void setDropBoxAccessToken(String accessTtoken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("dropbox_accesstoken", accessTtoken);
        editor.commit();
    }
    public void setDropBoxID(String id){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("dropbox_id", id);
        editor.commit();
    }

    public void setDeviceType(int type) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("device_type", type);
        editor.commit();
    }

    public void setOrientation(int orientation){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("orientation", orientation);
        editor.commit();
    }

    public void setFriendsIdStr(String friends) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("friends", friends);
        editor.commit();
    }

    public void setSiAccessToken(String userAccessToken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("si_access_token", userAccessToken);
        editor.commit();
    }

    public void setUserStarBalance(int starCount) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("star_balance", starCount);
        editor.commit();
    }

    //Profile 관련

    public void setMyUserType(String userType){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("my_user_type",userType);
        editor.commit();
    }
    public void setProfileSkip(boolean isSkip){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("profile_skip", isSkip);
        editor.commit();
    }

    public void setMyGradeCode(String grade){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("grade_code", grade);
        editor.commit();
    }
    public void setMyGradeName(String grade){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("grade_name", grade);
        editor.commit();
    }

    public void setMySubjectCode(String subject){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("subject_code", subject);
        editor.commit();
    }
    public void setMySubjectName(String subject){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("subject_name", subject);
        editor.commit();
    }

    public void setMyLanguageCode(String language){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language_code", language);
        editor.commit();
    }
    public void setMyLanguageName(String language){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language_name", language);
        editor.commit();
    }


    public void setMyIntroduction(String introduction){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("introduction", introduction);
        editor.commit();
    }
    public void setMyeducation(String education){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("education", education);
        editor.commit();
    }

    public void setChangeProfile(Boolean isChanged){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_profile_changed", isChanged);
        editor.commit();
    }



//    Badge count start-----------------------------------------------------------------------------

    public int getNotiBadgeCount(){
        return preferences.getInt("noti_badge_count",0);
    }
    public void setNotiBadgeCount(int count){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("noti_badge_count",count);
        editor.commit();
    }
    public int updateNotiBadgeCount(){
        int notiBadgeCount = preferences.getInt("noti_badge_count",0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("noti_badge_count",notiBadgeCount+1);
        editor.commit();

        return preferences.getInt("noti_badge_count",0);
    }
    public void setReqjoinNotiBadgeSeqno(String seqno){
        String seqnoStr = preferences.getString("reqjoin_seqno_str","");
        String[] seqnoArr = seqnoStr.split(",");

        for(int i=0; i<seqnoArr.length;i++){
            if(seqno.equals(seqnoArr[i]))
                return;
        }
        seqnoStr += seqno +",";

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("reqjoin_seqno_str",seqnoStr.substring(0,seqnoStr.length()-1));
        editor.commit();
    }
    public int getReqjoinNotiBadgeCount(String seqno){
        return preferences.getInt("reqjoin_noti_badge_count_"+seqno,0);
    }
    public int updateReqjoinNotiBadgeCount(String seqno){
        int reqjoinNotiBadgeCount = preferences.getInt("reqjoin_noti_badge_count_"+seqno,0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("reqjoin_noti_badge_count_"+seqno,reqjoinNotiBadgeCount+1);
        editor.commit();

        return preferences.getInt("reqjoin_noti_badge_count_"+seqno,0);
    }

    public void clearNotiBadgeCount(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("noti_badge_count");

        String seqnoStr = preferences.getString("reqjoin_seqno_str","");
        String[] seqnoArr = seqnoStr.split(",");
        for(int i=0; i<seqnoArr.length;i++)
        {
            editor.remove("reqjoin_noti_badge_count_"+seqnoArr[i]);
        }


        editor.commit();
    }
    public void clearReqjoinNotiBadgeCount(String seqno){
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("reqjoin_noti_badge_count_"+seqno);
        editor.commit();
    }
//  Badge count end---------------------------------------------------------------------------------


    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public void clearRoomData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("roomid");
        editor.remove("roomtitle");
        editor.remove("master_user_nm");
        editor.remove("teacher_userno");
        editor.remove("pen_alpha");
        editor.remove("pen_width");
        editor.remove("pen_color");
        editor.remove("pen_color_idx");
        editor.remove("eraser_width");
        editor.remove("shape_type_idx");
        editor.remove("fill_type_idx");
        editor.remove("lpen_color");
        editor.remove("spen_color");
        editor.remove("cpen_color");
        editor.remove("lpen_color_idx");
        editor.remove("spen_color_idx");
        editor.remove("cpen_color_idx");
        editor.remove("spen_border_color");
        editor.remove("cpen_border_color");
        editor.remove("spen_border_color_idx");
        editor.remove("cpen_border_color_idx");
        editor.remove("lpen_width");
        editor.remove("spen_width");
        editor.remove("cpen_width");
        editor.remove("lpen_alpha");
        editor.remove("spen_alpha");
        editor.remove("cpen_alpha");
        editor.remove("laser_type");
        editor.remove("laser_color_idx");
        editor.commit();
    }

    public void setAppRunningFlag(boolean flag) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("app_running_flag", flag);
        editor.commit();
    }


    public boolean getAppRunningFlag() {
        return preferences.getBoolean("app_running_flag", false);
    }


    public void clearAppRunningFlag() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("app_running_flag");
        editor.commit();
    }

    public boolean contains(String key) {
        return preferences.contains(key);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (OnPreferenceChangeListener listener : mOnPreferenceChangeListeners) {
            if (listener != null) {
                listener.onPreferenceChanged(sharedPreferences, key);
            }
        }
    }

    public void registerOnPreferenceChangeListener(OnPreferenceChangeListener l) {
//        mOnPreferenceChangeListener = l;

        mOnPreferenceChangeListeners.add(l);

        final SharedPreferences prefs = preferences;
        if (prefs != null) {
            prefs.registerOnSharedPreferenceChangeListener(this);
        }
    }

    public void unregisterOnPreferenceChangeListener(OnPreferenceChangeListener l) {
//        mOnPreferenceChangeListener = null;
        mOnPreferenceChangeListeners.remove(l);

        final SharedPreferences prefs = preferences;
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
