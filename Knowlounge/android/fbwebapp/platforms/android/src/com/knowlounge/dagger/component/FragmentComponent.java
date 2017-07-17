package com.knowlounge.dagger.component;

import android.support.v4.app.Fragment;

import com.knowlounge.base.BaseFragment;
import com.knowlounge.dagger.modules.FragmentModule;
import com.knowlounge.dagger.scopes.PerFragment;

import dagger.Component;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-02-20.
 */

@PerFragment
@Component(dependencies = AppComponent.class, modules = FragmentModule.class)
public interface FragmentComponent {
    void inject(BaseFragment fragment);

    Fragment fragment();
}
