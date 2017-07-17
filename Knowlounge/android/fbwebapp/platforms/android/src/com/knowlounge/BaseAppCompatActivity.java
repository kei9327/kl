package com.knowlounge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;

import com.knowlounge.util.ImeUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Minsu on 2016-05-27.
 */
public class BaseAppCompatActivity extends AppCompatActivity implements ImeUtil.ImeStateHost {

    private static final String TAG = "BaseAppCompatActiviy";
    private boolean hasActionBar;

    // Tracks the list of observers opting in for IME state change.
    private final Set<ImeUtil.ImeStateObserver> mImeStateObservers = new HashSet<>();

    // Tracks the soft keyboard display state
    private boolean mImeOpen;

    // Used to determine if a onDisplayHeightChanged was due to the IME opening or rotation of the
    // device
    private int mLastScreenHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hasActionBar = getDelegate().hasWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR);
    }

    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : ImeUtil.ImeStateHost>
     */

    @Override
    public void onDisplayHeightChanged(int heightMeasureSpec) {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (screenHeight != mLastScreenHeight) {

            // Appears to be an orientation change, don't fire ime updates
            mLastScreenHeight = screenHeight;
            return;
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && actionBar.isShowing()) {
            screenHeight -= actionBar.getHeight();
        }
        final int height = View.MeasureSpec.getSize(heightMeasureSpec);

        final boolean imeWasOpen = mImeOpen;
        mImeOpen = screenHeight - height > 100;

        Log.d(TAG, getLocalClassName() + ".onDisplayHeightChanged " +
                "imeWasOpen: " + imeWasOpen + " mImeOpen: " + mImeOpen + " screenHeight: " +
                screenHeight + " height: " + height);

        if (imeWasOpen != mImeOpen) {
            for (final ImeUtil.ImeStateObserver observer : mImeStateObservers) {
                observer.onImeStateChanged(mImeOpen);
            }
        }
    }

    @Override
    public void registerImeStateObserver(ImeUtil.ImeStateObserver observer) {
        mImeStateObservers.add(observer);
    }

    @Override
    public void unregisterImeStateObserver(ImeUtil.ImeStateObserver observer) {
        mImeStateObservers.remove(observer);
    }

    @Override
    public boolean isImeOpen() {
        return mImeOpen;
    }
}
