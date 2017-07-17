package com.knowrecorder.develop;

import android.content.Context;

import com.knowrecorder.develop.audio.AudioPlayer;

/**
 * Created by Changha on 2017-02-03.
 */

public class ProcessStateModel  {

    private static ProcessStateModel instance = null;

    private boolean fileCopyState = false;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isRemodeling = false;
    private boolean isPageLoading = false;

    private long recordingStartTime = 0;
    private long lastRecordingTime = 0;

    private boolean isToolPopup = false;

    private ProcessStateModel() {}

    public static ProcessStateModel getInstanse(){
        if(instance == null)
            instance = new ProcessStateModel();

        return instance;
    }
    public void setFileCopyState(boolean fileCopyState) { this.fileCopyState = fileCopyState; }
    public boolean isFileCopyState(){ return fileCopyState; }

    public void setIsRecording(boolean isRecording){ this.isRecording = isRecording; }
    public boolean isRecording(){ return this.isRecording ; }

    public void setIsRemodeling(boolean isRemodeling) { this.isRemodeling = isRemodeling ; }
    public boolean isRemodeling() { return this.isRemodeling ; }

    public void setIsPlaying( boolean isPlaying){ this.isPlaying = isPlaying ; }
    public boolean isPlaying(){ return this.isPlaying ; }

    public void setIsPageLoading(boolean isPageLoading) { this.isPageLoading = isPageLoading ;}
    public boolean isPageLoading(){ return this.isPageLoading ; }

    public void setLastRecordingTime(long lastRecordingTime){ this.lastRecordingTime = lastRecordingTime;}

    public void startRecording(){
        recordingStartTime = System.currentTimeMillis();
    }
    public void endRecording(long endTime){
        lastRecordingTime = endTime;
    }

    public long getElapsedTime(){
        if(isRecording) {
            return System.currentTimeMillis() - recordingStartTime + lastRecordingTime;
        }else{
            return lastRecordingTime;
        }
    }

    public void setToolPopup(boolean toolPopup){ this.isToolPopup = toolPopup; }
    public boolean isToolPopupShown() { return this.isToolPopup ; }
}
