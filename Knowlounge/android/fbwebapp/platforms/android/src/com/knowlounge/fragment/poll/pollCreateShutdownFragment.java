package com.knowlounge.fragment.poll;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.PollCreateData;
import com.knowlounge.util.AndroidUtils;

public class pollCreateShutdownFragment extends Fragment {
    private final String TAG = "pollCreateType1Fragment";

    private final int TIME_30 = 1;
    private final int TIME_40 = 2;
    private final int TIME_50 = 3;
    private final int TIME_60 = 4;
    private final int TIME_DIRECT = 5;
    private final int TIME_UNLIMIT = 0;

    View rootView;

    WenotePreferenceManager prefManager;

    ImageView shutdownBackBtn;

    ImageView icoTime30, icoTime40, icoTime50, icoTime60, icoUnlimit, icoTimeDirect;
    LinearLayout optTime30, optTime40, optTime50, optTime60, optUnlimit, optTimeDirect;
    TextView txtTime30, txtTime40, txtTime50, txtTime60, txtUnlimit, txtTimeDirect;

    LinearLayout setShutdownLayout;
    TextView shutdownOkBtn;
    EditText shutdownDirectInputEdit;


    public static pollCreateShutdownFragment newInstance(PollCreateData pollCreate_data_set) {
        pollCreateShutdownFragment fragment = new pollCreateShutdownFragment();
        return fragment;
    }


    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d("pollCreateType1Fragment", "come in");
        prefManager = WenotePreferenceManager.getInstance(getActivity());

        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_shutdown_time, container, false);

        setFindViewById();
        setUI();

        optTime30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shudown_select(TIME_30);
            }
        });
        optTime40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shudown_select(TIME_40);
            }
        });
        optTime50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shudown_select(TIME_50);
            }
        });
        optTime60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { shudown_select(TIME_60); }
        });
        optTimeDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shudown_select(TIME_DIRECT);
            }
        });
        optUnlimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shudown_select(TIME_UNLIMIT);
            }
        });

        shutdownOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(RoomActivity.activity.pollData.getShutdownChecked() == TIME_DIRECT) {
                    if(shutdownDirectInputEdit.getText().length() !=0) {
                        if(AndroidUtils.checkRangeNumberInString(shutdownDirectInputEdit.getText().toString(),30,600))
                            RoomActivity.activity.pollData.setShutdownTime(Integer.parseInt(shutdownDirectInputEdit.getText().toString()));
                        else {
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_poll_timealert), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.poll_time_directguide), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                shutdownDirectInputEdit.requestFocus();
                shutdownDirectInputEdit.post(new Runnable() {
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(shutdownDirectInputEdit.getWindowToken(), 0);
                    }
                });
                getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }


    private void setUI() {
        txtTime30.setText(String.format(getResources().getString(R.string.poll_time_item),"30"));
        txtTime40.setText(String.format(getResources().getString(R.string.poll_time_item),"40"));
        txtTime50.setText(String.format(getResources().getString(R.string.poll_time_item),"50"));
        txtTime60.setText(String.format(getResources().getString(R.string.poll_time_item),"60"));
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        shutdownBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                RoomActivity.activity.pollData.clear_shutdown();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        setShutdownLayout.setVisibility(View.GONE);
        switch (RoomActivity.activity.pollData.getShutdownChecked()){
            case 1: shudown_select(TIME_30); break;
            case 2: shudown_select(TIME_40); break;
            case 3: shudown_select(TIME_50); break;
            case 4: shudown_select(TIME_60); break;
            case 5: shudown_select(TIME_DIRECT); break;
            case 0: shudown_select(TIME_UNLIMIT); break;
        }
    }


    public void shudown_select(int index) {
        switch (index) {
            case TIME_30:
                type_checked(icoTime30, icoTime40, icoTime50, icoTime60, icoTimeDirect, icoUnlimit);
                type_text_checked(txtTime30, txtTime40, txtTime50, txtTime60, txtTimeDirect, txtUnlimit);
                setShutdownLayout.setVisibility(View.GONE);
                RoomActivity.activity.pollData.setShutdownChecked(TIME_30);
                break;
            case TIME_40:
                type_checked(icoTime40, icoTime30, icoTime50, icoTime60, icoTimeDirect, icoUnlimit);
                type_text_checked(txtTime40, txtTime30, txtTime50, txtTime60, txtTimeDirect, txtUnlimit);
                setShutdownLayout.setVisibility(View.GONE);
                RoomActivity.activity.pollData.setShutdownChecked(TIME_40);
                break;
            case TIME_50 :
                type_checked(icoTime50, icoTime40, icoTime30, icoTime60, icoTimeDirect, icoUnlimit);
                type_text_checked(txtTime50, txtTime40, txtTime30, txtTime60, txtTimeDirect, txtUnlimit);
                setShutdownLayout.setVisibility(View.GONE);
                RoomActivity.activity.pollData.setShutdownChecked(TIME_50);
                break;
            case TIME_60 :
                type_checked(icoTime60, icoTime40, icoTime50, icoTime30, icoTimeDirect, icoUnlimit);
                type_text_checked(txtTime60, txtTime40, txtTime50, txtTime30, txtTimeDirect, txtUnlimit);
                setShutdownLayout.setVisibility(View.GONE);
                RoomActivity.activity.pollData.setShutdownChecked(TIME_60);
                break;
            case TIME_DIRECT :
                type_checked(icoTimeDirect, icoTime40, icoTime50, icoTime60, icoTime30, icoUnlimit);
                type_text_checked(txtTimeDirect, txtTime40, txtTime50, txtTime60, txtTime30, txtUnlimit);
                setShutdownLayout.setVisibility(View.VISIBLE);
                RoomActivity.activity.pollData.setShutdownChecked(TIME_DIRECT);
                int shutdownTime = RoomActivity.activity.pollData.getShutdowntime();
                Log.d(TAG, "shutdownTime : " + shutdownTime);
                shutdownDirectInputEdit.setText(Integer.toString(shutdownTime));
                break;
            case TIME_UNLIMIT :
                type_checked(icoUnlimit, icoTime40, icoTime50, icoTime60, icoTime30, icoTimeDirect);
                type_text_checked(txtUnlimit, txtTime30, txtTimeDirect, txtTime40, txtTime50, txtTime60);
                setShutdownLayout.setVisibility(View.GONE);
                RoomActivity.activity.pollData.setShutdownChecked(TIME_UNLIMIT);
                break;
        }
    }


    public void onDestroy() {
        super.onDestroy();
        Log.d("pollCreateType1Fragment", "onDestroy");
    }


    public void onPause() {
        super.onPause();
        Log.d("pollCreateType1Fragment", "TestAPI onPause");
    }


    public void type_checked(ImageView check, ImageView noncheck, ImageView noncheck2, ImageView noncheck3, ImageView noncheck4, @Nullable ImageView noncheck5) {
        check.setImageResource(R.drawable.btn_checkbox_on);
        noncheck.setImageResource(R.drawable.btn_checkbox);
        noncheck2.setImageResource(R.drawable.btn_checkbox);
        noncheck3.setImageResource(R.drawable.btn_checkbox);
        noncheck4.setImageResource(R.drawable.btn_checkbox);
        noncheck5.setImageResource(R.drawable.btn_checkbox);
    }


    public void type_text_checked(TextView check, TextView noncheck, TextView noncheck2, TextView noncheck3, TextView noncheck4, @Nullable TextView noncheck5) {
        check.setTypeface(null, Typeface.BOLD);
        noncheck.setTypeface(null, Typeface.NORMAL);
        noncheck2.setTypeface(null, Typeface.NORMAL);
        noncheck3.setTypeface(null, Typeface.NORMAL);
        noncheck4.setTypeface(null, Typeface.NORMAL);
        noncheck5.setTypeface(null, Typeface.NORMAL);
    }

    public void setFindViewById() {

        shutdownBackBtn = (ImageView) rootView.findViewById(R.id.shutdown_back_btn);
        icoTime30 = (ImageView) rootView.findViewById(R.id.type3_check1_img);
        icoTime40 = (ImageView) rootView.findViewById(R.id.type3_check2_img);
        icoTime50 = (ImageView) rootView.findViewById(R.id.type3_check3_img);
        icoTime60 = (ImageView) rootView.findViewById(R.id.type3_check4_img);
        icoTimeDirect = (ImageView) rootView.findViewById(R.id.type3_check5_img);
        icoUnlimit = (ImageView) rootView.findViewById(R.id.img_opt_time_unlimit);

        optTime30 = (LinearLayout) rootView.findViewById(R.id.type3_check1);
        optTime40 = (LinearLayout) rootView.findViewById(R.id.type3_check2);
        optTime50 = (LinearLayout) rootView.findViewById(R.id.type3_check3);
        optTime60 = (LinearLayout) rootView.findViewById(R.id.type3_check4);
        optTimeDirect = (LinearLayout) rootView.findViewById(R.id.type3_check5);
        optUnlimit = (LinearLayout) rootView.findViewById(R.id.layout_opt_time_unlimit);
        setShutdownLayout = (LinearLayout) rootView.findViewById(R.id.set_shutdown_layout);

        txtTime30 = (TextView) rootView.findViewById(R.id.type3_check1_text);
        txtTime40 = (TextView) rootView.findViewById(R.id.type3_check2_text);
        txtTime50 = (TextView) rootView.findViewById(R.id.type3_check3_text);
        txtTime60 = (TextView) rootView.findViewById(R.id.type3_check4_text);
        txtTimeDirect = (TextView) rootView.findViewById(R.id.type3_check5_text);
        txtUnlimit = (TextView) rootView.findViewById(R.id.txt_opt_time_unlimit);
        shutdownOkBtn = (TextView) rootView.findViewById(R.id.shutdown_ok_btn);

        shutdownDirectInputEdit = (EditText) rootView.findViewById(R.id.shutdown_direct_input_edit);
    }
}