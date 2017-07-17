package com.knowlounge.network.restful.zico.command;

import java.util.Locale;

import rx.Observable;

/**
 * Created by Mansu on 2017-02-13.
 */

public abstract class ApiCommand<T>  {

    public interface ApiCallEvent<D> {
        void onApiCall(ApiCommand<? extends D> command, Observable<D> observer);
    }

    protected ApiCallEvent mApiCallEvent;

    public ApiCommand event(ApiCallEvent<T> event) {
        mApiCallEvent = event;
        return this;
    }

    public String getLocale() {
        return Locale.getDefault().toString().replace("_", "-");
    }

    public abstract Observable<T> buildApi();

    public abstract void execute();
}