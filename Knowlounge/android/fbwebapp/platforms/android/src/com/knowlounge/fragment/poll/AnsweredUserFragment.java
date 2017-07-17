package com.knowlounge.fragment.poll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.knowlounge.R;
import com.knowlounge.adapter.AnsweredUserListAdapter;
import com.knowlounge.model.PollAnsweredUser;

import java.util.ArrayList;

/**
 * Created by Mansu on 2016-12-15.
 */

@SuppressLint("ValidFragment")
public class AnsweredUserFragment extends Fragment {

    private GridView mGridView;
    private ArrayList<PollAnsweredUser> userList;
    private AnsweredUserListAdapter mAdapter;
    private Context context;



    public AnsweredUserFragment(ArrayList<PollAnsweredUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.page_answered_user_list, container, false);
        mGridView = (GridView) view.findViewById(R.id.answered_user_grid_view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (context != null) {
            mAdapter = new AnsweredUserListAdapter(context, userList);
            if (mGridView != null) {
                mGridView.setAdapter(mAdapter);
            }
        }

    }


    public void update(final ArrayList<PollAnsweredUser> list) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userList = list;
                    mAdapter.setList(list);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
