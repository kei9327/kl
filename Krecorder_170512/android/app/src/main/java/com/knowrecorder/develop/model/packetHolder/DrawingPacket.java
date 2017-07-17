package com.knowrecorder.develop.model.packetHolder;

import java.util.ArrayList;

/**
 * Created by we160303 on 2017-02-16.
 */

public class DrawingPacket {
    private String  type;
    private int action;
    private ArrayList<float[]> points;
    private float x;
    private float y;

    //todo text에 관련된 값


    public String getType() {
        return type;
    }

    public int getAction() {
        return action;
    }

    public ArrayList<float[]> getPoints() {
        return points;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public DrawingPacket(DrawingPacketBuilder builder) {
        this.type = builder.type;
        this.action = builder.action;
        this.points = builder.points;
        this.x = builder.x;
        this.y = builder.y;
    }

    public static class DrawingPacketBuilder {
        private String  type;
        private int action;
        private ArrayList<float[]> points;
        private float x;
        private float y;

        public DrawingPacketBuilder setType(String type){
            this.type = type;
            return this;
        }
        public DrawingPacketBuilder setAction(int action){
            this.action = action;
            return this;
        }
        public DrawingPacketBuilder setPoints(ArrayList<float[]> points){
            this.points = points;
            return this;
        }
        public DrawingPacketBuilder setX(float x){
            this.x = x;
            return this;
        }
        public DrawingPacketBuilder setY(float y){
            this.y = y;
            return this;
        }

        public DrawingPacket build() {
            return new DrawingPacket(this);
        }
    }

}
