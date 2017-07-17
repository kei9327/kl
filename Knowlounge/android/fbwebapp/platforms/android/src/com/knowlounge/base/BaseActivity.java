package com.knowlounge.base;

import android.support.v7.app.AppCompatActivity;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.dagger.component.AppComponent;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-02-20.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected AppComponent getAppComponent() {
        return ((KnowloungeApplication) getApplication()).getAppComponent();
    }
}
