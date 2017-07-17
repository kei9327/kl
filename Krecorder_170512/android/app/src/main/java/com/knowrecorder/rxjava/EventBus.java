package com.knowrecorder.rxjava;

/**
 * Created by we160303 on 2016-12-06.
 */

public class EventBus extends RxEventBus {
    private static EventBus instance;

    public static EventBus getInstance() {
        if(instance == null)
            instance = new EventBus();
        return instance;
    }
}
