package com.knowlounge.dagger.modules;

import com.knowlounge.view.room.RoomView;

import dagger.Module;
import dagger.Provides;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

@Module
public class RoomMvpViewModule {

    private final RoomView mView;

    public RoomMvpViewModule(RoomView view) {
        this.mView = view;
    }

    @Provides
    RoomView provideMvpView() {
        return mView;
    }
}
