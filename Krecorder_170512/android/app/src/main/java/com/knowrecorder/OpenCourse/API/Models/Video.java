package com.knowrecorder.OpenCourse.API.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Video {

    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("hits")
    @Expose
    private Integer hits;
    @SerializedName("archive_file")
    @Expose
    private String archiveFile;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("last_modified_date")
    @Expose
    private String lastModifiedDate;
    @SerializedName("visible")
    @Expose
    private Integer visible;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("author_thumbnail")
    @Expose
    private String authorThumbnail;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("filesize")
    @Expose
    private Integer filesize;
    @SerializedName("published_date")
    @Expose
    private String publishedDate;
    @SerializedName("playtime")
    @Expose
    private Integer playtime;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("sharecode")
    @Expose
    private String sharecode;
    @SerializedName("accumulated")
    @Expose
    private Integer accumulated;
    @SerializedName("totalcnt")
    @Expose
    private Integer totalcnt;
    @SerializedName("lang")
    @Expose
    private String lang;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("author_id")
    @Expose
    private String author_id;

    public Integer getAccumulated() {
        return accumulated;
    }

    public void setAccumulated(Integer accumulated) {
        this.accumulated = accumulated;
    }

    public Integer getTotalcnt() {
        return totalcnt;
    }

    public void setTotalcnt(Integer totalcnt) {
        this.totalcnt = totalcnt;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    /**
     *
     * @return
     * The category
     */
    public String getCategory() {
        return category;
    }

    /**
     *
     * @param category
     * The category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     *
     * @return
     * The hits
     */
    public Integer getHits() {
        return hits;
    }

    /**
     *
     * @param hits
     * The hits
     */
    public void setHits(Integer hits) {
        this.hits = hits;
    }

    /**
     *
     * @return
     * The archiveFile
     */
    public String getArchiveFile() {
        return archiveFile;
    }

    /**
     *
     * @param archiveFile
     * The archive_file
     */
    public void setArchiveFile(String archiveFile) {
        this.archiveFile = archiveFile;
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
     * The author
     */
    public String getAuthor() {
        return author;
    }

    /**
     *
     * @param author
     * The author
     */
    public void setAuthor(String author) {
        this.author = author;
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
     * The lastModifiedDate
     */
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     *
     * @param lastModifiedDate
     * The last_modified_date
     */
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     *
     * @return
     * The visible
     */
    public Integer getVisible() {
        return visible;
    }

    /**
     *
     * @param visible
     * The visible
     */
    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     *
     * @return
     * The thumbnail
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     *
     * @param thumbnail
     * The thumbnail
     */
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     *
     * @return
     * The authorThumbnail
     */
    public String getAuthorThumbnail() {
        return authorThumbnail;
    }

    /**
     *
     * @param authorThumbnail
     * The author_thumbnail
     */
    public void setAuthorThumbnail(String authorThumbnail) {
        this.authorThumbnail = authorThumbnail;
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
     * The filesize
     */
    public Integer getFilesize() {
        return filesize;
    }

    /**
     *
     * @param filesize
     * The filesize
     */
    public void setFilesize(Integer filesize) {
        this.filesize = filesize;
    }

    /**
     *
     * @return
     * The publishedDate
     */
    public String getPublishedDate() {
        return publishedDate;
    }

    /**
     *
     * @param publishedDate
     * The published_date
     */
    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    /**
     *
     * @return
     * The playtime
     */
    public Integer getPlaytime() {
        return playtime;
    }

    /**
     *
     * @param playtime
     * The playtime
     */
    public void setPlaytime(Integer playtime) {
        this.playtime = playtime;
    }

    /**
     *
     * @return
     * The categoryId
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     *
     * @param categoryId
     * The category_id
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The sharecode
     */
    public String getSharecode() {
        return sharecode;
    }

    /**
     *
     * @param sharecode
     * The sharecode
     */
    public void setSharecode(String sharecode) {
        this.sharecode = sharecode;
    }

}