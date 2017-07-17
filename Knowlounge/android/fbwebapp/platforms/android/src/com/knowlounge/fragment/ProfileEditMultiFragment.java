package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.adapter.ProfileMultiSelectAdapter;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.ProfileMultiSelectItem;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ProfileEditMultiFragment extends Fragment
{
    private final String TAG = "UserProfileEditMulti";
    private final String TYPE_NAME = "name";
    private final String TYPE_CODE = "code";
    private final int BLANK = 0;

    private int lastPosition = -1;

    private WenotePreferenceManager prefManager;

    private View rootView;
    private ProfileMultiSelectAdapter adapter;

    ImageView btnMainleftProfileMultiBack;
    ListView mainleftProfileMultiList;
    TextView mainleftProfileMultiCommitBtn, mainleftProfileMultiTitle;

    private ArrayList<ProfileMultiSelectItem> codeData = new ArrayList<ProfileMultiSelectItem>();

    private String checkedData;
    private String category;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_profile_edit_multi, container, false);

        adapter = new ProfileMultiSelectAdapter(getActivity(), codeData);

        category = getArguments().getString("multi_type");
        setFindViewById();
        setUI();
        return rootView;
    }


    private void setFindViewById() {
        btnMainleftProfileMultiBack = (ImageView) rootView.findViewById(R.id.btn_mainleft_profile_multi_back);
        mainleftProfileMultiList =  (ListView) rootView.findViewById(R.id.mainleft_profile_multi_list);
        mainleftProfileMultiCommitBtn =  (TextView) rootView.findViewById(R.id.mainleft_profile_multi_commit_btn);
        mainleftProfileMultiTitle = (TextView) rootView.findViewById(R.id.mainleft_profile_multi_title);

        //codeData = new ArrayList<ProfileMultiSelectItem>();

    }

    private void setUI() {
        if(category.equals(GlobalConst.CATEGORY_SUBJECT)){
            mainleftProfileMultiTitle.setText(getResources().getString(R.string.profile_subject));
            checkedData = prefManager.getMySubjectCode();
        }else if (category.equals(GlobalConst.CATEGORY_LANGUAGE)){
            mainleftProfileMultiTitle.setText(getResources().getString(R.string.profile_language));
            checkedData = prefManager.getMyLanguageCode();
        }else{
            mainleftProfileMultiTitle.setText(getResources().getString(R.string.profile_grade));
            checkedData = prefManager.getMyGradeCode();
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnMainleftProfileMultiBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.keyboardHide(getActivity());
                MainActivity._instance.getSupportFragmentManager().popBackStack();
            }
        });

        mainleftProfileMultiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(category.equals(GlobalConst.CATEGORY_GRADE))
                {
                    if(lastPosition != -1)
                        adapter.toggleChecked(lastPosition);
                    if(lastPosition != position) {
                        adapter.toggleChecked(position);
                        lastPosition = position;
                    }else{
                        lastPosition = -1;
                    }
                }else{
                    adapter.toggleChecked(position);
                }
            }
        });
        mainleftProfileMultiCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject obj;
                    if(category.equals(GlobalConst.CATEGORY_GRADE)) {
                        obj = new JSONObject();
                        JSONArray jsonArr = new JSONArray();
                        String[] tempArr = adapter.getResult(TYPE_CODE).split(",");
                        for(String data : tempArr)
                            jsonArr.put(data);
                        obj.put("grade", jsonArr);
                        updateProfileRestClient(obj);
                    }else if(category.equals(GlobalConst.CATEGORY_SUBJECT)){
                        obj = new JSONObject();
                        JSONArray jsonArr = new JSONArray();
                        String[] tempArr = adapter.getResult(TYPE_CODE).split(",");
                        for(String data : tempArr)
                            jsonArr.put(data);
                        obj.put("subject", jsonArr);
                        updateProfileRestClient(obj);
                    }else{
                        obj = new JSONObject();
                        JSONArray jsonArr = new JSONArray();
                        String[] tempArr = adapter.getResult(TYPE_CODE).split(",");
                        for(String data : tempArr)
                            jsonArr.put(data);
                        obj.put("language", jsonArr);
                        updateProfileRestClient(obj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        getCodeRestClient(category);
    }

    private void updateProfileRestClient(JSONObject obj) {

        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RestClient.postWithJsonString(getContext(),"profile/updateProfile.json",masterCookie, checksumCookie, obj, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "send message success.. response : " + obj.toString());
                try {
                    String result = obj.getString("result");
                    if(result.equals("0")){
                        if(category.equals(GlobalConst.CATEGORY_GRADE)) {
                            prefManager.setMyGradeCode(adapter.getResult(TYPE_CODE));
                            prefManager.setMyGradeName(adapter.getResult(TYPE_NAME));
                        }else if(category.equals(GlobalConst.CATEGORY_SUBJECT)){
                            prefManager.setMySubjectCode(adapter.getResult(TYPE_CODE));
                            prefManager.setMySubjectName(adapter.getResult(TYPE_NAME));
                        }else{
                            prefManager.setMyLanguageCode(adapter.getResult(TYPE_CODE));
                            prefManager.setMyLanguageName(adapter.getResult(TYPE_NAME));
                        }
                        Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.toast_profile_change),Toast.LENGTH_SHORT).show();
                        getFragmentManager().popBackStack();
                        AndroidUtils.keyboardHide(getActivity());
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



    public void getCodeRestClient(String code){
        RequestParams params = new RequestParams();
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();
        params.put("category", code);

        if(code.equals("PT002"))
            params.put("usertype", prefManager.getMyUserType());


        RestClient.getWithCookie("profile/getProfileCodeList.json",masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d("grade_code", "send message success.. response : " + obj.toString());
                try {
                    if(obj.getString("result").equals("0")) {
                        JSONArray arr = obj.getJSONArray("list");
                        String[] checkedCode = checkedData.split(",");
                        int checkedCodePositon = 0;
                        //codeData = new ArrayList<ProfileMultiSelectItem>();


                        for (int i=0; i < arr.length(); i++) {
                            JSONObject data = arr.getJSONObject(i);
                            if (checkedData.length() == BLANK)
                                codeData.add(new ProfileMultiSelectItem(data.getString("name"), data.getString("code"), false));
                            else {
                                if (data.getString("code").equals(checkedCode[checkedCodePositon])) {
                                    codeData.add(new ProfileMultiSelectItem(data.getString("name"), data.getString("code"), true));
                                    lastPosition = i;
                                    if (checkedCodePositon < checkedCode.length - 1)
                                        checkedCodePositon++;
                                } else {
                                    codeData.add(new ProfileMultiSelectItem(data.getString("name"), data.getString("code"), false));
                                }
                            }
                        }

                        // 2016.09.06 - adapter 객체를 new 하는 과정에서 NullPointException이 발생.. new 하는 시점을 수정하였음.
                        if(adapter != null) {
                            mainleftProfileMultiList.setAdapter(adapter);
                        }
                    }
                } catch (JSONException j) {
                    j.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
            }


        });
    }


}