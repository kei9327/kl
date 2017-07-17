package com.knowlounge.rxjava.message;

/**
 * Created by we160303 on 2016-10-24.
 */

public class MultiPageEvent {

    private int tag;

    public static final int ADD_PAGE = 0;
    public static final int DEL_PAGE = 1;
    public static final int CHANGE_PAGE = 2;
    public static final int ORDER_PAGE = 3;

    public static final int CURRENT_PAGE = 4;
    public static final int RELOAD_PAGE = 5;
    public static final int CHANGE_AUTH = 6;

    private String pageId;

    public MultiPageEvent(int tag, String data){
        this.tag = tag;
        this.pageId = data;
    }

    public int getTag() { return this.tag ;}
    public String getData() {return  this.pageId ; }

}
