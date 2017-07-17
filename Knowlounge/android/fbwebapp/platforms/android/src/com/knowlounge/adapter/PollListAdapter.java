package com.knowlounge.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.knowlounge.fragment.poll.QuestionResultFragment;
import com.knowlounge.fragment.poll.pollCreateFragment;
import com.knowlounge.fragment.poll.pollListFragment;
import com.knowlounge.model.PollList;
import com.knowlounge.util.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Minsu on 2016-01-12.
 */
public class PollListAdapter extends BaseAdapter {
    private final String TAG = "PollListAdapter";

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<PollList> list;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private String selectedPollitemno;
    private WenotePreferenceManager prefManager;

    ProgressDialog mProgressDialog = null;

    int type;
    int selectPosition = -1;


    private static PollListSelectListener mCallback = null;

    public interface PollListSelectListener {
        public void onSelect();
    }
    public static void setPollLIstListener(PollListSelectListener listener){
        mCallback = listener;
    }

    public PollListAdapter(Context context, FragmentManager fragmentManager, FragmentTransaction fragmentTransaction, ArrayList<PollList> list, int type) {

        super();
        this.context = context;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
        this.type = type;
        this.selectedPollitemno = "";
        this.fragmentManager = fragmentManager;
        this.fragmentTransaction = fragmentTransaction;
        prefManager = WenotePreferenceManager.getInstance(this.context);


    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public PollList getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.d(getClass().getSimpleName(), "getView");
        if (convertView == null) {
                convertView = inflater.inflate(R.layout.poll_list_row, parent, false);
        }

        if (position == 0) {
            convertView.setPadding(AndroidUtils.getPxFromDp(context, 30), AndroidUtils.getPxFromDp(context, 10),AndroidUtils.getPxFromDp(context,30),0);
        } else {
            convertView.setPadding(AndroidUtils.getPxFromDp(context, 30), 0, AndroidUtils.getPxFromDp(context, 30), 0);
        }

        if (position == list.size()-1) {
            convertView.findViewById(R.id.poll_list_divider).setVisibility(View.GONE);
        } else {
            convertView.findViewById(R.id.poll_list_divider).setVisibility(View.VISIBLE);
        }

        PollList tempData = getItem(position);
        final LinearLayout pollListCheckLayout = (LinearLayout) convertView.findViewById(R.id.poll_list_check_layout);
        final ImageView pollListCheck = (ImageView) convertView.findViewById(R.id.poll_list_check);
        final TextView poll_list_title = (TextView) convertView.findViewById(R.id.poll_list_title);

        final ImageView icoListArrow = (ImageView) convertView.findViewById(R.id.ico_list_arrow);

        if (type == GlobalConst.VIEW_COMPLETE_POLLLIST_FRAGMENT) {   // 완료된 폴 리스트일 때,
            if (tempData.getChecked()) {
                pollListCheck.setImageResource(R.drawable.btn_checkbox_on);
                poll_list_title.setTypeface(null, Typeface.BOLD);
                poll_list_title.setTextColor(Color.parseColor("#5a5a5a"));
            } else {
                pollListCheck.setImageResource(R.drawable.btn_checkbox);
                poll_list_title.setTypeface(null, Typeface.NORMAL);
                poll_list_title.setTextColor(Color.parseColor("#969696"));
            }
        } else {
            if (pollListFragment.edittingMode) {   // 편집모드 상태일 때,
                pollListCheckLayout.setVisibility(View.VISIBLE);
                icoListArrow.setVisibility(View.GONE);
                if (tempData.getChecked()) {
                    pollListCheck.setImageResource(R.drawable.btn_checkbox_on);
                    poll_list_title.setTypeface(null, Typeface.BOLD);
                    poll_list_title.setTextColor(Color.parseColor("#5a5a5a"));
                } else {
                    pollListCheck.setImageResource(R.drawable.btn_checkbox);
                    poll_list_title.setTypeface(null, Typeface.NORMAL);
                    poll_list_title.setTextColor(Color.parseColor("#969696"));
                }
            } else {
                pollListCheckLayout.setVisibility(View.GONE);
                icoListArrow.setVisibility(View.VISIBLE);
                if (tempData.getChecked()) {
                    pollListCheck.setImageResource(R.drawable.btn_checkbox_on);
                    poll_list_title.setTypeface(null, Typeface.BOLD);
                    poll_list_title.setTextColor(Color.parseColor("#5a5a5a"));
                } else {
                    pollListCheck.setImageResource(R.drawable.btn_checkbox);
                    poll_list_title.setTypeface(null, Typeface.NORMAL);
                    poll_list_title.setTextColor(Color.parseColor("#969696"));
                }
            }
        }

        pollListCheckLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT){
                    if(!pollListFragment.edittingMode){
                        for(PollList data : list)
                            data.setChecked(false);
                    }
                }
                selectPosition = position;
                list.get(position).toggleChecked();
                mCallback.onSelect();
                notifyDataSetChanged();
            }
        });

        poll_list_title.setText(list.get(position).getTitle());
        poll_list_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
                    if (!pollListFragment.edittingMode) {
                        Bundle argument = new Bundle();
                        argument.putString("polltempno", list.get(position).getPolltempno());

                        pollCreateFragment createFragment = new pollCreateFragment();
                        createFragment.setArguments(argument);
                        fragmentTransaction.replace(R.id.dialog_main_container, createFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                } else {
                    mProgressDialog = ProgressDialog.show(context, "", context.getResources().getString(R.string.now_loading), true);
                    getRestClient(list.get(position).getPolltempno());
                }
            }
        });
        return convertView;
    }

    public void setSelectPollInit(){
        for(int i=0; i<list.size();i++)
            list.get(i).setChecked(false);
    }


    public String getSelectedPollitemno(){

        if(selectPosition == -1)
            return "";

        if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT){
            if(pollListFragment.edittingMode){
                return multiSelected();
            }else {
                return getItem(selectPosition).getPolltempno();
            }
        }else{
            return multiSelected();
        }
    }

    public String getSelectedPollitemTitle(){
       return getItem(selectPosition).getTitle();
    }

    public String multiSelected(){
        String result = "";
        for(PollList item:list){
            if(item.getChecked()) {
                result += item.getPolltempno() + "|";
            }
        }
        return result.equals("") ? "" : result.substring(0,result.length()-1);
    }

    public void getRestClient(String pollno){
        RequestParams params = new RequestParams();
        params.put("pollno",pollno);
        RestClient.post("poll/get.json", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                try {
                    Bundle argument = new Bundle();
                    argument.putString("obj", obj.getJSONObject("map").toString());
                    QuestionResultFragment completeFragment = new QuestionResultFragment();
                    completeFragment.setArguments(argument);
                    fragmentTransaction.replace(R.id.dialog_main_container, completeFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } catch(JSONException j) {
                    j.printStackTrace();
                } finally {
                    if(mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
            }
        });

    }

}

