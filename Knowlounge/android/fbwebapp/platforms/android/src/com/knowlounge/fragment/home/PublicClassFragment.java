package com.knowlounge.fragment.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.util.logger.AppLog;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by we160303 on 2016-06-11.
 */
public class PublicClassFragment extends RoomListFragment implements MainActivity.reNetworkConnected {
    private String TAG = "PublicClassFragment";

    // API 콜 관련
    private String URL_All_PUBLIC_ROOM_LIST = "main/public.json";
    private String URL_PUBLIC_ROOM_LIST = "room/list/public.json";

    private SectionedRecyclerViewAdapter sectionAdapter;

    private ArrayList<Room> mEditorPickClassDataSet;
    private ArrayList<Room> mMostViewClassDataSet;
    private ArrayList<Room> mPublicClassDataSet;

    private String mPublicClassIdx;
    private LinearLayout classLoadingLayout;
    private AutofitRecyclerView recyclerView;

    private boolean isClick = true;
    private long mLastClickTime = 0;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        classLoadingLayout.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        Log.d(TAG,"onCreateView");

        classLoadingLayout = (LinearLayout) view.findViewById(R.id.class_loading_layout);
        sectionAdapter = new SectionedRecyclerViewAdapter();
        mEditorPickClassDataSet= new ArrayList<Room>();
        mMostViewClassDataSet = new ArrayList<Room>();
        mPublicClassDataSet = new ArrayList<Room>();

        sectionAdapter.addSection("editor_pick_class",new ClassSection(isTablet? R.drawable.ico_list_editorspick : R.drawable.ico_list_editorspick_color, getResources().getString(R.string.main_public_editor),mEditorPickClassDataSet,1));
        sectionAdapter.addSection("most_view_class",new ClassSection(isTablet? R.drawable.ico_list_mostviewed : R.drawable.ico_list_mostviewed_color, getResources().getString(R.string.main_public_most),mMostViewClassDataSet,2));
        sectionAdapter.addSection("public_class",new ClassSection(isTablet? R.drawable.ico_list_newpublic : R.drawable.ico_list_newpublic_color, getResources().getString(R.string.main_public_recent),mPublicClassDataSet,3));

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                getAllClassList();
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

        getAllClassList();

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
        if (classLoadingLayout.isShown())
            getAllClassList();
    }

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
                    params.bottomMargin = hasFooter()? 0 : (int)(18*prefManager.getDensity());
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
                    try {
                        //enterRoom(roomCode, tokenStr);
                        ((MainActivity) getActivity()).enterRoom(roomCode);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }catch(IndexOutOfBoundsException indexOut){
                    AppLog.d(AppLog.CLASSLIST_TAG, "IndexOutOfBoundsException");
                    getAllClassList();
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


            headerHolder.cardviewHeaderMoreBtn.setVisibility(View.GONE);
            headerHolder.cardviewHeaderTitleImg.setImageResource(titleResource);
            headerHolder.cardviewHeaderTitleText.setText(title);

        }

        //ClassFooter

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return new FooterViewHolder(view);
        }
        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            Log.d("footer_index_layout",footerViewHolder.getLayoutPosition()+"");
            Log.d("footer_index_adapter",footerViewHolder.getAdapterPosition()+"");

            footerViewHolder.cardview_seemore_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    if(sectionNum == 3)
                        getPublicClassList();
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
     * My room list API 호출하여 나의 Live 리스트를 렌더링함
     */
    public void getAllClassList() {
        if (!KnowloungeApplication.isNetworkConnected) {
            Toast.makeText(getActivity(), getString(R.string.network_disconnected), Toast.LENGTH_SHORT).show();
            if(mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        recyclerView.setScrollAble(true);
        classLoadingLayout.setVisibility(View.VISIBLE);

        String rows = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? CLASS_VIEW_ROWS_PHONE_BASIC : CLASS_VIEW_ROWS_TABLET_BASIC ;
        new MainApiCommand()
            .command("getPublicClassList")
            .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
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
                                    Log.d(TAG, "[RxJava / getAllClassList] onCompleted");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "[RxJava / getAllClassList] onError");
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    classLoadingLayout.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onNext(JsonObject object) {
                                    Log.d(TAG, "[RxJava / getAllClassList] onNext");
                                    Log.d(TAG, "[RxJava / getAllClassList] object : " + object.toString());

                                    mEditorPickClassDataSet.clear();
                                    mMostViewClassDataSet.clear();
                                    mPublicClassDataSet.clear();

                                    int apiResult = object.get("result").getAsInt();
                                    if (apiResult == 0) {
                                        JsonArray editorPickClassList = object.getAsJsonObject("map").getAsJsonArray("recommand");
                                        JsonArray mostViewClassList = object.getAsJsonObject("map").getAsJsonArray("mostview");
                                        JsonArray publicClassList = object.getAsJsonObject("map").getAsJsonArray("public");
                                        Log.d(TAG, publicClassList.toString());
                                        // Editor's Pick 섹션
                                        sectionAdapter.getSection("editor_pick_class").setHasFooter(false);
                                        renderRoomList(editorPickClassList, mEditorPickClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_C, "0");

                                        // Most View 섹션
                                        sectionAdapter.getSection("most_view_class").setHasFooter(false);
                                        renderRoomList(mostViewClassList, mMostViewClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_B, "0");

                                        // Public Class 섹션
                                        String publicMore =  object.getAsJsonObject("map").get("public_more").getAsString();
                                        sectionAdapter.getSection("public_class").setHasFooter(TextUtils.equals(publicMore, "1") ? true : false);
                                        renderRoomList(publicClassList, mPublicClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_D, publicMore);

                                        mPublicClassIdx = publicClassList.size() == 0  ? null : publicClassList.get(publicClassList.size() - 1).getAsJsonObject().get("idx").getAsString();

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


//        RequestParams params = new RequestParams();
//        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
//            params.put("rows", CLASS_VIEW_ROWS_PHONE_BASIC);
//        }else{
//            params.put("rows", CLASS_VIEW_ROWS_TABLET_BASIC);
//        }
//        String masterCookie = prefManager.getUserCookie();
//        String checkSum = prefManager.getChecksumCookie();
//        RestClient.getWithCookie(URL_All_PUBLIC_ROOM_LIST, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                try {
//
//                    Log.d(TAG, "room : " + response.toString());
//
//                    mEditorPickClassDataSet.clear();
//                    mMostViewClassDataSet.clear();
//                    mPublicClassDataSet.clear();
//
//                    int apiResult = response.getInt("result");
//                    if(apiResult == 0) {
//                        JSONArray editorPickClassList = response.getJSONObject("map").getJSONArray("recommand");
//                        JSONArray mostViewClassList = response.getJSONObject("map").getJSONArray("mostview");
//                        JSONArray publicClassList = response.getJSONObject("map").getJSONArray("public");
//
//                        int len = editorPickClassList.length();
//                        if (len > 0) {
//
//                            for (int i = 0; i < len; i++) {
//                                JSONObject obj = editorPickClassList.getJSONObject(i);
//                                sectionAdapter.getSection("editor_pick_class").setHasFooter(false);
//                                //Log.d(TAG, "room : " + obj.toString());
//
//                                String seqNo = obj.getString("seqno");
//                                String roomId = obj.getString("roomid");
//                                String roomTitle = obj.getString("title");
//                                int roomCnt = obj.getInt("readcnt");
//                                String userNm = obj.getString("usernm");
//                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
//                                String userLimitCnt = obj.getString("user_limit_cnt");
//
//                                String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
//
//                                mEditorPickClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb,userLimitCnt,false,GlobalCode.CODE_ROOM_TYPE_C,"0"));
//                            }
//                            Log.d(TAG, "invokeMyRoomList - Room list load complete.. roomDataSet size : " + mEditorPickClassDataSet.size());
//                        } else {
//                            sectionAdapter.getSection("editor_pick_class").setHasFooter(false);
//                        }
//
//                        int len2 = mostViewClassList.length();
//                        if(len2 > 0) {
//
//                            for (int i = 0; i < len2; i++) {
//                                JSONObject obj = mostViewClassList.getJSONObject(i);
//                                sectionAdapter.getSection("most_view_class").setHasFooter(false);
//                                //Log.d(TAG, "room : " + obj.toString());
//
//                                String seqNo = obj.getString("seqno");
//                                String roomId = obj.getString("roomid");
//                                String roomTitle = obj.getString("title");
//                                int roomCnt = obj.getInt("readcnt");
//                                String userNm = obj.getString("usernm");
//                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
//                                String userLimitCnt = obj.getString("user_limit_cnt");
//
//                                String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
//
//                                mMostViewClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb,userLimitCnt,false,GlobalCode.CODE_ROOM_TYPE_B,"0"));
//                            }
//                            Log.d(TAG, "invokeMyRoomList - Room list load complete.. roomDataSet size : " + mMostViewClassDataSet.size());
//                        }else {
//                            sectionAdapter.getSection("most_view_class").setHasFooter(false);
//                        }
//
//                        int len3 = publicClassList.length();
//                        if(len3 > 0) {
//
//                            String publicMore =  response.getJSONObject("map").getString("public_more");
//
//                            if(publicMore.equals("1"))
//                                sectionAdapter.getSection("public_class").setHasFooter(true);
//                            else
//                                sectionAdapter.getSection("public_class").setHasFooter(false);
//
//
//                            for (int i = 0; i < len3; i++) {
//                                JSONObject obj = publicClassList.getJSONObject(i);
//
//                                //Log.d(TAG, "room : " + obj.toString());
//
//                                String seqNo = obj.getString("seqno");
//                                String roomId = obj.getString("roomid");
//                                String roomTitle = obj.getString("title");
//                                int roomCnt = obj.getInt("readcnt");
//                                String userNm = obj.getString("usernm");
//                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
//                                String userLimitCnt = obj.getString("user_limit_cnt");
//                                mPublicClassIdx = obj.getString("idx");
//
//                                String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
//
//                                mPublicClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb,userLimitCnt,false,GlobalCode.CODE_ROOM_TYPE_D, publicMore));
//                            }
//                            Log.d(TAG, "invokeMyRoomList - Room list load complete.. roomDataSet size : " + mPublicClassDataSet.size());
//
//                        }else {
//                            sectionAdapter.getSection("public_class").setHasFooter(false);
//                        }
//                        mSwipeRefreshLayout.setRefreshing(false);
//                        classLoadingLayout.setVisibility(View.GONE);
//                        sectionAdapter.notifyDataSetChanged();
//                    }
//                } catch (JSONException e) {
//                    mPublicClassDataSet.add(new Room("", "", "", "", 0, "", "",""));  // 헤더 들어갈 자리..
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d(TAG, "Get room list onFailure.. statusCode : " + statusCode);
//                mPublicClassDataSet.add(new Room("", "", "", "", 0, "", "",""));  // 헤더 들어갈 자리..
//            }
//        });
    }


    public void getPublicClassList() {
        String idx = mPublicClassIdx;
        String rows = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? CLASS_VIEW_ROWS_PHONE_BASIC : CLASS_VIEW_ROWS_TABLET;
        String flag = "next";

        new RoomApiCommand()
            .command("getPublicClassList")
            .credential(NetworkUtils.getApiCredential(prefManager.getUserCookie(), prefManager.getChecksumCookie()))
            .idx(idx)
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
                                    int apiResult = object.get("result").getAsInt();

                                    sectionAdapter.getSection("public_class").setHasFooter(false);

                                    if (apiResult == 0) {
                                        JsonArray publicRoomList = object.getAsJsonArray("list");
                                        Log.d(TAG, "Retrofit & OkHttp & RxJava API Call result : " + publicRoomList.toString());

                                        String more = object.get("more").getAsString();

                                        if(more.equals("1"))
                                            sectionAdapter.getSection("public_class").setHasFooter(true);

                                        renderRoomList(publicRoomList, mPublicClassDataSet, false, GlobalCode.CODE_ROOM_TYPE_D, more);

                                        sectionAdapter.notifyDataSetChanged();
                                        mPublicClassIdx = publicRoomList.size() == 0  ? null : publicRoomList.get(publicRoomList.size() - 1).getAsJsonObject().get("idx").getAsString();
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }).execute();

//        RequestParams params = new RequestParams();
//        params.put("idx", mPublicClassIdx);
//
//        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
//            params.put("rows", CLASS_VIEW_ROWS_PHONE_BASIC);
//        }else{
//            params.put("rows", CLASS_VIEW_ROWS_TABLET);
//        }
//
//        params.put("flag","next");
//
//        String masterCookie = prefManager.getUserCookie();
//        String checkSum = prefManager.getChecksumCookie();
//        RestClient.getWithCookie(URL_PUBLIC_ROOM_LIST, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                try {
//                    //Log.d(TAG, "room : " + roomInfo.toString());
//                    int apiResult = response.getInt("result");
//                    if(apiResult == 0) {
//
//                        JSONArray myRoomList = response.getJSONArray("list");
//                        String more = response.getString("more");
//
//                        if(more.equals("1"))
//                            sectionAdapter.getSection("public_class").setHasFooter(true);
//                        else
//                            sectionAdapter.getSection("public_class").setHasFooter(false);
//
//                        int len = myRoomList.length();
//                        if (len > 0) {
//                            for (int i = 0; i < len; i++) {
//                                JSONObject obj = myRoomList.getJSONObject(i);
//
//                                String seqNo = obj.getString("seqno");
//                                String roomId = obj.getString("roomid");
//                                String roomTitle = obj.getString("title");
//                                int roomCnt = obj.getInt("readcnt");
//                                String userNm = obj.getString("usernm");
//                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
//                                mPublicClassIdx = obj.getString("idx");
//                                String userLimitCnt = obj.getString("user_limit_cnt");
//
//                                String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
//                                mPublicClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb, userLimitCnt, false, GlobalCode.CODE_ROOM_TYPE_D, more));
//                            }
//                            sectionAdapter.notifyDataSetChanged();
//                        } else {
//
//                        }
//
//                    }
//                    Log.d(TAG, "invokeMyRoomList - Room list load complete.. roomDataSet size : " + mPublicClassDataSet.size());
//
//                } catch (JSONException e) {
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d(TAG, "Get room list onFailure");
//            }
//        });
    }




}
