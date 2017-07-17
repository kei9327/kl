package com.knowlounge.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.R;
import com.knowlounge.adapter.SelectFriendRecyclerAdapter;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.FriendUser;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SNSFriendListDepthTwoFragment extends Fragment implements View.OnClickListener, SelectFriendRecyclerAdapter.userRemove{
    private final String TAG = "SNSFriendList2";

    private View mRootView;
    private View footer;
    private WenotePreferenceManager prefManager;
    public DbOpenHelper mDbOpenHelper;

    private String svrFlag;
    private String svrHost;

    private ArrayList<MyRoom> myRoomList = new ArrayList<MyRoom>();   // 받은 초대 리스트
    public ArrayList<FriendUser> selectedFriendList = new ArrayList<FriendUser>();// 선택한 친구 리스트
    public String selectedList;
    private MyRoomAdapter myRoomAdapter;

    private ImageView btnMoveBack;
    private ListView myRoomListForInvite;
    private Button btnSendInvite;
    private TextView selectUsersMoreViewBtnDepth2;

    private RecyclerView selectedUsersListDepth2;
    private SelectFriendRecyclerAdapter selectedFriendsAdapter;
    private GridLayoutManager mLayoutManager;

    private boolean isSelectFriendOpen = false;
    private String selectRoomId = "";
    private String selectRoomTitle = "";

    private int dencityHeight;
    private int dencityMargin;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.sns_friend_list_fragment_depth2, container, false);


        mDbOpenHelper = new DbOpenHelper(getActivity());
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);

        selectedList = getArguments().getString("select_list");

        getSelectFriend();
        setFindViewById();

        SelectFriendRecyclerAdapter.setOnUserRemove(this);

        svrFlag = getResources().getString(R.string.svr_flag);
        svrHost = getResources().getString(getResources().getIdentifier("svr_host_" + svrFlag, "string", getActivity().getPackageName()));

        return mRootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myRoomListForInvite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (myRoomAdapter.getSelectedPosition() == position) {
                    myRoomAdapter.setSelectedPosition(-1);
                    view.findViewById(R.id.my_room_selector).setVisibility(View.GONE);
                    myRoomList.get(position).setIsSelect(false);
                    selectRoomId = "";
                    selectRoomTitle = "";

                } else {
                    if (myRoomAdapter.getSelectedPosition() > -1) {
                        for (int i=0; i<parent.getChildCount(); i++) {
                            parent.getChildAt(i).findViewById(R.id.my_room_selector).setVisibility(View.GONE);
                        }
                        myRoomList.get(myRoomAdapter.getSelectedPosition()).setIsSelect(false);
                    }

                    view.findViewById(R.id.my_room_selector).setVisibility(View.VISIBLE);
                    myRoomAdapter.setSelectedPosition(position);
                    myRoomList.get(position).setIsSelect(true);

                    selectRoomId = myRoomAdapter.getItem(position).getRoomId();
                    selectRoomTitle = myRoomAdapter.getItem(position).getRoomTitle();
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        invokeMyRoomList(prefManager.getCookieQueryStr());

        if(selectedFriendList.size() > getRowCount()) {
            selectUsersMoreViewBtnDepth2.setVisibility(View.VISIBLE);
            selectUsersMoreViewBtnDepth2.setText(String.format(getResources().getString(R.string.invite_seemore),Integer.toString(selectedFriendList.size() - getRowCount())));
        } else {
            selectUsersMoreViewBtnDepth2.setVisibility(View.GONE);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_move_back :
                getFragmentManager().popBackStack();
                break;
            case R.id.select_users_more_view_btn_depth2 :
                if(isSelectFriendOpen){
                    isSelectFriendOpen = false;
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dencityHeight);
                    selectedUsersListDepth2.setLayoutParams(params);
                    selectUsersMoreViewBtnDepth2.setText(String.format(getResources().getString(R.string.invite_seemore),Integer.toString(selectedFriendList.size() - getRowCount())));
                } else {
                    isSelectFriendOpen = true;
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    selectedUsersListDepth2.setLayoutParams(params);
                    selectUsersMoreViewBtnDepth2.setText(getResources().getString(R.string.invite_seemore_fold));
                }
                break;
            case R.id.btn_send_invite :
                if(TextUtils.isEmpty(selectRoomId)) {
                    Toast.makeText(getContext(), getResources().getString(R.string.toast_invite_select), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "sendInvite Params : receiverId : " + selectedList);
                sendInvite(selectRoomId, selectRoomTitle, selectedList, prefManager.getUserNo(), prefManager.getUserNm());
                break;
        }
    }
    @Override
    public void onUserRemove(int position) {

    }


    private void setFindViewById() {
        dencityHeight = AndroidUtils.getPxFromDp(getContext(),(float)40);
        dencityMargin = AndroidUtils.getPxFromDp(getContext(),(float)10);

        btnMoveBack = (ImageView) mRootView.findViewById(R.id.btn_move_back);
        myRoomListForInvite = (ListView) mRootView.findViewById(R.id.my_room_list_for_invite);
        btnSendInvite = (Button) mRootView.findViewById(R.id.btn_send_invite);
        selectedUsersListDepth2 = (RecyclerView) mRootView.findViewById(R.id.selected_users_list_depth2);
        selectUsersMoreViewBtnDepth2 = (TextView) mRootView.findViewById(R.id.select_users_more_view_btn_depth2);

        if(myRoomAdapter == null){
            myRoomAdapter = new MyRoomAdapter(getContext());
        }

        myRoomListForInvite.setAdapter(myRoomAdapter);

        if(selectedFriendsAdapter == null){
            selectedFriendsAdapter = new SelectFriendRecyclerAdapter(getContext(), selectedFriendList);
            mLayoutManager = new GridLayoutManager(getActivity(),(int)(AndroidUtils.getPxFromDp(getContext(),(float)280))){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
                @Override
                public int getSpanSize(int position) {
                    //TODO 넓이 가져와서 3 2 1 값으로 체인지
                    return getSpanNum(selectedFriendsAdapter.getItemUserNm(position));
                }
            });
            selectedUsersListDepth2.setHasFixedSize(true);
            selectedUsersListDepth2.setLayoutManager(mLayoutManager);
        }
        selectedUsersListDepth2.setAdapter(selectedFriendsAdapter);

        btnMoveBack.setOnClickListener(this);
        selectUsersMoreViewBtnDepth2.setOnClickListener(this);
        btnSendInvite.setOnClickListener(this);
    }


    // 선택한 유저가 나오는 공간에 한줄에 몇명의 유저가 들어가는지를 구하는 메소드
    public int getRowCount() {
        int spanSum = 0;
        int rowCnt = 0;
        for(int i=0; i<selectedFriendList.size();i++){
            spanSum += getSpanNum(selectedFriendsAdapter.getItemUserNm(i));
            if(spanSum < mLayoutManager.getSpanCount())
                rowCnt++;
            else
                break;
        }
        return rowCnt;
    }

    // 한 유저가 차지하는 Span값을 구하는 메소드
    public int getSpanNum(String name){

        View view = getActivity().getLayoutInflater().inflate(R.layout.row_select_user_chips,null,false);
        ((TextView) view.findViewById(R.id.select_usernm)).setText(name);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d("viewRect",Integer.toString(view.getMeasuredWidth()));
        int width = view.getMeasuredWidth() + dencityMargin;

        return width;
    }

    private void invokeMyRoomList(String cookieQueryStr) {
        RestClient.postWithBasicAuth("room/list.json", cookieQueryStr, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject roomInfo = response.getJSONObject("list");

                    //Log.d(TAG, "room : " + roomInfo.toString());

                    JSONArray list = roomInfo.getJSONArray("myroom");
                    JSONArray visitRoomList = roomInfo.getJSONArray("history");
                    JSONArray bookmarkList = roomInfo.getJSONArray("bookmark");

                    int len = list.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject obj = list.getJSONObject(i);

                        //Log.d(TAG, "room : " + obj.toString());

                        String seqNo = obj.getString("seqno");
                        String roomId = obj.getString("roomid");
                        String roomTitle = obj.getString("title");
                        int roomCnt = obj.getInt("readcnt");
                        String userNm = obj.getString("usernm");
                        String userThumb = obj.getString("thumbnail");

                        String roomThumbnail = svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";

                        MyRoom myRoomInfo = new MyRoom(seqNo, roomId, roomTitle, roomThumbnail);
                        myRoomList.add(myRoomInfo);
                        myRoomAdapter.add(myRoomInfo);
                        myRoomAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get room list onFailure");
            }
        });
    }

    private void sendInvite(String roomId, String roomTitle, String receiveUserId, String userNo, String userNm) {

        String inviteDataform = getResources().getString(R.string.history_recv_HC003_IV001);
        String msg = String.format(inviteDataform, userNm, roomTitle) ;

        RequestParams params = new RequestParams();
        params.put("roomid", roomId);
        params.put("title", roomTitle.replace("'", "\\'"));
        params.put("msg", msg);
        params.put("userid", receiveUserId);
        params.put("guserno", userNo);
        params.put("gusernm", userNm);


        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        if(TextUtils.isEmpty(masterCookie) || TextUtils.isEmpty(checksumCookie)) {
            // TODO : 예외처리
        }

        RestClient.postWithCookie("invite/send.json", masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "send message success.. response : " + response.toString());
                try {
                    String resultCode = response.getString("result");
                    if ("0".equals(resultCode)) {
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_invite_send), Toast.LENGTH_SHORT).show();
                        getFragmentManager().popBackStack();

                    } else {
                        // TODO : 예외처리
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
        });
    }


    private void getSelectFriend() {
        String[] list = selectedList.split(",");
        for(String row:list)
            selectedFriendList.add(mDbOpenHelper.getFriend(row));
    }

    private class MyRoom{

        private String seqNo;
        private String roomId;
        private String roomTitle;

        private String roomThumbnail;
        private boolean isSelect = false;

        public MyRoom(String roomSeqNo, String roomId, String roomTitle, String roomThumbnail) {
            this.seqNo = roomSeqNo;
            this.roomId = roomId;
            this.roomTitle = roomTitle;
            this.roomThumbnail = roomThumbnail;
        }

        public String getSeqNo() {
            return this.seqNo;
        }
        public String getRoomId() {
            return this.roomId;
        }
        public String getRoomTitle() {
            return this.roomTitle;
        }
        public String getRoomThumbnail() {
            return this.roomThumbnail;
        }
        public boolean getIsSelect() {
            return this.isSelect;
        }

        public void setIsSelect(boolean isSelect) {
            this.isSelect = isSelect;
        }
    }


    public class MyRoomAdapter extends ArrayAdapter<MyRoom> {
        int selectedPosition = -1;
        public MyRoomAdapter(Context context) {
            super(context, 0);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rows_invite_my_room, null);
            }
            MyRoom item = getItem(position);

            String roomId = item.getRoomId();
            String roomTitle = item.getRoomTitle();
            String roomThumbnail = item.getRoomThumbnail();
            boolean isSelect = item.getIsSelect();

            Uri thumbnailUri = Uri.parse(roomThumbnail);

            ImageView thumbnailIcon = (ImageView) convertView.findViewById(R.id.my_room_thumb);
            TextView roomTitleTextView = (TextView) convertView.findViewById(R.id.room_title_text);

            roomTitleTextView.setText(roomTitle);

            Glide.with(getActivity())
                    .load(thumbnailUri)
                    .error(getActivity().getResources().getIdentifier("thumbnail_0" + (Integer.parseInt(item.getSeqNo())%4+1), "drawable", getActivity().getPackageName()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(thumbnailIcon);

            if (!isSelect) {
                convertView.findViewById(R.id.my_room_selector).setVisibility(View.GONE);
            } else {
                convertView.findViewById(R.id.my_room_selector).setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        public int getSelectedPosition() {return selectedPosition;}

        public void setSelectedPosition(int position){
                selectedPosition = position;
        }
    }
}