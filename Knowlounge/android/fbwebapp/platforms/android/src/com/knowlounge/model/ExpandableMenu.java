package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-14.
 */
public class ExpandableMenu {

    private String group_title;
    private int group_resource;
    private String menuTag;
    private boolean hasSubList;

    public ExpandableMenu(String group_title, int group_resource, String tag, boolean hasSubList) {
        this.group_title = group_title;
        this.group_resource = group_resource;
        this.menuTag = tag;
        this.hasSubList = hasSubList;
    }

    public void setGroup_title(String title){this.group_title = title;}
    public void setGroup_resource(int group_resource){this.group_resource = group_resource;}
    public String getGroup_title(){return this.group_title;}
    public int getGroup_resource(){return this.group_resource;}
    public String getMenuTag() {
        return this.menuTag;
    }
    public boolean isHasSubList() {
        return this.hasSubList;
    }

}
