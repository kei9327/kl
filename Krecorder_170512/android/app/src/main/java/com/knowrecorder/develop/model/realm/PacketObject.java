package com.knowrecorder.develop.model.realm;

import com.knowrecorder.develop.model.realmHoler.PacketObjectHolder;

import io.realm.RealmObject;

/**
 * Created by Changha on 2017-02-03.
 */

public class PacketObject extends RealmObject{
    private long id;
    private long mid;
    private long noteid;// todo 지워도 되는 컬럼
    private long pageid;
    private String type;
    private String body;
    private float runtime;
    private boolean isStatic;
    private boolean isPDFPage;
    private boolean isEditingMode;
    private boolean isAddPage; // todo 지워도 되는 컬럼
    private boolean isDrawn;// todo 지워도 되는 컬럼

    public long getId() {
        return id;
    }

    public long getMid() {
        return mid;
    }

    public long getNoteid() {
        return noteid;
    }

    public long getPageId() {
        return pageid;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public float getRunTime() {
        return runtime;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isPDFPage() {
        return isPDFPage;
    }

    public boolean isEditingMode() {
        return isEditingMode;
    }

    public boolean isAddPage() {
        return isAddPage;
    }

    public boolean isDrawn() {
        return isDrawn;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public void setNoteid(long noteid) {
        this.noteid = noteid;
    }

    public void setPageid(long pageId) {
        this.pageid = pageId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRunTime(float runTime) {
        this.runtime = runTime;
    }

    public void setStatic(boolean aStatic) {
        this.isStatic = aStatic;
    }

    public void setPdfPage(boolean pdfPage) {
        this.isPDFPage = pdfPage;
    }

    public void setEditingMode(boolean editingMode) {
        this.isEditingMode = editingMode;
    }

    public void setAddPage(boolean addPage) {
        isAddPage = addPage;
    }

    public void setDrawn(boolean drawn) {
        isDrawn = drawn;
    }

    public PacketObjectHolder clone(){
        PacketObjectHolder packet = new PacketObjectHolder.
                PacketObjectHolderBuilder(this.id, this.mid, this.pageid, this.type, this.body, this.runtime)
                .setIsStaticEnabled(this.isStatic)
                .setIsPdfPageEnabled(this.isPDFPage)
                .setIsEditingEnabled(this.isEditingMode)
                .build();
        return packet;
    }
}
