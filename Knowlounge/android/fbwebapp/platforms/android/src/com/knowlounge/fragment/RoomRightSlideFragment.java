package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.ChattingEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by we160303 on 2016-07-25.
 */
public class RoomRightSlideFragment extends Fragment implements
        RoomActivity.onCheckArriveBadge,
        RoomActivity.onOpenRightMenuListener {

    private final String TAG = "CRSMF";

    @BindView(R.id.class_right_slide_menu_userlist) ImageView user;
    @BindView(R.id.class_right_slide_menu_invite) ImageView invite;
    @BindView(R.id.class_right_slide_menu_community) ImageView community;
    @BindView(R.id.class_right_slide_menu_comment) ImageView comment;
    @BindView(R.id.class_right_slide_menu_back) ImageView back;

    @BindView(R.id.class_right_slide_menu_userlist_badge) TextView userBadge;
    @BindView(R.id.class_right_slide_menu_community_badge) TextView communityBadge;
    @BindView(R.id.class_right_slide_menu_comment_badge) TextView commentBadge;

    private String whisperId = "";

    private Subscriber<? super Object> mSubscriber = new Subscriber<Object>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            if(o instanceof ChattingEvent){
                final ChattingEvent data = (ChattingEvent)o;
                Log.d(TAG, "whisperID : "+data.getWisperId());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        whisperId = data.getWisperId();
                    }
                });

            }
        }
    };

    @Override
    public void onAttach(Context context) {
        Log.d(TAG,"onAttach");
        super.onAttach(context);
        RoomActivity.setOnOpenRightMenuListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.get().getBustObservable()
                .subscribe(mSubscriber);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_parent_right_drawer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        RoomActivity.addOnCheckArriveBadge(this);
    }

    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        RoomActivity.removeCheckArriveBadge(this);
    }

    @Override
    public void onDestroy() {
        mSubscriber.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.class_right_slide_menu_userlist)
    void onClickUserList() {
        openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW, GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW,true);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.class_right_slide_menu_invite)
    void onClickInvite() {
        openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_INVITE_VIEW, 0, true);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.class_right_slide_menu_community)
    void onClickCommunity() {
        openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW, GlobalConst.CANVAS_RIGHT_MENU_CHAT_VIEW,true);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.class_right_slide_menu_comment)
    void onClickComment() {
        openRightMenu(GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW, 0, true);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.class_right_slide_menu_back)
    void onClickBack() {
        ((RoomActivity) getActivity()).getDrawerLayout().closeDrawer(Gravity.RIGHT);
    }


    @Override
    public void checkArriveBadge() {
        checkBadge();
    }


    @Override
    public void openRightMenu(int parent, int child, boolean isCanvas) {

        if(isCanvas && RoomActivity.currentParent == parent) {
            return;
        }
        RoomActivity.currentParent = parent;
        selectedTab(parent);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle nextMenu = new Bundle();
        nextMenu.putInt("switch",child);
        nextMenu.putInt("type", GlobalConst.THROUGHT_ROOM);
        switch (parent) {
            case GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW:
                RoomRightUserTabFragment userListFragment = new RoomRightUserTabFragment();
                userListFragment.setArguments(nextMenu);
                fragmentTransaction.replace(R.id.class_right_slide_menu_container, userListFragment);
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_INVITE_VIEW:
                if (RoomActivity.activity.getGuestFlag()) {
                    GuestLoginFragment guestLoginFragment = new GuestLoginFragment();
                    fragmentTransaction.replace(R.id.class_right_slide_menu_container, guestLoginFragment);
                } else {
                    SNSFriendListDepthOneFragment inviteFragment = new SNSFriendListDepthOneFragment();
                    inviteFragment.setArguments(nextMenu);
                    fragmentTransaction.replace(R.id.class_right_slide_menu_container, inviteFragment);
                }
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW :
                RoomRightCommunityTabFragment communityFragment = new RoomRightCommunityTabFragment();
                if (!whisperId.equals("")) {
                    Log.d(TAG,"Argument WhisperId : " + whisperId);
                    nextMenu.putString("whisper", whisperId);
                }
                nextMenu.putInt("switch", GlobalConst.CANVAS_RIGHT_MENU_CLASS_CHAT_VIEW);  // 클래스 채팅을 기본으로..
                communityFragment.setArguments(nextMenu);
                fragmentTransaction.replace(R.id.class_right_slide_menu_container, communityFragment);

                whisperId = "";
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW :
                CommentFragment commentFragment = new CommentFragment();
                fragmentTransaction.replace(R.id.class_right_slide_menu_container, commentFragment);
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void selectedTab(int parent) {
        user.setAlpha(0.4f);
        invite.setAlpha(0.4f);
        community.setAlpha(0.4f);
        comment.setAlpha(0.4f);

        checkBadge();
        switch (parent){
            case GlobalConst.CANVAS_RIGHT_MENU_USER_VIEW : user.setAlpha(1.0f);break;
            case GlobalConst.CANVAS_RIGHT_MENU_INVITE_VIEW : invite.setAlpha(1.0f);break;
            case GlobalConst.CANVAS_RIGHT_MENU_COMMUNITY_VIEW : community.setAlpha(1.0f);break;
            case GlobalConst.CANVAS_RIGHT_MENU_COMMENT_VIEW : comment.setAlpha(1.0f);break;
        }

    }
    private void checkBadge(){

        if(RoomActivity.activity.getReqBadgeCount() != 0 ) {
            userBadge.setVisibility(View.VISIBLE);
            userBadge.setText(RoomActivity.activity.getReqBadgeCount()+"");
        }else{
            userBadge.setVisibility(View.GONE);
        }

        int communityBadgeCount = RoomActivity.activity.getChatBadgeCnt() + RoomActivity.activity.getClassChatBadgeCnt();

        if( communityBadgeCount != 0){
            communityBadge.setVisibility(View.VISIBLE);
            communityBadge.setText(communityBadgeCount+"");
        }else{
            communityBadge.setVisibility(View.GONE);
        }

        if(RoomActivity.activity.getCommentBadgeCnt() != 0){
            commentBadge.setVisibility(View.VISIBLE);
            commentBadge.setText(RoomActivity.activity.getCommentBadgeCnt()+"");
        }else{
            commentBadge.setVisibility(View.GONE);
        }
    }
}
