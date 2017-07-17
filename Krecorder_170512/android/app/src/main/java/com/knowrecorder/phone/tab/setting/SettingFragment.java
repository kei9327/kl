package com.knowrecorder.phone.tab.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by we160303 on 2016-11-29.
 */

public class SettingFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private SharedPreferences preferences;

    private Switch playLandscape;
    private Switch playOnlyWifi;

    private LinearLayout videoMore;
    private LinearLayout realtimeMore;
    private LinearLayout inquireBtn;
    private LinearLayout termsOfUse;
    private LinearLayout privacyPolicy;
    private LinearLayout realTImeExplain;
    private TextView videoExplain;
    private TextView versionText;

    private ImageView videoMoreImg;
    private ImageView realtimeMoreImg;

    private TextView connectKnowlounge;
    private final String knowLoungePackageName = "com.knowlounge";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.p_fragment_setting, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferences = getActivity().getSharedPreferences(KnowRecorderApplication.PREFERENCE_NAME, MODE_PRIVATE);

        playLandscape = (Switch) rootView.findViewById(R.id.play_landscape);
        playOnlyWifi = (Switch) rootView.findViewById(R.id.play_only_wifi);

        videoMore = (LinearLayout) rootView.findViewById(R.id.how_video_more);
        realtimeMore = (LinearLayout) rootView.findViewById(R.id.how_realtime_more);
        inquireBtn = (LinearLayout) rootView.findViewById(R.id. inquire_btn);
        termsOfUse = (LinearLayout) rootView.findViewById(R.id.terms_of_use);
        privacyPolicy = (LinearLayout) rootView.findViewById(R.id.privacy_policy);
        videoExplain = (TextView) rootView.findViewById(R.id.how_video_explain);
        realTImeExplain = (LinearLayout) rootView.findViewById(R.id.how_realtime_explain);
        connectKnowlounge = (TextView) rootView.findViewById(R.id.connect_knowlounge);
        versionText = (TextView) rootView.findViewById(R.id.version_text);

        videoMoreImg = (ImageView) rootView.findViewById(R.id.how_video_more_img);
        realtimeMoreImg = (ImageView) rootView.findViewById(R.id.how_realtime_more_img);

        playLandscape.setChecked(preferences.getBoolean("isOnlyLandscape",false));
        playOnlyWifi.setChecked(preferences.getBoolean("isOnlyWifi",false));

        playLandscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor1 = preferences.edit();
                editor1.putBoolean("isOnlyLandscape", isChecked);
                editor1.commit();
            }
        });
        playOnlyWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor1 = preferences.edit();
                editor1.putBoolean("isOnlyWifi", isChecked);
                editor1.commit();
            }
        });

        try {
            versionText.setText(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0).versionName);
        }catch (PackageManager.NameNotFoundException e){
        }
        inquireBtn.setOnClickListener(this);
        termsOfUse.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);

        videoMore.setOnClickListener(listener);
        realtimeMore.setOnClickListener(listener);
        connectKnowlounge.setOnClickListener(listener);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Bundle argument = new Bundle();
        switch (v.getId()) {

            case R.id.inquire_btn:
                argument.putString("type","I");
                argument.putString("uri","https://docs.google.com/forms/d/1BqC9Y1OPie3UH_MZ808HhmF6kE5O1bElsQ4eZ3UBrhw/viewform");
                break;
            case R.id.terms_of_use :
                argument.putString("type","T");
                argument.putString("uri","https://www.knowlounges.com/fb/terms");
                break;
            case R.id.privacy_policy :
                argument.putString("type","P");
                argument.putString("uri","https://www.knowlounges.com/fb/privacy");
                break;
        }
        PhoneSettingWebViewActivity fragment = new PhoneSettingWebViewActivity();
        fragment.setArguments(argument);
        fragment.show(getFragmentManager(), "contact");
    }
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.how_video_more:

                    if (videoExplain.isShown()) {
                        videoExplain.setVisibility(View.GONE);
                        videoMoreImg.setImageResource(R.drawable.btn_setting_fold);
                    } else {
                        videoExplain.setVisibility(View.VISIBLE);
                        videoMoreImg.setImageResource(R.drawable.btn_setting_fold_on);
                    }

                    break;
                case R.id.how_realtime_more:
                    if (realTImeExplain.isShown()) {
                        realTImeExplain.setVisibility(View.GONE);
                        realtimeMoreImg.setImageResource(R.drawable.btn_setting_fold);
                    } else {
                        realTImeExplain.setVisibility(View.VISIBLE);
                        realtimeMoreImg.setImageResource(R.drawable.btn_setting_fold_on);
                    }
                    break;
                case R.id.connect_knowlounge :

                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + knowLoungePackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + knowLoungePackageName)));
                    }

                    break;
            }

        }
    };
}
