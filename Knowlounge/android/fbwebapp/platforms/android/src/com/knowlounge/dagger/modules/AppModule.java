package com.knowlounge.dagger.modules;

import android.app.Application;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.view.room.Navigator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-02-20.
 */

@Module
public class AppModule {
    private final KnowloungeApplication application;

    public AppModule(KnowloungeApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return this.application;
    }

    @Provides
    @Singleton
    Navigator provideNavigator() {
        return new Navigator();
    }
}
