package com.knowrecorder.develop.model.realmHoler;

import com.knowrecorder.develop.model.realm.PacketObject;

/**
 * Created by Changha on 2017-02-03.
 */

public class PacketObjectHolder {
    private long id;
    private long mid;
    private long pageId;
    private String type;
    private String body;
    private float runTime;
    private boolean isStatic;
    private boolean isPdfPage;
    private boolean isEditing;

    public long getId() {
        return id;
    }

    public long getMid() {
        return mid;
    }

    public long getPageId() {
        return pageId;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public float getRunTime() {
        return runTime;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isPdfPage() {
        return isPdfPage;
    }

    public boolean isEditing() {
        return isEditing;
    }

    private PacketObjectHolder(com.knowrecorder.develop.model.realmHoler.PacketObjectHolder.PacketObjectHolderBuilder builder){
        this.id = builder.id;
        this.mid = builder.mid;
        this.pageId = builder.pageId;
        this.type = builder.type;
        this.body = builder.body;
        this.runTime = builder.runTime;
        this.isStatic = builder.isStatic;
        this.isPdfPage = builder.isPdfPage;
        this.isEditing = builder.isEditing;
    }

    public PacketObject clone(){
        PacketObject packetObject = new PacketObject();
        packetObject.setId(this.id);
        packetObject.setMid(this.mid);
        packetObject.setPageid(this.pageId);
        packetObject.setType(this.type);
        packetObject.setBody(this.body);
        packetObject.setRunTime(this.runTime);
        packetObject.setStatic(this.isStatic);
        packetObject.setPdfPage(this.isPdfPage);
        packetObject.setEditingMode(this.isEditing);

        return packetObject;
    }


    public static class PacketObjectHolderBuilder {
        private long id;
        private long mid;
        private long pageId;
        private String type;
        private String body;
        private float runTime;
        private boolean isStatic;
        private boolean isPdfPage;
        private boolean isEditing;

        public PacketObjectHolderBuilder(long id, long mid, long pageId, String type, String body, float runTime){
            this.id = id;
            this.mid = mid;
            this.pageId = pageId;
            this.type = type;
            this.body = body;
            this.runTime = runTime;
            this.isStatic = false;
            this.isPdfPage = false;
            this.isEditing = false;
        }

        public com.knowrecorder.develop.model.realmHoler.PacketObjectHolder.PacketObjectHolderBuilder setIsStaticEnabled(boolean isStatic){
            this.isStatic = isStatic;
            return this;
        }

        public com.knowrecorder.develop.model.realmHoler.PacketObjectHolder.PacketObjectHolderBuilder setIsPdfPageEnabled(boolean isPdfPage){
            this.isPdfPage = isPdfPage;
            return this;
        }

        public com.knowrecorder.develop.model.realmHoler.PacketObjectHolder.PacketObjectHolderBuilder setIsEditingEnabled(boolean isEditing){
            this.isEditing = isEditing;
            return this;
        }

        public com.knowrecorder.develop.model.realmHoler.PacketObjectHolder build(){
            return new com.knowrecorder.develop.model.realmHoler.PacketObjectHolder(this);
        }
    }
}
