package com.knowlounge.rxjava;

/**
 * Created by we160303 on 2016-10-05.
 */

public class EventBus extends RxEventBus{
    private static EventBus instance;

    public static EventBus get() {
        if(instance == null)
            instance = new EventBus();
        return instance;
    }
}
