package com.knowrecorder.develop.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.rxjava.RxEventFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by we160303 on 2017-03-08.
 */

public class AudioPlayer {
    private static AudioPlayer instance = null;
    private Context mContext;

    private String audioPath = null;
    private int initTime = 0;
    private int playTime = 0;
    private int duration;


    private MediaPlayer mediaPlayer;


    public AudioPlayer(Context context) {
        this.mContext = context;
    }

    public static AudioPlayer getInstance(Context context) {
        if (instance == null)
            instance = new AudioPlayer(context);
        return instance;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public void setInitTime(int initTime) {
        this.initTime = initTime;
    }
    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public int getPlayTime() {
        return playTime;
    }

    public int getDuration() {
        return duration;
    }
    public int prevGetDuration() {
        try {
            if (audioPath == null)
                return 0;

            File file = new File(audioPath);
            if (!file.exists())
                return 0;

            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(mContext, Uri.fromFile(file));
            player.prepare();
            int duration = player.getDuration();
            Log.d("AudioPlayer"," Duration : " +duration );
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getCurrentPosition(){
        if(mediaPlayer == null)
            return -1;
        return mediaPlayer.getCurrentPosition();
    }
    public void startPlay() throws IOException {

        if (audioPath == null)
            throw new NullPointerException();

        File file = new File(audioPath);
        if (!file.exists())
            throw new FileNotFoundException();

        if (mediaPlayer != null) {
            //stop play
            stopPlay();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(mContext, Uri.fromFile(file));
        mediaPlayer.prepare();

        if (initTime > mediaPlayer.getDuration() || initTime < 0)
            throw new IllegalArgumentException();

        duration = mediaPlayer.getDuration();
        Log.d("AudioPlayer"," Duration : " +duration );

        mediaPlayer.seekTo(initTime);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                RxEventFactory.get().post(new EventType(EventType.PLAYER_END));
            }
        });

    }

    public void stopPlay() {
        playTime = mediaPlayer.getCurrentPosition();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}


