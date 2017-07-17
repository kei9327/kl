package com.knowlounge.fragment.poll;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.model.RoomUser;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.customview.CircleAnimIndicator;
import com.knowlounge.customview.LinearProgressView;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.ClassUser;
import com.knowlounge.model.PollAnsweredUser;
import com.knowlounge.model.PollCreateData;
import com.knowlounge.plugins.PollPlugin;

import org.apache.cordova.CordovaWebView;

import java.util.ArrayList;
import java.util.List;

public class PollTimerFragment extends Fragment implements PollPlugin.EventListener {
    private final String TAG = "PollTimerFragment";
    public static int answerTime;
    public static String pollTitleName;

    private WenotePreferenceManager prefManager;
    private Thread timeThread;
    private boolean threadRunning;
    private String pollno;
    private String target;

    private View rootView;

    private TextView pollAnswerTimerTitle, mPollFinishBtn;
    private TextView txtAnswerCnt, txtTotalCnt;
    private int checkOneSecond=0;

    //private RoundProgressBarView timeProgressBar;
    private LinearProgressView linearProgressView;

    Context context;
    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private CircleAnimIndicator mIndicator;

    private ArrayList<PollAnsweredUser> userList;

    private int answerCnt = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        prefManager = WenotePreferenceManager.getInstance(context);

        PollPlugin.setOnEventListener(this);

    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_timer, container, false);
        setFindViewById();

        pollAnswerTimerTitle.setText(pollTitleName);

        if (answerTime == 0)
            linearProgressView.setIsUnlimit(true);
        else
            linearProgressView.setIsUnlimit(false);
        linearProgressView.setMax(answerTime * 100+100);

        //timeProgressBar.setMax(answerTime*100);

        pollno = getArguments().getString("pollno");
        target = getArguments().getString("target");

        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_user_list);
        mIndicator = (CircleAnimIndicator) rootView.findViewById(R.id.circleAnimIndicator);

        txtAnswerCnt = (TextView) rootView.findViewById(R.id.txt_answer_cnt);
        txtTotalCnt = (TextView) rootView.findViewById(R.id.txt_total_cnt);

        ArrayList<AnsweredUserFragment> fragments = new ArrayList<AnsweredUserFragment>();
        userList = new ArrayList<PollAnsweredUser>();
        ArrayList<PollAnsweredUser> list = new ArrayList<PollAnsweredUser>();

        int cnt = 0;
        for (RoomUser user : RoomActivity.classUserList) {
            String userNo = user.getUserNo();
            String userId = user.getUserId();
            String userNm = user.getUserNm();
            String userThumb = user.getThumbnail();

            if(TextUtils.equals(target, PollCreateData.TARGET_USER_TEACHER)) {  // target이 "teacher"인 판서폴
                if (TextUtils.equals(userNo, RoomActivity.activity.getClassMasterUserNo())) {
                    PollAnsweredUser answerUser = new PollAnsweredUser(userNo, userId, userNm, userThumb, false);
                    userList.add(answerUser);
                    list.add(answerUser);
                    fragments.add(new AnsweredUserFragment(new ArrayList<PollAnsweredUser>(list), context));
                    list.clear();
                    break;
                }
            } else {   // target이 "all"인 판서폴
                if (userNo.equals(RoomActivity.activity.getUserNo()))
                    continue;

                cnt++;
                PollAnsweredUser answerUser = new PollAnsweredUser(userNo, userId, userNm, userThumb, false);
                userList.add(answerUser);
                list.add(answerUser);
                if (cnt % 12 == 0 || cnt == (RoomActivity.classUserList.size() - 1)) {
                    fragments.add(new AnsweredUserFragment(new ArrayList<PollAnsweredUser>(list), context));
                    list.clear();
                }
            }
        }

        // Pager Indicator 사이의 간격
        mIndicator.setItemMargin((int) (14 * KnowloungeApplication.density));
        mIndicator.setAnimDuration(300);   //애니메이션 속도
        mIndicator.createDotPanel(fragments.size(), R.drawable.btn_poll_userlist_indicater, R.drawable.btn_poll_userlist_indicater_on);   //indecator 생성

        mAdapter = new PagerAdapter(getFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        txtAnswerCnt.setText(Integer.toString(answerCnt));
        txtTotalCnt.setText(Integer.toString(userList.size()));

        return rootView;
    }


    public void updateAnswerUserList(String userNo) {
        int cnt = 0;
        ArrayList<PollAnsweredUser> list = new ArrayList<>();
        for (PollAnsweredUser user : userList) {
            cnt++;
            if (TextUtils.equals(user.getUserNo(), userNo)) {
                user.setAnswered(true);
                answerCnt++;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtAnswerCnt.setText(Integer.toString(answerCnt));
                    }
                });
            }
            list.add(user);
            int pagerIdx = 0;
            if (cnt == 12) {
                ((AnsweredUserFragment) mAdapter.getItem(pagerIdx)).update(new ArrayList<PollAnsweredUser>(list));
                list.clear();
            } else if (cnt == userList.size() && userList.size() < 12) {
                ((AnsweredUserFragment) mAdapter.getItem(pagerIdx)).update(new ArrayList<PollAnsweredUser>(list));
                list.clear();
            } else if (cnt > 12 && cnt == userList.size()) {
                pagerIdx++;
                ((AnsweredUserFragment) mAdapter.getItem(pagerIdx)).update(new ArrayList<PollAnsweredUser>(list));
                list.clear();
            } else if (cnt > 12) {
                pagerIdx++;
                ((AnsweredUserFragment) mAdapter.getItem(pagerIdx)).update(new ArrayList<PollAnsweredUser>(list));
                list.clear();
            }
        }
    }


    /**
     * ViewPager 전환시 호출되는 메서드
     */
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            mIndicator.selectDot(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                    alert_confirm.setMessage(getResources().getString(R.string.global_popup_stoppoll)).setCancelable(false).setPositiveButton(getResources().getString(R.string.global_ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    threadRunning = false;
                                    CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
                                    webView.sendJavascript("PollCtrl.Action.Master.stopPoll('" + pollno + "')");
                                    RoomActivity.activity.setIsPollProgress(false); // 진행자 쪽 폴 진행 상태값 업데이트
                                    RoomActivity.activity.setAnswerPollBtn();
                                    getActivity().finish();
                                }
                            }).setNegativeButton(getResources().getString(R.string.global_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    return;
                                }
                            });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
                return false;
            }
        });

        mPollFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                alert_confirm.setMessage(getResources().getString(R.string.global_popup_stoppoll)).setCancelable(false).setPositiveButton(getResources().getString(R.string.global_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                threadRunning = false;

                                if(timeThread != null && timeThread.isAlive()) {
                                    timeThread.interrupt();
                                }

                                CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
                                webView.sendJavascript("PollCtrl.Action.Master.stopPoll('" + pollno + "')");
                                RoomActivity.activity.setIsPollProgress(false); // 진행자 쪽 폴 진행 상태값 업데이트
                                RoomActivity.activity.setAnswerPollBtn();
                                getActivity().finish();
                            }
                        }).setNegativeButton(getResources().getString(R.string.global_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
        Log.d(TAG, pollno);
        Log.d(TAG, Integer.toString(answerTime));

        if (answerTime < 1)
            threadRunning = false;
        else
            threadRunning = true;

        timeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while((answerTime*100) > checkOneSecond && threadRunning) {
                        Log.d(TAG, answerTime + " / " + checkOneSecond);
                        if(checkOneSecond++ > 0) {
                            Message msg = shutdownTimeHandler.obtainMessage();
                            shutdownTimeHandler.sendMessage(msg);
                        }
                        Thread.sleep(10);
                    }
                    threadRunning = false;
                    getActivity().finish();
                    RoomActivity.activity.setIsPollProgress(false); // 진행자 쪽 폴 진행 상태값 업데이트
                    RoomActivity.activity.setAnswerPollBtn();
                    RoomActivity.activity.mWebViewFragment.getCordovaWebView().sendJavascript("PollCtrl.Action.Master.stopPoll('" + pollno + "')");
                } catch (Throwable t) {

                }
            }
        });
        if(threadRunning)
            timeThread.start();


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


    @Override
    public void onExitPoll() {

    }

    @Override
    public void onUpdateAnswerUser(String userNo) {
        updateAnswerUserList(userNo);
    }

    private  Handler shutdownTimeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            updateTime();
        }
    };

    private void updateTime() {
        //timeProgressBar.setProgress(checkOneSecond);
        linearProgressView.setProgress(checkOneSecond);
    }

    public void setFindViewById() {
        pollAnswerTimerTitle = (TextView) rootView.findViewById(R.id.poll_answer_timer_title);
        mPollFinishBtn = (TextView) rootView.findViewById(R.id.poll_finish_btn);

        //timeProgressBar = (RoundProgressBarView) rootView.findViewById(R.id.time_progress_bar);
        linearProgressView = (LinearProgressView) rootView.findViewById(R.id.linear_progress_view);
    }


    private class PagerAdapter extends FragmentStatePagerAdapter {
        private List<AnsweredUserFragment> fragments;

        public PagerAdapter(FragmentManager fm, List<AnsweredUserFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }
}