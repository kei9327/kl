package com.knowrecorder.develop.model.realm;

import com.knowrecorder.develop.model.realmHoler.TimeLineHolder;

import io.realm.RealmObject;

/**
 * Created by we160303 on 2017-02-03.
 */

public class TimeLine extends RealmObject{

    private long mid;
    private float startRun;
    private float endRun;
    private String type;
    private String remarks;

    public long getMid() {
        return mid;
    }

    public float getStartRun() {
        return startRun;
    }

    public float getEndRun() {
        return endRun;
    }

    public String getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public void setStartRun(float startRun) {
        this.startRun = startRun;
    }

    public void setEndRun(float endRun) {
        this.endRun = endRun;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public TimeLineHolder clone(){
        TimeLineHolder holder = new TimeLineHolder();

        holder.setMid(this.mid);
        holder.setStartRun(this.startRun);
        holder.setEndRun(this.endRun);
        holder.setType(this.type);
        holder.setRemarks(this.remarks);

        return holder;
    }
}
