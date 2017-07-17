package com.knowrecorder;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.SharedPreferencesManager;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;

/**
 * Created by ssyou on 2016-02-04.
 */
public class KnowRecorderApplication extends Application {

    public static AtomicInteger primaryKeyValue;
    public static AtomicInteger pdfPaperKey;
    public static AtomicInteger noteKey;
    public static AtomicInteger remodelingKey;
    public static boolean isPhone = false;
    public static final String PREFERENCE_NAME = "KnowRecorder";
    public static boolean DEBUG_FLAG = true;

    private static Context context;
    private static long lastRecordTime = 0;
    private static long currentTimeStame = 0;
    private Tracker mTracker;

    private static String language;
    private static String country;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        KnowRecorderApplication.context = context;
    }

    public static long getLastRecordTime() {
        return lastRecordTime;
    }

    public static void setLastRecordTime(long lastRecordTime) {
        KnowRecorderApplication.lastRecordTime = lastRecordTime;
    }

    public static long getCurrentTimeStame() {
        return currentTimeStame;
    }

    public static void setCurrentTimeStame(long timeStame) {
        KnowRecorderApplication.currentTimeStame= timeStame;
    }

    public static String getLanguage() { return language ; }
    public static String getCountry() { return country ; }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //폰인지 테블릿인지 판별
        int screenSizeType = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if(screenSizeType == Configuration.SCREENLAYOUT_SIZE_SMALL || screenSizeType == Configuration.SCREENLAYOUT_SIZE_NORMAL)
            isPhone = true;
        else
            isPhone = false;

        Locale locale = Locale.getDefault();
        language = locale.getLanguage();
        country = locale.getCountry();

        Realm.init(this);

//        RealmConfiguration config = null;
//        config = new RealmConfiguration.Builder()
//                .schemaVersion(0)
//                .migration(new MigrationIOS())
//                .build();
//        Realm.setDefaultConfiguration(config);
//        Realm realm = Realm.getDefaultInstance();
//        Log.d("Realmfile",config.toString());
//
//        try {
//            primaryKeyValue = new AtomicInteger(realm.where(Packet.class).max("id").intValue());
//        } catch (NullPointerException e) {
//            primaryKeyValue = new AtomicInteger(0);
//        }
//
//        try {
//            pdfPaperKey = new AtomicInteger(realm.where(PDFPaper.class).max("id").intValue());
//        } catch (NullPointerException e) {
//            pdfPaperKey = new AtomicInteger(0);
//        }
//
//        try {
//            noteKey = new AtomicInteger(realm.where(NoteModel.class).max("id").intValue());
//        } catch (NullPointerException e) {
//            noteKey = new AtomicInteger(0);
//        }
//
//        try {
//            remodelingKey = new AtomicInteger(realm.where(Remodeling.class).max("id").intValue());
//        } catch (NullPointerException e) {
//            remodelingKey = new AtomicInteger(0);
//        }
//
//        realm.close();


        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker("UA-81908022-2");
        }

        return mTracker;
    }
}
