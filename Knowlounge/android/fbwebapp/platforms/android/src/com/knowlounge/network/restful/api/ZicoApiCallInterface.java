package com.knowlounge.network.restful.api;

import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * 사전에 Rtc 연결시 필요한 REST 기능
 */
public interface ZicoApiCallInterface {

    @POST("peer/getAccessToken.json")
    Observable<JsonObject> getAccessToken(
            @Body RequestBody params);


    @GET("peer/getRtcServer.json")
    Observable<JsonObject> getRtcServer(
            @Query("roomid") String roomId);

    @GET("peer/getTurnServer.json")
    Observable<JsonObject> getTurnServer(
            @Query("caller") String callerUserId, @Query("callee") String calleeUserId);
}
