package com.knowlounge;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.knowlounge.dagger.component.AppComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Minsu on 2016-09-01.
 */
public class AndroidContext implements Application.ActivityLifecycleCallbacks {

    private final String TAG = "AndroidContext";

    private static AndroidContext sInstance;

    private final Context mContext;

    private final KnowloungeApplication mApplication;

    private Set<Class<? extends Activity>> mRunningApps = new HashSet<>();


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : Application.ActivityLifecycleCallbacks>
     */

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed " + activity.getClass());
        mRunningApps.add(activity.getClass());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused " + activity.getClass());
        mRunningApps.remove(activity.getClass());
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<methods>
     */

    /**
     * The android context object cannot be created until the android
     * has created the application object. The AndroidContext object
     * must be initialized before other singletons can use it.
     */
    public static void initialize(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new AndroidContext(context);
        }
    }

    /**
     * Return a previously initialized instance, throw if it has not been
     * initialized yet.
     */
    public static AndroidContext instance() {
        if (sInstance == null) {
            throw new IllegalStateException("Android context was not initialized.");
        }
        return sInstance;
    }

    private AndroidContext(Context context) {
        mContext = context;
        mApplication = (KnowloungeApplication) context;
    }

    public Context getApplication() {
        return mContext;
    }

    public AppComponent getAppComponent() {
        return mApplication.getAppComponent();
    }

    public boolean isRtlMode() {
        return false;
    }

    public boolean isRunning(Class<? extends Activity> klass) {
        return mRunningApps.contains(klass);
    }

}