package com.knowlounge.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by we160303 on 2016-10-21.
 */

public class SharedPreferencesManager {
    private static Context mContext;
    private static SharedPreferences preferences;
    private static SharedPreferencesManager _instance;

    public static SharedPreferencesManager getInstance(Context context){
        mContext = context;
        preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        _instance = new SharedPreferencesManager();
        return _instance;
    }

    public void setStartDay(long startDay){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("start_day",startDay);
        editor.commit();
    }

    public long getStartDay(){
        return preferences.getLong("start_day", 0);
    }

    public void setLastDay(long lastDay){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("last_day",lastDay);
        editor.commit();
    }

    public long getLastDay(){
        return preferences.getLong("last_day",0);
    }
}
