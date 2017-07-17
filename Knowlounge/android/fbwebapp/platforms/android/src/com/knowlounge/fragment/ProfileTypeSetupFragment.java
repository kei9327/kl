package com.knowlounge.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by Minsu on 2016-02-15.
 */
public class ProfileTypeSetupFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SelectProfileFragment";
    private View rootView;
    private WenotePreferenceManager prefManager;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private LinearLayout profileSelectTypeBtnLayout;
    private Button btnStudent, btnTeacher;
    private TextView profileSkipBtn;
    private TextView profileStartMessage1, profileStartMessage2;

    private Context rootCtx;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        rootCtx = context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.select_profile_type, container, false);
        prefManager = WenotePreferenceManager.getInstance(getActivity());
        setFindViewById();

        setConfiguragionUiSet(getActivity().getResources().getConfiguration());

        return rootView;
    }

    private void setFindViewById() {

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        profileSelectTypeBtnLayout = (LinearLayout) rootView.findViewById(R.id.profile_select_type_btn_layout);

        btnStudent = (Button) rootView.findViewById(R.id.btn_student);
        btnTeacher = (Button) rootView.findViewById(R.id.btn_teacher);
        profileSkipBtn = (TextView) rootView.findViewById(R.id.profile_skip_btn);
        profileStartMessage1 = (TextView) rootView.findViewById(R.id.profile_start_message1);
        profileStartMessage2 = (TextView) rootView.findViewById(R.id.profile_start_message2);

        btnStudent.setOnClickListener(this);
        btnTeacher.setOnClickListener(this);
        profileSkipBtn.setOnClickListener(this);
    }


    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        Bundle argument = new Bundle();
        ProfileInitFragment setProfileFragment;
        switch (v.getId()) {
            case R.id.btn_student :
                argument.putInt("type", GlobalConst.TYPE_STUDENT_START);
                setProfileFragment = new ProfileInitFragment();
                setProfileFragment.setArguments(argument);
                fragmentTransaction.replace(R.id.profile_container, setProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.btn_teacher :
                argument.putInt("type", GlobalConst.TYPE_TEACHER_START);
                setProfileFragment = new ProfileInitFragment();
                setProfileFragment.setArguments(argument);
                fragmentTransaction.replace(R.id.profile_container, setProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.profile_skip_btn :
                getActivity().finish();

                prefManager.setProfileSkip(true);
                Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.toast_profile_nocreate),Toast.LENGTH_SHORT).show();
//                Log.d("Localeis03",Locale.getDefault().getISO3Language());
//                Log.d("LocaleLan",Locale.getDefault().getLanguage());
//                Log.d("Localedisplaylan",Locale.getDefault().getDisplayLanguage());
//                Log.d("Localecountry",Locale.getDefault().getCountry());
//                Log.d("LocaleDefault",Locale.getDefault().toString());
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setConfiguragionUiSet(newConfig);
    }

    private void setConfiguragionUiSet(Configuration newConfig) {
        if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnStudent.getLayoutParams();
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                params.rightMargin = 0;
                params.bottomMargin = (int) (10 * prefManager.getDensity());
                btnStudent.setLayoutParams(params);
                profileSelectTypeBtnLayout.setOrientation(LinearLayout.VERTICAL);
                profileStartMessage1.setVisibility(View.VISIBLE);
                profileStartMessage2.setVisibility(View.VISIBLE);
            } else {
                params.bottomMargin = 0;
                params.rightMargin = (int) (10 * prefManager.getDensity());
                btnStudent.setLayoutParams(params);
                profileSelectTypeBtnLayout.setOrientation(LinearLayout.HORIZONTAL);
                profileStartMessage1.setVisibility(View.GONE);
                profileStartMessage2.setVisibility(View.GONE);
            }
        }
    }
}
