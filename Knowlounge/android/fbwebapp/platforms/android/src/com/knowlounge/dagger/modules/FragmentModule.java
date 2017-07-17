package com.knowlounge.dagger.modules;

import android.support.v4.app.Fragment;

import com.knowlounge.dagger.scopes.PerFragment;

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
public class FragmentModule {
    private final Fragment fragment;

    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @PerFragment
    Fragment provideFragment() {
        return this.fragment;
    }
}
