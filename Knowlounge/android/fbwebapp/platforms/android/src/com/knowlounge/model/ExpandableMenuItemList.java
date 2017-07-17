package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-14.
 */
public class ExpandableMenuItemList {


    private String group_item_title;
    private int group_item_resource;

    public ExpandableMenuItemList(String group_item_title) { this.group_item_title = group_item_title;}

    public String getGroup_item_title(){return this.group_item_title;}
    public int getGroup_item_resource(){return this.group_item_resource;}

}
