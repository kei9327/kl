package com.knowrecorder.develop.model.packetHolder;

/**
 * Created by we160303 on 2017-02-16.
 */

public class ObjectCreatePacket {
    private String  type;
    private int action;
    private float originX;
    private float originY;
    private float endX;
    private float endY;
    private float scale;
    private float w;
    private float h;
    private float videoprogress;
    private float volume;
    private int pdfpageno;
    private String filename;
    //todo text에 관련된 값

    public String getType() {
        return type;
    }

    public int getAction() {
        return action;
    }

    public float getOriginX() {
        return originX;
    }

    public float getOriginY() {
        return originY;
    }

    public float getEndX() {
        return endX;
    }

    public float getEndY() {
        return endY;
    }

    public float getW() {
        return w;
    }

    public float getH() {
        return h;
    }

    public float getScale() {
        return scale;
    }

    public float getVideoprogress() {
        return videoprogress;
    }

    public float getVolume() {
        return volume;
    }

    public int getPdfpageno() {
        return pdfpageno;
    }

    public String getFilename() {
        return filename;
    }


    public ObjectCreatePacket(ObjectCreatePacketBuilder builder) {
        this.type = builder.type;
        this.action = builder.action;
        this.originX = builder.originX;
        this.originY = builder.originY;
        this.endX = builder.endX;
        this.endY = builder.endY;
        this.w = builder.w;
        this.h = builder.h;
        this.scale = builder.scale;
        this.videoprogress = builder.videoprogress;
        this.volume = builder.volume;
        this.pdfpageno = builder.pdfpageno;
        this.filename = builder.filename;
    }

    public static class ObjectCreatePacketBuilder {
        private String type;
        private int action;
        private float originX;
        private float originY;
        private float endX;
        private float endY;
        private float w;
        private float h;
        private float scale;
        private float videoprogress;
        private float volume;
        private int pdfpageno;
        private String filename;
        private long paperid;

        public ObjectCreatePacketBuilder setType(String type){
            this.type = type;
            return this;
        }

        public ObjectCreatePacketBuilder setAction(int action){
            this.action = action;
            return this;
        }

        public ObjectCreatePacketBuilder setOriginX(float originX){
            this.originX = originX;
            return this;
        }

        public ObjectCreatePacketBuilder setOriginY(float originY){
            this.originY = originY;
            return this;
        }

        public ObjectCreatePacketBuilder setEndX(float endX){
            this.endX= endX;
            return this;
        }

        public ObjectCreatePacketBuilder setEndY(float endY){
            this.endY= endY;
            return this;
        }

        public ObjectCreatePacketBuilder setW(float w){
            this.w = w;
            return this;
        }

        public ObjectCreatePacketBuilder setH(float h){
            this.h  = h;
            return this;
        }

        public ObjectCreatePacketBuilder setScale(float scale){
            this.scale= scale;
            return this;
        }

        public ObjectCreatePacketBuilder setVideoProgress(float videoprogress){
            this.videoprogress = videoprogress;
            return this;
        }

        public ObjectCreatePacketBuilder setVolume(float volume){
            this.volume = volume;
            return this;
        }

        public ObjectCreatePacketBuilder setPdfPageNo(int pdfpageno){
            this.pdfpageno = pdfpageno;
            return this;
        }

        public ObjectCreatePacketBuilder setFileName(String filename){
            this.filename = filename;
            return this;
        }

        public ObjectCreatePacket build() {
            return new ObjectCreatePacket(this);
        }
    }


}
