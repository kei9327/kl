package com.knowlounge.fragment.home;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.customview.AutofitRecyclerView;
import com.knowlounge.fragment.RoomListFragment;
import com.knowlounge.model.Room;
import com.knowlounge.network.restful.command.ApiCommand;
import com.knowlounge.network.restful.command.MainApiCommand;
import com.knowlounge.network.restful.command.RoomApiCommand;
import com.knowlounge.network.restful.func.ListApiRetry;
import com.knowlounge.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import com.knowlounge.sectionedrecyclerviewadapter.StatelessSection;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by we160303 on 2016-06-11.
 */
public class MyClassFragment extends RoomListFragment implements MainActivity.reNetworkConnected {
    private String TAG = "MyClassFragment";

    // API 콜 관련
    private String URL_ALL_CLASS_LIST = "main/myclass.json";
    private String URL_MY_CLASS_LIST = "room/list/my.json";
    private String URL_HISTORY_CLASS_LIST = "room/list/recent.json";
    private String URL_BOOKMARK_CLASS_LIST = "room/list/bookmark.json";

    private SectionedRecyclerViewAdapter sectionAdapter;

    private ArrayList<Room> mMyClassDataSet;
    private ArrayList<Room> mHistoryClassDataSet;
    private ArrayList<Room> mBookmarkClassDataSet;

    private String mMyClassIdx;
    private String mHistoryClassIdx;
    private String mBookmarkClassIdx;


    private LinearLayout classLoadingLayout;
    private AutofitRecyclerView recyclerView;

    private boolean isClick = true;
    private int retryCount;

    private long mLastClickTime = 0;
    private boolean isRemoveRoom = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        rootContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        classLoadingLayout.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        Log.d(TAG, "onCreateView");

        if(prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET)
            isTablet = true;

        classLoadingLayout = (LinearLayout) view.findViewById(R.id.class_loading_layout);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        mMyClassDataSet = new ArrayList<Room>();
        mHistoryClassDataSet = new ArrayList<Room>();
        mBookmarkClassDataSet = new ArrayList<Room>();

        svrFlag = getResources().getString(R.string.svr_flag);
        svrHost = getResources().getString(getResources().getIdentifier("svr_host_" + svrFlag, "string", getActivity().getPackageName()));

        sectionAdapter.addSection("my_class",new ClassSection(isTablet? R.drawable.ico_list_my : R.drawable.ico_list_my_color, getResources().getString(R.string.main_myclass_mine),mMyClassDataSet, 1));
        sectionAdapter.addSection("history_class",new ClassSection(isTablet? R.drawable.ico_list_history : R.drawable.ico_list_history_color, getResources().getString(R.string.main_myclass_history),mHistoryClassDataSet, 2));
        sectionAdapter.addSection("bookmark_class",new ClassSection(isTablet? R.drawable.ico_list_favorite : R.drawable.ico_list_favorite_color, getResources().getString(R.string.main_myclass_bookmark),mBookmarkClassDataSet, 3));

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                getAllMyRoomList();
            }
        });



        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(sectionAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_FOOTER:
                        return 3;
                    default:
                        if(isTablet)
                            return 1;
                        else
                            return 3;
                }
            }
        });

        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(sectionAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        retryCount = 0;

        getAllMyRoomList();

        MainActivity._instance.addNetworkReconnected(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).removeNetworkReconnected(this);
    }

    @Override
    public void reConnectedNetwork() {
        if(classLoadingLayout.isShown())
            getAllMyRoomList();
    }


    //HomeSection
    class ClassSection extends StatelessSection {
        int titleResource;
        String title;
        List<Room> list;
        int sectionNum;

        public ClassSection(int titleResource, String title, List<Room> list, int sectionNum) {
            super(R.layout.cardview_header, R.layout.cardview_footer, R.layout.cardview_room_item);

            this.titleResource = titleResource;
            this.title = title;
            this.list = list;
            this.sectionNum = sectionNum;
        }

        @Override
        public int getContentItemsTotal() {
            return list.size();
        }

        //ClassItem
        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            Log.d("count", "getItemViewHolder");
            return new ItemViewHolder(view);
        }
        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            Log.d("count", "onBindItemViewHolder");
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            //TODO 폰일경우 중간 line
            if(!isTablet){
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemHolder.rootView.getLayoutParams();
                if (position != list.size()-1) {
                    params.bottomMargin = (int)(18*prefManager.getDensity());
                } else {
                    int bottomMargin = hasFooter() ? 0 : (int)(18 * prefManager.getDensity());
                    params.bottomMargin = bottomMargin;
                }
                itemHolder.rootView.setLayoutParams(params);
            }
            Room room = list.get(position);
            String classThumbnail = room.roomThumbnail;
            String classTitle = room.roomTitle;
            int classCount = room.roomCount;

            String userThumbnail = room.creatorUserThumb;
            String userName = room.creatorUserNm;
            String roomlimit = room.userLimitCnt;

            String passwdFlag = room.passwdFlag;

            final String roomId = room.roomId;

            //set default info
            itemHolder.room_people_count.setText("~/" + roomlimit);
            itemHolder.room_view_count.setText(classCount + "");
            itemHolder.room_user_name.setText(userName);
            itemHolder.room_title.setText(classTitle);
//            if(!isTablet) {
//                if (list.size() != 0 && position == list.size() - 1 && list.get(list.size() - 1).more.equals("1"))
//                    itemHolder.cardview_underline.setVisibility(View.GONE);
//                else
//                    itemHolder.cardview_underline.setVisibility(View.VISIBLE);
//            }

            //subroom 인지 체크
            if (roomId.indexOf("_") > -1) {
                itemHolder.isSubroom.setVisibility(View.VISIBLE);
            } else {
                itemHolder.isSubroom.setVisibility(View.GONE);
            }

            if(sectionNum == 3){
                itemHolder.room_favorite_btn.setVisibility(View.VISIBLE);
            }else{
                itemHolder.room_favorite_btn.setVisibility(View.GONE);
            }

            //myRoom 체크
            if (list.get(position).isMyroom) {
                itemHolder.room_delete_btn.setVisibility(View.VISIBLE);
            } else {
                itemHolder.room_delete_btn.setVisibility(View.GONE);
            }

            //Class Thumbnail 처리
            if(android.text.TextUtils.equals(passwdFlag, "0")) {
                Uri thumbnailUri = Uri.parse(classThumbnail);
                Glide.with(getContext())
                        .load(thumbnailUri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(getContext().getResources().getIdentifier("thumbnail_0" + (Integer.parseInt(list.get(position).seqNo) % 4 + 1), "drawable", getContext().getPackageName()))
                        .into(itemHolder.room_thumbnail);
            } else {
                Glide.with(getContext())
                        .load(getResources().getIdentifier("thumbnail_secret", "drawable", getContext().getPackageName()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .into(itemHolder.room_thumbnail);
            }
            //User Thumbnail 처리
            Glide.with(getContext())
                    .load(userThumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(getContext().getResources().getIdentifier("img_userlist_default0" + (Integer.parseInt(list.get(position).seqNo) % 2 + 1), "drawable", getContext().getPackageName()))
                    .bitmapTransform(new CircleTransformTemp(getContext()))
                    .into(itemHolder.img_creator_thumbnail);



            itemHolder.room_delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
                    builder.setMessage(getResources().getString(R.string.main_popup_deleteclass)).setCancelable(true)
                            .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    removeRoom(roomId, position);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog confirm = builder.create();
                    confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    confirm.setCanceledOnTouchOutside(true);
                    confirm.setTitle(getResources().getString(R.string.global_popup_title));

                    confirm.show();
                }
            });

            itemHolder.room_favorite_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
                    builder.setMessage(getResources().getString(R.string.confirm_delbookmark)).setCancelable(true)
                            .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    delBookMark(roomId);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog confirm = builder.create();
                    confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    confirm.setCanceledOnTouchOutside(true);
                    confirm.setTitle(getResources().getString(R.string.global_popup_title));

                    confirm.show();
                }
            });

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                try {

                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    if(!isClick) {
                        return;
                    }


                    isClick = false;
                    itemHolder.rootView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isClick = true;
                        }
                    },500);
                    recyclerView.setScrollAble(false);

                    String roomId = list.get(position).roomId;
                    String roomCode = list.get(position).seqNo;
                    String passwd = "";
                    String tokenStr = "roomid=" + roomId + "&passwd=" + passwd;

                    //enterRoom(roomCode, tokenStr);
                    ((MainActivity) getActivity()).enterRoom(roomCode);

                }catch(IndexOutOfBoundsException indexOut){
                    AppLog.d(AppLog.CLASSLIST_TAG, "IndexOutOfBoundsException");
                    getAllMyRoomList();
                }
                }
            });
        }

        //ClassHeader
        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            Log.d("count","getHeaderViewHolder");
            return new HeaderViewHolder(view);
        }
        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            Log.d("count","onBindHeaderViewHolder");
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            //TODO header padding
            if (sectionNum == 1) {
                if(isTablet)
                    headerHolder.rootView.setPadding(0, (int)(20*prefManager.getDensity()), 0, 0);
                else
                    headerHolder.rootView.setPadding(0, (int)(18*prefManager.getDensity()), 0, 0);
            } else {
                headerHolder.rootView.setPadding(0, 0, 0, 0);
            }


            headerHolder.cardviewHeaderMoreBtn.setVisibility(View.GONE);

            headerHolder.cardviewHeaderTitleImg.setImageResource(titleResource);
            headerHolder.cardviewHeaderTitleText.setText(title);

            if(list.size() == 0){
                headerHolder.classEmptyLayout.setVisibility(View.VISIBLE);
                switch(sectionNum){
                    case 1:
                        headerHolder.classEmptyContent.setText(getResources().getString(R.string.myclass_mine_guide));
                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_mylist);
                        headerHolder.classEmptyBtn.setText(getResources().getString(R.string.global_new));
                        break;
                    case 2:
                        headerHolder.classEmptyContent.setText(getResources().getString(R.string.myclass_history_guide));
                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_historylist);
                        headerHolder.classEmptyBtn.setVisibility(View.GONE);
                        break;
                    case 3:
                        headerHolder.classEmptyContent.setText(getResources().getString(R.string.myclass_bookmark_guide));
                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_favoritelist);
                        headerHolder.classEmptyBtn.setVisibility(View.GONE);
                        break;
                }
            }else{
                headerHolder.classEmptyLayout.setVisibility(View.GONE);
            }

            headerHolder.classEmptyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    switch (sectionNum){
                        case 1:
                            //createRoom();
                            if(!KnowloungeApplication.isNetworkConnected)
                                return;
                            ((MainActivity) getActivity()).createRoom();
                            return;
                    }
                }
            });
        }

        //ClassFooter

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            Log.d("count","getFooterViewHolder");
            return new FooterViewHolder(view);
        }
        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            Log.d("count","onBindFooterViewHolder");
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;

            footerViewHolder.cardview_seemore_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    switch (sectionNum){
                        case 1: getMyClassList(); return;
                        case 2: getHistoryClassList(); return;
                        case 3: getBookmarkClassList(); return;
                    }
                }
            });

        }


        public void removeRoom(String roomId, final int position) {
            new RoomApiCommand()
                .command("removeRoom")
                .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
                .roomId(roomId)
                .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                    @Override
                    public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                        observer.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "[RxJava / removeRoom] onCompleted");
                                        getAllMyRoomList();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "[RxJava / removeRoom] onError");
                                        e.printStackTrace();
                                        Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onNext(JsonObject object) {
                                        Log.d(TAG, "[RxJava / removeRoom] onNext");

                                        int apiResult = object.get("result").getAsInt();
                                        if(apiResult == 0) {
                                            list.remove(position);
                                            //getHomeRoomList();
                                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_class_deleted), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_class_delfail), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }).execute();
        }


        // 즐겨찾기 해제
        public void delBookMark(String roomId) {
            String url = "bm/remove.json";
            RequestParams params = new RequestParams();
            params.put("roomid", roomId);
            params.put("userno", prefManager.getUserNo());
            RestClient.postWithCookie(url, prefManager.getUserCookie(), prefManager.getChecksumCookie(), params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        int apiResult = response.getInt("result");
                        if(apiResult == 0) {
                            Toast.makeText(getActivity(),getResources().getString(R.string.toast_bookmark_del) , Toast.LENGTH_SHORT).show();
                            getAllMyRoomList();
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_bookmark_delfail), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_bookmark_delfail2), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "Remove room onFailure " + statusCode);
                }

            });
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder{

        private final View footerRootView;
        private final Button cardview_seemore_btn;

        public FooterViewHolder(View view) {
            super(view);
            footerRootView = view;
            cardview_seemore_btn = (Button) footerRootView.findViewById(R.id.cardview_seemore_btn);
        }
    }
    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView cardviewHeaderTitleImg;
        private final TextView cardviewHeaderTitleText;
        private final TextView cardviewHeaderMoreBtn;

        private final LinearLayout classEmptyLayout;
        private final TextView classEmptyContent;
        private final ImageView classEmptyImg;
        private final Button classEmptyBtn;

        public HeaderViewHolder(View view) {
            super(view);
            rootView = view;
            cardviewHeaderTitleImg = (ImageView) view.findViewById(R.id.cardview_header_title_img);
            cardviewHeaderTitleText = (TextView) view.findViewById(R.id.cardview_header_title_text);
            cardviewHeaderMoreBtn = (TextView) view.findViewById(R.id.cardview_header_more_btn);

            classEmptyLayout = (LinearLayout) view.findViewById(R.id.class_empty_layout);
            classEmptyContent = (TextView) view.findViewById(R.id.class_empty_content);
            classEmptyImg = (ImageView) view.findViewById(R.id.class_empty_img);
            classEmptyBtn = (Button) view.findViewById(R.id.class_empty_btn);
        }
    }
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;

        private final LinearLayout isSubroom;
        private final ImageView room_thumbnail;
        private final ImageView room_delete_btn;
        private final ImageView room_favorite_btn;
        private final TextView room_user_name;
        private final ImageView img_creator_thumbnail;
        private final TextView room_title;
        private final TextView room_people_count;
        private final TextView room_view_count;


        private final LinearLayout room_root_view;
//        private final View cardview_underline;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            isSubroom = (LinearLayout) rootView.findViewById(R.id.is_subroom);
            room_thumbnail = (ImageView) rootView.findViewById(R.id.room_thumbnail);
            room_delete_btn = (ImageView) rootView.findViewById(R.id.room_delete_btn);
            room_favorite_btn = (ImageView) rootView.findViewById(R.id.room_favorite_btn);
            room_user_name = (TextView) rootView.findViewById(R.id.room_user_name);
            img_creator_thumbnail = (ImageView) rootView.findViewById(R.id.img_creator_thumbnail);
            room_title = (TextView) rootView.findViewById(R.id.room_title);
            room_people_count = (TextView) rootView.findViewById(R.id.room_people_count);
            room_view_count = (TextView) rootView.findViewById(R.id.room_view_count);

            if(isTablet) {
                room_root_view = (LinearLayout) rootView.findViewById(R.id.room_root_view);
//                cardview_underline = null;
            }else {
                room_root_view = null;
//                cardview_underline = (View) rootView.findViewById(R.id.cardview_underline);
            }
        }
    }


    //Room 정보 받아오기
    /**
     * My room list API 호출하여 나의 Live 리스트를 렌더링함
     */
    public void getAllMyRoomList() {
        Log.d(TAG, "getAllMyRoomList");

        if (!KnowloungeApplication.isNetworkConnected) {
            Toast.makeText(getActivity(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
            if(mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        recyclerView.setScrollAble(true);
        classLoadingLayout.setVisibility(View.VISIBLE);

        String rows = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? CLASS_VIEW_ROWS_PHONE_BASIC : CLASS_VIEW_ROWS_TABLET_BASIC;

        new MainApiCommand()
            .command("getMyClassList")
            .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
            .rows(rows)
            .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                @Override
                public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                    observer.retryWhen(new RestCallRetry(3, 1000))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JsonObject>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "[RxJava] onCompleted");
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "[RxJava] onError");
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    classLoadingLayout.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onNext(JsonObject object) {
                                    Log.d(TAG, "[RxJava] onNext");

                                    mMyClassDataSet.clear();
                                    mHistoryClassDataSet.clear();
                                    mBookmarkClassDataSet.clear();

                                    int apiResult = object.get("result").getAsInt();
                                    if(apiResult == 0) {
                                        JsonArray myClassList = object.getAsJsonObject("map").getAsJsonArray("my");
                                        JsonArray historyClassList = object.getAsJsonObject("map").getAsJsonArray("recent");
                                        JsonArray bookmarkClassList = object.getAsJsonObject("map").getAsJsonArray("bookmark");

                                        String myMore =  object.getAsJsonObject("map").get("my_more").getAsString();
                                        String historyMore =  object.getAsJsonObject("map").get("recent_more").getAsString();
                                        String bookmarkMore =  object.getAsJsonObject("map").get("bookmark_more").getAsString();

                                        // 나의 수업 섹션

                                        sectionAdapter.getSection("my_class").setHasFooter(myMore.equals("1") ? true : false);
                                        renderRoomList(myClassList, mMyClassDataSet, true, GlobalCode.CODE_ROOM_TYPE_A, myMore);
                                        mMyClassIdx = myClassList.size() == 0 ? null : myClassList.get(myClassList.size() - 1).getAsJsonObject().get("idx").getAsString();

                                        // 이전에 참여했던 수업 섹션
                                        sectionAdapter.getSection("history_class").setHasFooter(historyMore.equals("1") ? true : false);
                                        renderRoomList(historyClassList, mHistoryClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_D, historyMore);
                                        mHistoryClassIdx = historyClassList.size() == 0 ? null : historyClassList.get(historyClassList.size() - 1).getAsJsonObject().get("idx").getAsString();

                                        // 즐겨찾기한 수업 섹션
                                        Log.d(TAG, bookmarkClassList.toString());
                                        sectionAdapter.getSection("bookmark_class").setHasFooter(bookmarkMore.equals("1") ? true : false);
                                        renderRoomList(bookmarkClassList, mBookmarkClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_B, bookmarkMore);
                                        mBookmarkClassIdx = bookmarkClassList.size() == 0 ? null : bookmarkClassList.get(bookmarkClassList.size() - 1).getAsJsonObject().get("idx").getAsString();

                                        mSwipeRefreshLayout.setRefreshing(false);
                                        classLoadingLayout.setVisibility(View.GONE);
                                        sectionAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }).execute();
    }


    public void getMyClassList() {

        String idx = mMyClassIdx;
        String rows = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? CLASS_VIEW_ROWS_PHONE_BASIC : CLASS_VIEW_ROWS_TABLET;
        String flag = "next";

        new RoomApiCommand()
            .command("getMyClassList")
            .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
            .idx(idx)
            .rows(rows)
            .flag(flag)
            .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                @Override
                public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                    observer.retryWhen(new ListApiRetry(3, 1000))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JsonObject>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "[RxJava / getMyClassList] onCompleted");
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "[RxJava / getMyClassList] onError");
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    classLoadingLayout.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onNext(JsonObject object) {
                                    Log.d(TAG, "[RxJava / getMyClassList] onNext");

                                    int apiResult = object.get("result").getAsInt();
                                    if(apiResult == 0) {
                                        JsonArray list = object.getAsJsonArray("list");
                                        String more = object.get("more").getAsString();

                                        Log.d(TAG, "Retrofit & OkHttp & RxJava API Call result : " + list.toString());

                                        if(more.equals("1"))
                                            sectionAdapter.getSection("my_class").setHasFooter(true);
                                        else
                                            sectionAdapter.getSection("my_class").setHasFooter(false);

                                        for(JsonElement elem : list) {
                                            JsonObject obj = elem.getAsJsonObject();
                                            String seqNo = obj.get("seqno").getAsString();
                                            String roomId = obj.get("roomid").getAsString();
                                            String roomTitle = obj.get("title").getAsString();
                                            int roomCnt = obj.get("readcnt").getAsInt();
                                            String userNm = obj.get("usernm").getAsString();
                                            String userThumb = obj.has("thumbnail") ? obj.get("thumbnail").getAsString() : "";
                                            mMyClassIdx = obj.get("idx").getAsString();
                                            String userLimitCnt = obj.get("user_limit_cnt").getAsString();
                                            String passwdFlag = obj.get("passwd").getAsString();

                                            String roomThumbnail = svrHost + "/data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
                                            mMyClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb,userLimitCnt,true,GlobalCode.CODE_ROOM_TYPE_A, more, passwdFlag));
                                        }
                                        sectionAdapter.notifyDataSetChanged();

                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }).execute();
    }


    private void getHistoryClassList() {
        RequestParams params = new RequestParams();
        params.put("idx", mHistoryClassIdx);

        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            params.put("rows", CLASS_VIEW_ROWS_PHONE_BASIC);
        }else{
            params.put("rows", CLASS_VIEW_ROWS_TABLET);
        }

        params.put("flag","next");

        String masterCookie = prefManager.getUserCookie();
        String checksum = prefManager.getChecksumCookie();
        RestClient.getWithCookie(URL_HISTORY_CLASS_LIST, masterCookie, checksum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String resultCode = response.getString("result");

                    if("0".equals(resultCode)) {

                        JSONArray visitRoomList = response.getJSONArray("list");
                        String more = response.getString("more");
                        sectionAdapter.getSection("history_class").setHasFooter(more.equals("1") ? true : false);

                        int len = visitRoomList.length();
                        if(len > 0) {
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = visitRoomList.getJSONObject(i);

                                Log.d(TAG, "room : " + obj.toString());

                                String seqNo = obj.getString("seqno");
                                String roomId = obj.getString("roomid");
                                String roomTitle = obj.getString("title");
                                int roomCnt = obj.getInt("readcnt");
                                String userNm = obj.getString("usernm");
                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
                                mHistoryClassIdx = obj.getString("idx");
                                String userLimitCnt = obj.getString("user_limit_cnt");

                                String roomThumbnail = svrHost + "/data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";

                                String passwdFlag = obj.getString("passwd");

                                mHistoryClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb,userLimitCnt, false, GlobalCode.CODE_ROOM_TYPE_D, more, passwdFlag));
                            }
                            sectionAdapter.notifyDataSetChanged();
                        } else {

                        }
                    } else {
                        // TODO : REST 예외처리
                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get visit room list onFailure");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if(throwable instanceof IOException) {
                    if(rootContext != null)
                        Toast.makeText(rootContext, rootContext.getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getBookmarkClassList() {

        RequestParams params = new RequestParams();
        params.put("idx", mBookmarkClassIdx);

        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            params.put("rows", CLASS_VIEW_ROWS_PHONE_BASIC);
        }else{
            params.put("rows", CLASS_VIEW_ROWS_TABLET);
        }

        params.put("flag","next");

        String masterCookie = prefManager.getUserCookie();
        String checksum = prefManager.getChecksumCookie();
        RestClient.getWithCookie(URL_BOOKMARK_CLASS_LIST, masterCookie, checksum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String resultCode = response.getString("result");
                    Log.d(TAG, "bookmark result = " + response.toString());
                    if ("0".equals(resultCode)) {

                        JSONArray bookmarkList = response.getJSONArray("list");

                        String more = response.getString("more");

                        sectionAdapter.getSection("bookmark_class").setHasFooter(more.equals("1") ? true : false);

                        int len = bookmarkList.length();
                        if (len > 0) {
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = bookmarkList.getJSONObject(i);

                                Log.d(TAG, "room : " + obj.toString());

                                String seqNo = obj.getString("seqno");
                                String roomId = obj.getString("roomid");
                                String roomTitle = obj.getString("title");
                                int roomCnt = obj.getInt("readcnt");
                                String userNm = obj.getString("usernm");
                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
                                mBookmarkClassIdx = obj.getString("idx");
                                String userLimitCnt = obj.getString("user_limit_cnt");

                                String roomThumbnail = svrHost + "/data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
                                String passwdFlag = obj.getString("passwd");

                                mBookmarkClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb,userLimitCnt, false, GlobalCode.CODE_ROOM_TYPE_B, more, passwdFlag));
                                sectionAdapter.notifyDataSetChanged();
                            }
                        } else {
                        }
                    } else {
                        // TODO : REST 예외처리
                    }

                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get visit room list onFailure");
                mBookmarkClassDataSet.add(new Room("", "", "", "", 0, "", "", ""));  // 헤더 들어갈 자리..
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onFailure - statusCode : " + statusCode);
                Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
                if(throwable instanceof IOException) {
                    if(rootContext != null)
                        Toast.makeText(rootContext, rootContext.getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


    private static class RestCallRetry implements
            Func1<Observable<? extends Throwable>, Observable<?>> {

        private final int maxRetries;
        private final int retryDelayMillis;
        private int retryCount;

        public RestCallRetry(final int maxRetries, final int retryDelayMillis) {
            this.maxRetries = maxRetries;
            this.retryDelayMillis = retryDelayMillis;
            this.retryCount = 0;
        }

        @Override
        public Observable<?> call(Observable<? extends Throwable> attempts) {
            return attempts
                    .flatMap(new Func1<Throwable, Observable<?>>() {
                        @Override
                        public Observable<?> call(Throwable throwable) {
                            Log.d(AppLog.AUTH_TAG, "RestAPI call retrying error: " + throwable);

                            if (++retryCount < maxRetries) {
                                // When this Observable calls onNext, the original
                                // Observable will be retried (i.e. re-subscribed).
                                Log.d(AppLog.AUTH_TAG, "RestAPI call retrying with delay: " + retryCount);

                                if (throwable instanceof HttpException) {
                                    HttpException response = (HttpException) throwable;
                                    int code = response.code();

                                    if (code == 502 || code == 503 || code == 504) {
                                        return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                                    } else {
                                        return Observable.error(throwable);
                                    }
                                }

                                return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                            }

                            // Max retries hit. Just pass the error along.
                            return Observable.error(throwable);
                        }
                    });
        }
    }
}
