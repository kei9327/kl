package com.knowlounge.dagger.modules;

import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.model.RoomUsers;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Mansu on 2017-03-23.
 */

@PerActivity
@Module
public class RoomUsersModule {
    private final RoomUsers mRoomUsers;

    public RoomUsersModule(RoomUsers roomUsers) {
        mRoomUsers = roomUsers;
    }

    @Provides
    @PerActivity
    RoomUsers provideRoomUsers() {
        return mRoomUsers;
    }
}
