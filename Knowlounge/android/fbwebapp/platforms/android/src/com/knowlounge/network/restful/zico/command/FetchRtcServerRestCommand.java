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

public class FetchRtcServerRestCommand extends ApiCommand {

    private String mRoomId;
    private int mAuto;

    @Override
    public Observable<JsonObject> buildApi() {
        ApiCallFactory<ZicoApiCallInterface> authApi
                = (ApiCallFactory<ZicoApiCallInterface>) RestApiFactory.getInstance().getApi(ApiCallFactory.API_TYPE_AUTH);

        return authApi.getApiInterface().getRtcServer(mRoomId);
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

    public FetchRtcServerRestCommand roomId(String roomId) {
        mRoomId = roomId;
        return this;
    }

    public FetchRtcServerRestCommand auto(int autoFlag) {
        mAuto = autoFlag;
        return this;
    }
}
