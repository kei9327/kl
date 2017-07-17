package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;

/**
 * Created by we160303 on 2016-07-25.
 */
public class RoomRightCommunityTabFragment extends Fragment implements View.OnClickListener, RoomActivity.onCheckArriveBadge{

    private View rootView;
    private FrameLayout btnChat, btnClassChat;
    private ImageView btnChatImg, btnClassChatImg;
    private TextView btnChatBadge, btnClassChatBadge;
    private boolean isShown = false;
    private String whisperId = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.fragment_right_community_tab, container, false);

        btnChat = (FrameLayout) rootView.findViewById(R.id.class_right_community_chat);
        btnClassChat = (FrameLayout) rootView.findViewById(R.id.class_right_community_class_chat);

        btnChatImg = (ImageView) rootView.findViewById(R.id.class_right_community_chat_img);
        btnClassChatImg = (ImageView) rootView.findViewById(R.id.class_right_community_class_chat_img);

        btnChatBadge = (TextView) rootView.findViewById(R.id.class_right_community_chat_badge);
        btnClassChatBadge = (TextView)rootView.findViewById(R.id.class_right_community_class_chat_badge);

        if(getArguments().getString("whisper") != null){
            whisperId = getArguments().getString("whisper");
            Log.d("communityTab","Argument WhisperId : " + whisperId);
        }

        int chattingViewMode = ((RoomActivity)getActivity()).getLastChatMode() == -1 ? getArguments().getInt("switch") :  ((RoomActivity)getActivity()).getLastChatMode();
        openChildFragment(chattingViewMode);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        RoomActivity.addOnCheckArriveBadge(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        RoomActivity.removeCheckArriveBadge(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnChat.setOnClickListener(this);
        btnClassChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.class_right_community_chat:
                openChildFragment(GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW);
                break;
            case R.id.class_right_community_class_chat :
                openChildFragment(GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW);
                break;
        }
    }

    @Override
    public void checkArriveBadge() {
        checkBadge();
    }

    private void openChildFragment(int toggle) {

        if(RoomActivity.currentCommunityChild == toggle && isShown)
            return;

        ((RoomActivity)getActivity()).setLastChatMode(toggle);

        isShown = true;
        RoomActivity.currentCommunityChild = toggle;

        selectedTab(toggle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (toggle){
            case GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW:
                ChattingFragment chatFragment = new ChattingFragment();
                if(!whisperId.equals("")){
                    Bundle argument = new Bundle();
                    argument.putString("whisper",whisperId);
                    chatFragment.setArguments(argument);
                }
                fragmentTransaction.replace(R.id.class_right_community_menu_container, chatFragment);
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW:
                ClassChattingFragment classChattingFragment = new ClassChattingFragment();
                fragmentTransaction.replace(R.id.class_right_community_menu_container, classChattingFragment);
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void selectedTab(int toggle) {
        btnChatImg.setImageResource(R.drawable.btn_rightmenu_tap_boardchat);
        btnClassChatImg.setImageResource(R.drawable.btn_rightmenu_tap_classchat);

        checkBadge();

        switch (toggle){
            case GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW :
                btnChatImg.setImageResource(R.drawable.btn_rightmenu_tap_boardchat_on);
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW :
                btnClassChatImg.setImageResource(R.drawable.btn_rightmenu_tap_classchat_on);
                break;
        }
    }

    private void checkBadge(){
        if(RoomActivity.activity.getChatBadgeCnt() != 0 ) {
            btnChatBadge.setVisibility(View.VISIBLE);
            btnChatBadge.setText(Integer.toString(RoomActivity.activity.getChatBadgeCnt()));
        }else{
            btnChatBadge.setVisibility(View.GONE);
        }

        if(RoomActivity.activity.getClassChatBadgeCnt() != 0 ) {
            btnClassChatBadge.setVisibility(View.VISIBLE);
            btnClassChatBadge.setText(Integer.toString(RoomActivity.activity.getClassChatBadgeCnt()));
        }else{
            btnClassChatBadge.setVisibility(View.GONE);
        }
    }


}
