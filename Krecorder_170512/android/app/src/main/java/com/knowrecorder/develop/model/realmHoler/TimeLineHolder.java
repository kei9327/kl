package com.knowrecorder.develop.model.realmHoler;


/**
 * Created by we160303 on 2017-02-03.
 */

public class TimeLineHolder{

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
}
