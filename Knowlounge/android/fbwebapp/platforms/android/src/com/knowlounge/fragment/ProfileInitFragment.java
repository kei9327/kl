package com.knowlounge.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.adapter.SelectItemRecyclerAdapter;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.Fitzpatrick;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Minsu on 2016-02-15.
 */
public class ProfileInitFragment extends Fragment implements ProfileMultiSelectItemFragment.SubmitListener {

    private static final String TAG = "ProfileInitFragment";
    private final int TYPE_STUDENT = 1;
    private final int TYPE_TEACHER = 2;

    private final int SPAN_TABLET = 340;
    private final int SPAN_PHONE_LANDSCAPE = 500;
    private final int SPAN_PHONE_PORTRAIT = 225;

    private final int BLANK = 0;
    private View rootView;
    private WenotePreferenceManager prefManager;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private EditText profileNameEdit, profileIdEdit, profileSchoolEdit;
    private Button profileSubmitBtn;
    private TextView profileGradeGuide, profileTitle;
    private String resultGrade="", resultSubject="", resultLanguage="";
    private String resultGradeNm = "";

    private RecyclerView subjectSelectedView;
    private RecyclerView languageSelectedView;
    private SelectItemRecyclerAdapter subjectAdapter;
    private SelectItemRecyclerAdapter languageAdapter;
    GridLayoutManager mSubjectLayoutManager, mLanguageLayoutManager;

    private int type;
    private String category = "";
    private int recyclerSpan;
    private boolean isStudent;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        type = getArguments().getInt("type");
        if(type == GlobalConst.TYPE_STUDENT_START) {
            rootView = inflater.inflate(R.layout.set_profile_student, container, false);
            isStudent = true;
        }else {
            rootView = inflater.inflate(R.layout.set_profile_teacher, container, false);
            isStudent = false;
        }

        prefManager = WenotePreferenceManager.getInstance(getActivity());
        ProfileMultiSelectItemFragment.setSubmitListener(this);
        setFindViewById();
        setUI();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        profileSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject obj;
                    int userType = isStudent ? TYPE_STUDENT : TYPE_TEACHER;
                    String name = profileNameEdit.getText().toString().length() != BLANK ? profileNameEdit.getText().toString() : "";
                    String school = profileSchoolEdit.getText().toString().length() != BLANK ? profileSchoolEdit.getText().toString() : "";
                    String grade = resultGrade.length() != BLANK ? resultGrade : "";
                    String subject = resultSubject.length() != BLANK ? resultSubject : "";
                    String language = resultLanguage.length() != BLANK ? resultLanguage : "";


                    switch (AndroidUtils.checkNameInput(profileNameEdit.getText().toString())){
                        case 1 :
                            Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.profile_name_guide), Toast.LENGTH_SHORT).show();return;
                        case 2 :
                            Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.profile_name_invalid), Toast.LENGTH_SHORT).show();return;
                        case 0 :
                            if(CommonUtils.isValidEmailAddress(profileIdEdit.getText().toString())){
                                obj = new JSONObject();
                                obj.put("name", AndroidUtils.spaceRemove(name));
                                obj.put("email",profileIdEdit.getText().toString());
                                obj.put("usertype", userType);
                                if(school != null && school.length() != BLANK){
                                    if(!AndroidUtils.hasEmoji(school)) {
                                        JSONArray jsonArr = new JSONArray();
                                        String[] tempArr = school.split(",");
                                        for (String data : tempArr)
                                            jsonArr.put(AndroidUtils.spaceRemove(data));
                                        obj.put("school", jsonArr);
                                    }else{
                                        Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_school_invalid), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if (grade != null && grade.length() != BLANK) {
                                    JSONArray jsonArr = new JSONArray();
                                    String[] tempArr = grade.split(",");
                                    for(String data : tempArr)
                                        jsonArr.put(data);
                                    obj.put("grade", jsonArr);
                                }
                                if (subject != null && subject.length() != BLANK) {
                                    JSONArray jsonArr = new JSONArray();
                                    String[] tempArr = subject.split(",");
                                    for(String data : tempArr)
                                        jsonArr.put(data);
                                    obj.put("subject", jsonArr);
                                }
                                if (language != null && language.length() != BLANK) {
                                    JSONArray jsonArr = new JSONArray();
                                    String[] tempArr = language.split(",");
                                    for(String data : tempArr)
                                        jsonArr.put(data);
                                    obj.put("language", jsonArr);
                                }
                                Log.d("setProfile_result : ", obj.toString());
                                setProfileRestClient(obj);
                            }else{
                                Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.profile_id_invalid), Toast.LENGTH_SHORT).show();
                                return;
                            }
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }

            }
        });
        ((LinearLayout)rootView.findViewById(R.id.profile_grade_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle argument = new Bundle();
                argument.putInt("start_type",type);
                argument.putString("check_data",resultGrade);
                argument.putString("category", GlobalConst.CATEGORY_GRADE);
                ProfileMultiSelectItemFragment fragment = new ProfileMultiSelectItemFragment();
                fragment.setArguments(argument);
                fragmentTransaction.replace(R.id.profile_container,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                hideKeyboard();
            }
        });
        ((LinearLayout)rootView.findViewById(R.id.profile_subject_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle argument = new Bundle();
                argument.putInt("start_type",type);
                argument.putString("check_data",resultSubject);
                argument.putString("category", GlobalConst.CATEGORY_SUBJECT);
                ProfileMultiSelectItemFragment fragment = new ProfileMultiSelectItemFragment();
                fragment.setArguments(argument);
                fragmentTransaction.replace(R.id.profile_container,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                hideKeyboard();
            }
        });
        ((LinearLayout)rootView.findViewById(R.id.profile_language_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle argument = new Bundle();
                argument.putInt("start_type",type);
                argument.putString("check_data",resultLanguage);
                argument.putString("category", GlobalConst.CATEGORY_LANGUAGE);
                ProfileMultiSelectItemFragment fragment = new ProfileMultiSelectItemFragment();
                fragment.setArguments(argument);
                fragmentTransaction.replace(R.id.profile_container,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                hideKeyboard();
            }
        });
        ((TextView)rootView.findViewById(R.id.profile_skip_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //MainActivity._instance.getSupportFragmentManager().popBackStack();
                getActivity().finish();
                prefManager.setProfileSkip(true);
                Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.toast_profile_nocreate),Toast.LENGTH_SHORT).show();
                hideKeyboard();
            }
        });

        profileNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == BLANK)
                    isBlankImgSet(((ImageView) rootView.findViewById(R.id.user_name_img)),"ico_firstprofile_name", true);
                else
                    isBlankImgSet(((ImageView) rootView.findViewById(R.id.user_name_img)),"ico_firstprofile_name",false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        profileIdEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length() == BLANK)
                    isBlankImgSet(((ImageView) rootView.findViewById(R.id.email_img)),"ico_firstprofile_email", true);
                else
                    isBlankImgSet(((ImageView) rootView.findViewById(R.id.email_img)),"ico_firstprofile_email",false);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        profileSchoolEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length() == BLANK)
                    isBlankImgSet(((ImageView) rootView.findViewById(R.id.school_img)),"ico_firstprofile_institution", true);
                else
                    isBlankImgSet(((ImageView) rootView.findViewById(R.id.school_img)),"ico_firstprofile_institution",false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void isBlankImgSet(ImageView view, String imgName, boolean isblank) {
        int resource;
        if(isblank){
            if(isStudent){
                resource = getResources().getIdentifier(imgName + "_dis_green", "drawable", getActivity().getPackageName());
            }else{
                resource = getResources().getIdentifier(imgName + "_dis_blue", "drawable", getActivity().getPackageName());
            }
        }else{
            resource = getResources().getIdentifier(imgName , "drawable", getActivity().getPackageName());
        }
        view.setImageResource(resource);
    }

    private void setProfileRestClient(JSONObject obj) {

        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RestClient.postWithJsonString(getContext(),"profile/setProfile.json",masterCookie, checksumCookie, obj, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "setProfile.json success / response : " + obj.toString());
                try {
                    String result = obj.getString("result");
                    if (result.equals("0")) {

                        MainActivity._instance.getProfile();


                        Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.toast_profile_create),Toast.LENGTH_SHORT).show();
                        prefManager.setChangeProfile(true);
                        getActivity().finish();
                        hideKeyboard();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리

            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
    }

    private void setUI() {


        profileNameEdit.setText(prefManager.getUserNm());
        profileIdEdit.setText(prefManager.getEmail());

        profileNameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                switch (AndroidUtils.checkNameInput(profileNameEdit.getText().toString())){
                    case 1 :
                        Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.profile_name_guide), Toast.LENGTH_SHORT).show();break;
                    case 2:
                        Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.profile_name_invalid), Toast.LENGTH_SHORT).show();break;
                }
            }
        });
        pretreatBlank();
        if(isStudent) {
            profileTitle.setText(getResources().getString(R.string.profile_title_student));
            ((TextView) rootView.findViewById(R.id.subject_guide_text)).setText(getResources().getString(R.string.profile_subject_studentguide));
            profileGradeGuide.setHint(getResources().getString(R.string.profile_grade_studentguide));
            profileGradeGuide.setText(resultGradeNm);

            subjectSelectedView.setBackgroundColor(Color.parseColor("#1ecd5a"));
            languageSelectedView.setBackgroundColor(Color.parseColor("#1ecd5a"));
        }else{

            profileTitle.setText(getResources().getString(R.string.profile_title_teacher));

            ((TextView) rootView.findViewById(R.id.subject_guide_text)).setText(getResources().getString(R.string.profile_subject_teacherguide));

            profileGradeGuide.setHint(getResources().getString(R.string.profile_grade_teacherguide));
            profileGradeGuide.setText(resultGradeNm);
            subjectSelectedView.setBackgroundColor(Color.parseColor("#00c3f0"));
            languageSelectedView.setBackgroundColor(Color.parseColor("#00c3f0"));
        }
        if(CommonUtils.isValidEmailAddress(prefManager.getEmail())){
            profileIdEdit.setClickable(false);
            profileIdEdit.setFocusable(false);
        }else{
            Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.profile_id_invalid), Toast.LENGTH_SHORT).show();
        }
        getRecyclerSpan();
    }

    private void pretreatBlank() {

        if(profileNameEdit.getText().toString().length() == BLANK)
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.user_name_img)),"ico_firstprofile_name", true);
        else
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.user_name_img)),"ico_firstprofile_name",false);

        if(profileIdEdit.getText().toString().length() == BLANK)
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.email_img)),"ico_firstprofile_email", true);
        else
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.email_img)),"ico_firstprofile_email",false);

        if(profileSchoolEdit.getText().toString().length() == BLANK)
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.school_img)),"ico_firstprofile_institution", true);
        else
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.school_img)),"ico_firstprofile_institution",false);

        if(resultGrade.length() == BLANK)
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.grade_img)),"ico_firstprofile_grade", true);
        else
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.grade_img)),"ico_firstprofile_grade",false);

        if(resultSubject.length() == BLANK) {
            subjectSelectedView.setVisibility(View.GONE);
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.subject_img)), "ico_firstprofile_subject", true);
        }else {
            subjectSelectedView.setVisibility(View.VISIBLE);
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.subject_img)), "ico_firstprofile_subject", false);
        }

        if(resultLanguage.length() == BLANK) {
            languageSelectedView.setVisibility(View.GONE);
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.language_img)), "ico_firstprofile_language", true);
        }else {
            languageSelectedView.setVisibility(View.VISIBLE);
            isBlankImgSet(((ImageView) rootView.findViewById(R.id.language_img)), "ico_firstprofile_language", false);
        }
    }

    private void getRecyclerSpan() {
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
            recyclerSpan = SPAN_TABLET;
        }else{
            recyclerSpan = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? SPAN_PHONE_LANDSCAPE : SPAN_PHONE_PORTRAIT;
        }
    }


    private String removeEmojis(String s) {
        for(Fitzpatrick fitzpatrick : Fitzpatrick.values()){
            s = s.replaceAll(fitzpatrick.unicode, "");
        }
        for(Emoji emoji : EmojiManager.getAll()){
            s = s.replaceAll(emoji.getUnicode(), "");
        }
        return s;
    }

    private void setFindViewById() {

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        profileNameEdit = (EditText) rootView.findViewById(R.id.profile_name_edit);
        profileIdEdit = (EditText) rootView.findViewById(R.id.profile_id_edit);
        profileSchoolEdit = (EditText) rootView.findViewById(R.id.profile_school_edit);
        profileGradeGuide = (TextView) rootView.findViewById(R.id.profile_grade_guide);
        profileSubmitBtn = (Button) rootView.findViewById(R.id.profile_submit_btn);
        profileTitle = (TextView) rootView.findViewById(R.id.profile_title);

        subjectSelectedView = (RecyclerView) rootView.findViewById(R.id.subject_selected_view);
        languageSelectedView  = (RecyclerView) rootView.findViewById(R.id.language_selected_view);

        if(subjectAdapter != null) {

            mSubjectLayoutManager = new GridLayoutManager(getActivity(), (int)(((recyclerSpan*prefManager.getDensity())*prefManager.getDensity()))){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            mSubjectLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
                @Override
                public int getSpanSize(int position) {
                    return getSpanNum(subjectAdapter.getItemNm(position));
                }
            });

            subjectSelectedView.setAdapter(subjectAdapter);
            subjectSelectedView.setHasFixedSize(true);
            subjectSelectedView.setLayoutManager(mSubjectLayoutManager);
        }
        if(languageAdapter != null){
            mLanguageLayoutManager = new GridLayoutManager(getActivity(),(int)(((recyclerSpan*prefManager.getDensity())*prefManager.getDensity()))){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            mLanguageLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
                @Override
                public int getSpanSize(int position) {
                    return getSpanNum(languageAdapter.getItemNm(position));
                }
            });

            languageSelectedView.setAdapter(languageAdapter);
            languageSelectedView.setHasFixedSize(true);
            languageSelectedView.setLayoutManager(mLanguageLayoutManager);
        }
    }

    private ArrayList<String> transformArray(String[] data) {
        ArrayList<String> result = new ArrayList<>();
        for(String item: data){
            result.add(item);
        }
        return result;
    }


    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onClickSubmitListener(String nameStr, String codeStr, String category) {
        this.category = category;
        if(category.equals(GlobalConst.CATEGORY_GRADE)){
            resultGrade = codeStr;
            resultGradeNm = nameStr;
            Log.d("grade_name",nameStr);
            Log.d("grade_code",codeStr);

        }
        if(category.equals(GlobalConst.CATEGORY_SUBJECT)){
            resultSubject = codeStr;
            Log.d("subject",codeStr);
            subjectAdapter = new SelectItemRecyclerAdapter(getContext(), transformArray(nameStr.split(",")), type);

        }else if(category.equals(GlobalConst.CATEGORY_LANGUAGE)){
            resultLanguage = codeStr;
            Log.d("language",codeStr);
            languageAdapter = new SelectItemRecyclerAdapter(getContext(), transformArray(nameStr.split(",")), type);
        }
    }
    public int getSpanNum(String name){
        View view = getActivity().getLayoutInflater().inflate(R.layout.item_chips,null,false);
        ((TextView) view.findViewById(R.id.item_nm)).setText(name);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d("viewRect multi density",Integer.toString((int)(view.getMeasuredWidth()*prefManager.getDensity())));
        return (int)((view.getMeasuredWidth()+(10*prefManager.getDensity()))*prefManager.getDensity());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                recyclerSpan = SPAN_PHONE_LANDSCAPE;
            else
                recyclerSpan = SPAN_PHONE_PORTRAIT;

                Log.d("screen","화면 전환 됨, recyclerSpan : " + recyclerSpan);
            if(subjectAdapter != null) {
                mSubjectLayoutManager = new GridLayoutManager(getActivity(), (int) (((recyclerSpan * prefManager.getDensity()) * prefManager.getDensity()))) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                mSubjectLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return getSpanNum(subjectAdapter.getItemNm(position));
                    }
                });
                subjectSelectedView.setLayoutManager(mSubjectLayoutManager);
                subjectAdapter.notifyDataSetChanged();
            }
            if(languageAdapter != null){
                mLanguageLayoutManager = new GridLayoutManager(getActivity(),(int)(((recyclerSpan*prefManager.getDensity())*prefManager.getDensity()))){
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                mLanguageLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
                    @Override
                    public int getSpanSize(int position) {
                        return getSpanNum(languageAdapter.getItemNm(position));
                    }
                });
                languageSelectedView.setLayoutManager(mLanguageLayoutManager);
                languageAdapter.notifyDataSetChanged();
            }

        }
    }
}
