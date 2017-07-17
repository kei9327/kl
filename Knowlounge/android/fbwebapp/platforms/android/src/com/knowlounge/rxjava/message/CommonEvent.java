package com.knowlounge.rxjava.message;

/**
 * Created by we160303 on 2016-10-24.
 */

public class CommonEvent {

    private int tag;
    private boolean result;

    public static final int TEACHER_ONLY_CAM = 1;

    public CommonEvent(int tag, boolean result){
        this.tag = tag;
        this.result = result;
    }

    public int getTag() { return this.tag ; }
    public boolean getResult() {return  this.result ; }

}
