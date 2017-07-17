package com.knowrecorder.phone.tab.model;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-11-30.
 */

public class SectionList {
    private String sectionTitle;
    private int sectionCategory;
    private ArrayList<VideoData> sectionItemList;

    public SectionList() {
    }

    public SectionList(String sectionTitle, ArrayList<VideoData> sectionItemList, int sectionCategory) {
        this.sectionTitle = sectionTitle;
        this.sectionItemList = sectionItemList;
        this.sectionCategory = sectionCategory;
    }

    public void setSectionTitle(String sectionTitle){
        this.sectionTitle = sectionTitle;
    }
    public String getSectionTitle (){
        return this.sectionTitle;
    }

    public void setSectionItemList(ArrayList<VideoData> sectionItemList){
        this.sectionItemList = sectionItemList;
    }
    public ArrayList<VideoData> getSectionItemList() {
        return this.sectionItemList;
    }

    public void setSectionCategory(int category){ this.sectionCategory = category ;}
    public int getSectionCategory() { return this.sectionCategory ;}
}
