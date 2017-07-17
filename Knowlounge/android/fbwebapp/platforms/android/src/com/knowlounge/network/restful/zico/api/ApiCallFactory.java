package com.knowlounge.network.restful.zico.api;

/**
 * Created by Mansu on 2017-02-06.
 */

public class ApiCallFactory<ApiInterface> {

    private int apiType;
    private ApiInterface mApiInterface;

    public static final int API_TYPE_NONE = -1;
    public static final int API_TYPE_AUTH = 1;



    public ApiCallFactory(int type, ApiInterface apiInterface) {
        this.apiType = type;
        this.mApiInterface = apiInterface;
    }

    public static <D> ApiCallFactory<D> create(int type, D apiInterface) {
        return new ApiCallFactory<>(type, apiInterface);
    }


    public static boolean isValidType(int type) {
        return (type > API_TYPE_NONE && type <= API_TYPE_AUTH);
    }

    public ApiInterface getApiInterface() {
        return mApiInterface;
    }

}
