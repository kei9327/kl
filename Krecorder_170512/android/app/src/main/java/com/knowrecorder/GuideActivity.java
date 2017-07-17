package com.knowrecorder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.knowrecorder.develop.RecordingBoardActivity;
import com.knowrecorder.develop.manager.SharedPreferencesManager;
import com.knowrecorder.phone.PhoneOpencourseActivity;

public class GuideActivity extends AppCompatActivity {

    private static final int MAX_PAGE = 3;
    private LinearLayout rootView;
    private ImageView circle1;
    private ImageView circle2;
    private ImageView circle3;
    private TextView title;
    private TextView subtitle1;
    private Tracker mTracker;
    private Button getStartedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KnowRecorderApplication application = (KnowRecorderApplication) getApplication();
        mTracker = application.getDefaultTracker();

        setContentView(R.layout.activity_guide);

        if(KnowRecorderApplication.isPhone) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        rootView = (LinearLayout) findViewById(R.id.guide_root_layout);

        circle1 = (ImageView) findViewById(R.id.circle1);
        circle2 = (ImageView) findViewById(R.id.circle2);
        circle3 = (ImageView) findViewById(R.id.circle3);

        title = (TextView) findViewById(R.id.title);
        subtitle1 = (TextView) findViewById(R.id.subtitle1);

        getStartedBtn = (Button) findViewById(R.id.get_started_btn);

        if(!KnowRecorderApplication.isPhone) {
            title.setText(getResources().getString(R.string.guide_title_1));
            subtitle1.setText(getResources().getString(R.string.guide_title_1_subtitle_1) + "\n" + getResources().getString(R.string.guide_title_1_subtitle_2));
        }else{
            subtitle1.setText(getResources().getString(R.string.intro_m1));
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        if (viewPager != null) {
            viewPager.setAdapter(new viewPagerAdapter(getSupportFragmentManager()));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }
                @Override
                public void onPageSelected(int position) {

                    circle1.setImageResource(R.drawable.ico_pagecontrol);
                    circle2.setImageResource(R.drawable.ico_pagecontrol);
                    circle3.setImageResource(R.drawable.ico_pagecontrol);

                    switch (position) {
                        case 0:
                            rootView.setBackgroundColor(getResources().getColor(R.color.guidePage1));
                            getStartedBtn.setBackgroundResource(R.drawable.bg_guide_get_started_btn1);
                            circle1.setImageResource(R.drawable.ico_pagecontrol_s);
                            break;

                        case 1:
                            rootView.setBackgroundColor(getResources().getColor(R.color.guidePage2));
                            getStartedBtn.setBackgroundResource(R.drawable.bg_guide_get_started_btn2);
                            circle2.setImageResource(R.drawable.ico_pagecontrol_s);
                            break;

                        case 2:
                            rootView.setBackgroundColor(getResources().getColor(R.color.guidePage3));
                            getStartedBtn.setBackgroundResource(R.drawable.bg_guide_get_started_btn3);
                            circle3.setImageResource(R.drawable.ico_pagecontrol_s);
                            break;
                    }

                    setServiceGuideText(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }
    private void setServiceGuideText(int position) {
        String guideText = "";
        String guideTitle = "";
        switch (position){
            case 0 :
                if(!KnowRecorderApplication.isPhone) {
                    guideTitle = getResources().getString(R.string.guide_title_1);
                    guideText = getResources().getString(R.string.guide_title_1_subtitle_1) + "\n" + getResources().getString(R.string.guide_title_1_subtitle_2);
                }else{
                    guideText = getResources().getString(R.string.intro_m1);
                }
                break;
            case 1 :
                if(!KnowRecorderApplication.isPhone) {
                    guideTitle = getResources().getString(R.string.guide_title_2);
                    guideText = getResources().getString(R.string.guide_title_2_subtitle_1);
                }else{
                    guideText = getResources().getString(R.string.intro_m2);
                }
                break;
            case 2 :
                if(!KnowRecorderApplication.isPhone) {
                    guideTitle = getResources().getString(R.string.guide_title_3);
                    guideText = getResources().getString(R.string.guide_title_3_subtitle_1) + "\n" + getResources().getString(R.string.guide_title_3_subtitle_2);
                }else{
                    guideText = getResources().getString(R.string.intro_m3);
                }
                break;
        }
        title.setText(guideTitle);
        subtitle1.setText(guideText);
    }

    private class viewPagerAdapter extends FragmentPagerAdapter {

        public viewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return GuideFragment.create(position);
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }

    public void startMainActivity(View v) {
        SharedPreferencesManager.getInstance(this).setGuideSkip(true);
        if(KnowRecorderApplication.isPhone)
            startActivity(new Intent(GuideActivity.this, PhoneOpencourseActivity.class));
        else
            startActivity(new Intent(GuideActivity.this, RecordingBoardActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("GuideActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
