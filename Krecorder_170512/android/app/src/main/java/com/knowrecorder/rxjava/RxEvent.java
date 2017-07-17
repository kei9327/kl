package com.knowrecorder.rxjava;

/**
 * Created by we160303 on 2016-12-06.
 */

public abstract class RxEvent {
    private String tag;

    public RxEvent(String tag){
        this.tag = tag;
    }

    public String getTag(){ return this.tag; }
}