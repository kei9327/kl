package com.knowlounge.network.restful.api;

import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Mansu on 2016-10-05.
 */

public interface AuthApiCallInterface {


    @POST("auth/reload.json")
    Observable<JsonObject> reloadAuth(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr);


    @POST("auth/star/gettoken.json")
    Observable<JsonObject> getToken(
            @Header("Accept-Language") String locale,
            @Header("Cookie") String cookieStr);


    @POST("fb/auth.json")
    Observable<JsonObject> facebookSignIn(
            @Header("Accept-Language") String locale,
            @Body RequestBody params);


    @POST("gl/auth.json")
    Observable<JsonObject> googleSignIn(
            @Header("Accept-Language") String locale,
            @Body RequestBody params);

}
