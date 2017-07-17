package com.knowrecorder.OpenCourse;

import com.knowrecorder.OpenCourse.API.Models.Category;
import com.knowrecorder.OpenCourse.API.Models.HomeVideo;
import com.knowrecorder.OpenCourse.API.Models.Playtime;
import com.knowrecorder.OpenCourse.API.Models.UploadVideo;
import com.knowrecorder.OpenCourse.API.Models.UserInfo;
import com.knowrecorder.OpenCourse.API.Models.Video;
import com.knowrecorder.OpenCourse.API.Models.VideoCount;
import com.knowrecorder.OpenCourse.API.Models.VideoUploadResponse;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiEndPointInterface {

    /*
        Get a User
     */
    @GET("users")
    Call<ResponseBody> getUser(@Query("sns_type") String snsType, @Query("user_id") String userId);

    /*
        Get category list
     */
    @GET("categories")
    Call<List<Category>> getCategories(@Query("api_key") String apiKey);

    /*
        Get Videos
     */
    @GET("videos/offset/{offset}/limit/{limit}")
    Call<List<Video>> getVideos(@Path("offset") int offset, @Path("limit") int limit, @Query("api_key") String apiKey);

    @GET("videos/offset/{offset}/limit/{limit}")
    Call<List<Video>> getVideos(@Path("offset") int offset, @Path("limit") int limit, @Query("api_key") String apiKey,
                                 @Query("category_id") int category_id);

    @GET("videos/offset/{offset}/limit/{limit}")
    Call<List<Video>> getVideos(@Path("offset") int offset, @Path("limit") int limit, @Query("api_key") String apiKey,
                                 @Query("order_by") String order, @Query("lang") String lang);

    @GET("videos/offset/{offset}/limit/{limit}")
    Call<List<Video>> getVideos(@Path("offset") int offset, @Path("limit") int limit, @Query("api_key") String apiKey,
                                 @Query("category_id") int category_id,
                                @Query("order_by") String order, @Query("lang") String lang);

    @GET("videos/main/limit/10")
    Call<List<HomeVideo>> getVideos(@Query("api_key") String apiKey,  @Query("lang") String lang);

    /*
        Get Videos (Category)
    */
    @GET("videos/offset/{offset}/limit/{limit}")
    Call<List<Video>> getVideosByCategory(@Path("offset") int offset, @Path("limit") int limit, @Query("category_id") int categoryId);

    /*
        Create a Video
     */
    @POST("videos")
    Call<VideoUploadResponse> createVideo(@Query("api_key") String apiKey, @Body UploadVideo uploadVideo);

    /*
        Get a Video
     */
    @GET("videos/{video_id}")
    Call<Video> getVideo(@Path("video_id") int videoId, @Query("api_key") String apiKey);

    @POST("videos/archive/{video_id}")
    Call<ResponseBody> uploadArchive(@Path("video_id") int videoId, @Query("api_key") String apiKey, @Body RequestBody archive);

    @POST("videos/thumbnail/{video_id}")
    Call<ResponseBody> uploadThumbnail(@Path("video_id") int videoId, @Query("api_key") String apiKey, @Body RequestBody thumbnail);


    @GET("videos/archive/{video_id}")
    Call<ResponseBody> downloadArchive(@Path("video_id") int videoId, @Query("api_key") String apiKey);

    @Streaming
    @GET("videos/archive/{video_id}")
    Call<ResponseBody> downloadArchiveStreaming(@Path("video_id") int videoId, @Query("api_key") String apiKey);

    @GET("videos/thumbnail/{video_id}")
    Call<ResponseBody> downloadThumbnail(@Path("video_id") int videoId, @Query("api_key") String apiKey);

    @GET("videos/search/{search_text}")
    Call<List<Video>> searchVideos(@Path("search_text") String searchText, @Query("api_key") String apiKey,
                              @Query("category_id") int categoryId,
                              @Query("orderBy") String order,
                              @Query("offset") int offset, @Query("limit") int limit, @Query("lang") String lang);

    @GET("videos/search/{search_text}")
    Call<List<Video>> searchVideos(@Path("search_text") String searchText, @Query("api_key") String apiKey,
                                    @Query("orderBy") String order,
                                   @Query("offset") int offset, @Query("limit") int limit, @Query("lang") String lang);

    @POST("users")
    Call<ResponseBody> createUser(@Query("api_key") String apiKey, @Body UserInfo userInfo);

    @PUT("users")
    Call<ResponseBody> updateUser(@Query("sns_type") String snsType, @Query("user_id") String userId,
                                  @Query("api_key") String apiKey, @Body UserInfo userInfo);

    @GET("search/count/{search_text}")
    Call<VideoCount> videoCount(@Path("search_text") String searchText, @Query("api_key") String apiKey, @Query("lang") String lang);

    @GET("search/count/{search_text}")
    Call<VideoCount> videoCount(@Path("search_text") String searchText,
                                @Query("category_id") int category_id, @Query("api_key") String apiKey, @Query("lang") String lang);


    @POST("videos/accumulate/{video_id}")
    Call<ResponseBody> accumulate(@Path("video_id") int videoId, @Query("api_key") String apiKey,
                                  @Body Playtime playtime);
}
