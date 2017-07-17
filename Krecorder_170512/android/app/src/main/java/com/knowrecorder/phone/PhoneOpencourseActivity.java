package com.knowrecorder.phone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.Managers.NoteManager;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.ViewerListActivity;
import com.knowrecorder.develop.opencourse.ocimport.OCImportTask;
import com.knowrecorder.phone.rxevent.PlayVideo;
import com.knowrecorder.phone.rxevent.SelectTab;
import com.knowrecorder.phone.tab.GroupFragment;
import com.knowrecorder.phone.tab.HomeFragment;
import com.knowrecorder.phone.tab.setting.SettingFragment;
import com.knowrecorder.phone.tab.Subject.SubjectFragment;
import com.knowrecorder.rxjava.EventBus;

import java.util.UUID;

import rx.Subscriber;

/**
 * Created by we160303 on 2016-11-29.
 */

public class PhoneOpencourseActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "P_OCActivity";
    public static final int ALL = 0;
    public static final int MATH = 1;
    public static final int SCIENCE = 2;
    public static final int LANGUAGE = 3;
    public static final int SOCIAL = 4;
    public static final int ART = 5;
    public static final int OTHERS = 6;

    public static final int HOME_TAB = 9999;
    public static final int SUBJECT_TAB = HOME_TAB + 1;
    public static final int GROUP_TAB = SUBJECT_TAB + 1;
    public static final int SETTING_TAB = GROUP_TAB + 1;

    private SharedPreferences preferences;

    private Tracker mTracker;
    private ImageView pHomeBtn;
    private ImageView pVideoBtn;
    private ImageView pGroupBtn;
    private ImageView pSettingBtn;

    private OCImportTask importTask = null;

    private int currentTab;
    private boolean twiceBackpressedToExit = false;

    private Subscriber<? super Object> mSubscriber = new Subscriber<Object>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            if(o instanceof SelectTab){
                final SelectTab data = (SelectTab)o;
                Log.d(TAG, "Selected TAb : "+data.getTag());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (data.getTab()){
                            case HOME_TAB :
                                openHomeTab();
                                break;
                            case SUBJECT_TAB :
                                openSubjectTab(data.getSubject());
                                break;
                            case GROUP_TAB :
                                openGroupTab();
                                break;
                            case SETTING_TAB :
                                openSettingTab();
                                break;
                        }
                    }
                });

            }else if(o instanceof PlayVideo){
                final PlayVideo data = (PlayVideo)o;
                if(preferences.getBoolean("isOnlyWifi",false)){
                    if(isWifiOnline())
                        playVideo(data.getVideoId(), data.getVideoTitle());
                    else
                        dialogShow(data.getVideoId(), data.getVideoTitle());
                }else {
                    playVideo(data.getVideoId(), data.getVideoTitle());
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p_activity_opencourse);

        EventBus.getInstance().getBusObservable()
                .subscribe(mSubscriber);

        PixelUtil.getInstance().setContext(getApplicationContext());

        KnowRecorderApplication application = (KnowRecorderApplication) getApplication();
        mTracker = application.getDefaultTracker();

        pHomeBtn = (ImageView) findViewById(R.id.p_opencourse_home);
        pVideoBtn = (ImageView) findViewById(R.id.p_opencourse_video);
        pGroupBtn = (ImageView) findViewById(R.id.p_opencourse_group);
        pSettingBtn = (ImageView) findViewById(R.id.p_opencourse_setting);

        pHomeBtn.setOnClickListener(this);
        pVideoBtn.setOnClickListener(this);
        pGroupBtn.setOnClickListener(this);
        pSettingBtn.setOnClickListener(this);

        preferences = getSharedPreferences(KnowRecorderApplication.PREFERENCE_NAME, MODE_PRIVATE);
        if (!preferences.getBoolean("isGuideSkip", false)) {
            SharedPreferences.Editor editor1 = preferences.edit();
            editor1.putBoolean("isGuideSkip", true);
            editor1.commit();
        }

        openHomeTab();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mTracker.setScreenName("ViewerListActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriber.unsubscribe();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.p_opencourse_home :
                openHomeTab();
                break;

            case R.id.p_opencourse_video :
                openSubjectTab(ALL);
                break;

            case R.id.p_opencourse_group :
                openGroupTab();
                break;

            case R.id.p_opencourse_setting :
                openSettingTab();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (twiceBackpressedToExit) {
            System.exit(0); // 애플리케이션 종료
        }

        twiceBackpressedToExit = true;
        Toast.makeText(this, R.string.twice_backpress_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                twiceBackpressedToExit = false;
            }
        }, 2000);
    }

    private void openHomeTab(){
        if(currentTab == HOME_TAB)
            return;

        clearTAb();
        currentTab = HOME_TAB;
        pHomeBtn.setImageResource(R.drawable.btn_bottom_home_on);

        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.p_opencourse_container, homeFragment, "homeTab").commit();
    }
    private void openSubjectTab(int subject){
        if(currentTab == SUBJECT_TAB)
            return;

        clearTAb();
        currentTab = SUBJECT_TAB;
        pVideoBtn.setImageResource(R.drawable.btn_bottom_subjects_on);

        Bundle argument = new Bundle();
        argument.putInt("subject",subject);

        SubjectFragment subjectFragment = new SubjectFragment();
        subjectFragment.setArguments(argument);

        getSupportFragmentManager().beginTransaction().replace(R.id.p_opencourse_container, subjectFragment, "videoTab").commit();
    }
    private void openGroupTab(){
        if(currentTab == GROUP_TAB)
            return;

        clearTAb();
        currentTab = GROUP_TAB;
        pGroupBtn.setImageResource(R.drawable.btn_bottom_myschool_on);

        GroupFragment groupFragment = new GroupFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.p_opencourse_container, groupFragment, "groupTab").commit();
    }
    private void openSettingTab(){
        if(currentTab == SETTING_TAB)
            return;

        clearTAb();
        currentTab = SETTING_TAB;
        pSettingBtn.setImageResource(R.drawable.btn_bottom_setting_on);

        SettingFragment settingFragment = new SettingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.p_opencourse_container, settingFragment, "settingTab").commit();
    }

    private void clearTAb(){
        pHomeBtn.setImageResource(R.drawable.btn_bottom_home);
        pVideoBtn.setImageResource(R.drawable.btn_bottom_subjects);
        pGroupBtn.setImageResource(R.drawable.btn_bottom_myschool);
        pSettingBtn.setImageResource(R.drawable.btn_bottom_setting);
    }

    private void playVideo(int videoId, String title){
        if (isOnline()) {
            final ProgressDialog progressDialog = new ProgressDialog(PhoneOpencourseActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMax(100);
            progressDialog.setMessage(getString(R.string.opencourse_download_step1));
            progressDialog.setTitle(getString(R.string.please_wait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (importTask != null) {
                        importTask.threadCancel();
                        importTask = null;
                    }
                    dialog.dismiss();
                }
            });

            progressDialog.setProgressDrawable(getDrawableToRes(R.drawable.progress_drawable));
            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button negativeButton = progressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);

                    negativeButton.setTextColor(Color.parseColor("#a0c81e"));
                    negativeButton.setTypeface(null, Typeface.BOLD);
//                    progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
                }
            });
            progressDialog.show();

            importTask = new OCImportTask(PhoneOpencourseActivity.this, progressDialog, videoId, title);
            importTask.execute();
        } else {
            Toast.makeText(PhoneOpencourseActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean isWifiOnline() {
        try{
            ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
            if(wifi == NetworkInfo.State.CONNECTED){
                return true;
            }
        }catch (NullPointerException npe){
            return false;
        }
        return false;
    }

    private void dialogShow(final int videoId, final String title){
        AlertDialog.Builder alertDilog = new AlertDialog.Builder(this);
        alertDilog.setMessage(getResources().getString(R.string.c_wifi))
                  .setCancelable(false)
                //todo 다이얼로그 yes 에서 확인 문구로 바꾸기
                  .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          SharedPreferences.Editor editor1 = preferences.edit();
                          editor1.putBoolean("isOnlyWifi", false);
                          editor1.commit();

                          playVideo(videoId, title);
                      }
                  })
                  .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                      }
                  })
                  .show();
    }


    private Drawable getDrawableToRes(int resID){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resID, getTheme());
        } else {
            return getResources().getDrawable(resID);
        }
    }
}
