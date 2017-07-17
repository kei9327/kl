package com.knowrecorder.OpenCourse.Audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ssyou on 2016-10-14.
 */

public class OCAudioPlayer {
    private String audioPath = null;
    private int initTime = 0;
    private int playTime = 0;
    private int duration = 0;
    private MediaPlayer mediaPlayer;
    private Context context;

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public void setInitTime(int initTime) {
        this.initTime = initTime;
    }

    public void startPlay() throws IOException {
        if (audioPath == null)
            throw new NullPointerException();

        File file = new File(audioPath);
        if (!file.exists())
            throw new FileNotFoundException();

        if (mediaPlayer != null)
            stopPlay();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(context, Uri.fromFile(file));
        mediaPlayer.prepare();

        if (initTime > mediaPlayer.getDuration() || initTime < 0)
            throw new IllegalArgumentException();

        duration = mediaPlayer.getDuration();
        mediaPlayer.seekTo(initTime);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playTime = 0;
            }
        });
    }

    public void stopPlay() {
        playTime = mediaPlayer.getCurrentPosition();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public int getPlayPosition() {
        if (mediaPlayer == null)
            throw new NullPointerException();

        return mediaPlayer.getCurrentPosition() > mediaPlayer.getDuration() ? mediaPlayer.getDuration() : mediaPlayer.getCurrentPosition();
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getDuration() {
        return getDuration(audioPath);
    }

    public int getDuration(String audioPath) {
        if (audioPath == null)
            return 0;

        File file = new File(audioPath);
        if (!file.exists())
            return 0;

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, Uri.fromFile(file));
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mmr.release();
        return Integer.parseInt(durationStr);
    }



    public void setDuration(int duration) {
        this.duration = duration;
    }
}
