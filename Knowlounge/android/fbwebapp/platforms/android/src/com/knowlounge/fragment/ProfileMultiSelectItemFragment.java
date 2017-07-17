package com.knowlounge.fragment;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.ProfileMultiSelectItem;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;


public class ProfileMultiSelectItemFragment extends Fragment
{
    private final String TAG = "PMultiSelectItem";
    private final String TYPE_NAME = "name";
    private final String TYPE_CODE = "code";
    private final int BLANK = 0;

    private View rootView;

    WenotePreferenceManager prefManager;

    android.support.v4.app.FragmentManager fragmentManager;
    android.support.v4.app.FragmentTransaction fragmentTransaction;

    private MultiSelectAdapter adapter;

    @BindView(R.id.profile_multi_select_list_root_view) RelativeLayout profileMultiSelectListRootView;
    @BindView(R.id.multi_select_title) TextView multiSelectTitle;
    @BindView(R.id.multi_select_list_view) ListView multiSelectListView;
    @BindView(R.id.multi_select_ok) ImageView multiSelectOk;

    String checkedData;
    String category;
    int startType;
    ArrayList<ProfileMultiSelectItem> codeData;

    public static SubmitListener submitListener = null;

    public interface SubmitListener {
        void onClickSubmitListener(String nameStr, String codeStr, String category);
    }

    public static void setSubmitListener(SubmitListener listener){
        submitListener = listener;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.profile_multi_select_list, container, false);
            ButterKnife.bind(this, rootView);
        }
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        category = getArguments().getString("category");
        startType = getArguments().getInt("start_type");
//        setFindViewById();
        setUI();
        return rootView;
    }

    private void setUI() {
        if (startType == GlobalConst.TYPE_STUDENT_START) {
            profileMultiSelectListRootView.setBackgroundColor(getResources().getColor(R.color.app_base_color));
        } else {
            profileMultiSelectListRootView.setBackgroundColor(Color.parseColor("#19d7ff"));
        }

        if (category.equals(GlobalConst.CATEGORY_SUBJECT)) {
            multiSelectTitle.setText(getResources().getString(R.string.profile_subject));
        } else if (category.equals(GlobalConst.CATEGORY_LANGUAGE)) {
            multiSelectTitle.setText(getResources().getString(R.string.profile_language));
        } else {
            multiSelectTitle.setText(getResources().getString(R.string.profile_grade));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        multiSelectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (category.equals(GlobalConst.CATEGORY_GRADE)) {
                    for (int i=0; i<adapter.getCount(); i++) {
                        if (position != i)
                            adapter.getItem(i).initCheck();
                    }
                    adapter.getItem(position).toggleCheck();
                    adapter.notifyDataSetChanged();

                } else {
                    adapter.getItem(position).toggleCheck();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        multiSelectOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nmStr = getResult(TYPE_NAME);
                String codeStr = getResult(TYPE_CODE);
                submitListener.onClickSubmitListener(nmStr,codeStr, category);
                getFragmentManager().popBackStack();
            }
        });

    }

    private String getResult(String type) {
        String result="";
        for(int i =0; i<codeData.size() ; i++){
            if(adapter.getItem(i).isChecked()) {
                if(type.equals(TYPE_NAME))
                    result += adapter.getItem(i).getItemNm() + ",";
                else if(type.equals(TYPE_CODE))
                    result += adapter.getItem(i).getItemCode() + ",";
            }
        }
        return !result.equals("") ? result.substring(0,result.length()-1) : "";
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            checkedData = getArguments().getString("check_data");
        }catch(NullPointerException ne){
            checkedData = "";
        }
        getCodeRestClient(category);
    }

    private void setFindViewById(){
        profileMultiSelectListRootView = (RelativeLayout) rootView.findViewById(R.id.profile_multi_select_list_root_view);
        multiSelectTitle = (TextView) rootView.findViewById(R.id.multi_select_title);
        multiSelectListView = (ListView) rootView.findViewById(R.id.multi_select_list_view);
        multiSelectOk = (ImageView) rootView.findViewById(R.id.multi_select_ok);

//        View header = getActivity().getLayoutInflater().inflate(R.layout.profile_multi_select_header, null, false);
//        multiSelectListView.addHeaderView(header);

    }


    public void getCodeRestClient(String code){
        RequestParams params = new RequestParams();
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        params.put("category", code);
        if(code.equals("PT002"))
            params.put("usertype", startType == GlobalConst.TYPE_STUDENT_START ? "1" : "2");

        Log.d("result_param",params.toString());
        RestClient.getWithCookie("profile/getProfileCodeList.json",masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d("grade_code", "send message success.. response : " + obj.toString());
                try {
                    JSONArray arr = obj.getJSONArray("list");
                    String[] checkedCode = checkedData.split(",");
                    int checkedCodePositon = 0;
                    codeData = new ArrayList<ProfileMultiSelectItem>();

                    for(int i=0 ; i < arr.length(); i++){
                        JSONObject data = arr.getJSONObject(i);
                        if(checkedData.length() == BLANK)
                            codeData.add(new ProfileMultiSelectItem(data.getString("name"),data.getString("code"),false));
                        else{
                            if(data.getString("code").equals(checkedCode[checkedCodePositon])) {
                                codeData.add(new ProfileMultiSelectItem(data.getString("name"), data.getString("code"), true));
                                if(checkedCodePositon < checkedCode.length-1)
                                    checkedCodePositon++;
                            }else{
                                codeData.add(new ProfileMultiSelectItem(data.getString("name"), data.getString("code"), false));
                            }
                        }
                    }

                    adapter = new MultiSelectAdapter(getActivity(),R.layout.profile_multi_select_list_row,codeData);
                    multiSelectListView.setAdapter(adapter);

                }catch(JSONException j){
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
    private String transformResult(boolean[] selected, String[] codeArr) {
        String result = "";
        int i = 0;
        for(boolean data: selected){
            if(data) {
                result  += codeArr[i] + ",";
            }
            i++;
        }
        return result.length() !=BLANK ? result.substring(0, result.length()-1) : "";
    }


    public class MultiSelectAdapter extends ArrayAdapter<ProfileMultiSelectItem>{

        public MultiSelectAdapter(Context context, int resource, List<ProfileMultiSelectItem> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.profile_multi_select_list_row, parent, false);
            }


            ImageView profileMultiSelectRowCheck = (ImageView) convertView.findViewById(R.id.profile_multi_select_row_check);
            TextView profileMultiSelectRowName = (TextView) convertView.findViewById(R.id.profile_multi_select_row_name);
            LinearLayout profileMultiSelectLayout = (LinearLayout) convertView.findViewById(R.id.profile_multi_select_layout);
            profileMultiSelectRowName.setText(getItem(position).getItemNm());
            if(position == 0){
                convertView.setPadding (0,(int)(20*prefManager.getDensity()),0,0);
            }else if(position == getCount()-1){
                convertView.setPadding (0,0,0,(int)(20*prefManager.getDensity()));
            }else{
                convertView.setPadding(0,0,0,0);
            }


            if(getItem(position).isChecked()){  //check on
                profileMultiSelectLayout.setBackground(getResources().getDrawable(R.drawable.bg_profile_chips_on));
                if(startType == GlobalConst.TYPE_STUDENT_START) {
                    profileMultiSelectRowCheck.setImageResource(R.drawable.ico_firstprofile_check_green);
                    profileMultiSelectRowName.setTextColor(getResources().getColor(R.color.app_base_color));
                }else{
                    profileMultiSelectRowCheck.setImageResource(R.drawable.ico_firstprofile_check_blue);
                    profileMultiSelectRowName.setTextColor(Color.parseColor("#19d7ff"));
                }
            }else{  //check off
                profileMultiSelectRowCheck.setImageResource(0);
                profileMultiSelectRowName.setTextColor(Color.parseColor("#ffffff"));
                if(startType== GlobalConst.TYPE_STUDENT_START) {
                    profileMultiSelectLayout.setBackground(getResources().getDrawable(R.drawable.bg_set_profile_item_student));
                }else{
                    profileMultiSelectLayout.setBackground(getResources().getDrawable(R.drawable.bg_set_profile_item_teacher));
                }
            }

            return convertView;
        }

    }

}