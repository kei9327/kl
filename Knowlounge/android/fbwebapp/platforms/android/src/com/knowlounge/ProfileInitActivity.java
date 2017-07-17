package com.knowlounge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.knowlounge.fragment.ProfileTypeSetupFragment;
import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by we160303 on 2016-05-09.
 */
public class ProfileInitActivity extends AppCompatActivity {

    private final String TAG = "ProfileInitActivity";

    WenotePreferenceManager prefManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_profile_init);

        prefManager = WenotePreferenceManager.getInstance(getApplicationContext());

//              TODO 선생 학생 선택 fragment
        ProfileTypeSetupFragment profileTypeSetupFragment = new ProfileTypeSetupFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.profile_container, profileTypeSetupFragment, "ProfileTypeSetup").commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, getSupportFragmentManager().getBackStackEntryCount() + "");
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            prefManager.setProfileSkip(true);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_profile_nocreate), Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
