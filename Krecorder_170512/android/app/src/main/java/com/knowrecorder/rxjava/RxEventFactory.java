package com.knowrecorder.rxjava;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p/>
 * Alo
 * <p/>
 *
 * 이벤트를 등록하고 원하는 구독자에게 알려주기 위한 관리자
 *
 * 이벤트 등록
 *  mSubscription = RxEventFactory.get().subscribe(Event.class, new Action1<Event>() {
 *      @Override
 *      public void call(TalkBoxEvent event) {
 *          if (isResumed()) {
 *              mTexts.setText(event.texts);
 *          }
 *      }
 *  });
 *
 *  이벤트 해제
 *   mSubscription.unsubscribe();
 *
 * author: Jun-hyoung Lee
 * date: 2016-07-27.
 *
 * history
 *  2016-09-21. (by Jun-hyoung Lee)
 *      - 1개 이상의 구독자를 추가하고 각각의 구독자들에게 이벤트를 동시에 알릴 수 있도록 기능 추가.
 *      - 이벤트 구독할 때에 실행될 스케줄러를 지정할 수 있도록 기능 추가.
 */
public class RxEventFactory {

    private static final RxEventFactory sInstance = new RxEventFactory();

    private final Subject<Object, Object> mSubjects = new SerializedSubject<>(PublishSubject.create());
    private final Map<Class, Integer> mRefCounts = new HashMap<>();

    private RxEventFactory() {

    }


    public static RxEventFactory get() {
        return sInstance;
    }

    /**
     * 이벤트를 처리하기 위하여 이벤트 클래스를 등록한다.
     * @param eventClass 이벤트 클래스
     * @param action 이벤트를 처리하기 위한 메소드
     * @param scheduler 이벤트를 알리기 위한 스케줄러
     * @param <T> 이벤트 클래스 타입
     * @return 이벤트 구독자
     */
    public <T> Subscription subscribe(
            @NonNull final Class<T> eventClass, @NonNull Action1<T> action, @NonNull Scheduler scheduler) {
        addRefs(eventClass);
        Log.d("RxEventFactory", "Subscribing " + eventClass.getSimpleName() + " event. refs: " + getRefCount(eventClass));

        return mSubjects
                .ofType(eventClass)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        removeRefs(eventClass);
                        Log.d("RxEventFactory", "Unsubscribing " + eventClass.getSimpleName() + " event. refs: " + getRefCount(eventClass));
                    }
                })
                .observeOn(scheduler)
                .subscribe(action);
    }

    public <T> Subscription subscribe(
            @NonNull final Class<T> eventClass, @NonNull Action1<T> action) {
        return subscribe(eventClass, action, Schedulers.immediate());
    }

    /**
     * 이벤트를 발생시킨다.
     * @param event 이벤트 클래스
     * @param noSubscriber 이벤트를 등록한 인스턴스가 없을 떄 실행된다.
     * @param <T> 이벤트 클래스 타입
     */
    public <T> void post(@NonNull T event, @Nullable Action1<T> noSubscriber) {
        if (getRefCount(event.getClass()) > 0) {
            mSubjects.onNext(event);
        } else {
            if (noSubscriber != null) {
                noSubscriber.call(event);
            }
        }
    }

    public <T> void post(@NonNull T event) {
        post(event, null);
    }

    private synchronized int getRefCount(Class eventClass) {
        if (mRefCounts.containsKey(eventClass)) {
            return mRefCounts.get(eventClass);
        } else {
            return 0;
        }
    }

    private synchronized void putRefCount(Class eventClass, int refCount) {
        if (refCount == 0) {
            mRefCounts.remove(eventClass);
        } else {
            mRefCounts.put(eventClass, refCount);
        }
    }

    private synchronized void addRefs(Class eventClass) {
        putRefCount(eventClass, getRefCount(eventClass) + 1);
    }

    private synchronized void removeRefs(Class eventClass) {
        putRefCount(eventClass, getRefCount(eventClass) - 1);
    }
}
