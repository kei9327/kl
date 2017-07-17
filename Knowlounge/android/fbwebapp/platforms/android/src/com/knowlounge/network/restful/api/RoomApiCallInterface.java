package com.knowlounge.network.restful.api;

import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Minsu on 2016-09-09.
 */
public interface RoomApiCallInterface {

    @GET("room/list/my.json")
    Observable<JsonObject> getMyClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("idx") String idx,
            @Query("rows") String rows,
            @Query("flag") String flag);


    @GET("room/list/recent.json")
    Observable<JsonObject> getRecentClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("idx") String idx,
            @Query("rows") String rows,
            @Query("flag") String flag);


    @GET("room/list/bookmark.json")
    Observable<JsonObject> getBookmarkClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("idx") String idx,
            @Query("rows") String rows,
            @Query("flag") String flag);


    @GET("room/list/friends.json")
    Observable<JsonObject> getFriendClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("friends") String friends,
            @Query("idx") String idx,
            @Query("rows") String rows,
            @Query("flag") String flag);


    @GET("room/list/school.json")
    Observable<JsonObject> getSchoolClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("idx") String idx,
            @Query("rows") String rows,
            @Query("flag") String flag);


    @GET("room/list/public.json")
    Observable<JsonObject> getPublicClassList(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Query("idx") String idx,
            @Query("rows") String rows,
            @Query("flag") String flag);

    @POST("room/remove.json")
    Observable<JsonObject> removeRoom(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr,
            @Body RequestBody params);
}
