package com.knowrecorder.rxjava;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by we160303 on 2016-12-06.
 */

public class RxEventBus {
    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());
    public void post(Object o){ bus.onNext(o);}

    public Observable<Object> getBusObservable() { return bus ;}
}
