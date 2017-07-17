package com.knowlounge.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Minsu on 2016-01-28.
 */
public class AndroidUtils {

    public static String spaceRemove(String data){
        return data.trim();
    }

    //Emoji 있는지 판별
    public static boolean hasEmoji(String data){
        Pattern emoji = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
        Matcher emojiMatcher = emoji.matcher(data);
        if(emojiMatcher.find())
            return true;
        else
            return hasOneEmoji(data);
    }
    public static boolean hasOneEmoji(String data){
        String result = "";
        for(int i=0 ; i< data.length() ; i++){
            if(data.codePointAt(i) == 9786)
                return true;
        }
        return false;
    }


    public static boolean checkRangeName(String name,int minLen ,int maxLen){
        if(name.length() >= minLen && name.length() <= maxLen){
            return true;
        }
        return false;
    }

    public static boolean checkRangeNumberInString(String number, int min, int max){
        if(number.length() > 9)
            return false;
        else if(number.length() !=0 && (Integer.parseInt(number) >= min && Integer.parseInt(number) <= max)){
            return true;
        }else{
            return false;
        }
    }

    public static boolean checkSpecialChar(String data){
        String specialCaracter = "!?.,()@:;/-♡*'\"~^#+×÷=%￦&♤☆♧\\_<>{}[]`|$€£¥°○●□■♢※《》¤¡¿";
        String pattern = ".*[" + Pattern.quote(specialCaracter) + "].*";

        return Pattern.matches(pattern, data);
    }


    public static int checkNameInput(String data) {

        String specialCaracter = "!@#$%^&*()_+=~`?/>.<,:;{}[]\'\"|";
        String pattern = ".*[" + Pattern.quote(specialCaracter) + "].*";

        if (data.length() == 0) {
            return 1;
        } else if (data.length() < 2 || data.length() > 40) {
            return 2;
        } else if (Pattern.matches(pattern,data)) {
//            return 3;
        }
        return 0;
    }


    public static String changeSizeThumbnail(String url, int size) {
        String resultUrl = "";
        if (url.matches(".*google.*")) {
            resultUrl = url.substring(0, url.length()-2) + size;
        } else if (url.matches(".*facebook.*")) {
            resultUrl = url + "?height=" + size;
        } else {

        }
        return resultUrl;
    }


    public static String getServerHost() {
        String host = "";
        String flag = Resources.getSystem().getString(R.string.svr_flag);
        String idStr = "svr_host_" + flag;
        host = Resources.getSystem().getString(Resources.getSystem().getIdentifier(idStr, "string", ""));

        return host;
    }

    public static void keyboardHide(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if(activity.getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    /**
     param : String date
     result = device_Date - date
     */
    public static String transformDate(Context mContext, String date) {

        WenotePreferenceManager prefManager = WenotePreferenceManager.getInstance(mContext);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date nDate = getDateFormat(new Date());
        Date cDate = getDateFormat(date);

        long nowDate = nDate.getTime();
        long curDate = cDate.getTime()+prefManager.getGlobalTimeTurm();
        long gap;

        Log.d("time_now",nowDate+"");
        Log.d("time_date",curDate+"");

        long transCdate = curDate + prefManager.getGlobalTimeTurm();
        String transDate = format.format(new Date(transCdate));

        gap = (nowDate - curDate);

        if (gap < (1000*60) )
            return mContext.getResources().getString(R.string.history_time_now);

        gap = (nowDate/(1000*60)) - (curDate/(1000*60));
        if (gap < 60)
            return String.format(mContext.getResources().getString(R.string.history_time_minute),(int)gap);

        gap = (nowDate/(1000*60*60)) - (curDate/(1000*60*60));
        if (gap < 24)
            return String.format(mContext.getResources().getString(R.string.history_time_hour),(int)gap);

        gap = (nowDate/(1000*60*60*24)) - (curDate/(1000*60*60*24));
        if (gap == 1)
            return  mContext.getResources().getString(R.string.history_time_yesterday);
        else
            return transDate;
    }

    public static long getGlobalTimeTurm(Context mContext, String date) {

        Date nDate = getDateFormat(new Date());
        Date cDate = getDateFormat(date);

        long gap = nDate.getTime() - cDate.getTime();

        return (long)gap;
    }

    public static Date getDateFormat(String data){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        Date date = null;

        try {
            date = format.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
    public static Date getDateFormat(Date data){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        String tempDate = format.format(data);
        Date date = null;

        try {
            date = format.parse(tempDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static SpannableStringBuilder setHighlightText(String data, String target){
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String str = data;
        sb.append(str);
        int startIdx = data.indexOf(target);
        int endIdx = startIdx + target.length();
        if(startIdx == -1) {
            return null;
        } else {
            sb.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
    }

    //뱃지 update

    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }


    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }


    @SuppressWarnings("deprecation")
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        String topActivityName;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningAppProcessInfo> Info = manager.getRunningAppProcesses();
            topActivityName = Info.get(0).processName;
            Log.d("AndroidUtils", "Top Activity Name (API 21 over) : " + topActivityName);
        } else {
            List<ActivityManager.RunningTaskInfo> Info = manager.getRunningTasks(1);
            ComponentName topActivity = Info.get(0).topActivity;
            topActivityName = topActivity.getPackageName();
            Log.d("AndroidUtils", "Top Activity Name (API 21 under) : " + topActivityName);
        }
        return topActivityName;
    }

    public static boolean isKnowloungeRunningCheck(Context context){
        Log.d("AndroidUtils", "Context's Package Name : " + context.getPackageName());
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        String topActivityName;
        String currentActivityName = context.getPackageName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topActivityName = am.getRunningAppProcesses().get(0).processName;
            Log.d("AndroidUtils", "Top Activity Name (API 21 over) : " + topActivityName);
        } else {
            List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
            ComponentName topActivity = Info.get(0).topActivity;
            topActivityName = topActivity.getPackageName();
            Log.d("AndroidUtils", "Top Activity Name (API 21 under) : " + topActivityName);
            Log.d("AndroidUtils", "Top Activity Class Name (API 21 under) : " + topActivity.getClassName());
        }

        if(topActivityName.equals(currentActivityName))
            return true;
        else
            return false;
    }


    /**
     * Hides a spinner's drop down.
     */
    public static void hideSpinnerDropDown(Spinner spinner) {
        try {
            Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
            method.setAccessible(true);
            method.invoke(spinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isOverByte(String data, int range){
        try {
            if (data.toString().getBytes("UTF-8").length > range)
                return true;
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getPxFromDp(Context context, float dp){
        int px = 0;
        px = (int) (dp * context.getResources().getDisplayMetrics().density);
        return px;
    }

    public static int getDpFromPx(Context context, float px) {
        int dp = 0;
        dp = (int) (px / context.getResources().getDisplayMetrics().density);
        return dp;
    }

    public static String getRoomCode(String data){
        return data.replaceAll("[^0-9]","");
    }

    public static void requestEssentialPermission(Activity ctx, String[] permissions, int requestId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<String> needPermissions = new ArrayList<String>();
            needPermissions.clear();

            for (int i=0; i< permissions.length; i++) {
                if (ctx.checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    needPermissions.add(permissions[i]);
                    // 거부된 퍼미션을 추가함..
                }
            }

            if (!needPermissions.isEmpty()) {
                String[] requestArray = needPermissions.toArray(new String[needPermissions.size()]);
                ctx.requestPermissions(requestArray, requestId);
            }
        }
    }

    public static String getHash(String sourceDir) {
        // TODO Auto-generated method stub

        File file = new File(sourceDir);
        String outputTxt= "";
        String hashcode = null;

        try {
            FileInputStream input = new FileInputStream(file);

            ByteArrayOutputStream output = new ByteArrayOutputStream ();
            byte [] buffer = new byte [65536];
            int l;


            while ((l = input.read (buffer)) > 0)
                output.write (buffer, 0, l);

            input.close ();
            output.close ();

            byte [] data = output.toByteArray ();

            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );

            byte[] bytes = data;

            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            StringBuilder sb = new StringBuilder();

            for ( byte b : bytes ) {
                sb.append( String.format("%02X", b) );
            }

            hashcode = sb.toString();


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hashcode.toString();
    }


//    public static void callMethodByName(String methodName) {
//        Method method;
//        method.invoke()
//    }


    public static String getLanguageCode() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String localeStr = language + "-" + country;

        if (isLocaleSupport(localeStr)) {
            return localeStr;
        } else if (isLocaleSupport(language)) {
            return language;
        } else {
            return "en";
        }
    }

    private static boolean isLocaleSupport(String localeStr) {
        for (String str : GlobalConst.SUPPORT_LANGUAGES) {
            if (str.equals(localeStr)) {
                return true;
            }
        }
        return false;
    }

    public static int getDistintionTime(long a, long b){
        return (int)((b-a)/(1000*60*60*24));
    }

}