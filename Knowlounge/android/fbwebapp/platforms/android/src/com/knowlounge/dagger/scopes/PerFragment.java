package com.knowlounge.dagger.scopes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-02-20.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerFragment { }
