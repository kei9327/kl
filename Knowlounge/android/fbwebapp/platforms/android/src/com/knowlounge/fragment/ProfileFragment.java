package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;


public class ProfileFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "ProfileFragment";
    private View rootView;

    WenotePreferenceManager prefManager;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    ImageView btnMainleftProfileBack, mainleftProfileThumbnail;
    TextView mainleftProfileId, mainleftProfileMyName, mainleftProfileSchool, mainleftProfileGrade, mainleftProfileSubject, mainleftProfileUsedLanguage, mainleftProfileIntroduce;
    LinearLayout btnMainleftMyname, btnMainleftId, btnMainleftSchool, btnMainleftGrade, btnMainleftSubject, btnMainleftUsedLanguage, btnMainleftIntroduce;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);

    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        setFindViewById();
        setUI();

        return rootView;
    }


    private void setUI() {
        mainleftProfileId.setHint(getResources().getString(R.string.profile_id_guide));
        mainleftProfileMyName.setHint(getResources().getString(R.string.profile_name_guide));
        mainleftProfileSchool.setHint(getResources().getString(R.string.profile_institution_guide));
        mainleftProfileUsedLanguage.setHint(getResources().getString(R.string.profile_language_guide));
        mainleftProfileIntroduce.setHint(getResources().getString(R.string.profile_bio_edit));
        if(prefManager.getMyUserType().equals("1")) // 학생
        {
            mainleftProfileGrade.setHint(getResources().getString(R.string.profile_grade_studentguide));
            mainleftProfileSubject.setHint(getResources().getString(R.string.profile_subject_studentguide));
        }else{
            mainleftProfileGrade.setHint(getResources().getString(R.string.profile_grade_teacherguide));
            mainleftProfileSubject.setHint(getResources().getString(R.string.profile_subject_teacherguide));
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnMainleftProfileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity._instance.getSupportFragmentManager().popBackStack();
                //getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO 프로필 설정 기본 셋팅
        mainleftProfileId.setText(prefManager.getEmail());
        mainleftProfileMyName.setText(prefManager.getUserNm());
        mainleftProfileSchool.setText(prefManager.getMyeducation());
        mainleftProfileGrade.setText(prefManager.getMyGradeName());
        mainleftProfileSubject.setText(prefManager.getMySubjectName());
        mainleftProfileUsedLanguage.setText(prefManager.getMyLanguageName());
        mainleftProfileIntroduce.setText(prefManager.getMyIntroduction());

        try {
            Glide.with(getActivity()).load(AndroidUtils.changeSizeThumbnail(prefManager.getUserThumbnail(), 200))
                                     .error(getResources().getDrawable(R.drawable.img_userlist_default01))
                                     .transform(new CircleTransformTemp(getActivity()))
                                     .into(mainleftProfileThumbnail);
        } catch (IllegalArgumentException lA) {
            lA.printStackTrace();
        }
    }

    private void setFindViewById() {

        btnMainleftProfileBack = (ImageView) rootView.findViewById(R.id.btn_mainleft_profile_back);
        mainleftProfileThumbnail = (ImageView) rootView.findViewById(R.id.mainleft_profile_thumbnail);

        mainleftProfileId = (TextView) rootView.findViewById(R.id.mainleft_profile_id);
        mainleftProfileMyName = (TextView) rootView.findViewById(R.id.mainleft_profile_my_name);
        mainleftProfileIntroduce = (TextView) rootView.findViewById(R.id.mainleft_profile_introduce);
        mainleftProfileSchool = (TextView) rootView.findViewById(R.id.mainleft_profile_school);
        mainleftProfileGrade =(TextView) rootView.findViewById(R.id.mainleft_profile_grade);
        mainleftProfileSubject = (TextView) rootView.findViewById(R.id.mainleft_profile_subject);
        mainleftProfileUsedLanguage = (TextView) rootView.findViewById(R.id.mainleft_profile_used_language);

        btnMainleftMyname = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_myname);
        btnMainleftId = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_id);
        btnMainleftUsedLanguage = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_used_language);
        btnMainleftIntroduce = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_profile_introduce);
        btnMainleftSchool = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_school);
        btnMainleftGrade = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_grade);
        btnMainleftSubject = (LinearLayout) rootView.findViewById(R.id.btn_mainleft_subject);

        btnMainleftMyname.setOnClickListener(this);
        btnMainleftId.setOnClickListener(this);
        btnMainleftSchool.setOnClickListener(this);
        btnMainleftGrade.setOnClickListener(this);
        btnMainleftSubject.setOnClickListener(this);
        btnMainleftUsedLanguage.setOnClickListener(this);
        btnMainleftIntroduce.setOnClickListener(this);
        mainleftProfileThumbnail.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mainleft_myname:
                nextEditFragment(GlobalConst.VIEW_MAINLEFT_PROFILE_MYNAME);
                break;
            case R.id.btn_mainleft_id:
                nextEditFragment(GlobalConst.VIEW_MAINLEFT_PROFILE_ID);
                break;
            case R.id.btn_mainleft_school:
                nextEditFragment(GlobalConst.VIEW_MAINLEFT_PROFILE_SCHOOL);
                break;
            case R.id.btn_mainleft_grade:
                nextMultiFragment(GlobalConst.CATEGORY_GRADE);
                break;
            case R.id.btn_mainleft_subject:
                nextMultiFragment(GlobalConst.CATEGORY_SUBJECT);
                break;
            case R.id.btn_mainleft_used_language:
                nextMultiFragment(GlobalConst.CATEGORY_LANGUAGE);
                break;
            case R.id.btn_mainleft_profile_introduce:
                nextEditFragment(GlobalConst.VIEW_MAINLEFT_PROFILE_INTRODUCE);
                break;
            case R.id.mainleft_profile_thumbnail :
//                Toast.makeText(getActivity().getBaseContext(), "준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void nextEditFragment(int type) {
        Bundle arguments = new Bundle();
        arguments.putInt("edit_type", type);
        MainActivity._instance.openUserProfieEdit(arguments);
//        ProfileEditFragment mainleftMenuProfileEditFragment = new ProfileEditFragment();
//        mainleftMenuProfileEditFragment.setArguments(arguments);
//        fragmentTransaction.replace(R.id.mainleft_container, mainleftMenuProfileEditFragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
    }


    public void nextMultiFragment(String type) {
        Bundle arguments = new Bundle();
        arguments.putString("multi_type", type);
        MainActivity._instance.openUserProfileEditMulti(arguments);
//        ProfileEditMultiFragment mainleftMenuProfileMultiFragment = new ProfileEditMultiFragment();
//        mainleftMenuProfileMultiFragment.setArguments(arguments);
//        fragmentTransaction.replace(R.id.mainleft_container, mainleftMenuProfileMultiFragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
    }
}