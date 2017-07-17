package com.knowlounge.fragment.poll;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.adapter.PollResult_Type3UserListAdapter;
import com.knowlounge.adapter.poll.DrawingAnswerListAdapter;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.AnswerQuestionAndAnswerResultData;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.model.PollCreateData;
import com.knowlounge.plugins.PollPlugin;
import com.knowlounge.util.AndroidUtils;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuestionResultFragment extends Fragment implements PollPlugin.EventListener
{
    private final String TAG = "QuestionResultFragment";
    private final int TABLET_GAGE = 180;
    private final int PHONE_LANDSCAPE = 400;
    private final int PHONE_PORTRATE = 220;

    private WenotePreferenceManager prefManager;

    private View rootView;
    private View AnswerResultItem;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private TextView pollResultTitle, pollResultSendBtn, pollResultFinishBtn;
    private LinearLayout resultItemLayout, noAnswerView;
    private LayoutInflater inflater;
    private String pollno;
    private ArrayList<PollResultDetail> detailArr;
    private PollResult_Type3UserListAdapter adapter;

    private DrawingAnswerListAdapter drawingAnswerAdapter;

    private ListView pollResultType3;
    private ListView drawingResultView;

    private JSONObject obj;
    private int maxGage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            obj = new JSONObject(getArguments().getString("obj"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        this.inflater = inflater;
        prefManager = WenotePreferenceManager.getInstance(getActivity());
        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_result, container, false);
        setFindViewById();
        PollPlugin.setOnEventListener(this);
        dataSetting();

        return rootView;
    }

    private void dataSetting() {
        try {
            Log.d(TAG, "dataSetting / " + obj.toString());
            String title = obj.getString("title");
            String pollType = obj.getString("polltype");
            int answerusercnt = Integer.parseInt(obj.getString("answerusercnt"));
            int answerTotalCnt = Integer.parseInt(obj.getString("answertotalcnt"));
            String allowcnt = obj.getString("allowcnt");
            final String pollno = obj.getString("pollno");

            String presentor = obj.getString("presentor");

            int max_answer = answerTotalCnt;

            JSONArray answerItemList = obj.getJSONArray("itemlist");

            initResultLayout();
            pollResultTitle.setText(title);
            this.pollno = pollno;


            if(!TextUtils.equals(presentor, RoomActivity.activity.getUserNo())) {
                pollResultSendBtn.setVisibility(View.GONE);
            } else {
                pollResultSendBtn.setVisibility(View.VISIBLE);
            }

            pollResultFinishBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Close button click..");
                    getActivity().finish();
                }
            });

            pollResultSendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 결과 전송 plugin 주소
                    // 예외 처리
                    final CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();

                    webView.sendJavascript("PollCtrl.Action.Master.sendPollReport('" + pollno + "');");

                }
            });

            switch (Integer.parseInt(pollType)) {
                case PollCreateData.POLL_TYPE_MULTIPLE_CHOICE:
                case PollCreateData.POLL_TYPE_ALTER_CHOICE:
                    if(answerItemList.length() == 0) {
                        drawingResultView.setVisibility(View.GONE);
                        pollResultType3.setVisibility(View.GONE);
                        resultItemLayout.setVisibility(View.GONE);
                        noAnswerView.setVisibility(View.VISIBLE);
                    } else {
                        drawingResultView.setVisibility(View.GONE);
                        pollResultType3.setVisibility(View.GONE);
                        resultItemLayout.setVisibility(View.VISIBLE);
                        noAnswerView.setVisibility(View.GONE);
                    }

                    for(int i=0; i<answerItemList.length(); i++) {
                        JSONObject answerItem = answerItemList.getJSONObject(i);

                        String pollItemNo = answerItem.getString("pollitemno");
                        String pollNo = answerItem.getString("pollno");
                        int answerCnt = answerItem.getInt("answercnt");
                        //int totalCnt = answerItem.getInt("answertotalcnt");

                        final String itemIdx = answerItem.getString("itemidx");
                        final String itemNm = answerItem.getString("itemnm");
                        final JSONArray answerUserList = answerItem.getJSONArray("answeruser");


                        AnswerResultItem = inflater.inflate(R.layout.poll_result_row, null);
                        final LinearLayout item_click_btn = (LinearLayout)AnswerResultItem.findViewById(R.id.item_click_btn);
                        final TextView itemGraph = (TextView)AnswerResultItem.findViewById(R.id.item_grape);
                        final TextView item_percent = (TextView) AnswerResultItem.findViewById(R.id.item_percent);
                        final TextView result_people_count = (TextView) AnswerResultItem.findViewById(R.id.result_people_count);

                        int percent;

                        try {
                            if(answerCnt == 0)
                                percent = 0;
                            else
                                percent = (int) ((answerCnt * 100 / answerusercnt));
                        } catch(Exception e) {
                            e.printStackTrace();
                            percent = 0;
                        }
                        item_click_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 결과 상세 페이지 넘기기
                                TextView tv = (TextView) item_click_btn.findViewById(R.id.item_grape);
                                Log.d("item_click_btn", tv.getText().toString());
                                float position = Float.parseFloat(tv.getText().toString())-1;
                                Log.d("item_click_btn_int", Float.toString(position));

                                Bundle argument = new Bundle();
                                argument.putString("itemidx", itemIdx);
                                argument.putString("itemnm", itemNm);
                                argument.putString("answeruser", answerUserList.toString());

                                PollCompletRsultDetailFragment pollCompletResult = new PollCompletRsultDetailFragment();
                                pollCompletResult.setArguments(argument);
                                mFragmentTransaction.replace(R.id.dialog_main_container, pollCompletResult);
                                mFragmentTransaction.addToBackStack(null);
                                mFragmentTransaction.commit();

                            }
                        });

                        itemGraph.setLayoutParams(getParams(itemGraph, answerCnt, answerTotalCnt));
                        itemGraph.setText("   " + Integer.toString(i+1));
                        item_percent.setText(Integer.toString(percent) + "%");
                        result_people_count.setText(Integer.toString(answerCnt));
                        switch (i) {
                            case 0 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result1);break;
                            case 1 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result2);break;
                            case 2 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result3);break;
                            case 3 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result4);break;
                            case 4 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result5);break;
                            case 5 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result6);break;
                            case 6 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result7);break;
                            case 7 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result8);break;
                            case 8 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result9);break;
                            case 9 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result10);break;
                        }
                        resultItemLayout.addView(AnswerResultItem);
                    }

                    break;
                case PollCreateData.POLL_TYPE_SHORT_ANSWER :
                    try {
                        if(answerItemList.length() == 0) {
                            drawingResultView.setVisibility(View.GONE);
                            pollResultType3.setVisibility(View.GONE);
                            resultItemLayout.setVisibility(View.GONE);
                            noAnswerView.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            drawingResultView.setVisibility(View.GONE);
                            pollResultType3.setVisibility(View.VISIBLE);
                            resultItemLayout.setVisibility(View.GONE);
                            noAnswerView.setVisibility(View.GONE);
                        }
                        ArrayList<PollResultType3User> type3_result_answer = new ArrayList<PollResultType3User>();

                        for(int i=0; i< answerItemList.length(); i++){

                            JSONObject answerItem = answerItemList.getJSONObject(i);
                            String itemNm = answerItem.has("itemnm") ? answerItem.getString("itemnm") : "";
                            JSONObject answerUserInfo = answerItem.getJSONArray("answeruser").getJSONObject(0);

                            String userThumb = answerUserInfo.has("thumbnail") ? answerUserInfo.getString("thumbnail") : "";
                            String userNm = answerUserInfo.has("usernm") ? answerUserInfo.getString("usernm") : "";

                            type3_result_answer.add(new PollResultType3User(userThumb, userNm, itemNm));
                        }

                        adapter = new PollResult_Type3UserListAdapter(getActivity().getBaseContext(), type3_result_answer);
                        pollResultType3.setAdapter(adapter);

                    } catch (JSONException j) {
                        j.printStackTrace();
                    }
                    break;
                case PollCreateData.POLL_TYPE_DRAWING :
                    try {
                        if(answerItemList.length() == 0) {   // 판서 답변 결과가 없으면 별도로 UI 처리..
                            drawingResultView.setVisibility(View.GONE);
                            pollResultType3.setVisibility(View.GONE);
                            resultItemLayout.setVisibility(View.GONE);
                            noAnswerView.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            drawingResultView.setVisibility(View.VISIBLE);
                            pollResultType3.setVisibility(View.GONE);
                            resultItemLayout.setVisibility(View.GONE);
                            noAnswerView.setVisibility(View.GONE);
                        }

                        String svrFlag = getResources().getString(R.string.svr_flag);
                        String svrHost = getResources().getString(getResources().getIdentifier("svr_host_" + svrFlag, "string", getActivity().getPackageName()));

                        ArrayList<QuestionAnswerUser> drawAnswerList = new ArrayList<>();
                        for (int i=0; i< answerItemList.length(); i++) {
                            JSONObject answerItem = answerItemList.getJSONObject(i);

                            String filePath = answerItem.getString("filepath");
                            String pollFileNo = answerItem.getString("pollfileno");

//                            String pollUserNo = answerItem.has("polluserno") ? answerItem.getString("polluserno") : "";
//                            String userThumb = answerItem.has("thumbnail") ? answerItem.getString("thumbnail") : "";
//                            String userNm = answerItem.has("usernm") ? answerItem.getString("usernm") : "";

                            JSONObject answerUserInfo = answerItem.getJSONArray("answeruser").getJSONObject(0);

//                            String pollUserNo = answerUserInfo.has("polluserno") ? answerUserInfo.getString("polluserno") : "";
//                            String userThumb = answerUserInfo.has("thumbnail") ? answerUserInfo.getString("thumbnail") : "";
//                            String userNm = answerUserInfo.has("usernm") ? answerUserInfo.getString("usernm") : "";

                            String pollUserNo = answerUserInfo.has("polluserno") ? answerUserInfo.getString("polluserno") : "";
                            String userThumb = answerUserInfo.has("thumbnail") ? answerUserInfo.getString("thumbnail") : "";
                            String userNm = answerUserInfo.has("usernm") ? answerUserInfo.getString("usernm") : "";

                            QuestionAnswerUser answerResult = new QuestionAnswerUser(pollno, pollUserNo, userNm, userThumb, filePath);
                            answerResult.setPollFileNo(pollFileNo);

                            drawAnswerList.add(answerResult);

                            Log.d(TAG, "usernm : " + userNm);
                        }

                        drawingAnswerAdapter = new DrawingAnswerListAdapter(getActivity().getBaseContext(), drawAnswerList);
                        drawingResultView.setAdapter(drawingAnswerAdapter);

                    } catch (JSONException j) {
                        j.printStackTrace();
                    }
                    break;
            }







//            ArrayList<AnswerQuestionAndAnswerResultData> answerList = new ArrayList<AnswerQuestionAndAnswerResultData>();
//
//            for (int i = 0; i < answerItemList.length(); i++) {
//                JSONObject answerItem = answerItemList.getJSONObject(i);
//                String pollitemno = answerItem.getString("pollitemno");
//                String itemidx = answerItem.getString("itemidx");
//                String itemnm = answerItem.has("itemnm")? answerItem.getString("itemnm") : "";
//                JSONArray answerUser = answerItem.getJSONArray("answeruser");
//
//                int answerCnt = Integer.parseInt(answerItem.getString("answercnt"));
//                if (max_answer < answerCnt) {
//                    max_answer = answerCnt;
//                }
//                answerList.add(new AnswerQuestionAndAnswerResultData(pollitemno, answerCnt));
//                detailArr.add(new PollResultDetail(itemidx, itemnm, answerUser));
//            }
//
//            showAnswerResult(title, polltype, answertotalcnt, answerusercnt, allowcnt, pollno, answerList, max_answer);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("pollResultDeploy", e.getMessage());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


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

    private void showAnswerResult(String title, String polltype, int answertotalcnt, int answerusercnt, String allowcnt,
                                  String pollno, ArrayList<AnswerQuestionAndAnswerResultData> answerList, int maxcount ) {
        Log.d(TAG, "showAnswerResult / title : " + title);
        Log.d(TAG, "showAnswerResult / polltype : " + polltype);
        initResultLayout();
        pollResultTitle.setText(title);
        this.pollno = pollno;


        switch (Integer.parseInt(polltype)) {
            case PollCreateData.POLL_TYPE_MULTIPLE_CHOICE:
            case PollCreateData.POLL_TYPE_ALTER_CHOICE:
                if(answerList.size() == 0) {
                    drawingResultView.setVisibility(View.GONE);
                    pollResultType3.setVisibility(View.GONE);
                    resultItemLayout.setVisibility(View.GONE);
                    noAnswerView.setVisibility(View.VISIBLE);
                } else {
                    drawingResultView.setVisibility(View.GONE);
                    pollResultType3.setVisibility(View.GONE);
                    resultItemLayout.setVisibility(View.VISIBLE);
                    noAnswerView.setVisibility(View.GONE);
                }

                for(int i=0; i<answerList.size(); i++) {
                    AnswerResultItem = inflater.inflate(R.layout.poll_result_row, null);
                    final LinearLayout item_click_btn = (LinearLayout)AnswerResultItem.findViewById(R.id.item_click_btn);
                    final TextView itemGraph = (TextView)AnswerResultItem.findViewById(R.id.item_grape);
                    final TextView item_percent = (TextView) AnswerResultItem.findViewById(R.id.item_percent);
                    final TextView result_people_count = (TextView) AnswerResultItem.findViewById(R.id.result_people_count);
                    int answerCnt = answerList.get(i).getAnswercnt();
                    int percent;


                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 결과 상세 페이지 넘기기
                            TextView tv = (TextView) item_click_btn.findViewById(R.id.item_grape);
                            Log.d("item_click_btn", tv.getText().toString());
                            float position = Float.parseFloat(tv.getText().toString())-1;
                            Log.d("item_click_btn_int", Float.toString(position));

                            String itemidx = detailArr.get((int)position).getNum();
                            String itemnm = detailArr.get((int)position).getItemnm();
                            String answeruser = detailArr.get((int)position).getDetailArr().toString();

                            Bundle argument = new Bundle();
                            argument.putString("itemidx", itemidx);
                            argument.putString("itemnm", itemnm);
                            argument.putString("answeruser", answeruser);

                            PollCompletRsultDetailFragment pollCompletResult = new PollCompletRsultDetailFragment();
                            pollCompletResult.setArguments(argument);
                            mFragmentTransaction.replace(R.id.dialog_main_container, pollCompletResult);
                            mFragmentTransaction.addToBackStack(null);
                            mFragmentTransaction.commit();

                        }
                    };

                    try {
                        if (answerCnt == 0) {
                            percent = 0;
                            item_click_btn.setClickable(false);
                        } else {
                            percent = (int) ((answerCnt * 100 / answerusercnt));
                            item_click_btn.setClickable(true);
                            item_click_btn.setOnClickListener(listener);
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                        percent = 0;
                    }



                    itemGraph.setLayoutParams(getParams(itemGraph, answerList.get(i).getAnswercnt(), maxcount));
                    itemGraph.setText("   " + Integer.toString(i+1));
                    item_percent.setText(Integer.toString(percent) + "%");
                    result_people_count.setText(answerList.get(i).getAnswercnt()+"");
                    switch (i) {
                        case 0 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result1);break;
                        case 1 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result2);break;
                        case 2 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result3);break;
                        case 3 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result4);break;
                        case 4 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result5);break;
                        case 5 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result6);break;
                        case 6 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result7);break;
                        case 7 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result8);break;
                        case 8 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result9);break;
                        case 9 : itemGraph.setBackgroundResource(R.drawable.bg_rounded_corner_result10);break;
                    }
                    resultItemLayout.addView(AnswerResultItem);
                }

                break;
            case PollCreateData.POLL_TYPE_SHORT_ANSWER :
                try {
                    if(answerList.size() == 0) {
                        drawingResultView.setVisibility(View.GONE);
                        pollResultType3.setVisibility(View.GONE);
                        resultItemLayout.setVisibility(View.GONE);
                        noAnswerView.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        drawingResultView.setVisibility(View.GONE);
                        pollResultType3.setVisibility(View.VISIBLE);
                        resultItemLayout.setVisibility(View.GONE);
                        noAnswerView.setVisibility(View.GONE);
                    }
                    ArrayList<PollResultType3User> type3_result_answer = new ArrayList<PollResultType3User>();
                    for(int i=0; i< answerList.size() ; i++){
                        String thumbnail = detailArr.get(i).getDetailArr().getJSONObject(0).has("thumbnail") ?detailArr.get(i).getDetailArr().getJSONObject(0).getString("thumbnail") : "";
                        String usernm =  detailArr.get(i).getDetailArr().getJSONObject(0).has("usernm") ?detailArr.get(i).getDetailArr().getJSONObject(0).getString("usernm") : "";
                        String user_item_nm = detailArr.get(i).getItemnm();
                        type3_result_answer.add(new PollResultType3User(thumbnail, usernm, user_item_nm));
                    }

                    adapter = new PollResult_Type3UserListAdapter(getActivity().getBaseContext(), type3_result_answer);
                    pollResultType3.setAdapter(adapter);

                } catch (JSONException j) {
                    j.printStackTrace();
                }
                break;
            case PollCreateData.POLL_TYPE_DRAWING :
                try {
                    if(answerList.size() == 0) {
                        drawingResultView.setVisibility(View.GONE);
                        pollResultType3.setVisibility(View.GONE);
                        resultItemLayout.setVisibility(View.GONE);
                        noAnswerView.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        drawingResultView.setVisibility(View.VISIBLE);
                        pollResultType3.setVisibility(View.GONE);
                        resultItemLayout.setVisibility(View.GONE);
                        noAnswerView.setVisibility(View.GONE);
                    }
                    ArrayList<QuestionAnswerUser> drawAnswerList = new ArrayList<QuestionAnswerUser>();
                    for(int i=0; i< answerList.size() ; i++){

                        JSONObject userJson = detailArr.get(i).getDetailArr().getJSONObject(0);
                        String pollNo = userJson.has("pollno") ? userJson.getString("polluserno") : "";
                        String pollUserNo = userJson.has("polluserno") ? userJson.getString("polluserno") : "";
                        String thumbnail = userJson.has("thumbnail") ? userJson.getString("thumbnail") : "";
                        String usernm =  userJson.has("usernm") ? userJson.getString("usernm") : "";
                        drawAnswerList.add(new QuestionAnswerUser(pollNo, pollUserNo, usernm, thumbnail, ""));
                        Log.d(TAG, "usernm : " + usernm);
                    }

                    drawingAnswerAdapter = new DrawingAnswerListAdapter(getActivity().getBaseContext(), drawAnswerList);
                    drawingResultView.setAdapter(drawingAnswerAdapter);

                } catch (JSONException j) {
                    j.printStackTrace();
                }
                break;
            default :
        }
    }

    private void initResultLayout() {
        if(resultItemLayout.isShown())
            resultItemLayout.removeAllViews();
    }


    private LinearLayout.LayoutParams getParams(View v, int answercnt, int totalCnt) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
        //params.gravity = Gravity.LEFT;
        try {
            ((TextView)v).setGravity(Gravity.CENTER_VERTICAL);
            if (answercnt == 0) {
                params.width = AndroidUtils.getPxFromDp(getActivity(), 30);
                //((TextView)v).setGravity(Gravity.CENTER);
            } else {
                params.width = AndroidUtils.getPxFromDp(getActivity(), 30) + AndroidUtils.getPxFromDp(getActivity(), maxGage) * answercnt / totalCnt;

            }
        } catch (Exception e) {
            e.printStackTrace();
            params.width = AndroidUtils.getPxFromDp(getActivity(), 30);
        }
        return params;
    }


    public void setFindViewById() {
        pollResultTitle = (TextView) rootView.findViewById(R.id.poll_result_title);
        pollResultSendBtn = (TextView) rootView.findViewById(R.id.poll_result_send_btn);
        pollResultFinishBtn = (TextView) rootView.findViewById(R.id.poll_result_finish_btn);
        pollResultType3 = (ListView) rootView.findViewById(R.id.poll_result_type3);

        drawingResultView = (ListView) rootView.findViewById(R.id.list_drawing_result);


        noAnswerView = (LinearLayout) rootView.findViewById(R.id.no_answer_view);

        resultItemLayout = (LinearLayout) rootView.findViewById(R.id.result_item_layout);
        resultItemLayout.removeAllViews();

        mFragmentManager = getFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        detailArr = new ArrayList<PollResultDetail>();

        if (prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
            maxGage = TABLET_GAGE;
        } else {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                maxGage = PHONE_LANDSCAPE;
            else
                maxGage = PHONE_PORTRATE;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
            maxGage = TABLET_GAGE;
        }else{
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                maxGage = PHONE_LANDSCAPE;
            else
                maxGage = PHONE_PORTRATE;
            dataSetting();
        }
    }


    @Override
    public void onExitPoll() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onUpdateAnswerUser(String userNo) {

    }


    private class PollResultDetail {
        private String num;
        private String itemnm;
        private JSONArray detailArr;

        public PollResultDetail(String num, String itemnm, JSONArray arr){
            this.num = num;
            this.itemnm = itemnm;
            this.detailArr = arr;
        }

        public JSONArray getDetailArr() {
            return detailArr;
        }

        public String getItemnm() {
            return itemnm;
        }

        public String getNum() {
            return num;
        }
    }


    public class PollResultType3User {
        String userThumbnail;
        String userNm;
        String userItemNm;

        public PollResultType3User(String userThumbnail, String userNm, String userItemNm){
            this.userThumbnail = userThumbnail;
            this.userNm = userNm;
            this.userItemNm = userItemNm;
        }

        public String getUserItemNm() {
            return userItemNm;
        }

        public String getUserNm() {
            return userNm;
        }

        public String getUserThumbnail() {
            return userThumbnail;
        }
    }


    // 질문에 답변한 유저와 답안 정보를 관리하는 클래스
    public class QuestionAnswerUser {
        private String pollNo;
        private String pollUserNo;
        private String userThumbnail;
        private String userNm;
        private String answerData;
        private String pollFileNo;

        public QuestionAnswerUser(String pollNo, String pollUserNo, String userNm, String userThumbnail,  String userItemNm){
            this.pollNo = pollNo;
            this.pollUserNo = pollUserNo;
            this.userNm = userNm;
            this.userThumbnail = userThumbnail;
            this.answerData = userItemNm;
        }

        public String getPollNo() {
            // 폴 번호
            return pollNo;
        }

        public String getPollUserNo() {
            // 폴 유저번호
            return pollUserNo;
        }
        public String getAnswerData() {
            // 답변한 데이터 내용
            return answerData;
        }

        public String getUserNm() {
            // 답변한 유저의 이름
            return userNm;
        }

        public String getUserThumbnail() {
            // 답변한 유저의 썸네일
            return userThumbnail;
        }

        public String getPollFileNo() {
            return pollFileNo;
        }

        public void setPollFileNo(String pollFileNo) {
            this.pollFileNo = pollFileNo;
        }
    }
}