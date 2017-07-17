package com.knowlounge.network.restful.zico.command;

import com.google.gson.JsonObject;
import com.knowlounge.network.restful.zico.RestApiFactory;
import com.knowlounge.network.restful.zico.api.ApiCallFactory;
import com.knowlounge.network.restful.zico.api.ZicoApiCallInterface;

import okhttp3.FormBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-02.
 */

public class AuthRestCommand extends ApiCommand {

    private String mSns;
    private String mToken;
    private String mService;
    private String mIp;

    @Override
    public Observable<JsonObject> buildApi() {
        ApiCallFactory<ZicoApiCallInterface> authApi
                = (ApiCallFactory<ZicoApiCallInterface>) RestApiFactory.getInstance().getApi(ApiCallFactory.API_TYPE_AUTH);

        return authApi.getApiInterface().getAccessToken(new FormBody.Builder().add("sns", mSns)
                .add("token", mToken)
                .add("service", mService)
                .add("ip", mIp)
                .build());
    }

    @Override
    public void execute() {
        Observable<JsonObject> call = buildApi();

        if (mApiCallEvent != null) {
            mApiCallEvent.onApiCall(this, call);
        }
        else {
            call.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    public AuthRestCommand sns(String sns) {
        mSns = sns;
        return this;
    }

    public AuthRestCommand token(String token) {
        mToken = token;
        return this;
    }

    public AuthRestCommand service(String service) {
        mService = service;
        return this;
    }

    public AuthRestCommand ip(String ip) {
        mIp = ip;
        return this;
    }

}
