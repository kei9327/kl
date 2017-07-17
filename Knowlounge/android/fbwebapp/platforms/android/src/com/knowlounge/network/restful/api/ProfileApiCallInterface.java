package com.knowlounge.network.restful.api;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Minsu on 2016-08-29.
 * Retrofit Rest API Call interface
 */
public interface ProfileApiCallInterface {

    @GET("profile/getProfile.json")
    Observable<JsonObject> getProfile(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr);


    @GET("profile/getProfileCodeList.json")
    Observable<JsonObject> getProfileCodeList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("category") String category,
            @Query("usertype") String userType);


    @GET("profile/getProfileCodeListAll.json")
    Observable<JsonObject> getProfileCodeListAll(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr);


    @POST("profile/setProfile.json")
    Observable<JsonObject> setProfile(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Field("usertype") String userType,
            @Field("name") String userName,
            @Field("telno") String telNo,
            @Field("email") String email,
            @Field("bio") String bio,
            @Field("school") String school,
            @Field("grade") String grade,
            @Field("subject") String subject,
            @Field("language") String language);


    @POST("profile/updateProfile.json")
    Observable<JsonObject> updateProfile(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Field("name") String userName,
            @Field("telno") String telNo,
            @Field("email") String email,
            @Field("bio") String bio,
            @Field("school") String school,
            @Field("grade") String grade,
            @Field("subject") String subject,
            @Field("language") String language);


    @Multipart
    @POST("profile/uploadProfileImg.json")
    Observable<JsonObject> uploadProfileImg(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Part MultipartBody.Part file);



}
