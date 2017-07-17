package com.knowrecorder.Managers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by we160303 on 2017-01-16.
 */

public class PdfInfoManager {

    private static PdfInfoManager Instance;
    private JSONObject obj;

    private PdfInfoManager(){
        obj = new JSONObject();
    }

    public static PdfInfoManager getInstance(){
        if(Instance == null)
            Instance = new PdfInfoManager();
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
