package com.knowlounge.fragment.poll;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.PollCreateData;

public class pollCreateType2Fragment extends PollDialogFragment {
    private final String TAG = "pollCreateType1Fragment";

    private WenotePreferenceManager prefManager;

    private Context mContext;
    View rootView;
    PollCreateData pollCreateDataSet;

    ImageView type2PollBackbtn, type2Question1Check, type2Question2Check, type2Question3Check, type2Question4Check;
    LinearLayout type2Check1, type2Check2, type2Check3, type2Check4, type2DirectInputLayout;
    TextView type2Question1Text, type2Question2Text, type2Question3Text, type2Question4Text, type2OkBtn;
    EditText type2InputEdit1, type2InputEdit2;


    public static pollCreateType2Fragment newInstance(PollCreateData pollCreate_data_set) {
        pollCreateType2Fragment fragment = new pollCreateType2Fragment();
        fragment.pollCreateDataSet = pollCreate_data_set;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d("pollCreateType1Fragment", "come in");

        prefManager = WenotePreferenceManager.getInstance(getActivity());

        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_create_type2, container, false);

        setFindViewById();

        type2DirectInputLayout.setVisibility(View.INVISIBLE);

        type2Check1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                type2_select(PollCreateData.ALTER_OPT_A_B);
                pollCreateDataSet.setType2Checked(PollCreateData.ALTER_OPT_A_B);
            }
        });
        type2Check2.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                type2_select(PollCreateData.ALTER_OPT_O_X);
                pollCreateDataSet.setType2Checked(PollCreateData.ALTER_OPT_O_X);
            }
        });
        type2Check3.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                type2_select(PollCreateData.ALTER_OPT_AGREE_DISAGREE);
                pollCreateDataSet.setType2Checked(PollCreateData.ALTER_OPT_AGREE_DISAGREE);
            }
        });
        type2Check4.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView) {
                type2_select(PollCreateData.ALTER_OPT_CUSTOM);
                pollCreateDataSet.setType2Checked(PollCreateData.ALTER_OPT_CUSTOM);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        type2PollBackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                pollCreateDataSet.clear_type2();
            }
        });

        type2OkBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {

                type2InputEdit1.requestFocus();   //강제 키보드 내리기
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(type2InputEdit1.getWindowToken(), 0);
                if(pollCreateDataSet.getType2Checked() == -1) {
                    return;
                }

                String firstInputStr = type2InputEdit1.getText().toString();
                String secondInputStr = type2InputEdit2.getText().toString();

                if (pollCreateDataSet.getType2Checked() == PollCreateData.ALTER_OPT_CUSTOM && (firstInputStr.isEmpty() || secondInputStr.isEmpty())) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_poll_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

//                if(pollCreateDataSet.getType2Checked() == PollCreateData.ALTER_OPT_CUSTOM && (firstInputStr.isEmpty() || secondInputStr.isEmpty())) {
//                    type2InputEdit1.setText("1");
//                    type2InputEdit2.setText("2");
//                }

                pollCreateDataSet.setType2InputQuestion(type2InputEdit1.getText().toString(), type2InputEdit2.getText().toString());
                pollCreateDataSet.setIsChange(true);
                getFragmentManager().popBackStack();
                pollCreateDataSet.setCheckedType(PollCreateData.POLL_TYPE_ALTER_CHOICE);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        switch (pollCreateDataSet.getType2Checked()){
            case PollCreateData.ALTER_OPT_A_B:
                type2_select(PollCreateData.ALTER_OPT_A_B);
                break;
            case PollCreateData.ALTER_OPT_O_X:
                type2_select(PollCreateData.ALTER_OPT_O_X);
                break;
            case PollCreateData.ALTER_OPT_AGREE_DISAGREE:
                type2_select(PollCreateData.ALTER_OPT_AGREE_DISAGREE);
                break;
            case PollCreateData.ALTER_OPT_CUSTOM:
                Log.d(TAG, "type4 come in");
                type2_select(PollCreateData.ALTER_OPT_CUSTOM);
                Log.d(TAG, pollCreateDataSet.getType2InputQuestion());
                String[] question = pollCreateDataSet.getType2InputQuestion().split("\\|");

                type2InputEdit1.setText(question[0]);
                type2InputEdit2.setText(question[1]);
                break;
        }
    }



    public void type2_select(int index) {
        switch (index) {
            case PollCreateData.ALTER_OPT_A_B:
                typeImgChecked(type2Question1Check, type2Question2Check, type2Question3Check, type2Question4Check);
                typeTextChecked(type2Question1Text, type2Question2Text, type2Question3Text, type2Question4Text);
                type2DirectInputLayout.setVisibility(View.GONE);
                break;
            case PollCreateData.ALTER_OPT_O_X:
                typeImgChecked(type2Question2Check, type2Question1Check, type2Question3Check, type2Question4Check);
                typeTextChecked(type2Question2Text, type2Question1Text, type2Question3Text, type2Question4Text);
                type2DirectInputLayout.setVisibility(View.GONE);
                break;
            case PollCreateData.ALTER_OPT_AGREE_DISAGREE:
                typeImgChecked(type2Question3Check, type2Question2Check, type2Question1Check, type2Question4Check);
                typeTextChecked(type2Question3Text, type2Question2Text, type2Question1Text, type2Question4Text);
                type2DirectInputLayout.setVisibility(View.GONE);
                break;
            case PollCreateData.ALTER_OPT_CUSTOM:
                typeImgChecked(type2Question4Check, type2Question2Check, type2Question3Check, type2Question1Check);
                typeTextChecked(type2Question4Text, type2Question2Text, type2Question3Text, type2Question1Text);
                type2DirectInputLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    private void setFindViewById() {

        type2PollBackbtn = (ImageView) rootView.findViewById(R.id.type2_poll_backbtn);
        type2Question1Check = (ImageView) rootView.findViewById(R.id.type2_question_check);
        type2Question2Check = (ImageView) rootView.findViewById(R.id.type2_question2_check);
        type2Question3Check = (ImageView) rootView.findViewById(R.id.type2_question3_check);
        type2Question4Check = (ImageView) rootView.findViewById(R.id.type2_question4_check);

        type2Check1 = (LinearLayout) rootView.findViewById(R.id.type2_check_1);
        type2Check2 = (LinearLayout) rootView.findViewById(R.id.type2_check_2);
        type2Check3 = (LinearLayout) rootView.findViewById(R.id.type2_check_3);
        type2Check4 = (LinearLayout) rootView.findViewById(R.id.type2_direct_input_btn);
        type2DirectInputLayout = (LinearLayout) rootView.findViewById(R.id.type2_direct_input_layout);

        type2Question1Text = (TextView) rootView.findViewById(R.id.type2_question_text);
        type2Question2Text = (TextView) rootView.findViewById(R.id.type2_question2_text);
        type2Question3Text = (TextView) rootView.findViewById(R.id.type2_question3_text);
        type2Question4Text = (TextView) rootView.findViewById(R.id.type2_question4_text);
        type2OkBtn = (TextView) rootView.findViewById(R.id.type2_ok_btn);

        type2InputEdit1 = (EditText) rootView.findViewById(R.id.type2_input_edit);
        type2InputEdit2 = (EditText) rootView.findViewById(R.id.type2_input_edit2);
    }
}