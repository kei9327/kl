package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.knowlounge.R;
import com.knowlounge.adapter.CommentListAdapter;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.util.AndroidUtils;

import org.apache.cordova.CordovaWebView;

/**
 * Created by Minsu on 2016-04-07.
 */
public class CommentFragment extends Fragment implements RoomActivity.notiChangeData{

    private static String TAG = "CommentFragment";

    private CommentListAdapter commentAdapter;

    private ListView commentListView;
    private View rootView;

    private EditText inputComment;
    private Button btnSendComment;

    private boolean mLockListView;
    private Parcelable state;
    private int pageNum = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        RoomActivity.setOnNotiChangeData(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_comment, container, false);
        setFindViewById();
        RoomActivity.activity.clearCommentBadgeCnt();
        RoomActivity.activity.checkNotiBadgeCount();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "input count : " + Integer.toString(s.length()));
                if (s.length() != 0)
                    btnSendComment.setVisibility(View.VISIBLE);
                else
                    btnSendComment.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentStr = inputComment.getText().toString();
                RoomActivity.activity.sendComment(commentStr);
                inputComment.setText("");
                inputComment.clearFocus();
                AndroidUtils.keyboardHide(getActivity());
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onNotiChangeData() {
        Log.d(TAG, "onNotiChangeData");
        mLockListView = false;
        commentAdapter.notifyDataSetChanged();
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mLockListView = false;
//                commentAdapter.notifyDataSetChanged();
//            }
//        });

    }

    private void setFindViewById() {
        commentListView = (ListView) rootView.findViewById(R.id.comment_list);
        inputComment = (EditText) rootView.findViewById(R.id.input_comment);
        btnSendComment = (Button) rootView.findViewById(R.id.btn_send_comment);

        mLockListView = false;
        commentAdapter = new CommentListAdapter(getContext(), RoomActivity.commentList);

        commentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int count = totalItemCount - visibleItemCount;

                if(firstVisibleItem >= count && totalItemCount != 0
                        && mLockListView == false){
                    Log.i(TAG,"Lodding next comment");
                    mLockListView = true;
                    final CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
                    webView.sendJavascript("Ctrl.Comment.more()");
                }
            }
        });

        commentListView.setAdapter(commentAdapter);
    }
}