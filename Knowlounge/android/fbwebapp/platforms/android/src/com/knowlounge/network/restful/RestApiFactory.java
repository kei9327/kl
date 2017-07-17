package com.knowlounge.network.restful;

import android.util.SparseArray;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.network.restful.api.ApiCallFactory;
import com.knowlounge.network.restful.api.AuthApiCallInterface;
import com.knowlounge.network.restful.api.MainApiCallInterface;
import com.knowlounge.network.restful.api.ProfileApiCallInterface;
import com.knowlounge.network.restful.api.RoomApiCallInterface;
import com.knowlounge.network.restful.api.ZicoApiCallInterface;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Minsu on 2016-08-29.
 */
public class RestApiFactory {

//    private final String BASE_URL = "http://192.168.0.195/mapi/";
//    private final String BASE_URL = "https://dev.knowlounges.com/mapi/";
//    private final String BASE_URL = "https://www.knowlounges.com/mapi/";   // Retrofit 2.0 부터 baseUrl 뒤에 "/" 를 반드시 붙어야 함..


    private final String SI_BASE_URL = "https://www.sayalo.me:30443/knowlounge/";
    private final String SI_BASE_URL_DEV = "http://sidev.wescan.com:9000/knowlounge/";


    private static RestApiFactory _instance = new RestApiFactory();

    /**
     * SparseArray : key를 정수값으로만 가질 수 있는, HashMap과 유사한 자료구조. 일명 뜨문뜨문 배열
     **/
    private SparseArray<ApiCallFactory<?>> mApis = new SparseArray<ApiCallFactory<?>>();

    public static RestApiFactory getInstance() {
        return _instance;
    }


    public ApiCallFactory<?> getApi(int type) {
        if (!ApiCallFactory.isValidType(type)) {
            return null;
        }

        ApiCallFactory<?> wrapper = mApis.get(type);

        if (wrapper == null) {
            ApiCallFactory<?> apiInterface = createApi(KnowloungeApplication.KNOWLOUNGE_API_HOST, type);
            mApis.put(type, apiInterface);

            wrapper = apiInterface;
        }
        return wrapper;
    }


    private ApiCallFactory<?> createApi(String baseUrl, int type) {
        OkHttpClient okClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = null;
                        try {
                            response = chain.proceed(chain.request());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            return response;
                        }
                    }
                }).build();

        switch (type) {
            case ApiCallFactory.API_TYPE_PROFILE :
                Retrofit profileClient = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okClient)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                return ApiCallFactory.create(type, profileClient.create(ProfileApiCallInterface.class));

            case ApiCallFactory.API_TYPE_MAIN :
                Retrofit mainClient = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okClient)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                return ApiCallFactory.create(type, mainClient.create(MainApiCallInterface.class));

            case ApiCallFactory.API_TYPE_ROOM :
                Retrofit roomClient = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okClient)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                return ApiCallFactory.create(type, roomClient.create(RoomApiCallInterface.class));

            case ApiCallFactory.API_TYPE_AUTH :
                Retrofit authClient = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okClient)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                return ApiCallFactory.create(type, authClient.create(AuthApiCallInterface.class));

            case ApiCallFactory.API_TYPE_RTC_AUTH :
                Retrofit rtcAuthClient = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okClient)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                return ApiCallFactory.create(type, rtcAuthClient.create(ZicoApiCallInterface.class));
            default :
                return null;
        }
    }


}
