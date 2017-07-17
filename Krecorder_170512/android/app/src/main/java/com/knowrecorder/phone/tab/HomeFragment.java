package com.knowrecorder.phone.tab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.OpenCourse.API.Models.HomeVideo;
import com.knowrecorder.OpenCourse.API.Models.Video;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.phone.PhoneOpencourseActivity;
import com.knowrecorder.phone.rxevent.SelectTab;
import com.knowrecorder.phone.tab.PAdapter.HomeListAdapter;
import com.knowrecorder.phone.tab.PAdapter.ViewPagerAdapter;
import com.knowrecorder.phone.tab.model.DeepLink;
import com.knowrecorder.phone.tab.model.VideoData;
import com.knowrecorder.phone.tab.model.SectionList;
import com.knowrecorder.rxjava.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by we160303 on 2016-11-29.
 */

public class HomeFragment extends Fragment {
    private final String TAG = "HomeFragment";
    private final int CIRCLE_VIEW_ID = 0x8000;

    private View rootView;
    Retrofit retrofit;
    ApiEndPointInterface api;

    private ImageView toggleBtn;
    private ViewPager pager;
    private RecyclerView recyclerView;
    private LinearLayout circlePageControlLayout;
    private ProgressBar homeProgress;
    private TextView defaultPagerText;
    private int circleNumber = 0;

    HomeListAdapter adapter;

    ArrayList<DeepLink> deepLinks = new ArrayList<DeepLink>();
    ArrayList<SectionList> list = new ArrayList<SectionList>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.p_fragment_home, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        retrofit = new Retrofit.Builder()
                .baseUrl(ServerInfo.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiEndPointInterface.class);
        toggleBtn = (ImageView) rootView.findViewById(R.id.p_opencourse_toggle_btn);
        pager = (ViewPager) rootView.findViewById(R.id.p_opencourse_view_pager);
        circlePageControlLayout = (LinearLayout) rootView.findViewById(R.id.circle_page_control_layout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.p_opencourse_recycler_view);
        defaultPagerText = (TextView) rootView.findViewById(R.id.p_op_default_pager_text);
        homeProgress = (ProgressBar) rootView.findViewById(R.id.p_op_home_progress);

        getVideoData(getActivity());
        getDefaulData();
        setCirclePagerControl();

        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectTab event = new SelectTab();

                event.setTab(PhoneOpencourseActivity.SUBJECT_TAB);
                event.setSubject(PhoneOpencourseActivity.ALL);

                EventBus.getInstance().post(event);
            }
        });

        adapter = new HomeListAdapter(getContext(), list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getContext(), getActivity().getLayoutInflater(), deepLinks);
        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG,"onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG,"onPageSelected");
                selectedPage(position);
                defaultPagerText.setText(getDefaultPagerText(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG,"onPageScrollStateChanged");
            }
        });
        //this is 꼼수
        pager.setCurrentItem(1,true);
        pager.setCurrentItem(0,true);

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private void selectedPage(int position) {
        for(int i=0; i < circleNumber; i++)
        {
            ImageView iv = (ImageView) rootView.findViewById(CIRCLE_VIEW_ID+i);
            if(position == i){
                iv.setImageResource(R.drawable.ico_pagecontrol_s);
            }else{
                iv.setImageResource(R.drawable.ico_pagecontrol);
            }
        }
    }

    private void setCirclePagerControl() {
        int circleSize  = (int) PixelUtil.getInstance().convertDpToPixel(6);

        for(DeepLink data:deepLinks){
            ImageView iv = new ImageView(getActivity());
            iv.setId(CIRCLE_VIEW_ID + circleNumber++);
            iv.setPadding(0,0,circleSize,0);
            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(circleSize*2, circleSize);
            iv.setLayoutParams(params);
            circlePageControlLayout.addView(iv);
        }
    }



    public void getVideoData(final FragmentActivity activity) {
        Log.d(TAG, "getVideoData");

        Call<List<HomeVideo>> getVideos;
        getVideos = api.getVideos( ServerInfo.API_KEY, KnowRecorderApplication.getLanguage());

        getVideos.enqueue(new Callback<List<HomeVideo>>() {
            @Override
            public void onResponse(Call<List<HomeVideo>> call, Response<List<HomeVideo>> response) {
                List<HomeVideo> homeVideos = response.body();

                for(int i=PhoneOpencourseActivity.MATH; i<=PhoneOpencourseActivity.OTHERS ; i++) {

                    HomeVideo homeVideo = homeVideos.get(i-1);
                    ArrayList<VideoData> homeVideoDatas = new ArrayList<VideoData>();

                    for(Video video : homeVideo.getList())
                        homeVideoDatas.add(new VideoData(video.getId(), video.getThumbnail(), video.getTitle(), video.getAuthor(), Integer.toString(video.getHits()), video.getAuthorThumbnail(), formatSeconds(video.getPlaytime() * 1000)));
                    list.add(new SectionList(getMenuTitle(Integer.parseInt(homeVideo.getCategoryId()), activity), homeVideoDatas, Integer.parseInt(homeVideo.getCategoryId())));
                }

                homeProgress.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<HomeVideo>> call, Throwable t) {

            }
        });
    }

    public String formatSeconds(long millis) {
        long s = (millis / 1000) % 60;
        long m = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", m, s);
    }

    private String getMenuTitle(int i, FragmentActivity activity) {
        switch (i){
            case PhoneOpencourseActivity.MATH : return activity.getResources().getString(R.string.opencourse_menu_math);
            case PhoneOpencourseActivity.ART : return activity.getResources().getString(R.string.opencourse_menu_art);
            case PhoneOpencourseActivity.LANGUAGE : return activity.getResources().getString(R.string.opencourse_menu_language);
            case PhoneOpencourseActivity.SCIENCE : return activity.getResources().getString(R.string.opencourse_menu_science);
            case PhoneOpencourseActivity.SOCIAL : return activity.getResources().getString(R.string.opencourse_menu_social);
            case PhoneOpencourseActivity.OTHERS : return activity.getResources().getString(R.string.opencourse_menu_others);
        }
        return null;
    }

    public void getDefaulData() {
        deepLinks.add(new DeepLink(R.drawable.img_main_top_guide_001));
        deepLinks.add(new DeepLink(R.drawable.img_main_top_guide_002));
        deepLinks.add(new DeepLink(R.drawable.img_main_top_guide_003));
    }
    private String getDefaultPagerText(int position) {
        switch (position){
            case 0 : return getActivity().getResources().getString(R.string.intro_m1);
            case 1 : return getActivity().getResources().getString(R.string.intro_m2);
            case 2 : return getActivity().getResources().getString(R.string.intro_m3);
            default: return "";
        }
    }

}
