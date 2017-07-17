package com.knowrecorder.OpenCourse.API.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class UploadVideo {

    @SerializedName("category_id")
    @Expose
    private int categoryId;
    @SerializedName("playtime")
    @Expose
    private int playtime;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("filesize")
    @Expose
    private int filesize;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("visible")
    @Expose
    private int visible;
    @SerializedName("sns_type")
    @Expose
    private String snsType;
    @SerializedName("lang")
    @Expose
    private String lang;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("tags")
    @Expose
    private List<Object> tags = new ArrayList<Object>();

    /**
     *
     * @return
     * The categoryId
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     *
     * @param categoryId
     * The category_id
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     *
     * @return
     * The playtime
     */
    public int getPlaytime() {
        return playtime;
    }

    /**
     *
     * @param playtime
     * The playtime
     */
    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The filesize
     */
    public int getFilesize() {
        return filesize;
    }

    /**
     *
     * @param filesize
     * The filesize
     */
    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     * The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     * @return
     * The platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     *
     * @param platform
     * The platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     *
     * @return
     * The visible
     */
    public int getVisible() {
        return visible;
    }

    /**
     *
     * @param visible
     * The visible
     */
    public void setVisible(int visible) {
        this.visible = visible;
    }

    /**
     *
     * @return
     * The snsType
     */
    public String getSnsType() {
        return snsType;
    }

    /**
     *
     * @param snsType
     * The sns_type
     */
    public void setSnsType(String snsType) {
        this.snsType = snsType;
    }

    /**
     *
     * @return
     * The lang
     */
    public String getLang() {
        return lang;
    }

    /**
     *
     * @param snsType
     * The lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     *
     * @return
     * The country
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param snsType
     * The sns_type
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     * @return
     * The tags
     */
    public List<Object> getTags() {
        return tags;
    }

    /**
     *
     * @param tags
     * The tags
     */
    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

}