package com.knowlounge.network.restful.func;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Mansu on 2017-01-09.
 */

public class ListApiRetry implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final String TAG = "ListApiRetry";
    private final int maxRetries;
    private final int retryDelay;
    private int retryCnt = 0;

    public ListApiRetry(final int maxRetries, final int retryDelay) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
            .flatMap(new Func1<Throwable, Observable<?>>() {
                @Override
                public Observable<?> call(Throwable throwable) {

                    /**
                     * Note
                     * 회원가입에 실패하여 다시 시도해야 할 상황들을 열거한다.
                     */
                    Log.d(TAG, "AuthSignUp retrying error: " + throwable);

                    if (++retryCnt < maxRetries) {
                        // When this Observable calls onNext, the original
                        // Observable will be retried (i.e. re-subscribed).
                        Log.d(TAG, "AuthSignUp retrying authorize with delay: " + retryCnt);

                        if (throwable instanceof HttpException) {
                            HttpException response = (HttpException) throwable;
                            int code = response.code();

                            if (code == 502 || code == 503 || code == 504) {
                                return Observable.timer(retryDelay, TimeUnit.MILLISECONDS);
                            } else {
                                return Observable.error(throwable);
                            }
                        }

                        return Observable.timer(retryDelay, TimeUnit.MILLISECONDS);
                    }

                    // Max retries hit. Just pass the error along.
                    return Observable.error(throwable);
                }
            });
    }
}