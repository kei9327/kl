package com.knowrecorder.Youtube;

import com.knowrecorder.Youtube.Models.CategoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class CategoryApi {

    public interface CategoryApiService {
        @GET("/youtube/v3/videoCategories")
        Call<CategoryResponse> getCategories(
                @Query("part") String part,
                @Query("regionCode") String regionCode,
                @Query("key") String key
        );
    }
}
