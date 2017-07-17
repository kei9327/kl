package com.knowlounge.fragment.poll;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.PollCreateData;
import com.knowlounge.util.AndroidUtils;

import org.json.JSONException;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class pollCreateType1Fragment extends android.support.v4.app.Fragment {
    private final String TAG = "pollCreateType1Fragment";

    View rootView;
    View pollCreateType1Row;

    View lastQuestion;
    WenotePreferenceManager prefManager;


    @BindView(R.id.add_question) LinearLayout addQuestion;
    @BindView(R.id.del_question) LinearLayout delQuestion;
    @BindView(R.id.poll_question_add_layout) LinearLayout pollQuestionAddLayout;

    @BindView(R.id.btn_poll_addquestion) ImageView btnPollAddquestion;
    @BindView(R.id.btn_poll_delquestion) ImageView btnPollDelquestion;
    // ImageView dupchoiceCheck;
    @BindView(R.id.type1_poll_backbtn) ImageView backBtn;

    @BindView(R.id.allow_multichoice_switch) SwitchCompat switchAllowMultiChoice;

    @BindView(R.id.poll_addquestion_text) TextView pollAddquestionText;
    @BindView(R.id.poll_delquestion_text) TextView pollDelquestionText;
    @BindView(R.id.type1_ok_btn) TextView confirmBtn;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    PollCreateData pollCreateDataTemp;
    ArrayList<EditText> tempEdit;

    public static pollCreateType1Fragment newInstance(PollCreateData pollCreate_data_set) {
        pollCreateType1Fragment fragment = new pollCreateType1Fragment();
        fragment.pollCreateDataTemp = pollCreate_data_set;
        return fragment;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");

        prefManager = WenotePreferenceManager.getInstance(getActivity());

        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_create_type1, container, false);
        ButterKnife.bind(this, rootView);

        tempEdit = new ArrayList<EditText>();
        lastQuestion = null;

        addQuestion.setClickable(true);
        delQuestion.setClickable(false);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        try {
            switchAllowMultiChoice.setChecked(pollCreateDataTemp.getDupchecked());

            if (pollCreateDataTemp.getDupchecked()) {
                switchAllowMultiChoice.setChecked(true);
                //dupchoiceCheck.setImageResource(R.drawable.btn_checkbox_on);
                //dupchoiceText.setTextColor(Color.parseColor("#505050"));
            } else {
                //dupchoiceCheck.setImageResource(R.drawable.btn_checkbox);
                //dupchoiceText.setTextColor(Color.parseColor("#969696"));
            }
        } catch (NullPointerException ne) {
            ne.printStackTrace();
            switchAllowMultiChoice.setChecked(false);
            //dupchoiceCheck.setImageResource(R.drawable.btn_checkbox);
            //dupchoiceText.setTextColor(Color.parseColor("#969696"));
        }

        if (pollCreateDataTemp.getType1JSONArray() == null && pollCreateDataTemp.getPollCreateType1ArrSize() == 0) { // 처음 들어왔을 때
            Log.d(TAG, "FIRST_ADD");
            for (int i = 0; i < PollCreateData.DEFAULT_ITEM_CNT; i++) {
                addQuestion(inflater, i + 1, "");
                Log.d(TAG, "add blank poll");
            }
        } else if (pollCreateDataTemp.getType1JSONArray() != null && pollCreateDataTemp.getPollCreateType1ArrSize() == 0 && pollCreateDataTemp.getCheckedType() == 0) { //pollList를 타고 들어왔을 때
            try {
                Log.d(TAG, "JSON_ADD");
                int temp = pollCreateDataTemp.getType1JSONArray().length() > PollCreateData.DEFAULT_ITEM_CNT ? pollCreateDataTemp.getType1JSONArray().length() : PollCreateData.DEFAULT_ITEM_CNT;
                for (int i = 0; i < temp; i++) {
                    if (pollCreateDataTemp.getType1JSONArray().length() > i) {
                        addQuestion(inflater, i + 1, pollCreateDataTemp.getType1JSONArray().getJSONObject(i).getString("itemnm"));
                        Log.d(TAG, pollCreateDataTemp.getType1JSONArray().getJSONObject(i).getString("itemnm") + " add poll");
                    } else {
                        addQuestion(inflater, i + 1, "");
                        Log.d(TAG, "add blank poll");
                    }
                }
            } catch (JSONException j) {
                Log.e(TAG, j.getMessage());
            }
        } else if (pollCreateDataTemp.getPollCreateType1ArrSize() != 0) { // 수정하기 위해 다시 들어올 때
            Log.d(TAG, "AGAIN COME BACK_ADD");
            int temp = pollCreateDataTemp.getPollCreateType1ArrSize() > PollCreateData.DEFAULT_ITEM_CNT ? pollCreateDataTemp.getPollCreateType1ArrSize() : PollCreateData.DEFAULT_ITEM_CNT;
            for (int i = 0; i < temp; i++) {
                if (pollCreateDataTemp.getPollCreateType1ArrSize() > i) {
                    addQuestion(inflater, i + 1, pollCreateDataTemp.getPollCreateType1Arr_Index(i).getText().toString());
                    Log.d(TAG, pollCreateDataTemp.getPollCreateType1Arr_Index(i).getText().toString() + " add poll");
                } else {
                    addQuestion(inflater, i + 1, "");
                    Log.d(TAG, "add blank poll");
                }
            }
        }

        switchAllowMultiChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pollCreateDataTemp.setDupcheck(isChecked);
            }
        });

        /*
        dupchoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pollCreateDataTemp.getDupchecked()) {
                    pollCreateDataTemp.setDupcheck(true);
                    dupchoiceCheck.setImageResource(R.drawable.btn_checkbox_on);
                    dupchoiceText.setTextColor(Color.parseColor("#505050"));
                }
                else {
                    pollCreateDataTemp.setDupcheck(false);
                    dupchoiceCheck.setImageResource(R.drawable.btn_checkbox);
                    dupchoiceText.setTextColor(Color.parseColor("#969696"));
                }
            }
        });
        */

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( tempEdit.size() < PollCreateData.MAX_ITEM_CNT)
                   addQuestion(inflater, tempEdit.size() + 1, "");
                if (tempEdit.size() == PollCreateData.DEFAULT_ITEM_CNT +1)
                {
                    delQuestion.setClickable(true);
                    ableColor(btnPollDelquestion, pollDelquestionText, R.drawable.btn_poll_delquestion_on, "#505050");
                }
                if (tempEdit.size() == PollCreateData.MAX_ITEM_CNT) {
                    addQuestion.setClickable(false);
                    ableColor(btnPollAddquestion, pollAddquestionText, R.drawable.btn_poll_addquestion, "#969696");
                }
            }
        });


        delQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tempEdit.size() > PollCreateData.DEFAULT_ITEM_CNT)
                    delQuestoin(pollQuestionAddLayout, tempEdit.size());
                if (tempEdit.size() == PollCreateData.DEFAULT_ITEM_CNT) {
                    delQuestion.setClickable(false);
                    ableColor(btnPollDelquestion, pollDelquestionText, R.drawable.btn_poll_delquestion, "#969696");
                }
                if (tempEdit.size() == PollCreateData.MAX_ITEM_CNT -1) {
                    addQuestion.setClickable(true);
                    ableColor(btnPollAddquestion, pollAddquestionText, R.drawable.btn_poll_addquestion_on , "#505050");
                }
            }
        });

        // 뒤로가기 버튼 이벤트
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                pollCreateDataTemp.clear_type1();
            }
        });

        // 확인 버튼 이벤트
        confirmBtn.setOnClickListener(new View.OnClickListener()  // 확인 버튼
        {
            public void onClick(View v)
            {
                tempEdit.get(tempEdit.size() - 1).requestFocus();   //강제 키보드 내리기
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tempEdit.get(tempEdit.size() - 1).getWindowToken(), 0);

                filter();   //빈 질문 필터링
                pollCreateDataTemp.setPollCreateType1EditArrClear();
                pollCreateDataTemp.setPollCreateType1EditArr(tempEdit);
                pollCreateDataTemp.setIsChange(true);
                pollCreateDataTemp.setCheckedType(PollCreateData.POLL_TYPE_MULTIPLE_CHOICE);
                getFragmentManager().popBackStack();    //finish
            }
        });


        return rootView;
    }


    public void addQuestion(LayoutInflater inflater, int num, String text) {
        pollCreateType1Row = inflater.inflate(R.layout.poll_create_type1_row, null);
        TextView poll_type1_num = (TextView) pollCreateType1Row.findViewById(R.id.poll_type1_num);
        final EditText poll_type1_edit = (EditText) pollCreateType1Row.findViewById(R.id.poll_type1_edit);
        View poll_create_type1_underline = (View) pollCreateType1Row.findViewById(R.id.poll_create_type1_underline);

        if (lastQuestion != null) {
            lastQuestion.setBackgroundColor(Color.parseColor("#dedfe0"));
            poll_create_type1_underline.setBackgroundColor(Color.parseColor("#00000000"));
        } else {
            poll_create_type1_underline.setBackgroundColor(Color.parseColor("#00000000"));
        }
        lastQuestion = poll_create_type1_underline;

        poll_type1_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (AndroidUtils.isOverByte(s.toString(),50))
                    s.delete(s.length()-2, s.length()-1);
            }
        });

        poll_type1_num.setText(Integer.toString(num) + ".");
        poll_type1_edit.setText(text);
        tempEdit.add(poll_type1_edit);
        pollQuestionAddLayout.addView(pollCreateType1Row);
    }


    public void delQuestoin(LinearLayout layout, int index) {
        tempEdit.remove(index - 1);
        layout.removeViewAt(index - 1);
        lastQuestion = layout.getChildAt(index -2).findViewById(R.id.poll_create_type1_underline);
        lastQuestion.setBackgroundColor(Color.parseColor("#00000000"));
    }


    public void filter() {
        boolean result = checkPollCnt();
        int i = tempEdit.size()-1;
        while (i >= 0) {
            if (tempEdit.get(i).getText().toString().length() == 0) {
                if(result)
                    tempEdit.remove(i);
                else
                    tempEdit.get(i).setText(Integer.toString(i+1));
            }
            i--;
        }

    }


    public boolean checkPollCnt(){
        int cnt = 0;
        for(int i = tempEdit.size()-1; i>=0; i--) {
            if(tempEdit.get(i).getText().toString().length() != 0)
                cnt++;
        }
        return cnt >= PollCreateData.DEFAULT_ITEM_CNT ? true : false;
    }


    public void ableColor(ImageView iv, TextView tv, int resouce, String color) {
        iv.setImageResource(resouce);
        tv.setTextColor(Color.parseColor(color));
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
}