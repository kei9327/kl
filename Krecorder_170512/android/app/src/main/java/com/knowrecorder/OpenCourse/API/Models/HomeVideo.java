package com.knowrecorder.OpenCourse.API.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class HomeVideo {

    @SerializedName("category_id")
    @Expose
    private String categoryId;

    @SerializedName("list")
    @Expose
    private Video[] list;

    public String getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    public Video[] getList() {
        return list;
    }
    public void setList(Video[] list) {
        this.list = list;
    }
}