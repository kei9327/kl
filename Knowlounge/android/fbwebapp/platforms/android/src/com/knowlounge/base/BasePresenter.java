package com.knowlounge.base;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

public abstract class BasePresenter<V extends MvpView> implements MvpPresenter {

    private V mView;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        unbindView();
    }

    @SuppressWarnings("WeakerAccess")
    public void bindView(V view) {
        mView = view;
    }

    @SuppressWarnings("WeakerAccess")
    public void unbindView() {
        mView = null;
    }

    protected V getMvpView() {
        return mView;
    }
}
