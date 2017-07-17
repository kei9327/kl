package com.knowlounge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.poll.DrawingPollFragment;
import com.knowlounge.fragment.poll.PollAnswerFragment;
import com.knowlounge.fragment.poll.PollTimerFragment;
import com.knowlounge.fragment.poll.QuestionResultFragment;
import com.knowlounge.fragment.poll.pollCreateFragment;
import com.knowlounge.fragment.poll.pollListFragment;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.view.room.RoomActivity;

/**
 * Created by Minsu on 2016-02-12.
 */
public class PollPopupDialog extends AppCompatActivity implements pollCreateFragment.PollSaveBtnOnClickListener {

    private final String TAG = "PollPopupDialog";
    WenotePreferenceManager prefManager;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private int type;
    private int action;
    Intent intent;

    public static PollPopupDialog _instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _instance = this;

        Log.d(TAG, "onCreate");
        prefManager = WenotePreferenceManager.getInstance(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.poll_popup_dialog);

        setFinishOnTouchOutside(false);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        intent = getIntent();
        type = intent.getIntExtra("type", 0);

        Bundle param = intent.hasExtra("param") ? intent.getBundleExtra("param") : null;


        if (param == null) {
            Bundle argument = new Bundle();
            switch (type) {
                case GlobalConst.VIEW_CREATE_FRAGMENT:
                    Log.d(TAG, "질문 만들기 화면을 띄웁니다.");
                    pollCreateFragment.isFirst = true;
                    pollCreateFragment.course = GlobalConst.VIEW_CREATE_FRAGMENT;
                    pollCreateFragment createFragment = new pollCreateFragment();
                    fragmentTransaction.replace(R.id.dialog_main_container, createFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    break;
                case GlobalConst.VIEW_POLLLIST_FRAGMENT:
                    Log.d(TAG, "저장한 질문(폴 템플릿) 목록 화면을 띄웁니다.");
                    argument.putInt("type", GlobalConst.VIEW_POLLLIST_FRAGMENT);
                    pollListFragment listFragment = new pollListFragment();
                    listFragment.setArguments(argument);
                    fragmentTransaction.replace(R.id.dialog_main_container, listFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    break;
                case GlobalConst.VIEW_COMPLETE_POLLLIST_FRAGMENT :
                    Log.d(TAG, "완료된 질문 목록 화면을 띄웁니다.");
                    argument.putInt("type", GlobalConst.VIEW_COMPLETE_POLLLIST_FRAGMENT);
                    pollListFragment completListFragment = new pollListFragment();
                    completListFragment.setArguments(argument);
                    fragmentTransaction.replace(R.id.dialog_main_container, completListFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    break;
                default:
            }
        } else {
            String targetUI = param.getString("mode");
            if (TextUtils.equals(targetUI, "question")) {
                Log.d(TAG, "영역 캡쳐를 완료하고 다시 질문 화면으로 복귀합니다.");
                DrawingPollFragment fragment = new DrawingPollFragment();
                fragment.setArguments(param);
                fragmentTransaction.replace(R.id.dialog_main_container, fragment, "DrawingPollFragment");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } else {
                Log.d(TAG, "영역 캡쳐를 완료하고 다시 답변 화면으로 복귀합니다.");
                Bundle bundle = new Bundle();
                bundle.putString("obj", intent.getStringExtra("obj"));
                PollAnswerFragment answerFragment = new PollAnswerFragment();
                answerFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.dialog_main_container, answerFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    @Override
    public void onClicked(int type ) {
        Log.d(TAG, "interface_Clicked");

        switch (type) {
            case GlobalConst.ACTION_POLL_SAVE :
                Bundle argument = new Bundle();
                argument.putInt("type", GlobalConst.VIEW_POLLLIST_FRAGMENT);
                pollListFragment listFragment = new pollListFragment();
                listFragment.setArguments(argument);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.dialog_main_container, listFragment)
                        .commit();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.type = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        try {
            action = intent.getIntExtra("plugin_type", 0);
            if(action != 0) {
                if (action == GlobalConst.ACTION_MAKE_POLL_SHEET) {   // 질문을 받았을 때 답변 화면을 띄워줌..
                    Log.d(TAG, "질문을 받았습니다. 답변할 수 있는 화면을 출력합니다.");
                    Bundle bundle = new Bundle();
                    bundle.putString("obj", intent.getStringExtra("obj"));

//                    if (getSupportFragmentManager().findFragmentByTag("ANSWER_FRAGMENT") == null) {
                        PollAnswerFragment answerFragment = new PollAnswerFragment();
                        answerFragment.setArguments(bundle);
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.dialog_main_container, answerFragment, "ANSWER_FRAGMENT");
                        fragmentTransaction.addToBackStack(null);  // Replace 되는 프레그먼트를 백스택에 저장함..
                        fragmentTransaction.commit();
//                    } else {
//                        PollAnswerFragment answerFragment = (PollAnswerFragment) getSupportFragmentManager().findFragmentByTag("ANSWER_FRAGMENT");
//                        fragmentTransaction.show(answerFragment).commit();
//                    }
                } else if (action == GlobalConst.ACTION_SHOW_TIMER_PANEL) {
                    Log.d(TAG, "질문을 보냈습니다. 질문 대기 화면을 출력합니다.");
                    Bundle bundle = new Bundle();
                    bundle.putString("pollno", intent.getStringExtra("pollno"));
                    bundle.putString("target", intent.getStringExtra("target"));
                    PollTimerFragment answerTimerFragment = new PollTimerFragment();
                    answerTimerFragment.setArguments(bundle);
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.dialog_main_container, answerTimerFragment, "timer");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else if(action == GlobalConst.ACTION_SHOW_POLL_RESULT) {   // 질문 결과 그래프 화면을 띄워줌..
                    Log.d(TAG, "질문 결과 내용을 받았습니다. 결과 그래프 화면을 출력합니다.");
                    Bundle bundle = new Bundle();
                    bundle.putString("obj", intent.getStringExtra("obj"));
                    bundle.putInt("through", GlobalConst.ACTION_SHOW_POLL_RESULT);
                    if (getSupportFragmentManager().findFragmentByTag("QUESTION_RESULT") != null &&
                            (getSupportFragmentManager().findFragmentByTag("QUESTION_RESULT")).isVisible()) {
                        Log.d(TAG, "기존 Fragment를 닫고 새로 엽니다.");
                        getSupportFragmentManager().popBackStack();
                    }
                    QuestionResultFragment pollCompletRsultFragment = new QuestionResultFragment();
                    pollCompletRsultFragment.setArguments(bundle);
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.dialog_main_container, pollCompletRsultFragment, "QUESTION_RESULT");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        }catch(Exception e){
            Log.d(TAG,e.getMessage());
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "가로상태");
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d(TAG, "세로상태");
        }
    }

    @Override
    public void onBackPressed(){
        Log.d(TAG, "onBackPressed");
        pollCreateFragment.isFirst = true;
//        if (action == GlobalConst.ACTION_MAKE_POLL_SHEET) {  // 질문 답변창이 띄워진 상태일 경우..
//            Log.d(TAG, "폴 답변 진행중.. 답변 창을 잠시 hide 합니다...");
//            PollAnswerFragment answerFragment = (PollAnswerFragment) getSupportFragmentManager().findFragmentByTag("ANSWER_FRAGMENT");
//            getSupportFragmentManager().beginTransaction().hide(answerFragment).commit();
//        } else {
//            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
//                getSupportFragmentManager().popBackStack();
//            } else {
//                if (action != GlobalConst.ACTION_SHOW_TIMER_PANEL)
//                    finish();
//            }
//        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else if (getSupportFragmentManager().findFragmentByTag("DrawingPollFragment") != null) {
            getSupportFragmentManager().popBackStack();
            pollCreateFragment createFragment = new pollCreateFragment();
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.dialog_main_container, createFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            RoomActivity.activity.pollData.clear();
            if (action != GlobalConst.ACTION_SHOW_TIMER_PANEL)
                finish();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);

        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                AndroidUtils.keyboardHide(PollPopupDialog.this);
            } else {

            }
        }
        return super.dispatchTouchEvent(ev);
    }

    ProgressDialog mProgressDialog;
    public void showProgressDialog(String message) {
        mProgressDialog = ProgressDialog.show(this, "", message, true);
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}
