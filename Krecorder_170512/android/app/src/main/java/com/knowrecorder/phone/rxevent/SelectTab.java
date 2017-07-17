package com.knowrecorder.phone.rxevent;

import com.knowrecorder.rxjava.RxEvent;

/**
 * Created by we160303 on 2016-12-06.
 */

public class SelectTab extends RxEvent {

    private int tab;
    private int subject;
    public SelectTab() {
        super("");
    }

    public SelectTab(String tag) {
        super(tag);
    }

    public void setTab(int tab){
        this.tab = tab;
    }
    public int getTab(){
        return this.tab;
    }

    public void setSubject(int subject) { this.subject = subject ;}
    public int getSubject() { return this.subject ;}
}
