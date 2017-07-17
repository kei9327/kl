package com.knowrecorder.phone.tab.model;

/**
 * Created by we160303 on 2016-12-01.
 */

public class DeepLink {

    private String deepLinkThumbnail="";
    private String videoId="";
    private int defaultImage;

    public DeepLink(int defaultImage) {
        this.defaultImage = defaultImage;
    }

    public DeepLink(String deepLinkThumbnail, String videoId) {
        this.deepLinkThumbnail = deepLinkThumbnail;
        this.videoId = videoId;
    }

    public String getDeepLinkThumbnail(){return this.deepLinkThumbnail; }
    public String getVideoId(){return this.videoId; }
    public int getDefaultImage(){return this.defaultImage; }
}
