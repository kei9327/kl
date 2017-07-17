package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
public class RoomRightUserTabFragment extends Fragment implements View.OnClickListener, RoomActivity.onCheckArriveBadge{

    private View rootView;
    private FrameLayout roomNoti;
    private ImageView userList, roomNotiImg;
    private TextView roomNotiBadge;
    private boolean isShown = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.fragment_right_user_parent, container, false);
        userList = (ImageView) rootView.findViewById(R.id.class_right_user_list);
        roomNoti = (FrameLayout) rootView.findViewById(R.id.class_right_user_noti);

        roomNotiImg = (ImageView) rootView.findViewById(R.id.class_right_user_noti_img);
        roomNotiBadge = (TextView) rootView.findViewById(R.id.class_right_user_noti_badge);

        openChildFragment(getArguments().getInt("switch"));

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
        userList.setOnClickListener(this);
        roomNoti.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.class_right_user_list :
                openChildFragment(GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW);
                break;
            case R.id.class_right_user_noti :
                openChildFragment(GlobalConst.CANVAS_RIGHT_MENU_ROOMNOTI_VIEW);
                break;
        }
    }

    @Override
    public void checkArriveBadge() {
        checkBadge();
    }

    private void openChildFragment(int toggle) {

        if(RoomActivity.currentUserChild == toggle && isShown)
            return;
        isShown = true;
        RoomActivity.currentUserChild = toggle;

        selectedTab(toggle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (toggle){
            case GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW :
                RoomUserListFragment userListFragment = new RoomUserListFragment();
                fragmentTransaction.replace(R.id.class_right_user_menu_container, userListFragment);
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_ROOMNOTI_VIEW :
                RoomNotiListFragment roomNotiFragment = new RoomNotiListFragment();
                fragmentTransaction.replace(R.id.class_right_user_menu_container, roomNotiFragment);
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void selectedTab(int toggle) {
        userList.setImageResource(R.drawable.btn_rightmenu_tap_userlist);
        roomNotiImg.setImageResource(R.drawable.btn_rightmenu_tap_noti);
        checkBadge();
        switch (toggle){
            case GlobalConst.CANVAS_RIGHT_MENU_USERLIST_VIEW :
                userList.setImageResource(R.drawable.btn_rightmenu_tap_userlist_on);
                break;
            case GlobalConst.CANVAS_RIGHT_MENU_ROOMNOTI_VIEW :
                roomNotiImg.setImageResource(R.drawable.btn_rightmenu_tap_noti_on);
                break;
        }
    }

    private void checkBadge(){
        if(RoomActivity.activity.getReqBadgeCount() != 0 ) {
            roomNotiBadge.setVisibility(View.VISIBLE);
            roomNotiBadge.setText(Integer.toString(RoomActivity.activity.getReqBadgeCount()));
        }else{
            roomNotiBadge.setVisibility(View.GONE);
        }
    }



}
