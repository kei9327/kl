package com.knowlounge.fragment.home;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.gson.JsonObject;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.ProfileInitActivity;
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
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.util.logger.AppLog;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by we160303 on 2016-06-11.
 */
public class HomeClassFragment extends RoomListFragment implements MainActivity.reNetworkConnected {

    private String TAG = "HomeClassFragment";

//    public static final String CLASS_VIEW_ROWS_PHONE_BASIC = "2";
//    public static final String CLASS_VIEW_ROWS_PHONE = "3";
//    public static final String CLASS_VIEW_ROWS_TABLET_BASIC = "3";
//    public static final String CLASS_VIEW_ROWS_TABLET = "9";

    private String URL_HOME_ROOM_LIST = "main/home.json";

    private SectionedRecyclerViewAdapter sectionAdapter;

    private ArrayList<Room> mMyClassDataSet;
    private ArrayList<Room> mFriendClassDataSet;
    private ArrayList<Room> mSchoolClassDataSet;
    private ArrayList<Room> mPublicClassDataSet;

    private AutofitRecyclerView recyclerView;
    private LinearLayout classLoadingLayout;

    private boolean isClick = true;

    private int retryCount;
    private boolean listLoadingComplet;
    private long mLastClickTime = 0;
    private boolean isRemoveRoom = true;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        classLoadingLayout = (LinearLayout) view.findViewById(R.id.class_loading_layout);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        mMyClassDataSet = new ArrayList<Room>();
        mFriendClassDataSet = new ArrayList<Room>();
        mSchoolClassDataSet = new ArrayList<Room>();
        mPublicClassDataSet = new ArrayList<Room>();

        sectionAdapter.addSection("my_class",new HomeClassSection(isTablet? R.drawable.ico_list_my : R.drawable.ico_list_my_color, getResources().getString(R.string.main_myclass),mMyClassDataSet,1));
        sectionAdapter.addSection("friend_class",new HomeClassSection(isTablet? R.drawable.ico_list_friend : R.drawable.ico_list_friend_color, getResources().getString(R.string.main_friends),mFriendClassDataSet,2));
        sectionAdapter.addSection("featured Class",new HomeClassSection(isTablet? R.drawable.ico_list_recommendedpublic : R.drawable.ico_list_recommendedpublic_color, getResources().getString(R.string.main_public),mPublicClassDataSet,3));
        sectionAdapter.addSection("school_class",new HomeClassSection(isTablet? R.drawable.ico_list_school : R.drawable.ico_list_school_color, getResources().getString(R.string.main_school),mSchoolClassDataSet,4));

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                getHomeClassList();
            }
        });

        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(sectionAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
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
        Log.d(TAG, "onResume");
        retryCount = 0;
        listLoadingComplet = false;

        getHomeClassList();

        MainActivity._instance.addNetworkReconnected(this);
        Log.d(TAG, "onResume끝");
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).removeNetworkReconnected(this);
        //MainActivity._instance.removeNetworkReconnected(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void reConnectedNetwork() {
        if(classLoadingLayout.isShown())
            //getHomeRoomList();
            getHomeClassList();
    }

    //HomeSection
    class HomeClassSection extends StatelessSection {
        int titleResource;
        String title;
        List<Room> list;
        int sectionNum;

        public HomeClassSection(int titleResource, String title, List<Room> list, int sectionNum) {
            super(R.layout.cardview_header, R.layout.cardview_room_item);

            this.titleResource = titleResource;
            this.title = title;
            this.list = list;
            this.sectionNum = sectionNum;
        }

        @Override
        public int getContentItemsTotal() {
            return list.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            //TODO 폰일경우 중간 line
            if(!isTablet){
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemHolder.rootView.getLayoutParams();
                if(position != list.size()-1){
                    params.bottomMargin = (int)(18*prefManager.getDensity());
                }else{
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

            //subroom 인지 체크
            if (roomId.indexOf("_") > -1) {
                itemHolder.isSubroom.setVisibility(View.VISIBLE);
            } else {
                itemHolder.isSubroom.setVisibility(View.GONE);
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
                        .error(getContext().getResources().getIdentifier("thumbnail_0" + (Integer.parseInt(list.get(position).seqNo) % 4 + 1), "drawable", getContext().getPackageName()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
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

                    } catch(Exception e) {
                        AppLog.d(AppLog.CLASSLIST_TAG, e.getMessage());
                        //getHomeRoomList();
                        getHomeClassList();
                    }
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
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
                        headerHolder.classEmptyContent.setText(getResources().getString(R.string.main_friends_guide));
                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_friendslist);
                        headerHolder.classEmptyBtn.setVisibility(View.GONE);
                        break;
                    case 4:
                        if(prefManager.getMyUserType().equals("0") || prefManager.getMyeducation().equals("")){
                            headerHolder.classEmptyContent.setText(getResources().getString(R.string.main_school_setguide));
                            headerHolder.classEmptyBtn.setVisibility(View.VISIBLE);
                        }else if(!prefManager.getMyUserType().equals("0") && !prefManager.getMyeducation().equals("")){
                            headerHolder.classEmptyContent.setText(getResources().getString(R.string.main_school_guide));
                            headerHolder.classEmptyBtn.setVisibility(View.GONE);
                        }

                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_schoollist);
                        headerHolder.classEmptyBtn.setText(getResources().getString(R.string.main_school_setbtn));
                        break;
                }
            }else{
                headerHolder.classEmptyLayout.setVisibility(View.GONE);
            }

            headerHolder.cardviewHeaderMoreBtn.setVisibility(View.VISIBLE);
            headerHolder.cardviewHeaderMoreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getContext(), String.format("Clicked on more button from the header of Section %s",
//                            title),
//                            Toast.LENGTH_SHORT).show();
                    if (!KnowloungeApplication.isNetworkConnected)
                        return;

                    MainActivity._instance.mViewPager.setCurrentItem(sectionNum);
                }
            });

            headerHolder.classEmptyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!KnowloungeApplication.isNetworkConnected)
                        return;

                    switch (sectionNum) {
                        case 1:
                            //createRoom();
                            if(!KnowloungeApplication.isNetworkConnected)
                                return;
                            ((MainActivity) getActivity()).createRoom();
                            return;
                        case 4:

                            if (android.text.TextUtils.isEmpty(prefManager.getMyeducation())) {
                                if (android.text.TextUtils.equals(prefManager.getMyUserType(), "0")) {
                                    Context ctx = getActivity().getApplicationContext();
                                    Intent intent = new Intent(ctx, ProfileInitActivity.class);
                                    startActivity(intent);
                                } else {
                                    ((MainActivity) getActivity()).openUserProfile();

                                }
                            }
//                            if (prefManager.getMyUserType().equals("0") && prefManager.getMyeducation().equals("")) {
//                                Context ctx = getActivity().getApplicationContext();
//                                Intent intent = new Intent(ctx, ProfileInitActivity.class);
//                                startActivity(intent);
//                            } else if (!prefManager.getMyUserType().equals("0") && prefManager.getMyeducation().equals("")) {
//                                Intent intent = new Intent(getActivity(), MainLeftNavActivity.class);
//                                intent.putExtra("school", "ok");
//                                startActivity(intent);
//                            }
                            return;
                    }
                }
            });
        }

        // 방 삭제
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
                                        getHomeClassList();
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

//        public void removeRoom(String roomId, final int position) {
//            String url = "room/remove.json";
//            RequestParams params = new RequestParams();
//            params.put("roomid", roomId);
//            RestClient.postWithCookie(url, prefManager.getUserCookie(), prefManager.getChecksumCookie(), params, new JsonHttpResponseHandler(){
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    try {
//                        int apiResult = response.getInt("result");
//                        if(apiResult == 0) {
//                            list.remove(position);
//                            //getHomeRoomList();
//                            getHomeClassList();
//                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_class_deleted), Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_class_delfail), Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_class_delfail), Toast.LENGTH_LONG).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Log.d(TAG, "Remove room onFailure " + statusCode);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                    super.onFailure(statusCode, headers, throwable, errorResponse);
//                    Log.d(TAG, "onFailure - statusCode : " + statusCode);
//                    Log.d(TAG, "onFailure - exception name : " + throwable.getClass().getSimpleName());
//                    if(throwable instanceof IOException) {
//                        Toast.makeText(getContext(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//            });
//        }

        // 수업 생성


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
        private final TextView room_user_name;
        private final ImageView img_creator_thumbnail;
        private final TextView room_title;
        private final TextView room_people_count;
        private final TextView room_view_count;

        private final LinearLayout room_root_view;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            isSubroom = (LinearLayout) rootView.findViewById(R.id.is_subroom);
            room_thumbnail = (ImageView) rootView.findViewById(R.id.room_thumbnail);
            room_delete_btn = (ImageView) rootView.findViewById(R.id.room_delete_btn);
            room_user_name = (TextView) rootView.findViewById(R.id.room_user_name);
            img_creator_thumbnail = (ImageView) rootView.findViewById(R.id.img_creator_thumbnail);
            room_title = (TextView) rootView.findViewById(R.id.room_title);
            room_people_count = (TextView) rootView.findViewById(R.id.room_people_count);
            room_view_count = (TextView) rootView.findViewById(R.id.room_view_count);

            if(isTablet) {
                room_root_view = (LinearLayout) rootView.findViewById(R.id.room_root_view);
            }else {
                room_root_view = null;
            }
        }
    }

    //Room 정보 받아오기
    /**
     * My room list API 호출하여 나의 Live 리스트를 렌더링함
     */
    public void getHomeClassList() {

        if (!KnowloungeApplication.isNetworkConnected) {
            Toast.makeText(getActivity(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
            if(mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        recyclerView.setScrollAble(true);
        classLoadingLayout.setVisibility(View.VISIBLE);

        DbOpenHelper mDbOpenHelper = new DbOpenHelper(getActivity());
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);
        String friendsIdStr = mDbOpenHelper.getAllFriendId();
        mDbOpenHelper.close();

        String rows = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? CLASS_VIEW_ROWS_PHONE_BASIC : CLASS_VIEW_ROWS_TABLET_BASIC;

        new MainApiCommand()
                .command("getHomeClassList")
                .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
                .friends(friendsIdStr)
                .rows(rows)
                .event(new HomeClassListApiCallEvent())
                .execute();
    }


    private class HomeClassListApiCallEvent implements ApiCommand.ApiCallEvent<JsonObject> {
        @Override
        public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
            observer.retryWhen(new ListApiRetry(3, 1000))
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


                        if (e instanceof HttpException) {
                            HttpException response = (HttpException) e;
                            int code = response.code();

                            // oAuth 인증 에러 처리
                            if (code >= 200 && code < 300) {
                                // success
                            } else if (code == 401) {
                                // unauthenticated
                            } else {
                            }

                        }
                        else if (e instanceof SocketTimeoutException) {
                            //Toast.makeText(getActivity(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();

                        }
                        else if (e instanceof IOException) {
                            // network error
                        }
                        else {
                            // unexpected error
                            //Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(getActivity(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                        classLoadingLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(JsonObject object) {
                        Log.d(TAG, "[RxJava] onNext");

                        mMyClassDataSet.clear();
                        mFriendClassDataSet.clear();
                        mPublicClassDataSet.clear();
                        mSchoolClassDataSet.clear();

                        int apiResult = object.get("result").getAsInt();
                        if(apiResult == 0) {
                            JsonObject resultMap = object.getAsJsonObject("map");
                            Log.d(TAG, "Retrofit & OkHttp & RxJava API Call result : " + resultMap.toString());

                            JsonArray myRoomList = resultMap.getAsJsonArray("my");
                            JsonArray friendsRoomList = resultMap.getAsJsonArray("friends");
                            JsonArray schoolRoomList = resultMap.getAsJsonArray("school");
                            JsonArray publicRoomList = resultMap.getAsJsonArray("sumpublic");

                            renderRoomList(myRoomList, mMyClassDataSet, true, GlobalCode.CODE_ROOM_TYPE_A, "1");
                            renderRoomList(friendsRoomList, mFriendClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_B, "1");
                            renderRoomList(schoolRoomList, mSchoolClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_C, "1");
                            renderRoomList(publicRoomList, mPublicClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_D, "1");

                            sectionAdapter.notifyDataSetChanged();
                            classLoadingLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            listLoadingComplet = true;

                        } else {
                            Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

}
