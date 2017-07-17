package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowlounge.CircleTransform;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.fragment.dialog.ExtendReqDialogFragment;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.NoticeData;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by Minsu on 2016-05-30.
 */
public class RoomNotiListFragment extends Fragment implements ExtendReqDialogFragment.SetRoomNotiAdapterListener {

    private WenotePreferenceManager prefManager;
    private View rootView;

    SectionedRecyclerViewAdapter sectionAdapter;

    private ArrayList<NoticeData> inviteList;
    private ArrayList<NoticeData> requestList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_room_noti_list, container, false);

        sectionAdapter = new SectionedRecyclerViewAdapter();

        prefManager = WenotePreferenceManager.getInstance(getActivity());
        ExtendReqDialogFragment.setRoomNotiAdapterListener(this);

        prefManager.setNotiBadgeCount(prefManager.getNotiBadgeCount() - prefManager.getReqjoinNotiBadgeCount(RoomActivity.activity.getRoomCode()));
        prefManager.clearReqjoinNotiBadgeCount(RoomActivity.activity.getRoomCode());
        AndroidUtils.setBadge(getActivity().getApplicationContext(),prefManager.getNotiBadgeCount());

        requestList = new ArrayList<NoticeData>();
        inviteList = new ArrayList<NoticeData>();

        getRoomNotiList("",null,null);

        sectionAdapter.addSection("request", new ExpandableContactsSection(getResources().getString(R.string.canvas_noti_apply), requestList, 1));
        sectionAdapter.addSection("invite", new ExpandableContactsSection(getResources().getString(R.string.canvas_noti_invite), inviteList, 2));

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.room_notice_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sectionAdapter);
        sectionAdapter.notifyDataSetChanged();
        RoomActivity.activity.checkNotiBadgeCount();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    class ExpandableContactsSection extends StatelessSection {

        String title;
        List<NoticeData> list;
        int sectionNum;
        boolean expanded = true;

        public ExpandableContactsSection(String title, List<NoticeData> list, int sectionNum) {
            super(R.layout.fragment_room_noti_header, R.layout.fragment_room_noti_footer, R.layout.fragment_room_noti_item);
            this.title = title;
            this.list = list;
            this.sectionNum = sectionNum;
            setHasFooter(false);
        }

        @Override
        public int getContentItemsTotal() {
            return expanded? list.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            if(list.get(position).getSenderThumbNail() != null && !list.get(position).getSenderThumbNail().equals("")){
                Picasso.with(getContext()).load(list.get(position).getSenderThumbNail()).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(itemHolder.roomNoticeThumbnail);  // 썸네일 이미지 로드
            }
            SpannableStringBuilder sb = getText(position);
            if(sb != null)
                itemHolder.roomNoticeText.setText(sb);
            itemHolder.roomNoticeDate.setText(AndroidUtils.transformDate(getContext(),list.get(position).getDateTime()));
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
            if(sectionNum == 1) {
                headerHolder.roomNoticeNoDataText.setText(getResources().getString(R.string.canvas_noti_noapply));
                headerHolder.roomNoticeHeaderLayout.setBackgroundResource(R.drawable.user_list_title_style_section_first);
            }
            else {
                headerHolder.roomNoticeNoDataText.setText(getResources().getString(R.string.canvas_noti_noinvite));
                headerHolder.roomNoticeHeaderLayout.setBackgroundResource(R.drawable.user_list_title_style);
            }

            if(list.size() == 0 && expanded)
                headerHolder.roomNoticeNoData.setVisibility(View.VISIBLE);
            else
                headerHolder.roomNoticeNoData.setVisibility(View.GONE);


            headerHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    headerHolder.imgArrow.setImageResource(
                            expanded ? R.drawable.btn_rightdrawer_fold : R.drawable.btn_rightdrawer_fold
                    );
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return new FooterViewHolder(view);
        }

        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            final FooterViewHolder footerHolder = (FooterViewHolder) holder;

            footerHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle argument = new Bundle();
                    argument.putString("type","room");
                    argument.putString("roomid",prefManager.getCurrentRoomId());
                    argument.putString("code", RoomActivity.activity.getRoomCode());
                    argument.putString("masterno",prefManager.getCurrentTeacherUserNo());
                    FragmentManager fm = getFragmentManager();
                    ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                    dialogFragment.setArguments(argument);
                    dialogFragment.show(fm, "what");
                }
            });
        }
        private SpannableStringBuilder getText(int position) {
            String resourceId = "history";
            String category = list.get(position).getCategory();
            String historyType = list.get(position).getHistoryType();
            String resultText ="";

            if(sectionNum == 1){
                resourceId += "_recv";
            }else {
                resourceId += "_send";
            }
            resourceId += "_"+category+"_"+historyType;
            resultText = getResources().getString(getResources().getIdentifier(resourceId, "string", getContext().getPackageName()));
            Log.d("text", resultText);

            return  setParameter(position,resultText, historyType);
        }
        private SpannableStringBuilder setParameter(int position, String data, String historyType) {
            String extraNm = list.get(position).getExtraNm();
            String senderNm = list.get(position).getSenderName();
            String noticeTitle = list.get(position).getNoticeTitle();
            int cnt = list.get(position).getCnt();

            if(historyType.equals("IV002")){
                return AndroidUtils.setHighlightText(String.format(data, noticeTitle, senderNm),senderNm);
            }else if(historyType.equals("IV001")){
                return AndroidUtils.setHighlightText(String.format(data, senderNm, noticeTitle, extraNm), extraNm);
            }
            return null;
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final TextView tvTitle;
        private final ImageView imgArrow;

        private final LinearLayout roomNoticeHeaderLayout;
        private final LinearLayout roomNoticeNoData;
        private final TextView roomNoticeNoDataText;

        public HeaderViewHolder(View view) {
            super(view);

            rootView = view;
            tvTitle = (TextView) view.findViewById(R.id.room_notice_title);
            imgArrow = (ImageView) view.findViewById(R.id.room_notice_arrow);

            roomNoticeNoData = (LinearLayout) view.findViewById(R.id.room_notice_no_data);
            roomNoticeNoDataText = (TextView) view.findViewById(R.id.room_notice_no_data_text);
            roomNoticeHeaderLayout = (LinearLayout) view.findViewById(R.id.room_notice_header_layout);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder{
        private final View rootView;
        public FooterViewHolder(View view) {
            super(view);

            rootView = view;
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final ImageView roomNoticeThumbnail;
        private final TextView roomNoticeText;
        private final TextView roomNoticeDate;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            roomNoticeThumbnail = (ImageView) view.findViewById(R.id.room_notice_thumbnail);
            roomNoticeText = (TextView) view.findViewById(R.id.room_notice_text);
            roomNoticeDate = (TextView) view.findViewById(R.id.room_notice_date);
        }
    }
    /**
     * 룸 알림 리스트 받아오기
     * @param notiType : 조회모드
     * @param rows : null 허용.. null일 경우 100개씩 가져옴..
     * @param flag : null 허용.. 값은 "next" / "prev" 둘 중 하나임, null일 경우 디폴트값은 "next"
     */
    public void getRoomNotiList(String notiType, @Nullable String rows, @Nullable String flag) {

        inviteList.clear();
        requestList.clear();

        String roomId = prefManager.getCurrentRoomId();

        RequestParams params = new RequestParams();
        params.put("roomid", roomId);
        params.put("mode", notiType);
        if(rows != null) {
            params.put("rows", rows);
        }
        if(flag != null) {
            params.put("flag", flag);
        }

        String url = "history/list/class.json";
        RestClient.getWithCookie(url, prefManager.getUserCookie(), prefManager.getChecksumCookie(), params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("RoomNoti",response.toString());
                    int apiResult = response.getInt("result");
                    if(apiResult == 0) {
                        JSONObject responseBody = response.getJSONObject("map");
                        JSONArray inviteNoti = responseBody.getJSONArray("invite");  // 초대 알림리스트
                        JSONArray reqNoti = responseBody.getJSONArray("req");        // 참여요청 알림리스트
                        int inviteMoreFlag = responseBody.getInt("invite_more");
                        int reqMoreFlag = responseBody.getInt("req_more");

                        if(inviteNoti.length() != 0){
                            for(int i=0; i<inviteNoti.length(); i++){
                                JSONObject obj = inviteNoti.getJSONObject(i);
                                inviteList.add(setItem(obj));
                            }
                            sectionAdapter.getSection("invite").setHasFooter(false);
                        }
                        if(reqNoti.length() !=0){
                            for(int i=0; i<reqNoti.length(); i++){
                                JSONObject obj = reqNoti.getJSONObject(i);
                                requestList.add(setItem(obj));
                            }
                            if(requestList.get(0).getStatus() == 1 || reqNoti.length() == 0){
                                sectionAdapter.getSection("request").setHasFooter(false);
                            }else{
                                sectionAdapter.getSection("request").setHasFooter(true);
                            }
                        }else{
                            sectionAdapter.getSection("request").setHasFooter(false);
                        }
                        sectionAdapter.notifyDataSetChanged();
                    } else {
                        // TODO: 예외처리..
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

        });
    }
    private NoticeData setItem(JSONObject obj) {
        try {
            NoticeData tempData = new NoticeData(false);
            if (!obj.isNull("category"))
                tempData.setCategory(obj.getString("category"));
            if (!obj.isNull("senderthumbnail"))
                tempData.setSenderThumbNail(obj.getString("senderthumbnail"));
            if (!obj.isNull("cnt"))
                tempData.setCnt(Integer.parseInt(obj.getString("cnt")));
            if (!obj.isNull("status"))
                tempData.setStatus(Integer.parseInt(obj.getString("status")));
            if (!obj.isNull("usernm"))
                tempData.setUserName(obj.getString("usernm"));
            if (!obj.isNull("cdatetime"))
                tempData.setDateTime(obj.getString("cdatetime"));
            if (!obj.isNull("historytype"))
                tempData.setHistoryType(obj.getString("historytype"));
            if (!obj.isNull("historyno"))
                tempData.setHistoryNo(Integer.parseInt(obj.getString("historyno")));
            if (!obj.isNull("sendernm"))
                tempData.setSenderName(obj.getString("sendernm"));
            if (!obj.isNull("title"))
                tempData.setNoticeTitle(obj.getString("title"));
            if (!obj.isNull("roomseqno"))
                tempData.setRoomSeqNo(obj.getString("roomseqno"));
            if (!obj.isNull("extraname"))
                tempData.setExtraNm(obj.getString("extraname"));
            if (!obj.isNull("roomid"))
                tempData.setRoomId(obj.getString("roomid"));
            return tempData;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void updateData() {
        Log.d("aswq","aswq");

    }
}
