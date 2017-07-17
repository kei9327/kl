package com.knowrecorder;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.knowrecorder.Utils.PermissionChecker;
import com.knowrecorder.develop.RecordingBoardActivity;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.SharedPreferencesManager;
import com.knowrecorder.phone.PhoneOpencourseActivity;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private Tracker mTracker;
    private int permittionRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KnowRecorderApplication application = (KnowRecorderApplication) getApplication();
        mTracker = application.getDefaultTracker();
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_splash);

        FilePath.setFilePath(this);

        if(KnowRecorderApplication.isPhone) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            ((ImageView) findViewById(R.id.splash_img)).setImageResource(R.drawable.img_splash_ph);
            ((RelativeLayout) findViewById(R.id.activity_splash)).setBackgroundColor(Color.parseColor("#96c81e"));
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            ((ImageView) findViewById(R.id.splash_img)).setImageResource(R.drawable.img_splash);
            ((RelativeLayout) findViewById(R.id.activity_splash)).setBackgroundColor(Color.parseColor("#ffffff"));
        }

        PermissionChecker pChecker = new PermissionChecker();
        pChecker.check(this, permittionRequestCode);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //SharedPreferencesManager.getInstance(this).deleteOldViewersData();
        }




        //todo 해상도 로그
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Log.d("KnowRecorder", "Display Resolution[ Width :  " + display.getWidth() + " Height : " + display.getHeight() + "]");

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        Log.d("KnowRecorder", "Dvice Dencity : " + metrics.scaledDensity);


    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("SplashActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void runActivity()
    {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (SharedPreferencesManager.getInstance(SplashActivity.this).getGuideSkip()) {
                    if(KnowRecorderApplication.isPhone)
                        startActivity(new Intent(SplashActivity.this, PhoneOpencourseActivity.class));
                    else
                        startActivity(new Intent(getBaseContext(), RecordingBoardActivity.class ));
                } else {
                    startActivity(new Intent(getBaseContext(), GuideActivity.class));
                }

                finish();
            }
        }, 2000);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == permittionRequestCode){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("[PERMISSION]", permissions + "was granted" );
            }else{
                Log.e("[PERMISSION]", permissions + "was denied" );
            }
        }
        SharedPreferencesManager sp = SharedPreferencesManager.getInstance(this);
        if(sp.getFristLauncher()){
            sp.setFirstLauncher(false);
            sp.setRecentDataSize(sp.getRecentDataSzie());
        }

        runActivity();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
