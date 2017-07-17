package com.knowrecorder.Managers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by we160303 on 2016-12-08.
 */

public class NoteInfoManager {
    private static NoteInfoManager Instance;
    private JSONObject obj;

    private NoteInfoManager(){
        obj = new JSONObject();
    }

    public static NoteInfoManager getInstance(){
        if(Instance == null)
            Instance = new NoteInfoManager();
        return Instance;
    }

    public void appendInfo(String key, String value){
        Log.d("KnowRecorder",key+","+value);
        try {
            if (!obj.has(key))
                obj.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getNoteInfoJSON(){
        return obj.toString();
    }

}
