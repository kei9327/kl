package com.knowlounge.network.restful.api;

import com.google.gson.JsonObject;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Minsu on 2016-09-09.
 */
public interface MainApiCallInterface {

    @GET("main/home.json")
    Observable<JsonObject> getHomeClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("friends") String friends,
            @Query("rows") String rows);


    @GET("main/myclass.json")
    Observable<JsonObject> getMyClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("rows") String rows);


    @GET("main/friends.json")
    Observable<JsonObject> getFriendClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("friends") String friends,
            @Query("rows") String rows);


    @GET("main/public.json")
    Observable<JsonObject> getPublicClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("rows") String rows);


    @GET("main/school.json")
    Observable<JsonObject> getSchoolClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("rows") String rows);

}
