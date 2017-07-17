package com.knowlounge.dagger.component;

import android.app.Activity;

import com.knowlounge.apprtc.KlgeClientController;
import com.knowlounge.dagger.modules.RoomUsersModule;
import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.dagger.modules.ActivityModule;
import com.knowlounge.dagger.modules.RoomSpecModule;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.model.RoomUsers;

import dagger.Component;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

@PerActivity
@Component(dependencies = AppComponent.class, modules = {ActivityModule.class, RoomSpecModule.class, RoomUsersModule.class})
public interface RoomActivityComponent {
    void inject(RoomActivity activity);

    Activity activity();

    RoomSpec getRoomSpec();

    RoomUsers getRoomUsers();
}
