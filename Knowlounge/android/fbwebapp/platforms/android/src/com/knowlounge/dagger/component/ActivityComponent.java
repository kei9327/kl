package com.knowlounge.dagger.component;

import android.app.Activity;

import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.base.BaseActivity;
import com.knowlounge.base.BaseFragment;
import com.knowlounge.dagger.modules.ActivityModule;
import com.knowlounge.dagger.scopes.PerActivity;

import dagger.Component;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-02-20.
 */

@PerActivity
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
    void inject(BaseActivity activity);
    void inject(BaseFragment fragment);

    Activity activity();
}
