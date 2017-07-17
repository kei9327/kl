package com.knowrecorder.Managers;

/**
 * Created by ssyou on 2016-03-21.
 */
public class NoteManager {
    public static NoteManager instance = new NoteManager();
    private int noteId = 1;
    private String identifier;

    public static NoteManager getInstance() {
        return instance;
    }

    public void getAllNotes() {
    }

    public void initNewNote() {

    }

    public void setNoteResolution(){

    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
