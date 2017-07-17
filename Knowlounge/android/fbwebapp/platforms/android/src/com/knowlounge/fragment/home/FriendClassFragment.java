package com.knowlounge.fragment.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.util.TextUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by we160303 on 2016-06-11.
 */
public class FriendClassFragment extends RoomListFragment implements MainActivity.reNetworkConnected {

    private String TAG = "FriendClassFragment";

    // API 콜 관련
    private String URL_FRIEND_MAIN_LIST = "main/friends.json";
    private String URL_FRIEND_ROOM_LIST = "room/list/friends.json";

    private SectionedRecyclerViewAdapter sectionAdapter;

    private ArrayList<Room> mFriendClassDataSet;
    private String mFriendClassIdx = "";

    private LinearLayout classLoadingLayout;
    private AutofitRecyclerView recyclerView;

    private boolean isClick = true;
    private long mLastClickTime = 0;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
    }


    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        classLoadingLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        classLoadingLayout = (LinearLayout)view.findViewById(R.id.class_loading_layout);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        mFriendClassDataSet = new ArrayList<Room>();

        sectionAdapter.addSection("friend_class", new ClassSection(isTablet ? R.drawable.ico_list_friend : R.drawable.ico_list_friend_color, getResources().getString(R.string.main_friends_public), mFriendClassDataSet, 1));

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                getFriendClassList();
            }
        });

        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(sectionAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER :
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_FOOTER :
                        return 3;
                    default :
                        if (isTablet)
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
        Log.d(TAG, "onResume");
        super.onResume();

        getFriendClassList();

        MainActivity._instance.addNetworkReconnected(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).removeNetworkReconnected(this);
        //MainActivity._instance.removeNetworkReconnected(this);
    }


    @Override
    public void reConnectedNetwork() {
        if(classLoadingLayout.isShown())
            //getAllFriendRoomList();
            getFriendClassList();

    }


    //HomeSection
    class ClassSection extends StatelessSection {
        int titleResource;
        String title;
        List<Room> list;
        int sectionNum;

        public ClassSection(int titleResource, String title, List<Room> list, int sectionNum) {
            super(R.layout.cardview_header,R.layout.cardview_footer, R.layout.cardview_room_item);

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
                    params.bottomMargin = hasFooter() ? (int)0 : (int)(18*prefManager.getDensity());
                }
                itemHolder.rootView.setLayoutParams(params);
            }

            String classThumbnail = list.get(position).roomThumbnail;
            String classTitle = list.get(position).roomTitle;
            int  classCount = list.get(position).roomCount;

            String userThumbnail = list.get(position).creatorUserThumb;
            String userName = list.get(position).creatorUserNm;
            String roomlimit = list.get(position).userLimitCnt;

            final String roomId = list.get(position).roomId;

            //set default info
            itemHolder.room_people_count.setText("~/"+roomlimit);
            itemHolder.room_view_count.setText(classCount+"");
            itemHolder.room_user_name.setText(userName);
            itemHolder.room_title.setText(classTitle);

//            if(!isTablet) {
//                if (list.size() != 0 && position == list.size() - 1 && list.get(list.size() - 1).more.equals("1"))
//                    itemHolder.cardview_underline.setVisibility(View.GONE);
//                else
//                    itemHolder.cardview_underline.setVisibility(View.VISIBLE);
//            }
            //subroom 인지 체크
            if(roomId.indexOf("_") > -1){
                itemHolder.isSubroom.setVisibility(View.VISIBLE);
            } else {
                itemHolder.isSubroom.setVisibility(View.GONE);
            }

            //myRoom 체크
            if(list.get(position).isMyroom){
                itemHolder.room_delete_btn.setVisibility(View.VISIBLE);
            } else {
                itemHolder.room_delete_btn.setVisibility(View.GONE);
            }

            //Class Thumbnail 처리
            Uri thumbnailUri = Uri.parse(classThumbnail);
            Glide.with(getContext())
                    .load(thumbnailUri)
                    .error(getContext().getResources().getIdentifier("thumbnail_0" + (Integer.parseInt(list.get(position).seqNo)%4+1), "drawable", getContext().getPackageName()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(itemHolder.room_thumbnail);

            //User Thumbnail 처리
            Glide.with(getContext())
                    .load(userThumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(getContext().getResources().getIdentifier("img_userlist_default0" + (Integer.parseInt(list.get(position).seqNo)%2+1), "drawable", getContext().getPackageName()))
                    .bitmapTransform(new CircleTransformTemp(getContext()))
                    .into(itemHolder.img_creator_thumbnail);


            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                try {
                    if (!KnowloungeApplication.isNetworkConnected)
                        return;

                    if (!isClick) {
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
                    try {
                        ((MainActivity) getActivity()).enterRoom(roomCode);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IndexOutOfBoundsException indexOut) {
                    AppLog.d(AppLog.CLASSLIST_TAG, "IndexOutOfBoundsException");
                    //getAllFriendRoomList();
                    getFriendClassList();
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
                if (isTablet)
                    headerHolder.rootView.setPadding(0, (int) (20 * prefManager.getDensity()), 0, 0);
                else
                    headerHolder.rootView.setPadding(0, (int)(18 * prefManager.getDensity()), 0, 0);
            } else {
                headerHolder.rootView.setPadding(0, 0, 0, 0);
            }

            headerHolder.cardviewHeaderMoreBtn.setVisibility(View.GONE);
            headerHolder.cardviewHeaderTitleImg.setImageResource(titleResource);
            headerHolder.cardviewHeaderTitleText.setText(title);

            if (list.size() == 0) {
                headerHolder.classEmptyLayout.setVisibility(View.VISIBLE);
                switch (sectionNum) {
                    case 1:
                        headerHolder.classEmptyContent.setText(getResources().getString(R.string.main_friends_guide));
                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_friendslist);
                        headerHolder.classEmptyBtn.setVisibility(View.GONE);
                        break;
                }
            } else {
                headerHolder.classEmptyLayout.setVisibility(View.GONE);
            }

        }

        //ClassFooter

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return new FooterViewHolder(view);
        }


        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.cardview_seemore_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!KnowloungeApplication.isNetworkConnected)
                        return;
                    moreFriendClassList();
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
        private final TextView room_user_name;
        private final ImageView img_creator_thumbnail;
        private final TextView room_title;
        private final TextView room_people_count;
        private final TextView room_view_count;

//        private final View cardview_underline;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            isSubroom = (LinearLayout) rootView.findViewById(R.id.is_subroom);
            room_thumbnail = (ImageView) rootView.findViewById(R.id. room_thumbnail);
            room_delete_btn = (ImageView) rootView.findViewById(R.id. room_delete_btn);
            room_user_name = (TextView) rootView.findViewById(R.id. room_user_name);
            img_creator_thumbnail = (ImageView) rootView.findViewById(R.id. img_creator_thumbnail);
            room_title = (TextView) rootView.findViewById(R.id. room_title);
            room_people_count = (TextView) rootView.findViewById(R.id. room_people_count);
            room_view_count = (TextView) rootView.findViewById(R.id. room_view_count);

//            if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE)
//                cardview_underline = (View) rootView.findViewById(R.id.cardview_underline);
//            else
//                cardview_underline = null;
        }
    }


    /**
     * Friend list API 호출하여 친구 리스트를 렌더링함
     */
    public void getFriendClassList() {
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

        if(TextUtils.isEmpty(friendsIdStr)){
            sectionAdapter.getSection("friend_class").setHasFooter(false);
            sectionAdapter.notifyDataSetChanged();
            classLoadingLayout.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        String rows = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? CLASS_VIEW_ROWS_PHONE : CLASS_VIEW_ROWS_TABLET;

        new MainApiCommand()
                .command("getFriendClassList")
                .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
                .friends(friendsIdStr)
                .rows(rows)
                .event(new ApiCommand.ApiCallEvent<JsonObject>() {
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
                                        Toast.makeText(getActivity(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                                        mSwipeRefreshLayout.setRefreshing(false);
                                        classLoadingLayout.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onNext(JsonObject object) {
                                        Log.d(TAG, "[RxJava] onNext");

                                        mFriendClassDataSet.clear();
                                        sectionAdapter.getSection("friend_class").setHasFooter(false);

                                        int apiResult = object.get("result").getAsInt();
                                        if (apiResult == 0) {
                                            JsonObject resultMap = object.getAsJsonObject("map");
                                            Log.d(TAG, "Retrofit & OkHttp & RxJava API Call result : " + resultMap.toString());

                                            JsonArray friendsRoomList = resultMap.getAsJsonArray("friends");
                                            String more = resultMap.get("friends_more").getAsString();
                                            if (more.equals("1"))
                                                sectionAdapter.getSection("friend_class").setHasFooter(true);

                                            renderRoomList(friendsRoomList, mFriendClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_B, more);
                                            mFriendClassIdx = friendsRoomList.size() == 0 ? null : friendsRoomList.get(friendsRoomList.size() - 1).getAsJsonObject().get("idx").getAsString();

                                            sectionAdapter.notifyDataSetChanged();
                                            classLoadingLayout.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }).execute();
    }


    public void moreFriendClassList() {
        Log.d(TAG, "getFriendClassList - friends : " + prefManager.getFriendsIdStr());
        RequestParams params = new RequestParams();

        DbOpenHelper mDbOpenHelper = new DbOpenHelper(getActivity());
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);
        final String friendsIdStr = mDbOpenHelper.getAllFriendId();
        mDbOpenHelper.close();

        if (TextUtils.isEmpty(friendsIdStr)) {
            sectionAdapter.getSection("friend_class").setHasFooter(false);
            return;
        }

        String rows = "10";
        if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
            rows = CLASS_VIEW_ROWS_PHONE;
        } else {
            rows = CLASS_VIEW_ROWS_TABLET;
        }

        String flag = "next";

        new RoomApiCommand()
                .command("getFriendClassList")
                .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
                .friends(friendsIdStr)
                .idx(mFriendClassIdx)
                .rows(rows)
                .flag(flag)
                .event(new ApiCommand.ApiCallEvent<JsonObject>() {
                    @Override
                    public void onApiCall(ApiCommand<? extends JsonObject> command, Observable<JsonObject> observer) {
                        observer.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "[RxJava] onCompleted");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "[RxJava] onError");
                                        e.printStackTrace();
                                        Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onNext(JsonObject object) {
                                        Log.d(TAG, "[RxJava] onNext");
                                        sectionAdapter.getSection("friend_class").setHasFooter(false);

                                        int apiResult = object.get("result").getAsInt();
                                        if (apiResult == 0) {
                                            JsonArray friendsRoomList = object.getAsJsonArray("list");
                                            Log.d(TAG, "Retrofit & OkHttp & RxJava API Call result : " + friendsRoomList.toString());

                                            String more = object.get("more").getAsString();
                                            if (more.equals("1"))
                                                sectionAdapter.getSection("friend_class").setHasFooter(true);

                                            renderRoomList(friendsRoomList, mFriendClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_B, more);

                                            sectionAdapter.notifyDataSetChanged();
                                            mFriendClassIdx = friendsRoomList.size() == 0  ? null : friendsRoomList.get(friendsRoomList.size() - 1).getAsJsonObject().get("idx").getAsString();
                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }).execute();
    }
}
