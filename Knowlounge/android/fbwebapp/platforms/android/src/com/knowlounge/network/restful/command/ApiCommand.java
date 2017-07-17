package com.knowlounge.network.restful.command;

import java.util.Locale;

import rx.Observable;

/**
 * Created by Minsu on 2016-08-29.
 * Command 클래스의 상위 클래스
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
