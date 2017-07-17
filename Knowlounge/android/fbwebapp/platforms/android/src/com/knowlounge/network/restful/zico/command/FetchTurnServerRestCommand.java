package com.knowlounge.network.restful.zico.command;

import com.google.gson.JsonObject;
import com.knowlounge.network.restful.zico.RestApiFactory;
import com.knowlounge.network.restful.zico.api.ApiCallFactory;
import com.knowlounge.network.restful.zico.api.ZicoApiCallInterface;

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

public class FetchTurnServerRestCommand extends ApiCommand {

    private String mCallerUserId;
    private String mCalleeUserId;


    @Override
    public Observable<JsonObject> buildApi() {
        ApiCallFactory<ZicoApiCallInterface> zicoApi
                = (ApiCallFactory<ZicoApiCallInterface>) RestApiFactory.getInstance().getApi(ApiCallFactory.API_TYPE_AUTH);

        return zicoApi.getApiInterface().getTurnServer(mCallerUserId, mCalleeUserId);
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

    public FetchTurnServerRestCommand callerUserId(String callerUserId) {
        mCallerUserId = callerUserId;
        return this;
    }

    public FetchTurnServerRestCommand calleeUserId(String calleeUserId) {
        mCalleeUserId = calleeUserId;
        return this;
    }
}
