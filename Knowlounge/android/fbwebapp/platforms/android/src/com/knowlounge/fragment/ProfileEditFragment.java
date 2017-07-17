package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class ProfileEditFragment extends Fragment
{
    private final String TAG = "ProfileEditFragment";
    private final int introduceHeight = 150;
    private final int MAX_LENGTH_NAME = 40;
    private final int MAX_LENGTH_BIO  = 200;
    private View rootView;

    WenotePreferenceManager prefManager;

    ImageView btnMainleftProfileEditBack;
    TextView mainleftProfileEditTitle, mainleftProfileEditSubtext, mainleftProfileEditCommitBtn;
    EditText mainleftProfileEditEdittext;
    int editType;
    String updateText;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        setFindViewById();
        editType = getArguments().getInt("edit_type");
        initSetting(editType);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnMainleftProfileEditBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.keyboardHide(getActivity());
                MainActivity._instance.getSupportFragmentManager().popBackStack();
                //getFragmentManager().popBackStack();
            }
        });
        mainleftProfileEditCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateText = AndroidUtils.spaceRemove(mainleftProfileEditEdittext.getText().toString());

                AndroidUtils.keyboardHide(getActivity());
                if (editType == GlobalConst.VIEW_MAINLEFT_PROFILE_MYNAME) {
                    switch (AndroidUtils.checkNameInput(updateText)) {
                        case 0:
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("name", updateText);
                                updateProfile(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return;
                        case 1:
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_name_empty), Toast.LENGTH_LONG).show();
                            return;
                        case 2:
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_name_invalid), Toast.LENGTH_LONG).show();
                            return;
                        case 3:
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.error_cant_char_lmt), Toast.LENGTH_LONG).show();
                            return;
                    }
                } else if (editType == GlobalConst.VIEW_MAINLEFT_PROFILE_ID) {
                    if (updateText.length() != 0) {
                        if (CommonUtils.isValidEmailAddress(updateText)) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("email", updateText);
                                updateProfile(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_id_invalid), Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_id_empty), Toast.LENGTH_LONG).show();
                        return;
                    }

                } else if (editType == GlobalConst.VIEW_MAINLEFT_PROFILE_INTRODUCE) {
                    if (updateText.length() <= 200) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("bio", updateText);
                            updateProfile(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_bio_guide) , Toast.LENGTH_LONG).show();
                    }
                } else if (editType == GlobalConst.VIEW_MAINLEFT_PROFILE_SCHOOL) {
                    if (!AndroidUtils.hasEmoji(updateText) && !AndroidUtils.checkSpecialChar(updateText)) {
                        try {
                            JSONObject obj = new JSONObject();
                            JSONArray arr = new JSONArray();
                            arr.put(updateText);
                            obj.put("school", arr);
                            updateProfile(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.profile_school_invalid), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    private void updateProfile(JSONObject obj) {
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RestClient.postWithJsonString(getActivity(),"/profile/updateProfile.json",masterCookie, checksumCookie, obj, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "updateProfile.json success / response : " + obj.toString());
                try {
                    String result = obj.getString("result");
                    if (result.equals("0")) {
                        switch (editType) {
                            case GlobalConst.VIEW_MAINLEFT_PROFILE_MYNAME : prefManager.setUserNm(updateText);break;
                            case GlobalConst.VIEW_MAINLEFT_PROFILE_ID: prefManager.setEmail(updateText);break;
                            case GlobalConst.VIEW_MAINLEFT_PROFILE_INTRODUCE : prefManager.setMyIntroduction(updateText);break;
                            case GlobalConst.VIEW_MAINLEFT_PROFILE_SCHOOL : prefManager.setMyeducation(updateText);break;
                        }

                        Toast.makeText(getContext(), getResources().getString(R.string.toast_profile_change), Toast.LENGTH_LONG).show();
                        getFragmentManager().popBackStack();
                        prefManager.setChangeProfile(true);
                        MainActivity._instance.reloadAuthInfo();
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


    @Override
    public void onResume() {
        super.onResume();
        switch (editType) {
            case GlobalConst.VIEW_MAINLEFT_PROFILE_MYNAME :
                mainleftProfileEditEdittext.setText(prefManager.getUserNm());
                mainleftProfileEditEdittext.setHint(getResources().getString(R.string.profile_name_guide));
                break;
            case GlobalConst.VIEW_MAINLEFT_PROFILE_ID :
                mainleftProfileEditEdittext.setText(prefManager.getEmail());
                mainleftProfileEditEdittext.setHint(getResources().getString(R.string.profile_id_guide));
                break;
            case GlobalConst.VIEW_MAINLEFT_PROFILE_INTRODUCE :
                mainleftProfileEditEdittext.setText(prefManager.getMyIntroduction());
                mainleftProfileEditEdittext.setHint(getResources().getString(R.string.profile_bio_guide));
                break;
            case GlobalConst.VIEW_MAINLEFT_PROFILE_SCHOOL :
                mainleftProfileEditEdittext.setText(prefManager.getMyeducation());
                mainleftProfileEditEdittext.setHint(getResources().getString(R.string.profile_institution_guide));
                break;

        }
    }


    private void setFindViewById() {
        btnMainleftProfileEditBack = (ImageView) rootView.findViewById(R.id.btn_mainleft_profile_edit_back);
        mainleftProfileEditTitle = (TextView) rootView.findViewById(R.id.mainleft_profile_edit_title);
        mainleftProfileEditSubtext = (TextView) rootView.findViewById(R.id.mainleft_profile_edit_subtext);
        mainleftProfileEditCommitBtn = (TextView) rootView.findViewById(R.id.mainleft_profile_edit_commit_btn);
        mainleftProfileEditEdittext = (EditText) rootView.findViewById(R.id.mainleft_profile_edit_edittext);
    }


    private void initSetting(int type) {
        switch (type) {
            case GlobalConst.VIEW_MAINLEFT_PROFILE_MYNAME :
                mainleftProfileEditEdittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH_NAME)});
                mainleftProfileEditTitle.setText(getResources().getString(R.string.profile_name));
                mainleftProfileEditSubtext.setText(getResources().getString(R.string.profile_name_edit));
                mainleftProfileEditEdittext.setSingleLine(true);
                break;
            case GlobalConst.VIEW_MAINLEFT_PROFILE_ID :
                mainleftProfileEditTitle.setText(getResources().getString(R.string.profile_id));
                mainleftProfileEditSubtext.setText(R.string.profile_id_edit);
                mainleftProfileEditEdittext.setSingleLine(true);
                break;
            case GlobalConst.VIEW_MAINLEFT_PROFILE_INTRODUCE :
                mainleftProfileEditEdittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH_BIO)});
                mainleftProfileEditTitle.setText(getResources().getString(R.string.profile_bio));
                mainleftProfileEditSubtext.setText(getResources().getString(R.string.profile_bio_edit));
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mainleftProfileEditEdittext.getLayoutParams();
                params.height = AndroidUtils.getPxFromDp(getContext(),introduceHeight);
                mainleftProfileEditEdittext.setLayoutParams(params);
                mainleftProfileEditEdittext.setMaxHeight(params.height);
                mainleftProfileEditEdittext.setGravity(Gravity.TOP);
                mainleftProfileEditEdittext.setPadding((int)(10*prefManager.getDensity()),(int)(10*prefManager.getDensity()),(int)(10*prefManager.getDensity()),(int)(10*prefManager.getDensity()));
                mainleftProfileEditEdittext.setSingleLine(false);
                break;
            case GlobalConst.VIEW_MAINLEFT_PROFILE_SCHOOL :
                mainleftProfileEditTitle.setText(getResources().getString(R.string.profile_institution));
                mainleftProfileEditSubtext.setText(getResources().getString(R.string.profile_institution_edit));
                mainleftProfileEditEdittext.setSingleLine(true);
                break;
        }
    }

}