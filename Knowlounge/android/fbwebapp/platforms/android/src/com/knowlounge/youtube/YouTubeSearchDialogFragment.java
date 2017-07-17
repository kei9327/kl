package com.knowlounge.youtube;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.VideoIdEvent;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.sqllite.SearchKeyword;
import com.knowlounge.util.RestClient;
import com.knowlounge.youtube.adapter.HistoryVideoAdapter;
import com.knowlounge.youtube.adapter.SearchKeywordAdapter;
import com.knowlounge.youtube.adapter.SearchYoutubeAdapter;
import com.knowlounge.youtube.adapter.TutorialVideoAdapter;
import com.knowlounge.youtube.model.VideoModel;
import com.knowlounge.youtube.model.YouTubeModel;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-09-28.
 */
public class YouTubeSearchDialogFragment extends DialogFragment implements View.OnClickListener{

    private final String TAG = "YoutubeTest";

    private final int HISTORY_TAB = 0;
    private final int TUTORIAL_TAB = 1;
    private final int SEARCH_TAB = 2;
    private final int KEYWORD_MAX = 10;

    private View view;
    private WenotePreferenceManager prefManager;
    public DbOpenHelper mDbOpenHelper;

    private TextView cancelBtn;
    private RecyclerView recyclerView;
    private LinearLayout historyLayout, tutorialLayout, searchLayout, emptyResultLayout, youtubeSearchLayout;
    private ImageView historyIco, tutorialIco, searchIco, emptyResultImg, youtubeSearchIco, youtubeSearchClearIco;
    private TextView emptyResultText, youtubeSearchText;
    private EditText youtubeSearchEdit;

    private SearchKeywordAdapter keywordAdapter;
    private HistoryVideoAdapter hvAdapter;
    private TutorialVideoAdapter tvAdapter;
    private SearchYoutubeAdapter svAdapter;

    public ArrayList<VideoModel> tvList = new ArrayList<VideoModel>();
    public ArrayList<VideoModel> hvList = new ArrayList<VideoModel>();
    public ArrayList<YouTubeModel> svList = new ArrayList<YouTubeModel>();
    public ArrayList<SearchKeyword> keywordList = new ArrayList<SearchKeyword>();

    private int currentTab = TUTORIAL_TAB;
    private String currentIdx;
    private boolean isMore;

    //Youtube Search 관련
    private String currentSearchKeyworkd = "";
    private String nextToken;
    private boolean isEditTextFocus;
    private boolean isExistTutorialVideo = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        prefManager = WenotePreferenceManager.getInstance(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.youtube_activity, container);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setCancelable(false);


        mDbOpenHelper = new DbOpenHelper(getActivity());
        mDbOpenHelper.open(DataBases.CreateDB._SEARCH_KEYWORD_TABLE);

        setFindViewById();


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                softKeyboardHide(youtubeSearchEdit);
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        youtubeSearchIco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!svList.isEmpty() && isEditTextFocus){
                    youtubeSearchIco.setImageResource(R.drawable.ico_popup_video_search);
                    recyclerView.setAdapter(svAdapter);
                    softKeyboardHide(youtubeSearchEdit);
                }
            }
        });

        youtubeSearchEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditTextFocus =true;
                getKeyWordList(mDbOpenHelper.getAllSearchKeyword(prefManager.getUserId()));
                youtubeSearchIco.setImageResource(R.drawable.btn_popup_video_search_back);
                recyclerView.setAdapter(keywordAdapter);

                if(!recyclerView.isShown()){
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyResultLayout.setVisibility(View.GONE);
                }
            }
        });

        youtubeSearchEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        youtubeSearchEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                    if(youtubeSearchEdit.getText().toString().equals(""))
                        return false;

                    recyclerView.setAdapter(svAdapter);
                    searchYoutube(youtubeSearchEdit.getText().toString());
                    mDbOpenHelper.insert(new SearchKeyword(prefManager.getUserId(), currentSearchKeyworkd));

                }
                return false;
            }
        });
        youtubeSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "input count : " + Integer.toString(s.length()));
                if (s.length() != 0)
                    youtubeSearchClearIco.setVisibility(View.VISIBLE);
                else
                    youtubeSearchClearIco.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        youtubeSearchClearIco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youtubeSearchEdit.setText("");
                recyclerView.setVisibility(View.VISIBLE);
                emptyResultLayout.setVisibility(View.GONE);
                getKeyWordList(mDbOpenHelper.getAllSearchKeyword(prefManager.getUserId()));
                if(!isEditTextFocus) {
                    isEditTextFocus = true;
                    youtubeSearchIco.setImageResource(R.drawable.btn_popup_video_search_back);
                    recyclerView.setAdapter(keywordAdapter);
                    softKeyboardShow(youtubeSearchEdit);
                }
            }
        });

        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore(int currentPage) {
                Log.d(TAG, "onLoadMore");
                switch (currentTab){
                    case HISTORY_TAB :
                        if(isMore)
                            getVideoList(true);
                        break;
                    case TUTORIAL_TAB :
                        break;
                    case SEARCH_TAB :
                        if(currentSearchKeyworkd.length() != 0)
                            getYouTubeList(currentSearchKeyworkd);
                        break;
                }
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerViewOnItemClickListener(getActivity(), recyclerView, new RecyclerViewOnItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (currentTab){
                    case HISTORY_TAB :
                        //Todo 영상 공유
                        shearVideo(hvAdapter.getVideoId(position), hvAdapter.getVIdeoTitle(position), "Y");
                        break;
                    case TUTORIAL_TAB :
                        //Todo 영상 공유
                        shearVideo(tvAdapter.getVideoId(position), tvAdapter.getVIdeoTitle(position), "Y");
                        break;
                    case SEARCH_TAB :
                        if(isEditTextFocus)
                        {
                            Log.d(TAG, "recyclerView Keyword Clicked");
                            String keyword = keywordAdapter.getKeyword(position);
                            youtubeSearchEdit.setText(keyword);
                            recyclerView.setAdapter(svAdapter);
                            searchYoutube(keyword);
                            softKeyboardHide(youtubeSearchEdit);
                        }else{
                            //todo 영상 공유
                            shearVideo(svAdapter.getVideoId(position), svAdapter.getVIdeoTitle(position), "Y");
                        }
                        break;
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {

            }
        }));

    }

    @Override
    public void onStart() {
        if (KnowloungeApplication.isPhone){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        }else {
            getDialog().getWindow().setLayout((int) (320 * prefManager.getDensity()), (int) (540 * prefManager.getDensity()));
        }
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbOpenHelper.close();
    }

    private void setFindViewById() {

        // 탭 메뉴 관련 view
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        historyLayout = (LinearLayout) view.findViewById(R.id.history_layout);
        tutorialLayout = (LinearLayout) view.findViewById(R.id.tutorial_layout);
        searchLayout = (LinearLayout) view.findViewById(R.id.search_layout);
        historyIco = (ImageView) view.findViewById(R.id.history_ico);
        tutorialIco = (ImageView) view.findViewById(R.id.tutorial_ico);
        searchIco = (ImageView) view.findViewById(R.id.search_ico);
        cancelBtn = (TextView) view.findViewById(R.id.video_share_cancel);

        //빈페이지 관련 view
        emptyResultLayout = (LinearLayout) view.findViewById(R.id.empty_result_layout);
        emptyResultImg = (ImageView) view.findViewById(R.id.empty_result_img);
        emptyResultText = (TextView) view.findViewById(R.id.empty_result_text);

        //youtube Search bar 관련
        youtubeSearchLayout = (LinearLayout) view.findViewById(R.id.youtube_search_layout);
        youtubeSearchIco = (ImageView) view.findViewById(R.id.youtube_search_ico);
        youtubeSearchClearIco = (ImageView) view.findViewById(R.id.youtube_search_clear_btn);
        youtubeSearchEdit = (EditText) view.findViewById(R.id.youtube_search_edit);
        youtubeSearchText = (TextView) view.findViewById(R.id.youtube_search_text);

        tvAdapter = new TutorialVideoAdapter(getActivity(), tvList);
        hvAdapter = new HistoryVideoAdapter(getActivity(), hvList);
        svAdapter = new SearchYoutubeAdapter(getActivity(), svList);
        keywordAdapter = new SearchKeywordAdapter(getActivity(), keywordList);

        historyLayout.setOnClickListener(this);
        tutorialLayout.setOnClickListener(this);
        searchLayout.setOnClickListener(this);

        LinearLayoutManager glm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(glm);

        getVideoList(false);
        setShareVideo(currentTab);
    }

    private void setShareVideo(int selectedTab) {
        historyIco.setImageResource(R.drawable.btn_popup_video_history);
        tutorialIco.setImageResource(R.drawable.btn_popup_video_guide);
        searchIco.setImageResource(R.drawable.btn_popup_video_search);

        switch (selectedTab){
            case HISTORY_TAB :
                historyIco.setImageResource(R.drawable.btn_popup_video_history_on);
                youtubeSearchLayout.setVisibility(View.GONE);
                recyclerView.setAdapter(hvAdapter);
                hvAdapter.notifyDataSetChanged();
                break;
            case TUTORIAL_TAB :
                tutorialIco.setImageResource(R.drawable.btn_popup_video_guide_on);
                youtubeSearchLayout.setVisibility(View.GONE);
                recyclerView.setAdapter(tvAdapter);
                tvAdapter.notifyDataSetChanged();
                currentTab = TUTORIAL_TAB;
                break;
            case SEARCH_TAB :
                searchIco.setImageResource(R.drawable.btn_popup_video_search_on);
                youtubeSearchLayout.setVisibility(View.VISIBLE);
                currentTab = SEARCH_TAB;

                if(svList.isEmpty()){
                    isEditTextFocus = true;
                    youtubeSearchIco.setImageResource(R.drawable.btn_popup_video_search_back);
                    recyclerView.setAdapter(keywordAdapter);
                }else{
                    isEditTextFocus = false;
                    youtubeSearchIco.setImageResource(R.drawable.ico_popup_video_search);
                    recyclerView.setAdapter(svAdapter);
                }

                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.history_layout :
                if(currentTab == HISTORY_TAB)
                    return;

                currentTab = HISTORY_TAB;
                setShareVideo(HISTORY_TAB);
                checkEmptyData(HISTORY_TAB);
                break;
            case R.id.tutorial_layout :
                if(currentTab == TUTORIAL_TAB)
                    return;

                currentTab = TUTORIAL_TAB;
                setShareVideo(TUTORIAL_TAB);
                checkEmptyData(TUTORIAL_TAB);
                break;
            case R.id.search_layout :
                if(currentTab == SEARCH_TAB)
                    return;
                getKeyWordList(mDbOpenHelper.getAllSearchKeyword(prefManager.getUserId()));
                currentTab = SEARCH_TAB;
                setShareVideo(SEARCH_TAB);
                break;
        }
        softKeyboardHide(youtubeSearchEdit);

    }

    private void shearVideo(String videoId, String title, String videoType){
        Log.d(TAG, videoId);
        EventBus.get().post(new VideoIdEvent(videoId, title, videoType));
        getDialog().dismiss();
    }

    private void checkEmptyData(int tab) {
        boolean isEmpty = false;
        switch (tab) {
            case HISTORY_TAB:
                isEmpty = hvList.isEmpty();
                emptyResultText.setText(getResources().getText(R.string.videosharing_nohistory));
                emptyResultImg.setImageResource(R.drawable.thumb_popup_history);
                break;
            case TUTORIAL_TAB :
                isEmpty = tvList.isEmpty();
                emptyResultText.setText(getResources().getText(R.string.videosharing_nohistory));
                emptyResultImg.setImageResource(R.drawable.thumb_popup_history);
                break;
            case SEARCH_TAB :
                isEmpty = svList.isEmpty();
                emptyResultText.setText(getResources().getText(R.string.global_search_noresult));
                emptyResultImg.setImageResource(R.drawable.thumb_popup_search);
                break;
        }
        if (isEmpty){
            emptyResultLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            emptyResultLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void searchYoutube(String keyword) {
        nextToken = "";
        svList.clear();
        currentSearchKeyworkd = keyword;
        youtubeSearchIco.setImageResource(R.drawable.ico_popup_video_search);
        isEditTextFocus = false;
        getYouTubeList(keyword);
    }

    private void softKeyboardShow(View view) {
        Log.d(TAG,"keyboardShow");
        InputMethodManager mgr = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        mgr.showSoftInput(youtubeSearchEdit, InputMethodManager.SHOW_FORCED);
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }
    private void softKeyboardHide(View view) {
        try {
            view.requestFocus();
            InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getKeyWordList(ArrayList<SearchKeyword> allSearchKeyword) {
        int i=0;
        keywordList.clear();
        for(SearchKeyword data :allSearchKeyword) {
            keywordList.add(data);
            if(++i == KEYWORD_MAX) {
                return;
            }
        }

    }
    private void getVideoList(final boolean moreFlag) {

        RequestParams params = new RequestParams();

        params.put("tutorial","1");
        if(moreFlag)
            params.put("idx",currentIdx);
        params.put("rows","8");

        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();
        RestClient.getWithCookie("magicbox/list.json",masterCookie, checksumCookie, params,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "send message success.. response : " + obj.toString());
                try {
                    if(obj.getString("result").equals("0"))
                    {
                        if(!moreFlag)
                            hvList.clear();

                        JSONArray tl = obj.getJSONObject("map").has("tutorial_list") ? obj.getJSONObject("map").getJSONArray("tutorial_list") : null;
                        JSONArray cl = obj.getJSONObject("map").has("contents_list") ? obj.getJSONObject("map").getJSONArray("contents_list") : null;
                        if(!isExistTutorialVideo && tl != null) {
                            for (int i = 0; i < tl.length(); i++) {
                                JSONObject data = tl.getJSONObject(i);
                                VideoModel videoData = new VideoModel();
                                videoData.setTitle(data.has("title") ? data.getString("title") : "");
                                videoData.setSeqNo(data.has("seqno") ? data.getString("seqno") : "");
                                videoData.setUserNo(data.has("userno") ? data.getString("userno") : "");
                                videoData.setTypeFlag(data.has("typeflag") ? data.getString("typeflag") : "");
                                videoData.setMediaKey(data.has("mediakey") ? data.getString("mediakey") : "");
                                videoData.setThumbNail(data.has("thumbnail") ? data.getString("thumbnail") : "");
                                videoData.setcDateTime(data.has("cdatetime") ? data.getString("cdatetime") : "");
                                videoData.setUpdateTime(data.has("updatetime") ? data.getString("updatetime") : "");
                                tvList.add(videoData);
                            }
                            isExistTutorialVideo  = true;
                        }
                        isMore = obj.getJSONObject("map").has("ismore") ? (obj.getJSONObject("map").getString("ismore").equals("1") ? true : false) : false;
                        if(cl != null) {
                            for (int i = 0; i < cl.length(); i++) {
                                JSONObject data = cl.getJSONObject(i);
                                VideoModel videoData = new VideoModel();
                                videoData.setTitle(data.has("title") ? data.getString("title") : "");
                                videoData.setSeqNo(data.has("seqno") ? data.getString("seqno") : "");
                                videoData.setUserNo(data.has("userno") ? data.getString("userno") : "");
                                videoData.setTypeFlag(data.has("typeflag") ? data.getString("typeflag") : "");
                                videoData.setMediaKey(data.has("mediakey") ? data.getString("mediakey") : "");
                                videoData.setThumbNail(data.has("thumbnail") ? data.getString("thumbnail") : "");
                                videoData.setcDateTime(data.has("cdatetime") ? data.getString("cdatetime") : "");
                                videoData.setUpdateTime(data.has("updatetime") ? data.getString("updatetime") : "");
                                currentIdx = data.has("idx") ? data.getString("idx") : "" ;
                                hvList.add(videoData);
                            }
                        }
                        if(currentTab == TUTORIAL_TAB)
                            tvAdapter.notifyDataSetChanged();
                        else
                            hvAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if(throwable instanceof IOException) {
                }
            }
        } );

    }
    private void getYouTubeList(String keyworkd) {
        Log.d(TAG, "getYouTubeList");

        RestClient.getYouTubeList("search?"
                + "type=video&part=snippet&q=" + keyworkd
                + "&key=AIzaSyARoLjxV75LyhGMnHLzXYI2-Eh7lMvKUjc&maxResults=20&videoEmbeddable=true&pageToken="+nextToken+"&safeSearch=none", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    parsingJSonData(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    private void parsingJSonData(JSONObject jsonObject) throws JSONException{
        Log.d("YouTubeList",jsonObject.toString());
        JSONArray arr = jsonObject.getJSONArray("items");
        String videoid, title, imgUrl, date, channelTitle;
        int addCount = 0;

        for(int i=0; i < arr.length(); i++){
            JSONObject data = arr.getJSONObject(i);
            String kind = data.getJSONObject("id").getString("kind");
            if(kind.equals("youtube#video")){
                videoid = data.getJSONObject("id").getString("videoId");
            }else{
                continue;
            }

            title = data.getJSONObject("snippet").getString("title");
            date = data.getJSONObject("snippet").getString("publishedAt").substring(0,10);
            imgUrl = data.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url");
            channelTitle = data.getJSONObject("snippet").getString("channelTitle");
            svList.add(new YouTubeModel(videoid, title, imgUrl, date,channelTitle));
            addCount++;
        }

        if(jsonObject.has("nextPageToken")) {
            nextToken = jsonObject.getString("nextPageToken");
        }

        if(arr.length()!=0 && addCount == 0)
        {
            getYouTubeList(currentSearchKeyworkd);
            return;
        }
        svAdapter.notifyDataSetChanged();
        checkEmptyData(SEARCH_TAB);
    }
}
