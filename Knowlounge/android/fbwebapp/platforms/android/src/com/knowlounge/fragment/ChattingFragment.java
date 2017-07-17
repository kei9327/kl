package com.knowlounge.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.adapter.ChatListAdapter;
import com.knowlounge.adapter.ChatUserRecyclerAdapter;
import com.knowlounge.model.ChatUser;
import com.knowlounge.util.AndroidUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Minsu on 2016-02-17.
 */
public class ChattingFragment extends ListFragment implements ChatUserRecyclerAdapter.ChatUserListener, RoomActivity.notiChangeData {

    private static String TAG = "ChattingFragment";

    private ChatUserRecyclerAdapter mChatUserRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ChatListAdapter mChatAdapter;

    // ListView, RecyclerView는 BindView가 동작하지 않음 - 추후 확인 필요함
    @BindView (android.R.id.list) ListView chatListView;
    @BindView (R.id.chat_user_list) RecyclerView mRecyclerView;

    @BindView(R.id.btn_send_chat) TextView btnSendChat;
    @BindView(R.id.input_chat) EditText chatEditText;
    @BindView(R.id.chat_user_selector) ImageView chatUserSelector;
    @BindView(R.id.chat_user_whisper) ImageView chatUserWhisper;

    private String chatTargetUserNo = "";
    private String chatTargetUserNm = "";

    private boolean isLandscape = false;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "<onAttach / Knowlounge> context : " + context.getClass().getSimpleName());
        super.onAttach(context);
        RoomActivity.setOnNotiChangeData(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "<onCreateView / Knowlounge>");
        View view = inflater.inflate(R.layout.fragment_chatting, container, false);
        ButterKnife.bind(this, view);

        isLandscape = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? true : false;

        chatListView = (ListView) view.findViewById(android.R.id.list);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.chat_user_list);

        mChatUserRecyclerAdapter = new ChatUserRecyclerAdapter(RoomActivity.chatUserList, this);
        mChatAdapter = new ChatListAdapter(getContext(), RoomActivity.chatList);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mChatUserRecyclerAdapter);
        chatListView.setAdapter(mChatAdapter);
        RoomActivity.activity.clearChatBadgeCnt();
        RoomActivity.activity.checkNotiBadgeCount();

        return view;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(this.getClass().getSimpleName(), "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        chatUserSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerView.isShown()) {
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        //내용입력전 send버튼 gone처리
        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Toast.makeText(getActivity().getBaseContext(), "입력전", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Toast.makeText(getActivity().getBaseContext(), "입력중", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "input count : " + Integer.toString(s.length()));
                if (s.length() != 0)
                    btnSendChat.setVisibility(View.VISIBLE);
                else
                    btnSendChat.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
//                Toast.makeText(getActivity().getBaseContext(), "입력후", Toast.LENGTH_SHORT).show();
            }
        });
        chatEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }
            }
        });

        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatMsg = chatEditText.getText().toString();
                String targetUserNo = chatTargetUserNo;
                // 전체 메시지 예외처리
                String targetUserNm = TextUtils.isEmpty(chatTargetUserNo) ? "" : chatTargetUserNm;
                RoomActivity.activity.sendChat(chatMsg, targetUserNo, targetUserNm, "room");
                chatEditText.setText("");

                if(isLandscape && KnowloungeApplication.isPhone)
                    AndroidUtils.keyboardHide(getActivity());

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(getArguments() != null && getArguments().containsKey("whisper"))
        {
            initWhisper(getArguments().getString("whisper"));
        }
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
    public void onWisperSelected(String userNo, String thumbnail, String userNm, int position) {

        Log.d(TAG,"userno, usernm : " + userNo + ", " + userNm);

        Uri thumbnailUri = Uri.parse(thumbnail);
        chatTargetUserNm = userNm;
        chatTargetUserNo = userNo;

        if(position !=0) {
            Glide.with(getContext()).load(thumbnailUri).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(getContext().getResources().getIdentifier("img_userlist_default01", "drawable", getContext().getPackageName()))
                    .bitmapTransform(new CircleTransformTemp(getContext()))
                    .into(chatUserSelector);

        }else {
            Glide.with(getContext()).load(thumbnailUri).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(getContext().getResources().getIdentifier("img_userlist_defaultgroup01", "drawable", getContext().getPackageName()))
                    .bitmapTransform(new CircleTransformTemp(getContext()))
                    .into(chatUserSelector);
        }

        mRecyclerView.setVisibility(View.GONE);
        mChatUserRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNotiChangeData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatUserRecyclerAdapter.notifyDataSetChanged();
                mChatAdapter.notifyDataSetChanged();
            }
        });
    }


    private void initWhisper(String whisperId) {
        int i= 0;
        for(ChatUser data : RoomActivity.chatUserList){
            Log.d(TAG, "user id : " + data.getUserId());
            if(data.getUserId().equals(whisperId)){
                onWisperSelected(data.getUserNo(), data.getThumbnail(), data.getUserNm(), i);
            }
            i++;
        }
    }
}
