package com.knowlounge.fragment.poll;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.adapter.PollResultDetail_UserListAdapter;
import com.knowlounge.model.PollResultDetailUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PollCompletRsultDetailFragment extends Fragment {
    private final String TAG = "PollCompletRsultDetail";

    private View rootView;

    private ImageView mResultDetailBackBtn;
    private TextView mResultDetailItemTitle, mResultDetailAnswerTitle, mResultDetailContent, mResultDetailSelectorTitle;
    private ListView mResultDetailListview;

    private LinearLayout noListLayout;

    private LayoutInflater inflater;

    private PollResultDetail_UserListAdapter adapter;
    private ArrayList<PollResultDetailUser> mUserList;


    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        this.inflater = inflater;
        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_result_detail, container, false);
        setFindViewById();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mResultDetailBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, getArguments().getString("itemidx"));
        Log.d(TAG, getArguments().getString("itemnm"));
        Log.d(TAG, getArguments().getString("answeruser"));
        String itemidx = getArguments().getString("itemidx");
        String itemnm = getArguments().getString("itemnm");
        String answeruser = getArguments().getString("answeruser");

        mResultDetailItemTitle.setText(String.format(getResources().getString(R.string.result_detail_title),itemidx));
        mResultDetailAnswerTitle.setText(String.format(getResources().getString(R.string.result_detail_answer),itemidx));
        mResultDetailSelectorTitle.setText(String.format(getResources().getString(R.string.result_detail_answerer),itemidx));

        mResultDetailContent.setText(itemnm);

        Log.d(TAG, "onResume");
        try {
            JSONArray arr = new JSONArray(answeruser);

            if(arr.length() > 0) {
                mResultDetailListview.setVisibility(View.VISIBLE);
                noListLayout.setVisibility(View.GONE);
                for (int i = 0; i < arr.length(); i++) {
                    String thumbnail = arr.getJSONObject(i).has("thumbnail") ? arr.getJSONObject(i).getString("thumbnail") : "";
                    String usernm = arr.getJSONObject(i).has("usernm") ? arr.getJSONObject(i).getString("usernm") : "";
                    mUserList.add(new PollResultDetailUser(thumbnail, usernm));
                }

                adapter = new PollResultDetail_UserListAdapter(getActivity().getBaseContext(), mUserList);
                mResultDetailListview.setAdapter(adapter);
            } else {
                mResultDetailListview.setVisibility(View.GONE);
                noListLayout.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            Log.d("pollResultDeploy", e.getMessage());
        }

    }

    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    public void onPause()
    {
        super.onPause();
        Log.i(TAG, "onPause");
    }


    public void setFindViewById()
    {

        mResultDetailBackBtn = (ImageView) rootView.findViewById(R.id.result_detail_back_btn);

        mResultDetailItemTitle = (TextView) rootView.findViewById(R.id.result_detail_item_title);
        mResultDetailAnswerTitle = (TextView) rootView.findViewById(R.id.result_detail_answer_title);
        mResultDetailContent = (TextView) rootView.findViewById(R.id.result_detail_content);
        mResultDetailSelectorTitle = (TextView) rootView.findViewById(R.id.result_detail_selector_title);

        mResultDetailListview = (ListView) rootView.findViewById(R.id.result_detail_listview);

        noListLayout = (LinearLayout) rootView.findViewById(R.id.no_answer_view);

        mUserList = new ArrayList<PollResultDetailUser>();

    }
}