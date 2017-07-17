package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ConfigEditFragment extends Fragment
{
    private final String TAG = "ConfigEditFragment";
    private final int classtitleHeight = 50;
    private final int introduceHeight = 150;
    private final int CLASS_TITLE_MAX_LEN = 60;
    private final int CLASS_DESC_MAX_LEN = 200;
    private View rootView;

    WenotePreferenceManager prefManager;

    @BindView(R.id.btn_config_edit_back) ImageView btnConfigEditBack;
    @BindView(R.id.config_edit_title) TextView ConfigEditTitle;
    @BindView(R.id.config_edit_subtext) TextView ConfigEditSubtext;
    @BindView(R.id.config_edit_commit_btn) TextView ConfigEditCommitBtn;
    @BindView(R.id.config_edit_edittext) EditText ConfigEditEdittext;

    public interface OnRoomInfoListener {
        void setRoomTitle(String roomTitle);
        String getRoomTitle();

//        void setRoomDesc(String roomDesc);
//        String getRoomDesc();
    }

    OnRoomInfoListener mCallback;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
        mCallback = (OnRoomInfoListener) RoomActivity.activity;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_config_edit, container, false);
        ButterKnife.bind(this, rootView);

        initSetting();
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 뒤로가기 버튼 이벤트..
        btnConfigEditBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

                RoomActivity.activity.getSupportFragmentManager().popBackStack();
                //getFragmentManager().popBackStack();
            }
        });


        ConfigEditCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getArguments().getInt("type") == GlobalConst.VIEW_CLASSCONFIG_TITLE) {
                    String roomTitleStr = ConfigEditEdittext.getText().toString();
                    if (!TextUtils.isEmpty(roomTitleStr)) {
                        mCallback.setRoomTitle(roomTitleStr);
                        RoomActivity.activity.getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.toast_room_nulltitle), Toast.LENGTH_LONG).show();
                    }
                } else if (getArguments().getInt("type") == GlobalConst.VIEW_CLASSCONFIG_DESC) {
                        ConfigFragment._instance.updateRoomDesc(ConfigEditEdittext.getText().toString());
                        ConfigFragment.setConfigParams("content", ConfigEditEdittext.getText().toString());
                        RoomActivity.activity.getSupportFragmentManager().popBackStack();
                }

                AndroidUtils.keyboardHide(getActivity());
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        //TODO editText에 기본 정보 셋팅
    }


    private void initSetting() {
        int type = getArguments().getInt("type");
        switch (type) {
            case GlobalConst.VIEW_CLASSCONFIG_TITLE :
                String roomTitle = getArguments().getString("title");
                ConfigEditEdittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CLASS_TITLE_MAX_LEN)});
                ConfigEditEdittext.setMinHeight((int)(classtitleHeight * prefManager.getDensity()));
                ConfigEditEdittext.setSingleLine(true);
                ConfigEditTitle.setText(getResources().getString(R.string.canvas_setting_classtitle));
                ConfigEditSubtext.setText(getResources().getString(R.string.canvas_edittitle_guide));
                ConfigEditEdittext.setHint(getResources().getString(R.string.canvas_edittitle_hint));
                ConfigEditEdittext.setText(roomTitle);
                break;
            case GlobalConst.VIEW_CLASSCONFIG_DESC :
                String roomDesc = getArguments().getString("desc");
                ConfigEditEdittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CLASS_DESC_MAX_LEN)});
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ConfigEditEdittext.getLayoutParams();
                params.height = AndroidUtils.getPxFromDp(getContext(),introduceHeight);
                ConfigEditEdittext.setLayoutParams(params);
                ConfigEditEdittext.setMaxHeight(params.height);
                ConfigEditEdittext.setGravity(Gravity.TOP);

                ConfigEditEdittext.setPadding((int)(10*prefManager.getDensity()),(int)(10*prefManager.getDensity()),(int)(10*prefManager.getDensity()),(int)(10*prefManager.getDensity()));
                ConfigEditEdittext.setSingleLine(false);

                ConfigEditTitle.setText(getResources().getString(R.string.canvas_setting_descrpt));
                ConfigEditSubtext.setText(getResources().getString(R.string.canvas_setting_descrptedit));
                ConfigEditEdittext.setHint(getResources().getString(R.string.canvas_setting_descrptguide));
                ConfigEditEdittext.setText(roomDesc);
                break;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

    }
}