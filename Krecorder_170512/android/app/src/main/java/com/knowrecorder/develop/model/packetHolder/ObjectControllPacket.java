package com.knowrecorder.develop.model.packetHolder;

/**
 * Created by we160303 on 2017-02-16.
 */

public class ObjectControllPacket {
    private String  type;
    private int action;
    private long target;
    private boolean grabbed;
    private float dx;
    private float dy;
    private float scale;
    private float videoprogress;
    private float volumeprogress;
    private float startvalue;
    private float endvalue;
    private String content;
    private int pageno;
    private long totalVideoTime;

    public String getType() {
        return type;
    }

    public int getAction() {
        return action;
    }

    public long getTarget() {
        return target;
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getScale() {
        return scale;
    }

    public float getVideoprogress() {
        return videoprogress;
    }

    public float getVolumeprogress() {
        return volumeprogress;
    }

    public String getContent() {
        return content;
    }

    public int getPageno() {
        return pageno;
    }

    public float getStartvalue() {
        return startvalue;
    }

    public float getEndvalue() {
        return endvalue;
    }

    public long getTotalVideoTime() {
        return totalVideoTime;
    }

    public ObjectControllPacket(ObjectControllPacketBuilder builder) {
        this.type = builder.type;
        this.action = builder.action;
        this.target = builder.target;
        this.grabbed = builder.grabbed;
        this.dx = builder.dx;
        this.dy = builder.dy;
        this.scale = builder.scale;
        this.videoprogress = builder.videoprogress;
        this.volumeprogress = builder.volumeprogress;
        this.content = builder.content;
        this.pageno = builder.pageno;
        this.startvalue = builder.startvalue;
        this.endvalue = builder.endvalue;
        this.totalVideoTime = builder.totalVideoTime;
    }

    public static class ObjectControllPacketBuilder {
        private String  type;
        private int action;
        private long target;
        private boolean grabbed;
        private float dx;
        private float dy;
        private float scale;
        private float videoprogress;
        private float volumeprogress;
        private float startvalue;
        private float endvalue;
        private String content;
        private int pageno;
        private long totalVideoTime;

        public ObjectControllPacketBuilder setType(String type){
            this.type = type;
            return this;
        }

        public ObjectControllPacketBuilder setAction(int action){
            this.action = action;
            return this;
        }

        public ObjectControllPacketBuilder setTarget(long target){
            this.target = target;
            return this;
        }

        public ObjectControllPacketBuilder setGrabbed(boolean grabbed) {
            this.grabbed = grabbed;
            return this;
        }

        public ObjectControllPacketBuilder setDX(float dx) {
            this.dx = dx;
            return this;
        }

        public ObjectControllPacketBuilder setDY(float dy) {
            this.dy = dy;
            return this;
        }

        public ObjectControllPacketBuilder setScale(float scale) {
            this.scale = scale;
            return this;
        }

        public ObjectControllPacketBuilder setVideoProgress(float videoprogress){
            this.videoprogress = videoprogress;
            return this;
        }

        public ObjectControllPacketBuilder setVolumeProgress(float volumeprogress){
            this.volumeprogress = volumeprogress;
            return this;
        }

        public ObjectControllPacketBuilder setContent(String content){
            this.content = content;
            return this;
        }

        public ObjectControllPacketBuilder setPageNo(int pageno){
            this.pageno = pageno;
            return this;
        }

        public ObjectControllPacketBuilder setStartValue(float startvalue){
            this.startvalue= startvalue;
            return this;
        }

        public ObjectControllPacketBuilder setEndValue(float endvalue){
            this.endvalue = endvalue;
            return this;
        }

        public ObjectControllPacketBuilder setTotalVideoTime(long totalVideoTime){
            this.totalVideoTime = totalVideoTime;
            return this;
        }

        public ObjectControllPacket build() {
            return new ObjectControllPacket(this);
        }
    }

}
