package com.knowrecorder.develop.manager;

import android.text.TextUtils;

import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.model.realm.Notes;
import com.knowrecorder.develop.model.realmHoler.NotesHolder;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by we160303 on 2017-03-29.
 */

public class NoteManager {

    private static NoteManager instance = null;
    private RealmConfiguration realmConfiguration;

    public NoteManager() {
        if(!existRealmFile()){
            realmConfiguration = new RealmConfiguration.Builder()
                    .directory(new File(FilePath.NOTE_DIRECTORY))
                    .name("noteManager.realm")
                    .build();
        }else{
            realmConfiguration = new RealmConfiguration.Builder()
                    .assetFile(FilePath.NOTE_DIRECTORY+"noteManager.realm")
                    .directory(new File(FilePath.NOTE_DIRECTORY))
                    .name("noteManager.realm")
                    .build();
        }
    }

    private boolean existRealmFile() {
        File realmFile = new File(FilePath.NOTE_DIRECTORY+"noteManager.realm");
        return realmFile.exists();
    }

    public static NoteManager getInstance(){
        if(instance == null)
            instance = new NoteManager();
        return instance;
    }

    public void insertNote(String noteName, String title, String createDate){
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.beginTransaction();

        Notes note = new Notes();
            note.setNoteName(noteName);
            note.setTotaltime(0);
            note.setTitle(title);
            note.setCreateDate(createDate);
        realm.copyToRealm(note);

        realm.commitTransaction();
        realm.close();
    }
    public void deleteNote(String noteName){
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.beginTransaction();

        Notes note = realm.where(Notes.class).equalTo("noteName", noteName).findFirst();
        note.deleteFromRealm();

        realm.commitTransaction();
        realm.close();

    }

    public void updateTotalTimeInRealm(String noteName, float totalTime){
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.beginTransaction();

        Notes note = realm.where(Notes.class).equalTo("noteName", noteName).findFirst();
        note.setTotaltime(totalTime);
        realm.copyToRealm(note);

        realm.commitTransaction();
        realm.close();
    }

    public void updateTitleInRealm(String noteName, String title){
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.beginTransaction();

        Notes note = realm.where(Notes.class).equalTo("noteName", noteName).findFirst();
        note.setTitle(title);
        realm.copyToRealm(note);

        realm.commitTransaction();
        realm.close();
    }

    public String toString(String noteName){
        StringBuilder result = new StringBuilder();
        Realm realm = Realm.getInstance(realmConfiguration);

        Notes note = realm.where(Notes.class).equalTo("noteName", noteName).findFirst();
        result.append(" NoteName : ");
        result.append(note.getNoteName());
        result.append(" NoteTitle : ");
        result.append(note.getTitle());
        result.append(" TotalTime : ");
        result.append(note.getTotaltime());
        result.append(" NoteCreateDate : ");
        result.append(note.getCreateDate());
        realm.close();

        return result.toString();
    }

    public ArrayList<NotesHolder> getAllNote(){
        ArrayList<NotesHolder> result = new ArrayList<>();
        Realm realm = Realm.getInstance(realmConfiguration);

        RealmResults<Notes> notes = realm.where(Notes.class).findAll();
        for(Notes data : notes)
            result.add(data.clone());

        realm.close();
        return result;
    }

}
