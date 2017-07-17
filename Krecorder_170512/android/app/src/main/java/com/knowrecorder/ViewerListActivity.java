package com.knowrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crashlytics.android.Crashlytics;
import com.facebook.login.LoginManager;
//import com.fenchtose.tooltip.Tooltip;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.OpenCourse.API.Models.Video;
import com.knowrecorder.OpenCourse.API.Models.VideoCount;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.Utils.E;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.develop.opencourse.ocimport.OCImportTask;
import com.knowrecorder.transform.CircleBitmapTransform;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.EnumSet;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ViewerListActivity extends AppCompatActivity implements E{

    private static final int USER_THUMBNAIL = 1;
    private static final int VIDEO_THUMBNAIL = 2;
    private final static int MAX_MENU_COUNT = 7;
    private final int MAX_VIDEO = 30;
    private ArrayList<VideoData> videoDatas;
    private GridView gridView;
    private GridAdapter adapter;
    private Spinner spinner;
    private LinearLayout[] menuList = new LinearLayout[MAX_MENU_COUNT];
    private enum enumMenu {
        All,
        Math,
        Science,
        Language,
        Social,
        Art,
        Others
    }
    private boolean loading = false;
    Retrofit retrofit;
    ApiEndPointInterface api;
    private TextView tvCategory;
    private ProgressBar progressBar;
    private int scrollState;
    private int currentMenu = 0;
    private OCImportTask ocImportTask = null;
    private String[] spinnerArrays = new String[] {"most_popular", "recently", "name"};

    private String resultOrder = "recently";
    private boolean isSearchResult;
    private String searchText;
    private LinearLayout searchResultBox;
    private TextView tvSearchResults;
    private RelativeLayout gridViewLayout;
    private RelativeLayout searchNotFoundLayout;

    private View divider;
    private Tracker mTracker;
    private int totalListCnt;

    //todo 창하쓰
    //private Tooltip orderMenu = null;
    //todo 창하쓰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KnowRecorderApplication application = (KnowRecorderApplication) getApplication();
        mTracker = application.getDefaultTracker();

        setContentView(R.layout.activity_viewer_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getString(R.string.opencourse_back));
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(ServerInfo.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiEndPointInterface.class);

        searchResultBox = (LinearLayout) findViewById(R.id.search_result_box);
        tvSearchResults = (TextView) findViewById(R.id.tv_search_results);

        gridViewLayout = (RelativeLayout) findViewById(R.id.grid_wrapper);
        searchNotFoundLayout = (RelativeLayout) findViewById(R.id.search_not_found_layout);
        divider = (View) findViewById(R.id.category_divider);

        gridView = (GridView) findViewById(R.id.grid);
        tvCategory = (TextView) findViewById(R.id.tv_category);

        spinner = (Spinner) findViewById(R.id.spinner_order);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        resultOrder = "most_popular";
                        break;

                    case 1:
                        resultOrder = "recently";
                        break;

                    case 2:
                        resultOrder = "name";
                        break;
                    case 3:
                        resultOrder = "";
                        break;
                }

                refreshResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        videoDatas = new ArrayList<>();
        adapter = new GridAdapter(this, R.layout.video_view_in_grid, videoDatas);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                AccessToken accessToken = AccessToken.getCurrentAccessToken();
//                if (accessToken == null) {
//                    showLoginDialog();
//                } else {

                if(checkDate(videoDatas.get(position).date) == DATE.EARLY) {
                    Toast.makeText(ViewerListActivity.this, R.string.old_data, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isOnline()) {

                    final ProgressDialog progressDialog = new ProgressDialog(ViewerListActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMax(100);
                    progressDialog.setMessage(getString(R.string.opencourse_download_step1));
                    progressDialog.setTitle(getString(R.string.please_wait));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ocImportTask!= null) {
                                ocImportTask.threadCancel();
                                ocImportTask = null;
                            }
                            dialog.dismiss();
                        }
                    });
                    progressDialog.setProgressDrawable(getDrawableToRes(R.drawable.progress_drawable));
                    progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button negativeButton = progressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);

                            negativeButton.setTextColor(Color.parseColor("#a0c81e"));
                            negativeButton.setTypeface(null, Typeface.BOLD);
//                            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
                        }
                    });
                    progressDialog.show();
                    //todo 다운로드 작업중.....
//                    importTask = new OpenCourseImportTaskV2(ViewerListActivity.this, progressDialog, videoDatas.get(position).id, videoDatas.get(position).title);
//                    importTask.execute();
                    ocImportTask = new OCImportTask(ViewerListActivity.this, progressDialog, videoDatas.get(position).id, videoDatas.get(position).title);
                    ocImportTask.execute();

                } else {
                    Toast.makeText(ViewerListActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        menuList[0] = (LinearLayout) findViewById(R.id.all);
        menuList[1] = (LinearLayout) findViewById(R.id.math);
        menuList[2] = (LinearLayout) findViewById(R.id.science);
        menuList[3] = (LinearLayout) findViewById(R.id.language);
        menuList[4] = (LinearLayout) findViewById(R.id.social);
        menuList[5] = (LinearLayout) findViewById(R.id.art);
        menuList[6] = (LinearLayout) findViewById(R.id.others);

        for (int i = 0; i < MAX_MENU_COUNT; i++) {
            menuList[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activeSelectedMenu((LinearLayout) v);
                }
            });
        }

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                ViewerListActivity.this.scrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean needToLoading = false;
                int count = totalItemCount - visibleItemCount;
                if(isSearchResult) {
                    if(totalListCnt-visibleItemCount > firstVisibleItem) {
                        needToLoading = true;
                    }
                }else
                {
                    if(firstVisibleItem >= count){
                        needToLoading = true;
                    }
                }

                if (!loading && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    if (needToLoading && totalItemCount != 0 && !loading) {
                        loading = true;

                        Call<List<Video>> getVideos;
                        if (isSearchResult) {    // 검색 결과를 표시해주는 리스트 일 경우
                            if (currentMenu == 0) {
                                getVideos = api.searchVideos(searchText, ServerInfo.API_KEY, resultOrder, firstVisibleItem+visibleItemCount, 10, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
                            } else {
                                getVideos = api.searchVideos(searchText, ServerInfo.API_KEY, currentMenu, resultOrder, firstVisibleItem+visibleItemCount, 10, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
                            }
                        } else {    // 검색 결과가 아닌 카테고리 선택에 따른 결과일 경우
                            if (currentMenu > 0) {
                                getVideos = api.getVideos(totalItemCount, 10, ServerInfo.API_KEY, currentMenu, resultOrder, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
                            } else {
                                getVideos = api.getVideos(totalItemCount, 10, ServerInfo.API_KEY, resultOrder, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
                            }
                        }

                        getVideos.enqueue(new Callback<List<Video>>() {
                            @Override
                            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                                List<Video> videos = response.body();

                                for(Video video : videos) {
                                    VideoData videoData = new VideoData();
                                    videoData.author = video.getAuthor();
                                    videoData.hit = Integer.toString(video.getHits());
                                    videoData.thumbnailUrl = video.getThumbnail();
                                    videoData.title = video.getTitle();
                                    videoData.userThumbnail = video.getAuthorThumbnail();
                                    videoData.playtime = formatSeconds(video.getPlaytime() * 1000);
                                    videoData.id = video.getId();

                                    videoDatas.add(videoData);
                                }
                                adapter.notifyDataSetChanged();
                                loading = false;
                            }

                            @Override
                            public void onFailure(Call<List<Video>> call, Throwable t) {

                            }
                        });
                    }
                }
            }
        });

        progressBar.setVisibility(View.VISIBLE);
    }

    private void activeSelectedMenu(LinearLayout menu) {
        for (int i = 0; i < MAX_MENU_COUNT; i ++) {
            menuList[i].setBackground(getDrawableToRes(R.drawable.opencourse_category_bg));
            ((TextView) menuList[i].getChildAt(1)).setTextColor(Color.parseColor("#808080"));
        }

        ((ImageView) menuList[enumMenu.All.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_all));
        ((ImageView) menuList[enumMenu.Math.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_math));
        ((ImageView) menuList[enumMenu.Science.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_science));
        ((ImageView) menuList[enumMenu.Language.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_language));
        ((ImageView) menuList[enumMenu.Social.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_social));
        ((ImageView) menuList[enumMenu.Art.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_art));
        ((ImageView) menuList[enumMenu.Others.ordinal()].getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_others));

        if (menu == menuList[enumMenu.All.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_all_s));
            tvCategory.setText(getString(R.string.opencourse_menu_all));

            currentMenu = enumMenu.All.ordinal();

        } else if (menu == menuList[enumMenu.Math.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_math_s));
            tvCategory.setText(getString(R.string.opencourse_menu_math));

            currentMenu = enumMenu.Math.ordinal();

        } else if (menu == menuList[enumMenu.Science.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_science_s));
            tvCategory.setText(getString(R.string.opencourse_menu_science));

            currentMenu = enumMenu.Science.ordinal();

        } else if (menu == menuList[enumMenu.Language.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_language_s));
            tvCategory.setText(getString(R.string.opencourse_menu_language));

            currentMenu = enumMenu.Language.ordinal();

        } else if (menu == menuList[enumMenu.Social.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_social_s));
            tvCategory.setText(getString(R.string.opencourse_menu_social));

            currentMenu = enumMenu.Social.ordinal();

        } else if (menu == menuList[enumMenu.Art.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_art_s));
            tvCategory.setText(getString(R.string.opencourse_menu_art));

            currentMenu = enumMenu.Art.ordinal();

        } else if (menu == menuList[enumMenu.Others.ordinal()]) {

            ((ImageView) menu.getChildAt(0)).setImageDrawable(getDrawableToRes(R.drawable.ico_viewer_others_s));
            tvCategory.setText(getString(R.string.opencourse_menu_others));

            currentMenu = enumMenu.Others.ordinal();
        }

        if (menu == menuList[enumMenu.All.ordinal()])
            menu.setBackground(getDrawableToRes(R.drawable.opencourse_category_selected));
        else
            menu.setBackground(getDrawableToRes(R.drawable.opencourse_category_selected_top_border));
        ((TextView) menu.getChildAt(1)).setTextColor(Color.parseColor("#a0c828"));

        /*
        if (isSearchResult) {
            api.videoCount(searchText, currentMenu, ServerInfo.API_KEY, resultOrder.length() == 0 ? "" :  KnowRecorderApplication.getLanguage()).enqueue(new Callback<VideoCount>() {
                @Override
                public void onResponse(Call<VideoCount> call, Response<VideoCount> response) {
                    String count = response.body().getCount();

                    if (Integer.parseInt(count) == 0) {
                        tvSearchResults.setText(Html.fromHtml(getString(R.string.i_viewer_no_result_found)
                                .replace("#KEYWORD#", "<b>" + searchText + "</b>")
                        ));

                        gridViewLayout.setVisibility(View.INVISIBLE);
                        searchNotFoundLayout.setVisibility(View.VISIBLE);
                        divider.setVisibility(View.INVISIBLE);
                        spinner.setVisibility(View.INVISIBLE);
                    } else {
                        tvSearchResults.setText(Html.fromHtml(getString(R.string.i_search_result_ok)
                                .replace("#KEYWORD#", "<b>" + searchText + "</b>")
                                .replace("#COUNT#", "<b>" + count + "</b>")
                        ));
                        gridViewLayout.setVisibility(View.VISIBLE);
                        searchNotFoundLayout.setVisibility(View.GONE);
                        divider.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<VideoCount> call, Throwable t) {

                }
            });
        }*/
        refreshResults();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO: 여기서 검색처리 하면 됨 (query로 검색어가 날라온다)

                Log.d("onQueryTextSubmit", query);
                searchText = query;
                searchResultBox.setVisibility(View.VISIBLE);
                tvCategory.setVisibility(View.GONE);

                Call<VideoCount> videoCount = api.videoCount(query, ServerInfo.API_KEY, resultOrder.length() == 0 ? "" :  KnowRecorderApplication.getLanguage());
                videoCount.enqueue(new Callback<VideoCount>() {
                    @Override
                    public void onResponse(Call<VideoCount> call, Response<VideoCount> response) {
                        String count = response.body().getCount();

                        if (Integer.parseInt(count) == 0) {
                            tvSearchResults.setText(Html.fromHtml(getString(R.string.i_viewer_no_result_found)
                                    .replace("#KEYWORD#", "<b>" + searchText + "</b>")
                            ));
                            gridViewLayout.setVisibility(View.INVISIBLE);
                            searchNotFoundLayout.setVisibility(View.VISIBLE);
                            divider.setVisibility(View.INVISIBLE);
                            spinner.setVisibility(View.INVISIBLE);
                        } else {
                            tvSearchResults.setText(Html.fromHtml(getString(R.string.i_search_result_ok)
                                    .replace("#KEYWORD#", "<b>" + searchText + "</b>")
                                    .replace("#COUNT#", "<b>" + count + "</b>")
                            ));

                            gridViewLayout.setVisibility(View.VISIBLE);
                            searchNotFoundLayout.setVisibility(View.GONE);
                            divider.setVisibility(View.VISIBLE);
                            spinner.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoCount> call, Throwable t) {

                    }
                });

                refreshResults();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem menuItem = menu.findItem(R.id.action_search);

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isSearchResult = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isSearchResult = false;
                searchResultBox.setVisibility(View.GONE);
                tvCategory.setVisibility(View.VISIBLE);

                if (gridViewLayout.getVisibility() != View.VISIBLE)
                    gridViewLayout.setVisibility(View.VISIBLE);

                if (searchNotFoundLayout.getVisibility() != View.GONE)
                    searchNotFoundLayout.setVisibility(View.GONE);

                if (divider.getVisibility() != View.VISIBLE)
                    divider.setVisibility(View.VISIBLE);

                if (spinner.getVisibility() != View.VISIBLE)
                    spinner.setVisibility(View.VISIBLE);

                refreshResults();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GridAdapter extends ArrayAdapter<VideoData> {
        private Context context;
        private int layoutResourceId;
        private ArrayList<VideoData> videoDatas = new ArrayList<>();
        private LayoutInflater inflater;

        public GridAdapter(Context context, int layoutResourceId, ArrayList<VideoData> videoDatas) {
            super(context, layoutResourceId, videoDatas);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.videoDatas = videoDatas;
            this.inflater = ((Activity) context).getLayoutInflater();
        }

        public void setVideoDatas(ArrayList<VideoData> videoDatas) {
            this.videoDatas = videoDatas;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View view = convertView;

            if (convertView == null) {
                view = inflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.author = (TextView) view.findViewById(R.id.author);
                holder.hit = (TextView) view.findViewById(R.id.hit);
                holder.playtime = (TextView) view.findViewById(R.id.txt_playtime);
                holder.title = (TextView) view.findViewById(R.id.title);
                holder.userThumbnail = (ImageView) view.findViewById(R.id.user_thumbnail);
                holder.videoThumbnail = (ImageView) view.findViewById(R.id.video_thumbnail);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            //RECORDER-227
            if(videoDatas != null && videoDatas.size() >0) {
                VideoData videoData = videoDatas.get(position);
                holder.title.setText(videoData.title);
                holder.author.setText(videoData.author);
                holder.hit.setText(videoData.hit);
                holder.playtime.setText(videoData.playtime);

                Glide.with(context).load(videoData.thumbnailUrl + "?api_key=" + ServerInfo.API_KEY)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.videoThumbnail);
                Glide.with(context).load(videoData.userThumbnail)
                        .bitmapTransform(new CircleBitmapTransform(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.userThumbnail);
            }

            return view;
        }
    }

    private class VideoData {
        int id;
        int totalCnt;
        String thumbnailUrl;    // 비디오 썸네일
        String title;           // 비디오 제목
        String author;          // 비디오 작성자 이름
        String hit;             // 조회수
        String userThumbnail;   // 사용자 썸네임 이미지
        String playtime;        // 재생 시간
        String date;            //날짜
    }

    private class ViewHolder {
        ImageView videoThumbnail;
        ImageView userThumbnail;
        TextView title;
        TextView author;
        TextView hit;
        TextView playtime;
    }

    public String formatSeconds(long millis) {
        long s = (millis / 1000) % 60;
        long m = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void loadVideosByCategoryId(int categoryId) {
        videoDatas.clear();
        adapter.notifyDataSetChanged();

        Call<List<Video>> getVideos;
        if (categoryId == 0) {
            getVideos = api.getVideos(0, 10, ServerInfo.API_KEY);
        } else {
            getVideos = api.getVideos(0, 10, ServerInfo.API_KEY, categoryId);
        }
        getVideos.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                List<Video> videos = response.body();

                for(Video video : videos) {

                    VideoData videoData = new VideoData();
                    videoData.author = video.getAuthor();
                    videoData.hit = Integer.toString(video.getHits());
                    videoData.thumbnailUrl = video.getThumbnail();
                    videoData.title = video.getTitle();
                    videoData.userThumbnail = video.getAuthorThumbnail();
                    videoData.playtime = formatSeconds(video.getPlaytime() * 1000);
                    videoData.id = video.getId();

                    videoDatas.add(videoData);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                Toast.makeText(ViewerListActivity.this, R.string.opencourse_respond_fail, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showLoginDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_sign_in, (ViewGroup) findViewById(R.id.layout_root));

        LinearLayout loginWithFacebook = (LinearLayout) layout.findViewById(R.id.login_with_facebook);
        ImageButton btnClose = (ImageButton) layout.findViewById(R.id.btn_close);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);

        final AlertDialog loginDialog = builder.create();
        loginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(ViewerListActivity.this, Arrays.asList("public_profile", "email"));
                loginDialog.dismiss();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
            }
        });

        loginDialog.show();
        loginDialog.getWindow().setLayout((int) PixelUtil.getInstance().convertDpToPixel(360), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void refreshResults() {
        videoDatas.clear();

        final Call<List<Video>> getVideos;
        if (isSearchResult) {    // 검색 결과를 표시해주는 리스트 일 경우
            if (currentMenu == 0) {
                getVideos = api.searchVideos(searchText, ServerInfo.API_KEY, resultOrder, 0, 10, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
            } else {
                getVideos = api.searchVideos(searchText, ServerInfo.API_KEY, currentMenu, resultOrder, 0, 10, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
            }
        } else {    // 검색 결과가 아닌 카테고리 선택에 따른 결과일 경우
            if (currentMenu > 0) {
                getVideos = api.getVideos(0, 10, ServerInfo.API_KEY, currentMenu, resultOrder, resultOrder.length() == 0 ? "" :  KnowRecorderApplication.getLanguage());
            } else {
                getVideos = api.getVideos(0, 10, ServerInfo.API_KEY, resultOrder, resultOrder.length() == 0 ? "" : KnowRecorderApplication.getLanguage());
            }
        }

        getVideos.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                List<Video> videos = response.body();

                for(Video video : videos) {
                    VideoData videoData = new VideoData();
                    videoData.author = video.getAuthor();
                    videoData.hit = Integer.toString(video.getHits());
                    videoData.thumbnailUrl = video.getThumbnail();
                    videoData.title = video.getTitle();
                    videoData.userThumbnail = video.getAuthorThumbnail();
                    videoData.playtime = formatSeconds(video.getPlaytime() * 1000);
                    videoData.id = video.getId();
                    videoData.date = video.getPublishedDate();


                    if(isSearchResult){
                        if(video.getTotalcnt() != null){
                            totalListCnt = video.getTotalcnt();
                        }else{
                            totalListCnt = 0;
                        }
                        if (totalListCnt == 0) {
                            tvSearchResults.setText(Html.fromHtml(getString(R.string.i_viewer_no_result_found)
                                                                          .replace("#KEYWORD#", "<b>" + searchText + "</b>")
                            ));
                            gridViewLayout.setVisibility(View.INVISIBLE);
                            searchNotFoundLayout.setVisibility(View.VISIBLE);
                            divider.setVisibility(View.INVISIBLE);
                            spinner.setVisibility(View.INVISIBLE);
                        } else {
                            tvSearchResults.setText(Html.fromHtml(getString(R.string.i_search_result_ok)
                                                                          .replace("#KEYWORD#", "<b>" + searchText + "</b>")
                                                                          .replace("#COUNT#", "<b>" + totalListCnt + "</b>")
                            ));
                            gridViewLayout.setVisibility(View.VISIBLE);
                            searchNotFoundLayout.setVisibility(View.GONE);
                            divider.setVisibility(View.VISIBLE);
                            spinner.setVisibility(View.VISIBLE);
                        }
                    }
                    videoDatas.add(videoData);
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                Toast.makeText(ViewerListActivity.this, R.string.opencourse_respond_fail, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("ViewerListActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private Drawable getDrawableToRes(int resID){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resID, getTheme());
        } else {
            return getResources().getDrawable(resID);
        }
    }

    private int checkDate(String date){
        if(date == null){ return DATE.SAME; }

        date = date.substring(0,10); //서버형식이 2017-06-20T00:00:00 인데 중간에 요일이 Tus등으로 인식가능해야 정규포멧 변환이 가능한데 T(화요일) 한글자라 통으로 쓰기는 불가.
        Date videoDate = null;
        Date standardDate = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            videoDate = format.parse(date);
            standardDate = format.parse("2017-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(videoDate == null || standardDate == null)
            return DATE.SAME;
        return videoDate.compareTo(standardDate);
    }
}
