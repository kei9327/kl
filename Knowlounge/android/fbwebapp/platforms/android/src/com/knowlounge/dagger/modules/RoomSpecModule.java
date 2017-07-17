package com.knowlounge.dagger.modules;

import com.knowlounge.model.RoomSpec;
import com.knowlounge.dagger.scopes.PerActivity;

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

@PerActivity
@Module
public class RoomSpecModule {

    private final RoomSpec mRoomSpec;

    public RoomSpecModule(RoomSpec roomSpec) {
        mRoomSpec = roomSpec;
    }

    @Provides
    @PerActivity
    RoomSpec provideRoomSpec() {
        return mRoomSpec;
    }

    @Provides
    @PerActivity
    String provideUserNo() {
        return mRoomSpec.getUserNo();
    }
}
