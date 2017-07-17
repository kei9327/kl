package com.knowlounge.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;

import org.w3c.dom.Text;

/**
 * Created by Minsu on 2016-04-10.
 */
public class CommunicateFragment extends Fragment {

    private static final String TAG = "CommunicateFragment";
    private int tabIndex = 0;
    private FragmentTabHost mTabHost;

    private WenotePreferenceManager prefManager;
    private float density;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
//        Log.d(TAG, getActivity().getClass().getSimpleName());
//        String parentActivityName = getActivity().getClass().getSimpleName();

        View rootView = inflater.inflate(R.layout.fragment_communicate_tab_frame, container, false);

        prefManager = WenotePreferenceManager.getInstance(getActivity());
        density = prefManager.getDensity();

        mTabHost = (FragmentTabHost) rootView.findViewById(R.id.communicate_tabHost);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.d(TAG, "onTabChanged.. tabId : " + tabId + ", position : " + mTabHost.getCurrentTab());
                tabIndex = mTabHost.getCurrentTab();
            }
        });

//        if(savedInstanceState == null) {
//            Log.d(this.getClass().getSimpleName(), "onCreateView / savedInstanceState null");

            //mTabHost.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.communicate_tabContent);

            setNewTab(getContext(), mTabHost, "Tab1",getResources().getDrawable(R.drawable.btn_rightmenu_tap_chat_on), ChattingFragment.class,0);
            setNewTab(getContext(), mTabHost, "Tab2", getResources().getDrawable(R.drawable.btn_rightmenu_tap_comment), CommentFragment.class,1);

//            mTabHost.getTabWidget().getLayoutParams().height = (int)(48 * prefManager.getDensity());
            mTabHost.getTabWidget().setStripEnabled(false);
            mTabHost.setClipToPadding(false);
            mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    ((ImageView)mTabHost.getTabWidget().getChildAt(0).findViewById(R.id.tab_icon)).setImageResource(R.drawable.btn_rightmenu_tap_chat);
                    ((ImageView)mTabHost.getTabWidget().getChildAt(1).findViewById(R.id.tab_icon)).setImageResource(R.drawable.btn_rightmenu_tap_comment);

                    if(mTabHost.getCurrentTab() == 0){
                        ((ImageView)mTabHost.getTabWidget().getChildAt(0).findViewById(R.id.tab_icon)).setImageResource(R.drawable.btn_rightmenu_tap_chat_on);
                    }else{
                        ((ImageView)mTabHost.getTabWidget().getChildAt(1).findViewById(R.id.tab_icon)).setImageResource(R.drawable.btn_rightmenu_tap_comment_on);
                    }
                    AndroidUtils.keyboardHide(getActivity());
                }
            });
            //return inflater.inflate(R.layout.list, null);
            mTabHost.setCurrentTab(tabIndex);

//        } else {
//            int prevTabIdx = savedInstanceState.getInt("tabidx", 0);
//            Log.d(TAG, "onCreateView / savedInstanceState not null");
//
//
//            //mTabHost.setBackgroundColor(Color.parseColor("#D7DCDC"));
//            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.communicate_tabContent);
//
//            setNewTab(getContext(), mTabHost, "Chat",getResources().getDrawable(R.drawable.btn_rightmenu_tap_chat_on), ChattingFragment.class,0);
//            setNewTab(getContext(), mTabHost, "Comment", getResources().getDrawable(R.drawable.btn_rightmenu_tap_comment), CommentFragment.class,1);
//
//
//            //return inflater.inflate(R.layout.list, null);
//            mTabHost.setCurrentTab(prevTabIdx);
//            tabIndex = prevTabIdx;
//        }
        return mTabHost;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    public void setTabIndex(int idx) {
        this.tabIndex = idx;
        mTabHost.setCurrentTab(tabIndex);
    }

    private void setNewTab(Context context, FragmentTabHost tabHost, String tag, Drawable icon, Class<?> cls, int position){
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(getTabIndicator(tabHost.getContext(), icon, position)); // new function to inject our own tab layout
        //tabSpec.setContent(contentID);
        tabHost.addTab(tabSpec, cls, null);
    }

    private View getTabIndicator(Context context, Drawable drawable, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_child_tab, null);
        ImageView tv = (ImageView) view.findViewById(R.id.tab_icon);
        View line = (View) view.findViewById(R.id.center_line);
        View underline = (View) view.findViewById(R.id.child_under_line);
        underline.setVisibility(View.GONE);
        tv.setImageDrawable(drawable);
        if(position == 1)
            line.setVisibility(View.GONE);
        return view;
    }

}
