package com.knowrecorder.develop.event;

/**
 * Created by we160303 on 2017-02-06.
 */

public class BringToFrontView {
    int viewType;

    public BringToFrontView(int viewType) {
        this.viewType = viewType;
    }

    public int getViewType(){
        return this.viewType;
    }
}
