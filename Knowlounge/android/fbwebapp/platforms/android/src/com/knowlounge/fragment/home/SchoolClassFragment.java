package com.knowlounge.fragment.home;

import android.content.Context;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.knowlounge.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import com.knowlounge.sectionedrecyclerviewadapter.StatelessSection;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-06-11.
 */
public class SchoolClassFragment extends RoomListFragment implements MainActivity.reNetworkConnected {
    private String TAG = "SchoolClassFragment";

    // API 콜 관련
    private String URL_SCHOOL_MAIN_LIST = "main/school.json";
    private String URL_SCHOOL_ROOM_LIST = "room/list/school.json";

    private SectionedRecyclerViewAdapter sectionAdapter;

    private ArrayList<Room> mSchoolClassDataSet;
    private String mSchoolClassIdx = "";

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        Log.d(TAG, "onCreateView");

        classLoadingLayout = (LinearLayout) view.findViewById(R.id.class_loading_layout);
        sectionAdapter = new SectionedRecyclerViewAdapter();
        mSchoolClassDataSet = new ArrayList<Room>();

        sectionAdapter.addSection("friend_class",new ClassSection(isTablet? R.drawable.ico_list_school : R.drawable.ico_list_school_color, getResources().getString(R.string.main_school_public),mSchoolClassDataSet,1));

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                getAllSchoolSchoolList();
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

        getAllSchoolSchoolList();

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
            getAllSchoolSchoolList();
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
                    if (!KnowloungeApplication.isNetworkConnected)
                        return;
                    if (!isClick)
                        return;

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
                } catch (IndexOutOfBoundsException indexOut) {
                    AppLog.d(AppLog.CLASSLIST_TAG, "IndexOutOfBoundsException");
                    getAllSchoolSchoolList();
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

            if (list.size() == 0) {
                headerHolder.classEmptyLayout.setVisibility(View.VISIBLE);
                switch (sectionNum) {
                    case 1:
                        if (prefManager.getMyUserType().equals("0") || prefManager.getMyeducation().equals("")) {
                            headerHolder.classEmptyContent.setText(getResources().getString(R.string.main_school_setguide));
                        } else if(!prefManager.getMyUserType().equals("0") && !prefManager.getMyeducation().equals("")) {
                            headerHolder.classEmptyContent.setText(getResources().getString(R.string.main_school_guide));
                            headerHolder.classEmptyBtn.setVisibility(View.GONE);
                        }

                        headerHolder.classEmptyImg.setImageResource(R.drawable.thumb_list_schoollist);
                        headerHolder.classEmptyBtn.setText(getResources().getString(R.string.main_school_setbtn));
                        break;
                }
            } else {
                headerHolder.classEmptyLayout.setVisibility(View.GONE);
            }

            headerHolder.classEmptyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (sectionNum) {
                        case 1:
                            if (TextUtils.isEmpty(prefManager.getMyeducation())) {
                                if (TextUtils.equals(prefManager.getMyUserType(), "0")) {
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
//                                intent.putExtra("school","ok");
//                                startActivity(intent);
//                            }
                            return;
                    }
                }
            });

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

                    if(!KnowloungeApplication.isNetworkConnected)
                        return;

                    getSchoolClassList();
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


    //Room 정보 받아오기
    /**
     * My room list API 호출하여 나의 Live 리스트를 렌더링함
     */
    public void getAllSchoolSchoolList() {
        recyclerView.setScrollAble(true);
        classLoadingLayout.setVisibility(View.VISIBLE);

        RequestParams params = new RequestParams();
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            params.put("rows", CLASS_VIEW_ROWS_PHONE);
        }else{
            params.put("rows", CLASS_VIEW_ROWS_TABLET);
        }

        String masterCookie = prefManager.getUserCookie();
        String checkSum = prefManager.getChecksumCookie();

        RestClient.getWithCookie(URL_SCHOOL_MAIN_LIST, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG, "room : " + response.toString());

                    mSchoolClassDataSet.clear();

                    int apiResult = response.getInt("result");
                    if (apiResult == 0) {
                        JSONArray schoolClassList = response.getJSONObject("map").getJSONArray("school");
                        String more = response.getJSONObject("map").getString("school_more");
                        sectionAdapter.getSection("friend_class").setHasFooter(more.equals("1") ? true : false);

                        int len = schoolClassList.length();
                        if (len > 0) {
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = schoolClassList.getJSONObject(i);

                                //Log.d(TAG, "room : " + obj.toString());

                                String seqNo = obj.getString("seqno");
                                String roomId = obj.getString("roomid");
                                String roomTitle = obj.getString("title");
                                int roomCnt = obj.getInt("readcnt");
                                String userNm = obj.getString("usernm");
                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
                                String userLimitCnt = obj.getString("user_limit_cnt");
                                mSchoolClassIdx = obj.getString("idx");

                                String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
                                String passwdFlag = obj.getString("passwd");

                                mSchoolClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb, userLimitCnt, false, GlobalCode.CODE_ROOM_TYPE_C, more, passwdFlag));
                            }
                            Log.d(TAG, "invokeMyRoomList - Room list load complete.. roomDataSet size : " + mSchoolClassDataSet.size());
                        } else {

                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                        classLoadingLayout.setVisibility(View.GONE);
                        sectionAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get room list onFailure.. statusCode : " + statusCode);
            }
        });
    }

    public void getSchoolClassList() {
        RequestParams params = new RequestParams();

        if(mSchoolClassIdx != null && !mSchoolClassIdx.equals(""))
            params.put("idx", mSchoolClassIdx);

        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            params.put("rows", CLASS_VIEW_ROWS_PHONE);
        }else{
            params.put("rows", CLASS_VIEW_ROWS_TABLET);
        }

        params.put("flag","next");

        String masterCookie = prefManager.getUserCookie();
        String checkSum = prefManager.getChecksumCookie();
        RestClient.getWithCookie(URL_SCHOOL_ROOM_LIST, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //Log.d(TAG, "room : " + roomInfo.toString());
                    int apiResult = response.getInt("result");
                    if(apiResult == 0) {

                        JSONArray friendClassList = response.getJSONArray("list");
                        String more = response.getString("more");

                        sectionAdapter.getSection("friend_class").setHasFooter(more.equals("1") ? true : false);

                        int len = friendClassList.length();
                        if (len > 0) {
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = friendClassList.getJSONObject(i);

                                String seqNo = obj.getString("seqno");
                                String roomId = obj.getString("roomid");
                                String roomTitle = obj.getString("title");
                                int roomCnt = obj.getInt("readcnt");
                                String userNm = obj.getString("usernm");
                                String userThumb = obj.has("thumbnail") ? obj.getString("thumbnail") : "";
                                mSchoolClassIdx = obj.getString("idx");
                                String userLimitCnt = obj.getString("user_limit_cnt");

                                String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";
                                String passwdFlag = obj.getString("passwd");

                                mSchoolClassDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb, userLimitCnt, false, GlobalCode.CODE_ROOM_TYPE_C, more, passwdFlag));
                            }
                            sectionAdapter.notifyDataSetChanged();
                        } else {
                            sectionAdapter.getSection("friend_class").setHasFooter(false);
                        }

                    }
                    Log.d(TAG, "invokeMyRoomList - Room list load complete.. roomDataSet size : " + mSchoolClassDataSet.size());

                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get room list onFailure");
            }
        });
    }
}
