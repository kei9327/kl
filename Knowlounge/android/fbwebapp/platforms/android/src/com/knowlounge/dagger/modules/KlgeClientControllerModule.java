package com.knowlounge.dagger.modules;

import com.knowlounge.dagger.scopes.PerFragment;
import com.knowlounge.apprtc.KlgeClientController;

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
public class KlgeClientControllerModule {

    private final KlgeClientController mController;

    public KlgeClientControllerModule(KlgeClientController controller) {
        this.mController = controller;
    }

    @Provides
    @PerFragment
    KlgeClientController provideKlgeClientController() {
        return mController;
    }
}
