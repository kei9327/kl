package com.knowrecorder.develop.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.knowrecorder.develop.file.FilePath;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by we160303 on 2017-02-15.
 */

public class SharedPreferencesManager {
    private static SharedPreferencesManager instance = null;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private int recentDataSize = 5;
    private HashSet<String> recentDataSet;
    private Queue recentQueue;

    private final String FIRST_LAUNCHER = "first_louncher";
    private final String LAST_NOTE = "last_note";
    private final String IS_ONLY_LANDSCAPE = "isOnlyLandscape";
    private final String IS_GUIDE_SKIP = "isGuideSkip";
    private final String RECENT_DATA_SET = "recentData";
    private final String RECENT_DATA_SIZE = "recentDataSize";

    private SharedPreferencesManager(Context context) {
        mContext = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        recentQueue = new LinkedList();
        recentDataSet = (HashSet)preferences.getStringSet(RECENT_DATA_SET, new HashSet<String>());
        Iterator<String> it = recentDataSet.iterator();
        while(it.hasNext()) {
            recentQueue.add(it.next());
        }

    }

    public static SharedPreferencesManager getInstance(Context context){
        if(instance == null)
            instance = new SharedPreferencesManager(context);
        return instance;
    }

    public void setGuideSkip(boolean skip){
        editor.putBoolean(IS_GUIDE_SKIP, skip);
        editor.commit();
    }
    public boolean getGuideSkip(){ return preferences.getBoolean(IS_GUIDE_SKIP, false) ; }

    public void setFirstLauncher(boolean launcher){
        editor.putBoolean(FIRST_LAUNCHER, launcher);
        editor.putInt(RECENT_DATA_SIZE, recentDataSize);
        editor.commit();
    }

    public boolean getFristLauncher(){
        return preferences.getBoolean(FIRST_LAUNCHER, true);
    }

    public void setLastNote(String noteName){
        editor.putString(LAST_NOTE, noteName);
        editor.commit();
    }

    public String getLastNote(){
        return preferences.getString(LAST_NOTE, null);
    }


    public void setOnlyLandscape(boolean landscape){
        editor.putBoolean(IS_ONLY_LANDSCAPE, landscape);
        editor.commit();
    }

    public boolean getOnlyLandscape(){ return preferences.getBoolean(IS_ONLY_LANDSCAPE, false); }

    public boolean addRecentData(String noteId)
    {
        //preference의 set은 불변성을 가진다. 종료시 처음만들때의 1개의 셋으로 돌아가므로 카피본을 만들어야 함.
        HashSet<String> tempSet = new HashSet<>();

        recentQueue.add(noteId);
        if(recentQueue.size() > 5)
            recentQueue.poll();

        Queue tempQueue = new LinkedList(recentQueue);
        while(tempQueue.peek() != null){
            tempSet.add((String) tempQueue.poll());
        }
        editor.putStringSet(RECENT_DATA_SET, tempSet);
        editor.apply();

        return true;
    }

    public void setRecentDataSize(int size){
        recentDataSize = size;
        editor.putInt(RECENT_DATA_SIZE, size);
        editor.commit();
    }

    public int getRecentDataSzie(){
        return recentDataSize;
    }

    public void deleteOldViewersData() {
        //\Android\data\com.knowrecorder\files\packageName\viewers의 데이터를 어느정도 까지 지울지.
        //다 남기느냐 혹은 recent리스트에 남긴정도만 남기느냐 차이.
        //일단 다 남겨도 되는 이유는 기본과 달리 애플리캐이션정보의 데이터 삭제로 유저가 컨트롤 할 수 있기 때문에,
        //아래코드를 막으면 다 남긴다.

        recentDataSet = (HashSet)preferences.getStringSet(RECENT_DATA_SET, new HashSet<String>());
        File viewerDir = new File(FilePath.VIEWER_DIRECTORY);
        //폴더 구조가 viewer/videoid로 확실한 경우로 가정한 코드.
        if(viewerDir.exists()){
            for(File child : viewerDir.listFiles()){
                int length = child.toString().split("/").length;
                String videoDir  = child.toString().split("/")[length - 1];
                if(!recentDataSet.contains(videoDir)){
                    FilePath.deleteRecursive(child);
                }
            }
        }


    }
}
