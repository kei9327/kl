package com.knowlounge;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.knowlounge.dagger.component.DaggerAppComponent;
import com.knowlounge.receiver.NetworkStateReceiver;
import com.knowlounge.util.GoogleAnalyticsService;
import com.knowlounge.util.logger.AppLog;
import com.knowlounge.dagger.component.AppComponent;
import com.knowlounge.dagger.modules.AppModule;

import org.webrtc.PeerConnectionFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Minsu on 2016-06-11.
 */
public class KnowloungeApplication extends MultiDexApplication implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "KnowloungeApplication";
    public static boolean DEBUG = false;
    public static boolean isPhone = false;
    public static boolean isNetworkConnected = true;
    public static float density = 0f;

    public static int deviceWidth = 0;
    public static int deviceHeight = 0;

    public static String KNOWLOUNGE_API_HOST;
    public static String ZICO_API_HOST;

    public static String KNOWLOUNGE_PREMIUM_URL;

    public static String CANVAS_HTML_NAME = "";

    private boolean isVideoCodecHwAcceleration = false;

    private Thread.UncaughtExceptionHandler mSystemUncaughtExceptionHandler;

    private AppComponent mAppComponent;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        AndroidContext.initialize(this);

        //폰인지 테블릿인지 판별
        int screenSizeType = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if(screenSizeType == Configuration.SCREENLAYOUT_SIZE_SMALL || screenSizeType == Configuration.SCREENLAYOUT_SIZE_NORMAL)
            isPhone = true;
        else
            isPhone = false;


        mSystemUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        Context application = getApplicationContext();
        FacebookSdk.sdkInitialize(application);

        Stetho.initializeWithDefaults(application);

        GoogleAnalyticsService.initialize(this);

        if(!PeerConnectionFactory.initializeAndroidGlobals(this, true, true, isVideoCodecHwAcceleration)) {
            Log.d(TAG, "Failed to initializeAndroidGlobals");
        }

        DEBUG = isDebuggable(getApplicationContext());

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager mgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mgr.getDefaultDisplay().getMetrics(metrics);

        density = metrics.densityDpi / 160;

        deviceWidth = metrics.widthPixels;
        deviceHeight = metrics.heightPixels;

        String svrFlag = getResources().getString(R.string.svr_flag);
        String svrHost = getResources().getString(getResources().getIdentifier("svr_host_" + svrFlag, "string", getPackageName()));
        KNOWLOUNGE_API_HOST = svrHost + getResources().getString(R.string.svr_context);
        ZICO_API_HOST = getResources().getString(getResources().getIdentifier("zico_svr_host_" + svrFlag, "string", getPackageName()));

        String premiumSvrHost = getResources().getString(getResources().getIdentifier("premium_svr_host_" + svrFlag, "string", getPackageName()));
        KNOWLOUNGE_PREMIUM_URL = premiumSvrHost;

        CANVAS_HTML_NAME = TextUtils.equals(svrFlag, "1") ? "view_develop_remote.html" :
                            TextUtils.equals(svrFlag, "2") ? "view_release_remote.html" : "view.html";
        CANVAS_HTML_NAME = "view.html";

        Log.d(TAG, "DPI = " + metrics.densityDpi);
        Log.d(TAG, "density = " + density);
        Log.d(TAG, "screenWidth = " + deviceWidth + ", screenHeight = " + deviceHeight);
        Log.d(TAG, "Knowlounge Api Server Host : " + KNOWLOUNGE_API_HOST);
        Log.d(TAG, "ZICO Api Server Host : " + ZICO_API_HOST);

        setUpInjector();


    }

    private void setUpInjector() {
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    /**
     * 현재 디버그모드여부를 리턴
     *
     * @param context
     * @return
     */
    private boolean isDebuggable(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
            debuggable = false;
        }

        return debuggable;
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        final boolean background = getMainLooper().getThread() != thread;
        if (background) {
            AppLog.e(AppLog.TAG, "Uncaught exception in background thread " + thread, ex);

            final Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mSystemUncaughtExceptionHandler.uncaughtException(thread, ex);
                }
            });
        } else {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsStrting = sw.toString();

            AppLog.e(AppLog.TAG, "Uncaught exception " + exceptionAsStrting);

            mSystemUncaughtExceptionHandler.uncaughtException(thread, ex);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
    }

}