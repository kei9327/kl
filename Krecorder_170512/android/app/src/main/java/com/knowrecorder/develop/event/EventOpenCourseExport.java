package com.knowrecorder.develop.event;

/**
 * Created by we160303 on 2017-03-23.
 */

public class EventOpenCourseExport {
    private String title;
    private int categoryId;

    public EventOpenCourseExport(String title, int categoryId) {
        this.title = title;
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public int getCategoryId() {
        return categoryId;
    }
}
