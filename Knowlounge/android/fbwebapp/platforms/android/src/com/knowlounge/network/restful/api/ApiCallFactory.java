package com.knowlounge.network.restful.api;

/**
 * Created by Minsu on 2016-08-29.
 */
public class ApiCallFactory<ApiInterface> {

    private int apiType;
    private ApiInterface mApiInterface;

    public static final int API_TYPE_NONE = -1;
    public static final int API_TYPE_PROFILE = 0;
    public static final int API_TYPE_MAIN = 1;
    public static final int API_TYPE_ROOM = 2;
    public static final int API_TYPE_AUTH = 3;
    public static final int API_TYPE_INVITE = 4;
    public static final int API_TYPE_HISTORY = 5;
    public static final int API_TYPE_APP_VERSION = 6;
    public static final int API_TYPE_HELP = 7;
    public static final int API_TYPE_SI_PLATFORM = 8;

    public static final int API_TYPE_RTC_AUTH = 9;

    public static final int API_TYPE_BIZ_AUTH = 5;
    public static final int API_TYPE_BIZ_PROFILE = 6;



    public ApiCallFactory(int type, ApiInterface apiInterface) {
        this.apiType = type;
        this.mApiInterface = apiInterface;
    }

    public static <D> ApiCallFactory<D> create(int type, D apiInterface) {
        return new ApiCallFactory<>(type, apiInterface);
    }


    public static boolean isValidType(int type) {
        return (type > API_TYPE_NONE && type <= API_TYPE_RTC_AUTH);
    }

    public ApiInterface getApiInterface() {
        return mApiInterface;
    }

}
