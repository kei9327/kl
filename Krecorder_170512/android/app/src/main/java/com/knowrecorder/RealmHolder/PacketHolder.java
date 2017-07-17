package com.knowrecorder.RealmHolder;

/**
 * Created by ssyou on 2016-02-03.
 */
public class PacketHolder {
    private int id;
    private String command;
    private long timeStamp;
    private long timeStamp2 = 0;
    private long relativeTimeflow;
    private DrawingHolder drawingHolder = null;
    private ObjectHolder objectHolder = null;
    private TextHolder textHolder = null;
    private VideoHolder videoHolder = null;
    private int pageNumber;
    private int noteId;
    private boolean isRecord = false;
    private boolean isUndo = false;
    private boolean ignoreUndo = false;
    private int pageToJump;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public DrawingHolder getDrawingHolder() {
        return drawingHolder;
    }

    public void setDrawingHolder(DrawingHolder drawingHolder) {
        this.drawingHolder = drawingHolder;
    }

    public ObjectHolder getObjectHolder() {
        return objectHolder;
    }

    public void setObjectHolder(ObjectHolder objectHolder) {
        this.objectHolder = objectHolder;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public void setIsRecord(boolean isRecord) {
        this.isRecord = isRecord;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isUndo() {
        return isUndo;
    }

    public void setIsUndo(boolean isUndo) {
        this.isUndo = isUndo;
    }

    public boolean isIgnoreUndo() {
        return ignoreUndo;
    }

    public void setIgnoreUndo(boolean ignoreUndo) {
        this.ignoreUndo = ignoreUndo;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public long getTimeStamp2() {
        return timeStamp2;
    }

    public void setTimeStamp2(long timeStamp2) {
        this.timeStamp2 = timeStamp2;
    }

    public long getRelativeTimeflow() {
        return relativeTimeflow;
    }

    public void setRelativeTimeflow(long relativeTimeflow) {
        this.relativeTimeflow = relativeTimeflow;
    }

    public TextHolder getTextHolder() {
        return textHolder;
    }

    public void setTextHolder(TextHolder textHolder) {
        this.textHolder = textHolder;
    }

    public VideoHolder getVideoHolder() {
        return videoHolder;
    }

    public void setVideoHolder(VideoHolder videoHolder) {
        this.videoHolder = videoHolder;
    }

    public int getPageToJump() {
        return pageToJump;
    }

    public void setPageToJump(int pageToJump) {
        this.pageToJump = pageToJump;
    }

    public PacketHolder clone() {
        PacketHolder packetHolder = new PacketHolder();

        if (drawingHolder != null)
            packetHolder.setDrawingHolder(this.drawingHolder.clone());
        else
            packetHolder.setDrawingHolder(null);

        if (objectHolder != null)
            packetHolder.setObjectHolder(this.objectHolder.clone());
        else
            packetHolder.setObjectHolder(null);

        if (textHolder != null)
            packetHolder.setTextHolder(this.textHolder.clone());
        else
            packetHolder.setTextHolder(null);

        if (videoHolder != null)
            packetHolder.setVideoHolder(this.videoHolder.clone());
        else
            packetHolder.setVideoHolder(null);

        packetHolder.setId(id);
        packetHolder.setCommand(command);
        packetHolder.setTimeStamp(timeStamp);
        packetHolder.setTimeStamp2(timeStamp2);
        packetHolder.setRelativeTimeflow(relativeTimeflow);

        packetHolder.setObjectHolder(objectHolder);
        packetHolder.setTextHolder(textHolder);
        packetHolder.setVideoHolder(videoHolder);
        packetHolder.setPageNumber(pageNumber);
        packetHolder.setNoteId(noteId);
        packetHolder.setIsRecord(isRecord);
        packetHolder.setIsUndo(isUndo);
        packetHolder.setIgnoreUndo(ignoreUndo);
        packetHolder.setPageToJump(pageToJump);

        return packetHolder;
    }
}
