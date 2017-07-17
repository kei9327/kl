package com.knowlounge.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.knowlounge.MainActivity;
import com.knowlounge.ProfileInitActivity;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class AppConfigFragment extends Fragment {

    private final String TAG = "AppConfigFragment";

    private DropboxAPI dropboxAPI;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    private final static String ACCESS_KEY = "22kds8gc99w5fss";
    private final static String ACCESS_SECRET = "5ejbnibofa7t1vg";

    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    @BindView(R.id.btn_mainleft_setting_back) ImageView btnMainleftSettingBack;
    @BindView(R.id.establish_closed) LinearLayout establishClosed;
    @BindView(R.id.mainleft_setting_profile) LinearLayout mainleftSettingProfile;
    @BindView(R.id.btn_mainleft_dropbox) LinearLayout btnMainleftDropbox;
    @BindView(R.id.mainleft_beeper) LinearLayout mainleftBeeper;
    @BindView(R.id.service_access_terms) LinearLayout service_access_terms;
    @BindView(R.id.user_info_treaty) LinearLayout user_info_treaty;

    @BindView(R.id.mainleft_dropbox_id) TextView mainleftDropboxId;
    @BindView(R.id.mainleft_setting_ver) TextView mainleftSettingVer;

    @BindView(R.id.mainleft_push_toggle) SwitchCompat mainleftPushTogle;
    @BindView(R.id.switch_class_openflag) SwitchCompat classOpenFlagSwith;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private WenotePreferenceManager prefManager;

    private static String dropboxEmail = "";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "<onCreateView / Knowlounge>");
        View view = inflater.inflate(R.layout.fragment_app_config, container, false);
        ButterKnife.bind(this, view);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        setUpView();

        btnMainleftSettingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity._instance.popFragmentStack();



                //getFragmentManager().popBackStack();
            }
        });

        String accessToken = prefManager.getAccessToken();

        if (accessToken.length() != 0) {
            AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
            AndroidAuthSession session = new AndroidAuthSession(appKeyPair, accessToken);
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        } else {
            AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
            AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        }


        classOpenFlagSwith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefManager.setEstablish(isChecked == true ? GlobalConst.ESTABLISH_ALL_PUBLIC : GlobalConst.ESTABLISH_CLOSED);
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        switch (prefManager.getEstablish()) {
            case GlobalConst.ESTABLISH_CLOSED :
                classOpenFlagSwith.setChecked(false);
                break;
            case GlobalConst.ESTABLISH_ALL_PUBLIC :
                classOpenFlagSwith.setChecked(true);
                break;
        }

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                Log.d(TAG, "login");
                mDBApi.getSession().finishAuthentication();
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                prefManager.setAccessToken(accessToken);

                new DropboxAccountInfoTask().execute();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else if (mDBApi.getSession().isLinked()) {
            mainleftDropboxId.setText(getResources().getString(R.string.setting_dropbox_guide));
        }

        mainleftPushTogle.setChecked(prefManager.getNotification_message());

        try {
            PackageInfo pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            mainleftSettingVer.setText(pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    private void setUpView() {
        mainleftPushTogle.setShowText(false);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_mainleft_dropbox)
    void OnClickDropBoxItem() {
        if (prefManager.getDropboxLoggedIn()) {
            mDBApi.getSession().unlink();
            mainleftDropboxId.setText(getResources().getString(R.string.setting_dropbox_guide));
            prefManager.setAccessToken("");
            prefManager.setDropboxLoggedIn(false);
        } else {
            mDBApi.getSession().startOAuth2Authentication(getActivity());
            prefManager.setDropboxLoggedIn(true);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.mainleft_beeper)
    void OnClickBeeperItem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$AppNotificationSettingsActivity");
            intent.putExtra("app_package", getActivity().getPackageName());
            intent.putExtra("app_uid", getActivity().getApplicationInfo().uid);
            startActivity(intent);
        }else{
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName("com.android.settings","com.android.settings.SoundSettings"));
            startActivity(i);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.mainleft_push_toggle)
    void OnClickPushSwitch() {
        prefManager.setNotification(GlobalConst.NOTIFICATION_MESSAGE);
        mainleftPushTogle.setChecked(prefManager.getNotification_message());
        Log.d(TAG, Boolean.toString(prefManager.getNotification_message()));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.mainleft_setting_profile)
    void OnClickProfileItem() {
        if(prefManager.getMyUserType().equals("0")){
            Context ctx = getActivity();
            Intent intent = new Intent(ctx, ProfileInitActivity.class);
            startActivity(intent);
        } else {
            MainActivity._instance.openUserProfile();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.service_access_terms)
    void OnClickTermsItem() {
        nextFragment("T","https://www.knowlounges.com/fb/terms","terms");
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.user_info_treaty)
    void OnClickPrivacyItem() {
        nextFragment("P","https://www.knowlounges.com/fb/privacy","privacy");
    }


    private void nextFragment(String type, String uri, String tag){
        Bundle argument = new Bundle();
        argument.putString("type", type);
        argument.putString("uri", uri);
        FullScreenWebViewFragment fragment = new FullScreenWebViewFragment();
        fragment.setArguments(argument);
        fragment.show(getFragmentManager(), tag);
    }

    private class DropboxAccountInfoTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                dropboxEmail = mDBApi.accountInfo().email;
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mainleftDropboxId.setText(dropboxEmail);
        }
    }
}