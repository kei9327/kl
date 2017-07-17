package com.knowlounge.fragment.poll;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.util.AndroidUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.knowlounge.model.PollCreateData;
import com.knowlounge.util.RestClient;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class pollCreateFragment extends PollDialogFragment {

    private final String TAG = "pollCreateFragment";
    private Context mContext;
    private LinearLayout optMultiChoice, optAlterChoice, optShortAnswer, optDrawing, pollShoutdownBtn;
    private ImageView icoMultiChoice, icoAlterChoice, icoShortAnswer, icoDrawing;
    private TextView txtMultiChoice, txtAlterChoice, txtShortAnswer, txtDrawing;
    private TextView shutdownText, pollSaveBtn, pollSendBtn, type1Numtext, type2TextResult, pollCreateCancel;
    private EditText pollTitle;
    private String polltempno;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    public static boolean isFirst;
    public static int course;
    private boolean isReadySend;

    public interface PollSaveBtnOnClickListener{
        void onClicked(int type);
    }

    private PollSaveBtnOnClickListener pollSaveBtnOnClickListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        pollSaveBtnOnClickListener = (PollSaveBtnOnClickListener) context;
        mContext = context;
    }


    @Override
        public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.poll_create, container, false);
        }

        setTouchKeyboardHide(rootView);

        setFindViewById();
        setListener();

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        pollCreateCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirst = true;
                AndroidUtils.keyboardHide(getActivity());
                if (course == GlobalConst.VIEW_CREATE_FRAGMENT) {
                    getActivity().finish();
                } else {
                    getFragmentManager().popBackStack();
                }
                RoomActivity.activity.pollData.clear();
            }
        });

        // 질문 저장하기 버튼 이벤트
        pollSaveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.keyboardHide(getActivity());
                if(!isReadySend) {
                    completCheck();
                    return;
                }
                try {
                    final CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
                    JSONObject obj = setSendData(false);

                    Log.d("last_result", obj.toString());
                    if (completCheck()) {
                        webView.sendJavascript("PollCtrl.Action.Master.createPoll(" + obj.toString() + ");");
                        isFirst = true;
                        pollSaveBtn.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (course == GlobalConst.VIEW_CREATE_FRAGMENT) {
                                    pollSaveBtnOnClickListener.onClicked(GlobalConst.ACTION_POLL_SAVE);
                                }else{
                                    getFragmentManager().popBackStack();
                                }
                            }
                        }, 100);
                        RoomActivity.activity.pollData.clear();
                    }

                } catch (Exception e) {

                }
                Log.d(TAG, "savePoll");
            }
        });

        // 질문 보내기 버튼 이벤트
        pollSendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();

                if(!isReadySend) {
                    completCheck();
                    return;
                }

                if (RoomActivity.classUserList.size() <= 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_room_noone), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject obj = setSendData(true);
                    Log.d("last_result", obj.toString());
                    AndroidUtils.keyboardHide(getActivity());

                    webView.sendJavascript("PollCtrl.Action.Master.createPoll(" + obj.toString() + ");");
                    PollTimerFragment.answerTime = RoomActivity.activity.pollData.getShutdowntime();
                    PollTimerFragment.pollTitleName = pollTitle.getText().toString();
                    getActivity().finish();
                    RoomActivity.activity.pollData.clear();
                } catch (Exception e) {

                }
                Log.d(TAG, "makePollLIst");
            }
        });
    }


    /**
     * 질문 제목과 질문 방식이 선택된 여부를 체크하여, 저장과 보내기 버튼의 활성화/비활성화 여부를 제어하는 메서드
     */
    private void isSendableCheck() {
        Log.d(TAG, "isSendableCheck");
        Log.d(TAG, "질문 내용(제목) : " + pollTitle.getText().toString());
        Log.d(TAG, "선택한 질문 방식 : " + RoomActivity.activity.pollData.getCheckedType());
        if (sendableCheck()) {
            pollSaveBtn.setBackground(getResources().getDrawable(R.drawable.btn_poll_dialog_fill_green));
            pollSendBtn.setBackground(getResources().getDrawable(R.drawable.btn_poll_dialog_fill_green));

            pollSaveBtn.setClickable(true);
            pollSendBtn.setClickable(true);
            pollSaveBtn.setFocusable(true);
            pollSendBtn.setFocusable(true);
            isReadySend = true;
        } else {
            pollSaveBtn.setBackground(getResources().getDrawable(R.drawable.btn_poll_dialog_fill_gray));
            pollSendBtn.setBackground(getResources().getDrawable(R.drawable.btn_poll_dialog_fill_gray));

            pollSaveBtn.setClickable(false);
            pollSendBtn.setClickable(false);
            pollSaveBtn.setFocusable(false);
            pollSendBtn.setFocusable(false);
            isReadySend = false;
        }

        if(RoomActivity.activity.pollData.getCheckedType() == PollCreateData.POLL_TYPE_DRAWING) {
            pollSaveBtn.setBackground(getResources().getDrawable(R.drawable.btn_poll_dialog_fill_gray));
            pollSaveBtn.setClickable(false);
        }
    }

    private JSONObject setSendData(boolean sendFlag) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("title", pollTitle.getText().toString());
            obj.put("polltype", Integer.toString(RoomActivity.activity.pollData.getCheckedType()));
            obj.put("allowcnt", RoomActivity.activity.pollData.getDupcheck());// 고치기
            obj.put("sendflag", sendFlag);
            obj.put("shutdowntime", RoomActivity.activity.pollData.getShutdowntime());
            obj.put("polltempno", RoomActivity.activity.pollData.getPolltempno());
            obj.put("target", RoomActivity.activity.pollData.getTargetUser());
            obj.put("qusbinary", RoomActivity.activity.pollData.getCapturedImgBinary());

            if (RoomActivity.activity.pollData.getCheckedType() == PollCreateData.POLL_TYPE_MULTIPLE_CHOICE) {
                if (RoomActivity.activity.pollData.getPollCreateType1ArrSize() != 0)
                    obj.put("items", RoomActivity.activity.pollData.getQuestion());
                else
                    obj.put("items", RoomActivity.activity.pollData.getQuestion(RoomActivity.activity.pollData.getType1JSONArray()));
            } else if (RoomActivity.activity.pollData.getCheckedType() == PollCreateData.POLL_TYPE_ALTER_CHOICE) {
                if (RoomActivity.activity.pollData.getType2Checked() == PollCreateData.ALTER_OPT_CUSTOM)
                    obj.put("items", RoomActivity.activity.pollData.getType2InputQuestion());
                else
                    obj.put("items", pollType2ResultTransform(getResources().getString(RoomActivity.activity.pollData.getType2Result(RoomActivity.activity.pollData.getType2Checked()))));
            } else
                obj.put("items", "");

            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String pollType2ResultTransform(String result){
        String[] temp = result.split("\\/");
        return temp[0]+"|"+temp[1];
    }

    private void setListener(){
        optMultiChoice.setOnClickListener(btnClick);
        optAlterChoice.setOnClickListener(btnClick);
        optShortAnswer.setOnClickListener(btnClick);
        optDrawing.setOnClickListener(btnClick);
        pollShoutdownBtn.setOnClickListener(btnClick);
    }

    LinearLayout.OnClickListener btnClick = new OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.type1_poll_btn :   // 선다형
                    RoomActivity.activity.pollData.setPollTitle(pollTitle.getText().toString());
                    pollCreateType1Fragment type1fragment = new pollCreateType1Fragment().newInstance(RoomActivity.activity.pollData);
                    fragmentTransaction.replace(R.id.dialog_main_container, type1fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    AndroidUtils.keyboardHide(getActivity());

                    break;

                case R.id.type2_poll_btn :   // 양자택일
                    RoomActivity.activity.pollData.setPollTitle(pollTitle.getText().toString());
                    pollCreateType2Fragment type2fragment = new pollCreateType2Fragment().newInstance(RoomActivity.activity.pollData);
                    fragmentTransaction.replace(R.id.dialog_main_container, type2fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    AndroidUtils.keyboardHide(getActivity());
                    break;

                case R.id.type3_poll_btn :   // 단답형
                    typeImgChecked(icoShortAnswer, icoMultiChoice, icoAlterChoice, icoDrawing);
                    typeTextChecked(txtShortAnswer, txtMultiChoice, txtAlterChoice, txtDrawing);
                    type1Numtext.setText("");
                    type2TextResult.setText("");
//                    type2TextResult.setVisibility(View.INVISIBLE);
//                    type1Numtext.setVisibility(View.INVISIBLE);
                    RoomActivity.activity.pollData.setCheckedType(PollCreateData.POLL_TYPE_SHORT_ANSWER);
                    isSendableCheck();
                    AndroidUtils.keyboardHide(getActivity());
                    break;

                case R.id.btn_drawing_poll :
                    RoomActivity.activity.pollData.setPollTitle(pollTitle.getText().toString());
                    if (!RoomActivity.activity.getCreatorFlag()) {
                        Toast.makeText(getActivity().getApplicationContext(), "판서형 질문은 내 보드에서만 시작할 수 있습니다.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //RoomActivity.activity.pollData.setShutdownChecked(0);
                    DrawingPollFragment fragment = new DrawingPollFragment();
                    fragmentTransaction.replace(R.id.dialog_main_container, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    AndroidUtils.keyboardHide(getActivity());
                    break;

                case R.id.poll_shoutdown_btn :
                    RoomActivity.activity.pollData.setPollTitle(pollTitle.getText().toString());
                    pollCreateShutdownFragment shutdownFragment = new pollCreateShutdownFragment();
                    fragmentTransaction.replace(R.id.dialog_main_container, shutdownFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    AndroidUtils.keyboardHide(getActivity());
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //isSendableCheck();
        if (RoomActivity.activity.pollData.getType1JSONArray() != null)
            Log.d(TAG, RoomActivity.activity.pollData.getType1JSONArray().toString());
        if (getArguments() != null &&!getArguments().isEmpty()){  // 저장된 질문 불러오기 모드일 때..

            RequestParams params = new RequestParams();
            params.put("polltempno", getArguments().getString("polltempno"));
            RestClient.post("poll/tmp/get.json", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                    Log.d(TAG, "poll/tmp/get.json success.. response : " + obj.toString());
                    try {
                        JSONObject dataMap = obj.getJSONObject("map");
                        JSONArray item_arr = dataMap.getJSONArray("itemlist");
                        String title = dataMap.getString("title");
                        String polltype = dataMap.getString("polltype");
                        int allowcnt = dataMap.getInt("allowcnt");


                        RoomActivity.activity.pollData.setCheckedType(Integer.parseInt(polltype));
                        RoomActivity.activity.pollData.setPolltempno(getArguments().getString("polltempno"));

                        if (RoomActivity.activity.pollData.getCheckedType() == PollCreateData.POLL_TYPE_MULTIPLE_CHOICE) {
                            if (allowcnt > 1) {
                                RoomActivity.activity.pollData.setDupcheck(true);
                            }
                            RoomActivity.activity.pollData.setPoll_count(item_arr.length());
                            RoomActivity.activity.pollData.setType1JSONArray(item_arr);
                        } else if (RoomActivity.activity.pollData.getCheckedType() == PollCreateData.POLL_TYPE_ALTER_CHOICE) {
                            int type = transType(item_arr.getJSONObject(0).getString("itemnm")+"/"+item_arr.getJSONObject(1).getString("itemnm"));

                            if (type == PollCreateData.ALTER_OPT_CUSTOM)
                                RoomActivity.activity.pollData.setType2InputQuestion(item_arr.getJSONObject(0).getString("itemnm"),item_arr.getJSONObject(1).getString("itemnm"));

                            RoomActivity.activity.pollData.setType2Checked(type);
                        }

                        RoomActivity.activity.pollData.setIsChange(true);
                        change_setting();
                        getArguments().clear();

                        Log.d(TAG, "title : " + title);
                        pollTitle.setText(title);
                        isSendableCheck();
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "send message onFailure");
                    // TODO : 예외처리
                }
            });
        }

        change_setting();


        pollTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSendableCheck();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }


    public void setFindViewById() {

        optMultiChoice = (LinearLayout) rootView.findViewById(R.id.type1_poll_btn);
        optAlterChoice = (LinearLayout) rootView.findViewById(R.id.type2_poll_btn);
        optShortAnswer = (LinearLayout) rootView.findViewById(R.id.type3_poll_btn);
        optDrawing = (LinearLayout) rootView.findViewById(R.id.btn_drawing_poll);
        pollShoutdownBtn = (LinearLayout) rootView.findViewById(R.id.poll_shoutdown_btn);

        icoMultiChoice = (ImageView) rootView.findViewById(R.id.type1_check);
        icoAlterChoice = (ImageView) rootView.findViewById(R.id.type2_check);
        icoShortAnswer = (ImageView) rootView.findViewById(R.id.type3_check);
        icoDrawing = (ImageView) rootView.findViewById(R.id.drawing_poll_check);

        txtMultiChoice = (TextView) rootView.findViewById(R.id.type1_text);
        txtAlterChoice = (TextView) rootView.findViewById(R.id.type2_text);
        txtShortAnswer = (TextView) rootView.findViewById(R.id.type3_text);
        txtDrawing = (TextView) rootView.findViewById(R.id.drawing_poll_text);

        type1Numtext = (TextView)rootView.findViewById(R.id.type1_numText);
        type2TextResult = (TextView)rootView.findViewById(R.id.type2_text_result);
        shutdownText = (TextView)rootView.findViewById(R.id.shutdown_text);
        pollSaveBtn = (TextView)rootView.findViewById(R.id.poll_save_btn);
        pollSendBtn = (TextView)rootView.findViewById(R.id.poll_send_btn);
        pollCreateCancel = (TextView)rootView.findViewById(R.id.poll_create_cancel);

        pollTitle = (EditText)rootView.findViewById(R.id.poll_title);

        if (RoomActivity.activity.pollData.getCheckedType() == PollCreateData.POLL_TYPE_DRAWING) {
            shutdownText.setText("제한 시간 없음");
        } else {
            shutdownText.setText(String.format(getResources().getString(R.string.poll_time_item), "30"));
        }
    }

    public int transType(String type) {
        Log.d("PollCreate_trans", type);
        if (type.equals(getResources().getString(PollCreateData.TYPE2_BASIC_QUESTION_1)))
            return PollCreateData.ALTER_OPT_A_B;
        else if (type.equals(getResources().getString(PollCreateData.TYPE2_BASIC_QUESTION_2)))
            return PollCreateData.ALTER_OPT_O_X;
        else if (type.equals(getResources().getString(PollCreateData.TYPE2_BASIC_QUESTION_3)))
            return PollCreateData.ALTER_OPT_AGREE_DISAGREE;
        else
            return PollCreateData.ALTER_OPT_CUSTOM;
    }


    public boolean completCheck(){

        if (pollTitle.getText().length() == 0 && RoomActivity.activity.pollData.getCheckedType() != -1) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.poll_question_guide), Toast.LENGTH_LONG).show();
            return false;
        } else if (pollTitle.getText().length() != 0 && RoomActivity.activity.pollData.getCheckedType() == -1) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.poll_type), Toast.LENGTH_LONG).show();
            return false;
        }



        return true;
    }


    public boolean sendableCheck() {
        if (pollTitle.getText().length() == 0)
            return false;
        if (RoomActivity.activity.pollData.getCheckedType() == -1)
            return false;
        return true;
    }


    public void change_setting() {
        if (RoomActivity.activity.pollData.getIsChange()){
            switch (RoomActivity.activity.pollData.getCheckedType()){
                case PollCreateData.POLL_TYPE_MULTIPLE_CHOICE :
                    typeImgChecked(icoMultiChoice, icoAlterChoice, icoShortAnswer, icoDrawing);
                    typeTextChecked(txtMultiChoice, txtAlterChoice, txtShortAnswer, txtDrawing);
                    type1Numtext.setText(String.format(getResources().getString(R.string.poll_single_count), Integer.toString(RoomActivity.activity.pollData.getPoll_count())));
                    type2TextResult.setText("");
//                    type1Numtext.setVisibility(View.VISIBLE);
//                    type2TextResult.setVisibility(View.INVISIBLE);
                    break;
                case PollCreateData.POLL_TYPE_ALTER_CHOICE :
                    typeImgChecked(icoAlterChoice, icoMultiChoice, icoShortAnswer, icoDrawing);
                    typeTextChecked(txtAlterChoice, txtMultiChoice, txtShortAnswer, txtDrawing);
                    switch(RoomActivity.activity.pollData.getType2Checked()){
                        case PollCreateData.ALTER_OPT_A_B :
                            type2TextResult.setText(getResources().getString(R.string.poll_ox_ab));
                            break;
                        case PollCreateData.ALTER_OPT_O_X :
                            type2TextResult.setText(getResources().getString(R.string.poll_ox_ox));
                            break;
                        case PollCreateData.ALTER_OPT_AGREE_DISAGREE :
                            type2TextResult.setText(getResources().getString(R.string.poll_ox_yn));
                            break;
                        case PollCreateData.ALTER_OPT_CUSTOM :
                            type2TextResult.setText(getResources().getString(R.string.poll_ox_direct));
                            break;
                    }
                    type1Numtext.setText("");
//                    type2TextResult.setVisibility(View.VISIBLE);
//                    type1Numtext.setVisibility(View.INVISIBLE);
                    break;
                case PollCreateData.POLL_TYPE_SHORT_ANSWER :
                    typeImgChecked(icoShortAnswer, icoMultiChoice, icoAlterChoice, icoDrawing);
                    typeTextChecked(txtShortAnswer, txtMultiChoice, txtAlterChoice, txtDrawing);
//                    type2TextResult.setVisibility(View.INVISIBLE);
//                    type1Numtext.setVisibility(View.INVISIBLE);
                    type1Numtext.setText("");
                    type2TextResult.setText("");
                    break;

                case PollCreateData.POLL_TYPE_DRAWING :
                    typeImgChecked(icoDrawing, icoMultiChoice, icoAlterChoice, icoShortAnswer);
                    typeTextChecked(txtDrawing, txtMultiChoice, txtAlterChoice, txtShortAnswer);
//                    type2TextResult.setVisibility(View.INVISIBLE);
//                    type1Numtext.setVisibility(View.INVISIBLE);
                    type1Numtext.setText("");
                    type2TextResult.setText("");

                    pollSaveBtn.setBackground(getResources().getDrawable(R.drawable.btn_poll_dialog_fill_gray));
                    pollSaveBtn.setClickable(false);

                    break;
            }
            RoomActivity.activity.pollData.setIsChange(false);
        }

        pollTitle.setText(RoomActivity.activity.pollData.getPollTitle());

        int shutdownTime = RoomActivity.activity.pollData.getShutdowntime();
        if (shutdownTime == 0) {
            shutdownText.setText("제한 시간 없음");
        } else {
            shutdownText.setText(String.format(getResources().getString(R.string.poll_time_item), Integer.toString(shutdownTime)));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void setTouchKeyboardHide(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    AndroidUtils.keyboardHide(getActivity());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setTouchKeyboardHide(innerView);
            }
        }
    }
}