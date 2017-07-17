package com.knowrecorder.OpenCourse.API.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class UserInfo {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("locale")
    @Expose
    private String locale;
    @SerializedName("picture")
    @Expose
    private String picture;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("fb_user_id")
    @Expose
    private String fbUserId;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("sns_type")
    @Expose
    private String snsType;

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The gender
     */
    public String getGender() {
        return gender;
    }

    /**
     *
     * @param gender
     * The gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     * The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     *
     * @param locale
     * The locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     *
     * @return
     * The picture
     */
    public String getPicture() {
        return picture;
    }

    /**
     *
     * @param picture
     * The picture
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     *
     * @return
     * The timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     *
     * @param timezone
     * The timezone
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     *
     * @return
     * The fbUserId
     */
    public String getFbUserId() {
        return fbUserId;
    }

    /**
     *
     * @param fbUserId
     * The fb_user_id
     */
    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    /**
     *
     * @return
     * The accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     *
     * @param accessToken
     * The access_token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
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

}