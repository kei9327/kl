package com.knowrecorder.phone.tab.Subject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.OpenCourse.API.Models.Video;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.R;
import com.knowrecorder.phone.EndlessRecyclerViewScrollListener;
import com.knowrecorder.phone.PhoneOpencourseActivity;
import com.knowrecorder.phone.rxevent.EventCancel;
import com.knowrecorder.phone.rxevent.SelectTab;
import com.knowrecorder.phone.tab.PAdapter.SubjectListAdapter;
import com.knowrecorder.phone.tab.model.VideoData;
import com.knowrecorder.rxjava.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;

/**
 * Created by we160303 on 2016-11-29.
 */

public class SubjectFragment extends Fragment implements View.OnClickListener, SortSearchFragment.onSortAndSearchListner{
    private final String TAG = "SubjectFragment";

    Retrofit retrofit;
    ApiEndPointInterface api;

    private View rootView;
    private LinearLayout selectMenuLayout;
    private LinearLayout noSearchLayout;
    private RecyclerView videoRecycler;
    private ImageView btnBack;
    private ImageView btnSearch;
    private TextView menuTitle;
    private TextView searchTips;
    private ProgressBar subjectProgress;

    private FrameLayout sortSearchLayout;

    ArrayList<VideoData> list = new ArrayList<VideoData>();

    private SubjectListAdapter adapter;

    private int currentMenu;
    private int lastMenu;
    private String currentSort="";
    private String searchKeyword="";
    private int totalCount = 0;
    private boolean searchFlag = false;

    private Subscriber<? super Object> mSubscriber = new Subscriber<Object>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            if(o instanceof EventCancel){
                final EventCancel data = (EventCancel)o;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sortSearchLayout.setVisibility(View.GONE);
                        softKeyboardHide();
                    }
                });

            }
        }
    };

    private EndlessRecyclerViewScrollListener scrollListener;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getInstance().getBusObservable()
                .subscribe(mSubscriber);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.p_fragment_subject, container, false);
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

        selectMenuLayout = (LinearLayout) rootView.findViewById(R.id.select_menu_layout);
        noSearchLayout = (LinearLayout) rootView.findViewById(R.id.no_search_layout);
        videoRecycler = (RecyclerView) rootView.findViewById(R.id.p_op_video_recycler);

        btnBack = (ImageView) rootView.findViewById(R.id.p_op_btn_back);
        btnSearch = (ImageView) rootView.findViewById(R.id.p_op_btn_search);
        sortSearchLayout = (FrameLayout) rootView.findViewById(R.id.p_op_sort_search_layout);
        subjectProgress = (ProgressBar) rootView.findViewById(R.id.subject_progress);
        menuTitle = (TextView) rootView.findViewById(R.id.p_op_menu_title);
        searchTips = (TextView) rootView.findViewById(R.id.search_tips);

        currentSort = "recently";

        final SortSearchFragment sortSearchFragment = new SortSearchFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.p_op_sort_search_layout, sortSearchFragment).commit();

        SortSearchFragment.setOnSortAndSearchListner(this);

        final String searchTipSentence = getActivity().getResources().getString(R.string.i_searchtips_one) +"\n"+ getActivity().getResources().getString(R.string.i_searchtips_two);
        searchTips.setText(searchTipSentence);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalCount = 0;
                searchKeyword = "";
                list.clear();
                adapter.notifyDataSetChanged();
                subjectProgress.setVisibility(View.VISIBLE);
                sortSearchLayout.setVisibility(View.GONE);
                softKeyboardHide();
                if(!searchFlag) {
                    selectMenuLayout.setVisibility(View.VISIBLE);
                }else{
                    currentMenu = lastMenu;
                    setMenuTitle();
                    getCategoryData();
                    searchFlag = false;
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sortSearchLayout.isShown())
                    sortSearchLayout.setVisibility(View.GONE);
                else
                    sortSearchLayout.setVisibility(View.VISIBLE);
            }
        });

        adapter = new SubjectListAdapter(getContext(), list);

//        videoRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        videoRecycler.setLayoutManager(linearLayoutManager);
        videoRecycler.setAdapter(adapter);

        ((ImageView)rootView.findViewById(R.id.p_op_menu_math)).setOnClickListener(this);
        ((ImageView)rootView.findViewById(R.id.p_op_menu_science)).setOnClickListener(this);
        ((ImageView)rootView.findViewById(R.id.p_op_menu_language)).setOnClickListener(this);
        ((ImageView)rootView.findViewById(R.id.p_op_menu_social)).setOnClickListener(this);
        ((ImageView)rootView.findViewById(R.id.p_op_menu_art)).setOnClickListener(this);
        ((ImageView)rootView.findViewById(R.id.p_op_menu_others)).setOnClickListener(this);


        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getCategoryData();
            }
        };
        videoRecycler.addOnScrollListener(scrollListener);

        int subjectCategory = getArguments().getInt("subject");

        if( subjectCategory != PhoneOpencourseActivity.ALL){
            currentMenu = subjectCategory;
            getCategoryData();
            setMenuTitle();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriber.unsubscribe();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.p_op_menu_math :
                currentMenu = PhoneOpencourseActivity.MATH;
                break;
            case R.id.p_op_menu_science :
                currentMenu = PhoneOpencourseActivity.SCIENCE;
                break;
            case R.id.p_op_menu_language :
                currentMenu = PhoneOpencourseActivity.LANGUAGE;
                break;
            case R.id.p_op_menu_social :
                currentMenu = PhoneOpencourseActivity.SOCIAL;
                break;
            case R.id.p_op_menu_art :
                currentMenu = PhoneOpencourseActivity.ART;
                break;
            case R.id.p_op_menu_others :
                currentMenu = PhoneOpencourseActivity.OTHERS;
                break;
        }
        lastMenu = currentMenu;
        setMenuTitle();
        getCategoryData();
    }

    private void setMenuTitle(){
        switch (currentMenu){
            case PhoneOpencourseActivity.MATH :
                menuTitle.setText(getActivity().getResources().getString(R.string.opencourse_menu_math));
                break;
            case PhoneOpencourseActivity.SCIENCE:

                menuTitle.setText(getActivity().getResources().getString(R.string.opencourse_menu_science));
                break;
            case PhoneOpencourseActivity.LANGUAGE :
                menuTitle.setText(getActivity().getResources().getString(R.string.opencourse_menu_language));
                break;
            case PhoneOpencourseActivity.SOCIAL:
                menuTitle.setText(getActivity().getResources().getString(R.string.opencourse_menu_social));
                break;
            case PhoneOpencourseActivity.ART:
                menuTitle.setText(getActivity().getResources().getString(R.string.opencourse_menu_art));
                break;
            case PhoneOpencourseActivity.OTHERS:
                menuTitle.setText(getActivity().getResources().getString(R.string.opencourse_menu_others));
                break;
        }

    }

    private void getCategoryData() {

        final Call<List<Video>> getVideos;
        if(searchKeyword.equals(""))
            getVideos = api.getVideos(totalCount, 10, ServerInfo.API_KEY, currentMenu, currentSort, KnowRecorderApplication.getLanguage());
        else
            getVideos = api.searchVideos(searchKeyword, ServerInfo.API_KEY, currentMenu,  currentSort, totalCount, 10, KnowRecorderApplication.getLanguage());

        getVideos.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                List<Video> videos = response.body();
                for (Video video : videos) {
                    totalCount++;
                    list.add(new VideoData(video.getId(), video.getThumbnail(), video.getTitle(), video.getAuthor(), Integer.toString(video.getHits()), video.getAuthorThumbnail(), formatSeconds(video.getPlaytime() * 1000)));
                    Log.d("hit count",  video.getHits()+"");
                }

                if(videos.size() == 0 && searchFlag)
                    noSearchLayout.setVisibility(View.VISIBLE);
                else
                    noSearchLayout.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();
                subjectProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.opencourse_respond_fail, Toast.LENGTH_SHORT).show();
            }
        });
        selectMenuLayout.setVisibility(View.GONE);
    }
    public String formatSeconds(long millis) {
        long s = (millis / 1000) % 60;
        long m = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", m, s);
    }

    @Override
    public void onSortAndSearch(boolean isSearch ,int menu, String sort, String keyword) {
        searchFlag = isSearch;
        if(menu >= 0){
            currentMenu = menu;
        }
        if(!sort.equals("")){
            currentSort = sort;
        }
        if(!keyword.equals("")){
            searchKeyword = keyword;
        }else{
            searchKeyword = "";
        }

        if(isSearch) {
            menuTitle.setText(getActivity().getResources().getString(R.string.search_results));
            sortSearchLayout.setVisibility(View.GONE);
        }

        totalCount = 0;
        list.clear();
        softKeyboardHide();
        getCategoryData();
    }

    private void softKeyboardHide(){
        try {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
