package com.knowlounge.fragment.poll;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.adapter.PollAnswerAdapter;
import com.knowlounge.model.AnswerQuestionAndAnswerResultData;
import com.knowlounge.model.PollCreateData;
import com.knowlounge.plugins.PollPlugin;
import com.knowlounge.util.AndroidUtils;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PollAnswerFragment extends PollDialogFragment implements PollPlugin.EventListener {
    private final String TAG = "PollAnswerFragment";

    private View rootView;
    private Thread mTimeThread;          //시간 스레드
    private boolean mThreadRunning = true;

    private boolean isRunningTimer = false;

    private int mAnswerTime;

    private TextView mPollAnswerTitle, mTimeLimit, btnMinimize, sendBtn;

    private LinearLayout mPollLayout;      // 선다형, 양자택일, 단답형 UI를 포함하는 레이아웃
    private ScrollView singleAnswerView;   // 단일 선택만 가능한 답안의 뷰
    private ListView multipleAnswerView;   // 다중 선택이 가능한 답안의 뷰
    private EditText shortAnswerEditText;  // 단답형 답안의 뷰

    private LinearLayout mDrawingPollLayout;  // 판서형 질문 레이아웃

    // 전체 화면 옵션
    LinearLayout optFullScreen;
    ImageView icoFullScreen;
    TextView txtFullScreen;

    // 영역 선택 옵션
    LinearLayout optSelection;
    ImageView icoSelection;
    TextView txtSelection;

    private PollAnswerAdapter adapter;
    private boolean mode;

    private String pollNo, pollType, shutdownTime ;
    private RadioGroup radioGroupSingleAnswer;
    private String mRadioResult;
    private int checkOneSecond=0;

    private String imgBinary = "";
    ImageView capturePreview;

    public static PollAnswerFragment _instance;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        _instance = this;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_answer, container, false);

        setFindViewById();
        PollPlugin.setOnEventListener(this);

        try {

            JSONObject obj = null;
            if (getArguments().getString("obj") != null) {
                Log.d(TAG, "새로 답변 화면을 여는 flow 입니다.");
                RoomActivity.activity.pollData.setPollDataTmp(getArguments().getString("obj"));
                obj = new JSONObject(getArguments().getString("obj"));

                // 영역 선택을 한 상태에서 창을 최소화했다가 다시 열었을 때..
                if (RoomActivity.activity.pollData.getDrawingMethod() == PollCreateData.SELECTION_CAPTURE) {
                    Log.d(TAG, "영역 선택을 한 상태에서 창을 최소화했다가 다시 열었습니다.");
                    String imgBinary = RoomActivity.activity.pollData.getCapturedImgBinary();
                    this.imgBinary = imgBinary;
                    setCapturePreview(imgBinary);
                } else {
                    //captureFullScreen("answer");
                }
            } else {
                Log.d(TAG, "영역 캡쳐 완료 후 복귀하는 flow 입니다.");
                obj = new JSONObject(RoomActivity.activity.pollData.getPollDataTmp());
                String imgBinary = RoomActivity.activity.pollData.getCapturedImgBinary();
                this.imgBinary = imgBinary;
                setCapturePreview(imgBinary);

                typeImgChecked(icoSelection, icoFullScreen, null, null);
                typeTextChecked(txtSelection, txtFullScreen, null, null);
            }

            Log.d(TAG, "PARAM : " + obj.toString());

            JSONObject pollData = obj.getJSONObject("polldata").getJSONObject("map");

            pollNo = pollData.getString("pollno");
            pollType = pollData.getString("polltype");
            String title = pollData.getString("title");
            JSONArray itemList = pollData.getJSONArray("itemlist");

            shutdownTime = obj.getString("timelimit");
            int allowCnt = pollData.getInt("allowcnt");
            final ArrayList<AnswerQuestionAndAnswerResultData> itemStr = new ArrayList<AnswerQuestionAndAnswerResultData>();

            mPollAnswerTitle.setText(title);

            // 시간제한 타이머 설정..
            if (TextUtils.equals(shutdownTime, "0")) {
                mThreadRunning = false;
                mTimeLimit.setText("");
            } else {
                mThreadRunning = true;
                mAnswerTime = Integer.parseInt(shutdownTime) / 1000;
                if (RoomActivity.activity.pollAnswerTime == -1) {
                    isRunningTimer = false;
                    RoomActivity.activity.pollAnswerTime = Integer.parseInt(shutdownTime) / 1000;
                } else {
                    isRunningTimer = true;
                }

                String timerStr = " (" + String.format(getResources().getString(R.string.answer_remain), Integer.toString(RoomActivity.activity.pollAnswerTime)) + ")";
                Log.d(TAG, timerStr);
                mTimeLimit.setText(timerStr);
            }

            // 영역 선택 방법 UI 처리..
           switch (RoomActivity.activity.pollData.getDrawingMethod()) {
                case PollCreateData.FULL_SCREEN_CAPTURE :
                    typeImgChecked(icoFullScreen, icoSelection, null, null);
                    typeTextChecked(txtFullScreen, txtSelection, null, null);
                    break;
                case PollCreateData.SELECTION_CAPTURE :
                    typeImgChecked(icoSelection, icoFullScreen, null, null);
                    typeTextChecked(txtSelection, txtFullScreen, null, null);
                    break;
            }

            switch (Integer.parseInt(pollType)) {
                case PollCreateData.POLL_TYPE_MULTIPLE_CHOICE:
                case PollCreateData.POLL_TYPE_ALTER_CHOICE:
                    mPollLayout.setVisibility(View.VISIBLE);
                        mDrawingPollLayout.setVisibility(View.GONE);

                    shortAnswerEditText.setVisibility(View.GONE);

                    int len = itemList.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject item_obj = itemList.getJSONObject(i);
                        String pollitemno = item_obj.getString("pollitemno");
                        String itemnm = item_obj.getString("itemnm");
                        itemStr.add(new AnswerQuestionAndAnswerResultData(pollitemno, itemnm));
                        Log.d(TAG, itemnm);
                    }

                    if (allowCnt <= 1) {  // 단일 선택 지문..
                        Log.d(TAG, "just one select");
                        singleAnswerView.setVisibility(View.VISIBLE);
                        multipleAnswerView.setVisibility(View.GONE);

                        mode = true;
                        int index = 0;
                        for (AnswerQuestionAndAnswerResultData data : itemStr) {
                            String itemNm = data.getItemNm();
                            Log.d(TAG, itemNm);
                            View singleRow = getActivity().getLayoutInflater().inflate(R.layout.poll_answer_single_row, radioGroupSingleAnswer, false);
                            RadioButton radioButton = (RadioButton) singleRow.findViewWithTag("single_radio_btn");
                            radioButton.setId(index++);
                            radioButton.setText(itemNm);
//                        if(index == itemStr.size()){
//                            radioButton.setBackgroundColor(Color.parseColor("#00000000"));
//                        }
                            radioGroupSingleAnswer.addView(singleRow);
                        }
                        radioGroupSingleAnswer.setOnCheckedChangeListener(
                                new RadioGroup.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                                        Log.d(TAG, "onCheckedChanged");
                                        ((RadioButton) group.getChildAt(checkedId)).setTextColor(Color.parseColor("#5a5a5a"));
                                        mRadioResult = itemStr.get(checkedId).getPollitemno();
                                    }
                                }
                        );
                    } else {  // 다중 선택 지문..
                        Log.d(TAG, "multiple select");
                        singleAnswerView.setVisibility(View.GONE);
                        multipleAnswerView.setVisibility(View.VISIBLE);

                        mode = false;
                        adapter = new PollAnswerAdapter(getActivity().getBaseContext(), itemStr);

                        multipleAnswerView.setAdapter(adapter);
                        multipleAnswerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                adapter.toggleChecked(position);
                            }
                        });
                    }
                    break;
                case PollCreateData.POLL_TYPE_SHORT_ANSWER :
                    mDrawingPollLayout.setVisibility(View.GONE);
                    mPollLayout.setVisibility(View.VISIBLE);

                    shortAnswerEditText.setVisibility(View.VISIBLE);
                    singleAnswerView.setVisibility(View.GONE);
                    multipleAnswerView.setVisibility(View.GONE);
                    break;
                case PollCreateData.POLL_TYPE_DRAWING :
                    mPollLayout.setVisibility(View.GONE);
                    mDrawingPollLayout.setVisibility(View.VISIBLE);
                    optFullScreen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            typeImgChecked(icoFullScreen, icoSelection, null, null);
                            typeTextChecked(txtFullScreen, txtSelection, null, null);
                            RoomActivity.activity.pollData.setIsChange(true);
                            RoomActivity.activity.pollData.setCheckedType(PollCreateData.POLL_TYPE_DRAWING);
                            RoomActivity.activity.pollData.setDrawingMethod(PollCreateData.FULL_SCREEN_CAPTURE);
                            captureFullScreen("answer");
                        }
                    });

                    optSelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RoomActivity.activity.resetZoom();   // 줌 값은 100으로 초기화 : 줌 상태에서 영역 캡쳐를 진행할 때 이슈가 발생함 - 2017.01.26
                            getActivity().finish();
                            if (RoomActivity.activity != null) {
                                RoomActivity.activity.invokeAreaSelector("answer");
                            }
                        }
                    });
                    break;
            }


            //쓰레드 실행
            mTimeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while (RoomActivity.activity.pollAnswerTime != -1 && mThreadRunning) {
                            if(checkOneSecond++ > 0) {
                                Message msg = shutdown_time_handler.obtainMessage();
                                shutdown_time_handler.sendMessage(msg);
                            }
                            Thread.sleep(1000);
                        }
                        if (RoomActivity.activity.pollAnswerTime == 0) {
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_poll_end), Toast.LENGTH_SHORT).show();
                            RoomActivity.activity.pollAnswerTime = -1;
                        }
                        getActivity().finish();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });

            if(mThreadRunning) {
                if (!isRunningTimer) {
                    RoomActivity.activity.startPollTimerThread();
                    //mTimeThread.start();
                }
            }

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    mThreadRunning = false;
                }
                return false;
            }
        });
        if (TextUtils.equals(pollType, Integer.toString(PollCreateData.POLL_TYPE_DRAWING))) {
            btnMinimize.setVisibility(View.VISIBLE);
        }
        btnMinimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        // 폴 보내기 버튼 이벤트 리스너..
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 폴 보내기 and 초기화
                try {
                    CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
                    JSONObject obj = new JSONObject();
                    obj.put("pollno", pollNo);
                    obj.put("polltype", pollType);
                    obj.put("userno", RoomActivity.activity.getUserNo());
                    obj.put("userid", RoomActivity.activity.getUserId());
                    obj.put("usernm", RoomActivity.activity.getUserNm());
                    obj.put("timelimit", shutdownTime);
                    switch(pollType) {
                        case "0" :
                        case "1" :
                            if (mode) {  //Single Select
                                if(mRadioResult!=null && mRadioResult.length()!=0)
                                    obj.put("pollitemno", mRadioResult);
                                else{
                                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_poll_alert), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {    //Multiple Select
                                if(adapter.isChoiceAnswerMultiple()) {
                                    obj.put("pollitemno", adapter.getResult());
                                } else {
                                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_poll_alert) , Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            break;
                        case "2" :
                            obj.put("pollitemno", "");
                            if (shortAnswerEditText.getText().toString().length() != 0) {
                                obj.put("answertxt", shortAnswerEditText.getText().toString());
                            } else {
                                Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_poll_alert), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            break;
                        case "3" :
                            if (!TextUtils.isEmpty(imgBinary)) {
                                obj.put("answerbin", imgBinary);
                            } else {
                                Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_poll_alert), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            RoomActivity.activity.setIsPollProgress(false);
                            RoomActivity.activity.setAnswerPollBtn();
                            break;
                    }

                    Log.d(TAG, "Answer Data Param : " + obj.toString());

                    AndroidUtils.keyboardHide(getActivity());
                    webView.sendJavascript("PollCtrl.Action.Attender.submitPollResult(" + obj + ")");
                    mThreadRunning = false;

                } catch (JSONException j) {
                    j.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
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

    private Handler shutdown_time_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateTime();
        }
    };


    public void updateTime() {
        if (context != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String timerStr = " (" + String.format(context.getResources().getString(R.string.answer_remain), Integer.toString(RoomActivity.activity.pollAnswerTime)) + ")";
                    mTimeLimit.setText(timerStr);
                }
            });
        }
    }


    public void setFindViewById() {
        mPollAnswerTitle = (TextView) rootView.findViewById(R.id.poll_answer_title);
        mTimeLimit = (TextView) rootView.findViewById(R.id.time_limit);

        btnMinimize = (TextView) rootView.findViewById(R.id.poll_answer_minimize_btn);
        sendBtn = (TextView) rootView.findViewById(R.id.poll_answer_ok_btn);

        singleAnswerView = (ScrollView) rootView.findViewById(R.id.single_select_answer);
        multipleAnswerView = (ListView) rootView.findViewById(R.id.multi_select_answer);

        radioGroupSingleAnswer = (RadioGroup) rootView.findViewById(R.id.single_answer_radiogroup);

        shortAnswerEditText = (EditText) rootView.findViewById(R.id.poll_answer_type3_edit);

        mPollLayout = (LinearLayout) rootView.findViewById(R.id.layout_poll_answer);
        mDrawingPollLayout = (LinearLayout) rootView.findViewById(R.id.layout_drawing_poll_answer);

        optFullScreen = (LinearLayout) rootView.findViewById(R.id.opt_full_screen);
        icoFullScreen = (ImageView) rootView.findViewById(R.id.ico_full_screen);
        txtFullScreen = (TextView) rootView.findViewById(R.id.txt_full_screen);

        optSelection = (LinearLayout) rootView.findViewById(R.id.opt_selection);
        icoSelection = (ImageView) rootView.findViewById(R.id.ico_selection);
        txtSelection = (TextView) rootView.findViewById(R.id.txt_selection);

        capturePreview = (ImageView) rootView.findViewById(R.id.capture_preview);

    }


    /**
     * 전체화면 캡쳐 시작
     */
    public void captureFullScreen(String mode) {
        RoomActivity.activity.mWebViewFragment.getCordovaWebView().sendJavascript("PollCtrl.UI.captureCanvas('" + mode + "')");
    }

    @Override
    public void onExitPoll() {
        Log.d(TAG, "onExitPoll");
        if (getActivity() != null) {
            mThreadRunning = false;
            //Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.toast_poll_aborted), Toast.LENGTH_SHORT).show();
            RoomActivity.activity.pollData.clear();
            RoomActivity.activity.pollAnswerTime = -1;
            getActivity().finish();
        }
    }

    @Override
    public void onUpdateAnswerUser(String userNo) {

    }

    /**
     * 전체화면 캡쳐 바이너리 처리
     * @param imgBinary
     */
    public void applyFullScreenCapture(String imgBinary) {
        Log.d(TAG, "applyFullScreenCapture");
        this.imgBinary = imgBinary;
        setCapturePreview(this.imgBinary);
    }


    /**
     * 캡쳐한 바이너리를 ImageView에 출력
     * @param base64Str
     */
    public void setCapturePreview(String base64Str) {
        final byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);

        if (context != null) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(context)
                            .load(decodedString)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(capturePreview);
                }
            });
        }
    }
}