package com.knowlounge.youtube.model;

/**
 * Created by we160303 on 2016-09-28.
 */
public class YouTubeModel {
    private String channelTitle;
    private String videoId;
    private String title;
    private String url;
    private String publishedAt;

    public YouTubeModel(String videoId, String title, String url, String publishedAt, String channelTitle){

        this.videoId = videoId;
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
        this.channelTitle = channelTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getChannelTitle() { return this.channelTitle; }

    public void setChannelTitle(String chanelTitle) { this.channelTitle = chanelTitle; }

}
