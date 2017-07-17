package com.knowlounge.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.dialog.ExtendReqDialogFragment;
import com.knowlounge.fragment.dialog.TeacherOnlyCamDialogFragment;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.CommonEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by Minsu on 2015-12-15.
 */
public class ConfigFragment extends Fragment implements View.OnClickListener, ExtendReqDialogFragment.SetRoomNotiAdapterListener {

    private final String TAG = "ConfigFragment";

    public static ConfigFragment _instance = null;

    private View rootView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private WenotePreferenceManager prefManager;

    @BindView(R.id.ck_authtype) SwitchCompat ck_authtype;
    @BindView(R.id.ck_passwd) SwitchCompat ck_passwd;
    @BindView(R.id.passwd_txt) EditText passwdInput;
    @BindView(R.id.ck_chat) SwitchCompat ck_chat;
    @BindView(R.id.ck_cmt) SwitchCompat ck_cmt;
    @BindView(R.id.ck_exp) SwitchCompat ck_exp;
    @BindView(R.id.ck_openflag) SwitchCompat ck_openflag;

    @BindView(R.id.ck_only_teacher_cam) SwitchCompat ck_only_teacher_cam;
    @BindView(R.id.ck_only_teacher_video) SwitchCompat ck_only_teacher_video;

    @BindView(R.id.class_setting_class_title_layout) LinearLayout classSettingClassTitleLayout;
    @BindView(R.id.class_setting_participant_limit_layout) LinearLayout classSettingParticipantLimitLayout;
    @BindView(R.id.class_setting_open_flag_layout) LinearLayout classSettingPublicLimitLayout;
    @BindView(R.id.class_setting_class_desc_layout) LinearLayout classSettingClassDescLayout;
    @BindView(R.id.class_setting_passwd_openflag_container) LinearLayout classSettingPasswdOpenflagContainer;
    @BindView(R.id.layout_opt_passwd) LinearLayout layoutOptPasswd;

    @BindView(R.id.class_setting_class_title) TextView textViewRoomTitle;
    @BindView(R.id.txt_class_setting_participant_limit) TextView textViewUserLimit;
    @BindView(R.id.class_setting_open_flag) TextView textViewOpenFlag;
    @BindView(R.id.class_setting_class_desc) TextView textViewRoomDesc;

    @BindView(R.id.btn_room_setting_back) ImageView btnRoomSettingBack;
    @BindView(R.id.class_setting_class_title_next_arrow) ImageView classSettingClassTitleNextArrow;
    @BindView(R.id.class_setting_open_flag_next_arrow) ImageView classSettingPublicLimitNextArrow;
    @BindView(R.id.class_setting_class_desc_next_arrow) ImageView classSettingClassDescNextArrow;

    private int colorIdx;
    private String bgImg;
    private String roomTitle;
    private String roomContent;
    private boolean isParticipant = false;
    private boolean isPublic = false;

    private final int participant_max = 1;
    private final int public_max = 1;

    private String[] participant_arr = new String[2];
    private String[] public_arr = new String[2];

    private int participantSelected;
    private int publicSelected;

//    todo Subscribe
private Subscriber<? super Object> mSubscriber = new Subscriber<Object>() {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(Object o) {
        if(o instanceof CommonEvent){
            final CommonEvent data = (CommonEvent)o;
            Log.d(TAG, "Common TAG : "+data.getTag());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(data.getTag() == CommonEvent.TEACHER_ONLY_CAM)
                        classSettingComplete(data.getResult());
                }
            });
        }
    }
};


    public interface OnSetRoomConfigListener {
        void setRoomConfig(View view, JSONObject obj);
        String getRoomTitle();
        JSONObject getBgInfo();
    }

    OnSetRoomConfigListener mCallback;

    public static ConfigFragment getInstance() {
        return new ConfigFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnSetRoomConfigListener) RoomActivity.activity;
            _instance = this;
            prefManager = WenotePreferenceManager.getInstance(context);

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnSetRoomConfigListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        rootView = inflater.inflate(R.layout.fragment_config, container, false);
        ButterKnife.bind(this, rootView);

        EventBus.get().getBustObservable()
                .subscribe(mSubscriber);

        ExtendReqDialogFragment.setRoomNotiAdapterListener(this);

        String roomCode = getArguments().getString("roomCode");
        this.roomTitle = mCallback.getRoomTitle();

        JSONObject authInfo = RoomActivity.activity.getAuthInfo();
        JSONObject bgInfo = mCallback.getBgInfo();

        Log.d(TAG, "authInfo : " + authInfo.toString());
        Log.d(TAG, "bgInfo : " + bgInfo.toString());

        setFindViewById();
        initializeConfigUI(authInfo);

        textViewRoomTitle.setText(roomTitle);


        // 뒤로가기 버튼 이벤트
        btnRoomSettingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.getSupportFragmentManager().popBackStack();
                //getActivity().finish();
            }
        });

        ck_passwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (ck_passwd.isChecked()) {
                    classSettingPasswdOpenflagContainer.setVisibility(View.VISIBLE);
                } else {
                    classSettingPasswdOpenflagContainer.setVisibility(View.GONE);
                }
                return false;
            }
        });

        ck_passwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    classSettingPasswdOpenflagContainer.setVisibility(View.VISIBLE);
                } else {
                    classSettingPasswdOpenflagContainer.setVisibility(View.GONE);
                }
            }
        });

        applyRoomConfig(authInfo);
        applyRoomBg(rootView, bgInfo);

        Button applyBtn = (Button)rootView.findViewById(R.id.btn_room_update);

        ImageButton btnSetImg1 = (ImageButton)rootView.findViewById(R.id.btn_set_img1);
        ImageButton btnSetImg2 = (ImageButton)rootView.findViewById(R.id.btn_set_img2);
        ImageButton btnSetImg3 = (ImageButton)rootView.findViewById(R.id.btn_set_img3);
        ImageButton btnSetImg4 = (ImageButton)rootView.findViewById(R.id.btn_set_img4);

        ImageButton btnSetColor1 = (ImageButton)rootView.findViewById(R.id.btn_set_color1);
        ImageButton btnSetColor2 = (ImageButton)rootView.findViewById(R.id.btn_set_color2);
        ImageButton btnSetColor3 = (ImageButton)rootView.findViewById(R.id.btn_set_color3);
        ImageButton btnSetColor4 = (ImageButton)rootView.findViewById(R.id.btn_set_color4);
        ImageButton btnSetColor5 = (ImageButton)rootView.findViewById(R.id.btn_set_color5);
        ImageButton btnSetColor6 = (ImageButton)rootView.findViewById(R.id.btn_set_color6);
        ImageButton btnSetColor7 = (ImageButton)rootView.findViewById(R.id.btn_set_color7);
        ImageButton btnSetColor8 = (ImageButton)rootView.findViewById(R.id.btn_set_color8);   // 배경색 초기화..

        setBackgroundImageBtn(btnSetImg1, 1);
        setBackgroundImageBtn(btnSetImg2, 2);
        setBackgroundImageBtn(btnSetImg3, 3);
        setBackgroundImageBtn(btnSetImg4, 4);

        setBackgroundColorBtn(btnSetColor1, 1);
        setBackgroundColorBtn(btnSetColor2, 2);
        setBackgroundColorBtn(btnSetColor3, 3);
        setBackgroundColorBtn(btnSetColor4, 4);
        setBackgroundColorBtn(btnSetColor5, 5);
        setBackgroundColorBtn(btnSetColor6, 6);
        setBackgroundColorBtn(btnSetColor7, 7);
        setBackgroundColorBtn(btnSetColor8, 8);

//        if (!RoomActivity.activity.getCreatorFlag()) {
//            ck_authtype.setEnabled(false);
//            ck_passwd.setEnabled(false);
//            passwdInput.setEnabled(false);
//            ck_chat.setEnabled(false);
//            ck_cmt.setEnabled(false);
//            ck_exp.setEnabled(false);
//            btnSetImg1.setEnabled(false);
//            btnSetImg2.setEnabled(false);
//            btnSetImg3.setEnabled(false);
//            btnSetImg4.setEnabled(false);
//            btnSetColor1.setEnabled(false);
//            btnSetColor2.setEnabled(false);
//            btnSetColor3.setEnabled(false);
//            btnSetColor4.setEnabled(false);
//            btnSetColor5.setEnabled(false);
//            btnSetColor6.setEnabled(false);
//            btnSetColor7.setEnabled(false);
//            btnSetColor8.setEnabled(false);
//
//            applyBtn.setEnabled(false);
//        }

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ck_only_teacher_cam.isChecked()){
                    FragmentManager fm = getFragmentManager();
                    TeacherOnlyCamDialogFragment teacherOnlyCamDialogFragment = new TeacherOnlyCamDialogFragment();
                    teacherOnlyCamDialogFragment.show(fm, "teacher_only_cam");
                    return;
                }else{
                    classSettingComplete(true);
                }
            }
        });

//        if(ck_passwd.isChecked()){
//            passwdInput.setVisibility(View.VISIBLE);
//        } else {
//            passwdInput.setVisibility(View.GONE);
//        }

        return rootView;
    }

    private void classSettingComplete(boolean result){
        if(result) {
            try {
                String authFlag = ck_authtype.isChecked() ? "1" : "0";
                String passwd = ck_passwd.isChecked() ? passwdInput.getText().toString() : "";
                boolean ckUsePasswd = !TextUtils.equals(passwd, "") ? true : false;
                String chatFlag = ck_chat.isChecked() ? "1" : "0";
                String cmtFlag = ck_cmt.isChecked() ? "1" : "0";
                String expFlag = ck_exp.isChecked() ? "1" : "0";
                String openFlag = ck_openflag.isChecked() ? "1" : "0";

                String onlyTearchCamFlag = ck_only_teacher_cam.isChecked() ? "1" : "0";
                String onlyTearchVideoFlag = ck_only_teacher_video.isChecked() ? "1" : "0";

                String userLimitCnt = TextUtils.equals(textViewUserLimit.getText().toString(), participant_arr[0]) ? "3" : "30";
                String content = textViewRoomDesc.getText().toString();

                JSONObject obj = new JSONObject();

                obj.put("authtype", authFlag);
                obj.put("ckpasswd", ckUsePasswd);
                obj.put("passwd", passwd);
                obj.put("chatopt", chatFlag);
                obj.put("cmtopt", cmtFlag);
                obj.put("expopt", expFlag);
                obj.put("vcamopt", onlyTearchCamFlag);
                obj.put("vshareopt", onlyTearchVideoFlag);
                obj.put("openflag", openFlag);
                obj.put("userlimitcnt", TextUtils.isEmpty(userLimitCnt) ? 3 : Integer.parseInt(userLimitCnt));
                obj.put("content", content);

                RoomActivity.activity.setRoomConfigJavascript(obj, true);
                //mCallback.setRoomConfig(v, obj);

                applyRoomConfig(obj);
                //getActivity().finish();

            } catch (JSONException e) {
                Log.d(getClass().getSimpleName(), e.getMessage());
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mSubscriber.unsubscribe();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.class_setting_class_title_next_arrow :
            case R.id.class_setting_class_title_layout :
                //nextFragment(GlobalConst.VIEW_CLASSCONFIG_TITLE);
                RoomActivity.activity.openRoomSettingDetail(GlobalConst.VIEW_CLASSCONFIG_TITLE, roomTitle);
                break;
            case R.id.class_setting_participant_limit_layout :
                //Todo 참여해제 띄우기
                Bundle argument = new Bundle();
                argument.putString("type", "room_setting");
                argument.putString("roomid", prefManager.getCurrentRoomId());
                argument.putString("code", RoomActivity.activity.getRoomCode());
                argument.putString("masterno", prefManager.getCurrentTeacherUserNo());
                FragmentManager fm = getFragmentManager();
                ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                dialogFragment.setArguments(argument);
                dialogFragment.show(fm, "what");

                //pickerDialogFragment(GlobalConst.VIEW_CLASSCONFIG_PARTICIPANT);
                break;
            case R.id.class_setting_open_flag_next_arrow :
            case R.id.class_setting_open_flag_layout :
                pickerDialogFragment(GlobalConst.VIEW_CLASSCONFIG_PUBLIC);
                break;
            case R.id.class_setting_class_desc_next_arrow :
            case R.id.class_setting_class_desc_layout :
                //nextFragment(GlobalConst.VIEW_CLASSCONFIG_DESC);
                String roomDesc = textViewRoomDesc.getText().toString();
                RoomActivity.activity.openRoomSettingDetail(GlobalConst.VIEW_CLASSCONFIG_DESC, roomDesc);
                break;
        }
    }


    @Override
    public void updateData() {
        textViewUserLimit.setText(participant_arr[1]);
        textViewUserLimit.setClickable(false);
        textViewUserLimit.setFocusable(false);
        classSettingParticipantLimitLayout.setVisibility(View.GONE);
    }


    public static void setConfigParams(String flag, String data) {
        try {
            JSONObject obj = RoomActivity.activity.getAuthInfo();

            if (obj.has("title")) {
                obj.remove("title");
            }

            if (flag.length() != 0) {
                //obj.remove(flag);
                obj.put(flag, data);
            }

            RoomActivity.activity.setRoomConfigJavascript(obj, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void initializeConfigUI(JSONObject authInfo) {
        try {
            Log.d(TAG, authInfo.toString());
            String userlimitcnt  = authInfo.has("userlimitcnt") ? authInfo.getString("userlimitcnt") : "3";
            String openFlag = authInfo.has("openflag") ? authInfo.getString("openflag") : "1";
            String passwdFlag = authInfo.has("passwdflag") ? authInfo.getString("passwdflag") : "0";
            String passwdStr = authInfo.has("passwd") ? authInfo.getString("passwd") : "";
            this.roomContent = authInfo.has("content") ? authInfo.getString("content") : "";

            if (RoomActivity.activity.getUserLimitCnt() == 30) {
                textViewUserLimit.setText(participant_arr[1]);
                classSettingParticipantLimitLayout.setVisibility(View.GONE);
//                classSettingParticipantLimitLayout.setClickable(false);
//                classSettingParticipantLimitLayout.setFocusable(false);
//                classSettingParticipantLimitNextArrow.setClickable(false);
//                classSettingParticipantLimitNextArrow.setFocusable(false);

            } else {
                textViewUserLimit.setText(participant_arr[0]);
                classSettingParticipantLimitLayout.setVisibility(View.VISIBLE);
            }




            if(TextUtils.equals(passwdFlag, "1")) {
                ck_passwd.setChecked(true);
                passwdInput.setText(passwdStr);

                if (!RoomActivity.activity.getTeacherFlag() && RoomActivity.activity.getCreatorFlag()) {  // 학생보드의 개설자(학생)에게 비밀번호 설정 항목을 노출시키지 않음 - 2017.01.06
                    classSettingPasswdOpenflagContainer.setVisibility(View.GONE);
                    layoutOptPasswd.setVisibility(View.GONE);
                } else {
                    classSettingPasswdOpenflagContainer.setVisibility(View.VISIBLE);
                    layoutOptPasswd.setVisibility(View.VISIBLE);
                }
            } else {
                ck_passwd.setChecked(false);
                classSettingPasswdOpenflagContainer.setVisibility(View.GONE);
            }


            if (openFlag.equals("1")) {  // 공개 수업
                isPublic = true;
                ck_openflag.setChecked(true);
                //textViewOpenFlag.setText(public_arr[1]);
            } else {  // 비공개 수업
                isPublic = false;
                ck_openflag.setChecked(false);
                //textViewOpenFlag.setText(public_arr[0]);
//                ck_passwd.setChecked(true);
//                classSettingPasswdOpenflagContainer.setVisibility(View.VISIBLE);
            }

            textViewRoomDesc.setText(roomContent);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }


    private void setFindViewById() {
        participant_arr[0] = getResources().getString(R.string.canvas_participant_3);
        participant_arr[1] = String.format(getResources().getString(R.string.canvas_participant_25), 30+"");
        public_arr[0] = getResources().getString(R.string.setting_class_private);
        public_arr[1] = getResources().getString(R.string.setting_class_public);

        classSettingClassTitleLayout.setOnClickListener(this);
        classSettingClassTitleNextArrow.setOnClickListener(this);

        classSettingParticipantLimitLayout.setOnClickListener(this);
        //classSettingParticipantLimitNextArrow.setOnClickListener(this);

        classSettingPublicLimitLayout.setOnClickListener(this);
        classSettingPublicLimitNextArrow.setOnClickListener(this);

        classSettingClassDescLayout.setOnClickListener(this);
        classSettingClassDescNextArrow.setOnClickListener(this);
    }


    private void pickerDialogFragment(final int type) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialogCustom);
        View view = (View) getActivity().getLayoutInflater().inflate(R.layout.piker_dialog,null);
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.picker);
        picker.setMinValue(0);
        if(type == GlobalConst.VIEW_CLASSCONFIG_PARTICIPANT){
            picker.setMaxValue(participant_max);
            picker.setDisplayedValues(participant_arr);
            picker.setValue(0);
        } else {
            picker.setMaxValue(public_max);
            picker.setDisplayedValues(public_arr);
            picker.setValue(isPublic ? 1 : 0);
        }
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(type == GlobalConst.VIEW_CLASSCONFIG_PARTICIPANT)
                    isParticipant = newVal == 1 ? true : false ;
                else
                    isPublic = newVal == 1 ? true : false;
            }
        });
        builder.setView(view);

        builder.setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(type == GlobalConst.VIEW_CLASSCONFIG_PARTICIPANT) {
                            if (isParticipant) {
                                //Todo 참여해제 띄우기
                                Bundle argument = new Bundle();
                                argument.putString("type", "room_setting");
                                argument.putString("roomid", prefManager.getCurrentRoomId());
                                argument.putString("code", RoomActivity.activity.getRoomCode());
                                argument.putString("masterno", prefManager.getCurrentTeacherUserNo());
                                FragmentManager fm = getFragmentManager();
                                ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                                dialogFragment.setArguments(argument);
                                dialogFragment.show(fm, "what");
                            }
                        } else {
                            setConfigParams("openflag", isPublic? "1" : "0");
                            textViewOpenFlag.setText(isPublic ? public_arr[1] : public_arr[0]);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog confirm = builder.create();
        confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        confirm.setCanceledOnTouchOutside(true);
        confirm.show();
        confirm.getWindow().setLayout((int) (300 * prefManager.getDensity()), (int) (200 * prefManager.getDensity()));
    }


    private void nextFragment(int type) {
        Bundle argument = new Bundle();
        if(type == GlobalConst.VIEW_CLASSCONFIG_TITLE) {
            argument.putInt("type", GlobalConst.VIEW_CLASSCONFIG_TITLE);
            argument.putString("title", textViewRoomTitle.getText().toString());
        } else {
            argument.putInt("type", GlobalConst.VIEW_CLASSCONFIG_DESC);
            argument.putString("desc", textViewRoomDesc.getText().toString());
        }

        ConfigEditFragment configEditFragment = new ConfigEditFragment();
        configEditFragment.setArguments(argument);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.config_container, configEditFragment, "ConfigEditFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void applyRoomConfigHandler(final JSONObject obj) throws JSONException {
        RoomActivity.activity.setAuthInfo(obj);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyRoomConfig(obj);
            }
        });
    }


    private void applyRoomBg(View rootView, final JSONObject bgInfo) {
        try {
            colorIdx = bgInfo.getInt("coloridx");
            bgImg = bgInfo.getString("bgimg");
            if (colorIdx != -1) {
                ImageButton imgBtn = (ImageButton) rootView.findViewById(getResources().getIdentifier("btn_set_color" + (colorIdx + 1), "id", getActivity().getPackageName()));
                imgBtn.setImageResource(R.drawable.ico_setting_bgselect);
            }
            if (!TextUtils.isEmpty(bgImg)) {
                int bgImgIdx = Integer.parseInt(bgImg);
                ImageButton imgBtn = (ImageButton) rootView.findViewById(getResources().getIdentifier("btn_set_img" + bgImgIdx, "id", getActivity().getPackageName()));
                imgBtn.setImageResource(R.drawable.ico_setting_bgselect);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }


    public void applyRoomConfig(final JSONObject authInfo)  {
        try {

            Log.d(TAG, "applyRoomconfig : " + authInfo.toString());
            String authType = authInfo.has("authtype") ? authInfo.getString("authtype") : "0"; //
            String passwdStr = authInfo.has("passwd") ? authInfo.getString("passwd") : "";    // 비밀번호
            String chatFlag = authInfo.has("chatopt") ? authInfo.getString("chatopt") : "0";  // 채팅 허용여부
            String cmtFlag = authInfo.has("cmtopt") ? authInfo.getString("cmtopt") : "0";     // 코멘트 허용여부
            String expFlag = authInfo.has("expopt") ? authInfo.getString("expopt") : "0";     // 화면저장 허용여부
            String onlyTearchCamFlag =  authInfo.has("vcamopt") ? authInfo.getString("vcamopt") : "0";
            String onlyTearchVideoFlag =  authInfo.has("vshareopt") ? authInfo.getString("vshareopt") : "0";

            if (ck_authtype != null && "1".equals(authType)) {
                ck_authtype.setChecked(true);
            }

            if (!TextUtils.isEmpty(passwdStr)) {
                ck_passwd.setChecked(true);
                passwdInput.setText(passwdStr);
                if (!RoomActivity.activity.getTeacherFlag() && RoomActivity.activity.getCreatorFlag()) {
                    classSettingPasswdOpenflagContainer.setVisibility(View.GONE);
                } else {
                    classSettingPasswdOpenflagContainer.setVisibility(View.VISIBLE);
                }
            }

            if (ck_chat != null && "1".equals(chatFlag)) {
                ck_chat.setChecked(true);
            }

            if (ck_cmt != null && "1".equals(cmtFlag)) {
                ck_cmt.setChecked(true);
            }

            if (ck_exp != null && "1".equals(expFlag)) {
                ck_exp.setChecked(true);
            }

            if(ck_only_teacher_cam != null && "1".equals(onlyTearchCamFlag)){
                ck_only_teacher_cam.setChecked(true);
                ck_only_teacher_cam.setVisibility(View.GONE);
            }

            if(ck_only_teacher_video != null && "1".equals(onlyTearchVideoFlag)){
                ck_only_teacher_video.setChecked(true);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }
    }


    public void updateRoomDesc(final String desc) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewRoomDesc.setText(desc);
            }
        });
    }


    private void setBackgroundImageBtn(final ImageButton btn, final int idx) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCheckedImageResource("bgimg_set");
//              btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ico_setting_bgselect));
                btn.setImageResource(R.drawable.ico_setting_bgselect);
                RoomActivity.activity.setRoomBgImageJavascript(idx - 1);
            }
        });
    }

    private void setBackgroundColorBtn(final ImageButton btn, final int idx) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idx == 7) {
                    AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), 0xffffff00, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                        }

                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {
                            String selectedColor = String.format("0x%08x", color);
                            String colorStr = selectedColor.substring(4, 10);

                            int red = Integer.valueOf(colorStr.substring(0, 2), 16);
                            int green = Integer.valueOf(colorStr.substring(2, 4), 16);
                            int blue = Integer.valueOf(colorStr.substring(4, 6), 16);

                            RoomActivity.activity.setRoomBgColorCustomJavascript(red, green, blue);
                        }
                    });
                    dialog.show();
                } else {
                    clearCheckedImageResource("bgcolor_set");
                    //btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ico_setting_bgselect));
                    btn.setImageResource(R.drawable.ico_setting_bgselect);
                    RoomActivity.activity.setRoomBgColorJavascript(idx);
                }
            }
        });
    }

    private void clearCheckedImageResource(String tagStrParam) {
        View parentView = (View) rootView.findViewById(R.id.bg_btn_container);
        ArrayList<View> childViews = parentView.getTouchables();

        for(View child : childViews) {
            String tagStr = (String)child.getTag();

            if(tagStr.indexOf(tagStrParam) > -1) {
                //((ImageButton) child).setBackgroundColor(this.getResources().getColor(android.R.color.transparent));
                ((ImageButton) child).setImageResource(android.R.color.transparent);
            }
        }
    }
}
