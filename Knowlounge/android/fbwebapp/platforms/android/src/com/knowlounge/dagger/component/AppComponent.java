package com.knowlounge.dagger.component;

import android.app.Application;

import com.knowlounge.dagger.modules.AppModule;
import com.knowlounge.view.room.Navigator;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    Application application();

    Navigator navigator();
}
