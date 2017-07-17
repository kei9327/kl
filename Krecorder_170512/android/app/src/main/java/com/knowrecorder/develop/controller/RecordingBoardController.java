package com.knowrecorder.develop.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.knowrecorder.RealmMigration.MigrationIOS;
import com.knowrecorder.Utils.PermissionChecker;
import com.knowrecorder.develop.audio.AudioPlayer;
import com.knowrecorder.develop.audio.AudioRecorder;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.NoteManager;
import com.knowrecorder.develop.manager.SharedPreferencesManager;
import com.knowrecorder.develop.model.NewNote;
import com.knowrecorder.develop.model.NoteInfo;
import com.knowrecorder.develop.model.realm.Note;
import com.knowrecorder.develop.model.realm.Page;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by we160303 on 2017-01-31.
 */

public class RecordingBoardController {
    private Context mContext;
    private NewNote mNote;
    private RealmConfiguration mainConfig;

    public RecordingBoardController(Context context) {
        this.mContext = context;
    }

    public void createNote() {
        String noteName;

        NewNote newNote = new NewNote(mContext);
        noteName = newNote.getNoteName();

        FilePath.setNoteFolder(noteName);

        //todo realm Configuration 및 realm에 Note 정보 넣기
        mainConfig = new RealmConfiguration.Builder()
                .directory(new File(FilePath.NOTE_FOLDER))
                .name("default2.realm")
                .schemaVersion(3)
                .migration(new MigrationIOS())
                .build();
        Realm.setDefaultConfiguration(mainConfig);

        Log.d("Realmfile", mainConfig.toString());

        Realm realm = Realm.getInstance(mainConfig);
        realm.beginTransaction();

        Note note = new Note();
        note.setTitle(newNote.getTitle());
        note.setNoteId(newNote.getId());
        note.setCreateDate(newNote.getCreateDate());
        note.setInfo(getNoteInfo());
        realm.copyToRealm(note);

        Page page = new Page();
        page.setId(1);
        page.setPagenum(1);
        realm.copyToRealm(page);

        realm.commitTransaction();

        NoteManager.getInstance().insertNote(newNote.getNoteName(), newNote.getTitle(), newNote.getCreateDate());

        //todo sharedpreference setLastNote
        SharedPreferencesManager.getInstance(mContext).setLastNote(noteName);
        AudioRecorder.setFileName(noteName);
        AudioPlayer.getInstance(mContext).setAudioPath(FilePath.FILES_DIRECTORY + noteName + AudioRecorder.AUDIO_RECORDER_FILE_EXT_WAV);

        mNote = newNote;
    }

    public void setNote(String noteName) {
        //todo setNoteFoler
        FilePath.setNoteFolder(noteName);

        //todo realm Configuration 및 realm에 Note 정보 넣기
        mainConfig = new RealmConfiguration.Builder()
                             .assetFile(FilePath.NOTE_FOLDER+"default2.realm")
                             .directory(new File(FilePath.NOTE_FOLDER))
                .name("default2.realm")
                .schemaVersion(3)
                .migration(new MigrationIOS())
                .build();
        Realm.setDefaultConfiguration(mainConfig);

        Log.d("Realmfile",mainConfig.toString());

        File test = new File(FilePath.NOTE_FOLDER+"default2.realm");

        Realm realm = Realm.getInstance(mainConfig);
        Note note = realm.where(Note.class).findFirst();
        mNote = new NewNote(note.getNoteId(), noteName, note.getCreateDate(), note.getTitle());
        realm.close();

        //todo sharedpreference setLastNote
        SharedPreferencesManager.getInstance(mContext).setLastNote(noteName);
        AudioRecorder.setFileName(noteName);
        AudioPlayer.getInstance(mContext).setAudioPath(FilePath.FILES_DIRECTORY + noteName + AudioRecorder.AUDIO_RECORDER_FILE_EXT_WAV);

    }

    public void currentInitNote() {
        String lastNote = SharedPreferencesManager.getInstance(mContext).getLastNote();
        if (lastNote != null) {
            //todo note 셋팅
            setNote(lastNote);
        } else {
            //todo create Note
            createNote();
        }

    }

    public String getNoteInfo() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((AppCompatActivity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        NoteInfo noteInfo = new NoteInfo("android", metrics.widthPixels, (metrics.heightPixels-(45 * metrics.scaledDensity)), metrics.scaledDensity);

        return (new Gson()).toJson(noteInfo);
    }
    public void setPrevRealmAndAudioFile(){
        Realm.removeDefaultConfiguration();
        Realm.setDefaultConfiguration(mainConfig);

        AudioPlayer.getInstance(mContext).setAudioPath(FilePath.FILES_DIRECTORY + mNote.getNoteName() + AudioRecorder.AUDIO_RECORDER_FILE_EXT_WAV);
    }

    public NewNote getmNote(){ return mNote ;}
}
