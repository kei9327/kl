package com.knowrecorder.OpenCourse.API.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class VideoUploadResponse {

    @SerializedName("archive_upload_url")
    @Expose
    private String archiveUploadUrl;
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("thumbnail_upload_url")
    @Expose
    private String thumbnailUploadUrl;

    /**
     *
     * @return
     * The archiveUploadUrl
     */
    public String getArchiveUploadUrl() {
        return archiveUploadUrl;
    }

    /**
     *
     * @param archiveUploadUrl
     * The archive_upload_url
     */
    public void setArchiveUploadUrl(String archiveUploadUrl) {
        this.archiveUploadUrl = archiveUploadUrl;
    }

    /**
     *
     * @return
     * The id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The thumbnailUploadUrl
     */
    public String getThumbnailUploadUrl() {
        return thumbnailUploadUrl;
    }

    /**
     *
     * @param thumbnailUploadUrl
     * The thumbnail_upload_url
     */
    public void setThumbnailUploadUrl(String thumbnailUploadUrl) {
        this.thumbnailUploadUrl = thumbnailUploadUrl;
    }

}