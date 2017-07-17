package com.knowlounge.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.SoundSearcher;
import com.knowlounge.adapter.SelectFriendRecyclerAdapter;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.FriendUser;
import com.knowlounge.sqllite.DataBases;
import com.knowlounge.sqllite.DbOpenHelper;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.logger.AppLog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SNSFriendListDepthOneFragment extends Fragment implements  View.OnClickListener, SelectFriendRecyclerAdapter.userRemove{
    private final String TAG = "SNSFriendListDepth_One";

    private View mRootView;
    private WenotePreferenceManager prefManager;
    public DbOpenHelper mDbOpenHelper;

    public ArrayList<FriendUser> friendList;  // SNS 친구 리스트
    public ArrayList<FriendUser> selectedFriendList = new ArrayList<FriendUser>();  //  SNS 친구 select 리스트
    public ArrayList<FriendUser> searchFriendList = new ArrayList<FriendUser>(); // SNS 친구 search 리스트
    private FriendListAdapter snsFriendListAdapter; // SNS 친구 리스트 Adapter

    private ImageView inputSnsFriendSearchImg, btnClearFriendSearch, imageViewFriendListTitle, btnListToggle, sns_friend_search_empty_img, friend_no_result_img;
    private EditText inputSnsFriendSearch;
    private TextView snsFriendSearchResultTitle, textViewFriendListTitle, selectUsersMoreViewBtn,sns_friend_search_empty_text, friend_no_result_text;
    private LinearLayout snsFriendListTitle, searchMode, selectFriendLayout, snsFriendListLayer, snsFriendSearchEmptyLayout, friendNoResult, friendSearchLayout;
    private Button btnNextDepth;

    private View searchUnderLine;

    private ListView friendListView;

    private RecyclerView selectedUsersList;
    private SelectFriendRecyclerAdapter selectedFriendsAdapter;
    private GridLayoutManager mLayoutManager;

    private boolean isSelectFriendOpen = false;
    private boolean isFriendListOpen = true;

    private int spanCount = 0;

    private int type;

    private int dencityHeight;
    private int dencityMargin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //초기화 작업 메소드
        thisFragmentInit();
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        AppLog.d(AppLog.TAG, "onCreateView");
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.sns_friend_list_fragment_depth1, container, false);

        type = getArguments().getInt("type");
        prefManager = WenotePreferenceManager.getInstance(getContext());

        //이 화면에서 사용할 Resource 객체 바인딩하는 메소드
        setFindViewById();

        //UI초기화 작업 메소드
        thisFragmentUiInit();

        SelectFriendRecyclerAdapter.setOnUserRemove(this);

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputSnsFriendSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    btnClearFriendSearch.setVisibility(View.VISIBLE);
                    snsFriendListTitle.setVisibility(View.GONE);
                    snsFriendSearchResultTitle.setVisibility(View.VISIBLE);
                    searchUnderLine.setVisibility(View.VISIBLE);
                    searchMode.setBackgroundColor(Color.parseColor("#ebeced"));
                    friendListView.setVisibility(View.VISIBLE);
                    searchFriendList.clear();

                    for(int i = 0; i<friendList.size() ; i++){
                        //초성검색
                        String searchData = friendList.get(i).getUserNm();
                        String keyword = s.toString();

                        //영어일 경우 무조건 소문자로 변환
                        searchData = searchData.toLowerCase();
                        keyword = keyword.toLowerCase();

                        boolean boolResult = SoundSearcher.matchString(searchData,keyword);

                        if(boolResult){
                            searchFriendList.add(friendList.get(i));
                        }
                    }
                    snsFriendListAdapter = new FriendListAdapter(getContext(),searchFriendList);
                    friendListView.setAdapter(snsFriendListAdapter);
                    snsFriendListAdapter.notifyDataSetChanged();

                    if(searchFriendList.size() == 0){
                        friendNoResult.setVisibility(View.VISIBLE);
                        friendSearchLayout.setVisibility(View.GONE);
                    }else{
                        friendNoResult.setVisibility(View.GONE);
                        friendSearchLayout.setVisibility(View.VISIBLE);
                    }

                }else{
                    btnClearFriendSearch.setVisibility(View.GONE);
                    snsFriendListTitle.setVisibility(View.VISIBLE);
                    snsFriendSearchResultTitle.setVisibility(View.GONE);
                    searchUnderLine.setVisibility(View.GONE);
                    searchMode.setBackgroundColor(Color.parseColor("#00000000"));
                    inputSnsFriendSearch.setPadding(0,0,0,0);

                    if(friendList.size() != 0) {
                        friendNoResult.setVisibility(View.GONE);
                        friendSearchLayout.setVisibility(View.VISIBLE);
                    }

                    snsFriendListAdapter = new FriendListAdapter(getContext(),friendList);
                    friendListView.setAdapter(snsFriendListAdapter);
                    snsFriendListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                snsFriendListAdapter.toggleChecked(position);

                if(snsFriendListAdapter.getItem(position).IsChecked()) {
                    selectedFriendList.add(snsFriendListAdapter.getItem(position));
                }else{
                    selectedFriendList.remove(snsFriendListAdapter.getItem(position));
                }
                selectedFriendsAdapter.notifyDataSetChanged();

                setUiTransform(friendList.get(position).ischecked, friendList.get(position).getUserNm());
                AndroidUtils.keyboardHide(getActivity());

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mDbOpenHelper.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_clear_friend_search :
                inputSnsFriendSearchImg.setVisibility(View.VISIBLE);
                btnClearFriendSearch.setVisibility(View.GONE);
                inputSnsFriendSearch.setText("");
                break;
            case R.id.btn_next_depth :
                if (type == GlobalConst.THROUGHT_MAIIN) {
                    if (selectedFriendList.size() != 0) {
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        Bundle argument = new Bundle();
                        argument.putString("select_list",selectedFriendsAdapter.getSelectFriendList());

                        SNSFriendListDepthTwoFragment snsFriendListDepthTwoFragment = new SNSFriendListDepthTwoFragment();
                        snsFriendListDepthTwoFragment.setArguments(argument);
                        fragmentTransaction.replace(R.id.main_right_friend_list, snsFriendListDepthTwoFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        AndroidUtils.keyboardHide(getActivity());
                    } else {
                        Toast.makeText(getContext(),getResources().getString(R.string.toast_invite_noone), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    if(selectedFriendList.size() != 0) {
                        String receiverId = "";
                        for (FriendUser i : selectedFriendList) {
                            receiverId += i.getId() + ",";
                        }
                        receiverId = receiverId.substring(0, receiverId.length() - 1);

                        Log.d(TAG, "sendInvite Params : receiverId : " + receiverId);
                        sendInvite(RoomActivity.activity.getRoomId(), RoomActivity.activity.getRoomNm(), receiverId, prefManager.getUserNo(), prefManager.getUserNm());
                    }else{
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_invite_noone), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            case R.id.select_users_more_view_btn :
//              TODO SelectUserList 펼치기
                if (isSelectFriendOpen) {
                    isSelectFriendOpen = false;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dencityHeight);
                    selectedUsersList.setLayoutParams(params);
                    selectUsersMoreViewBtn.setText(String.format(getResources().getString(R.string.invite_seemore),Integer.toString(selectedFriendList.size() - getRowCount())));
                } else {
                    isSelectFriendOpen = true;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    selectedUsersList.setLayoutParams(params);
                    selectUsersMoreViewBtn.setText(getResources().getString(R.string.invite_seemore_fold));
                }
                break;
            case R.id.sns_friend_list_title :
                if(isFriendListOpen) {
                    isFriendListOpen = false;
                    friendListView.setVisibility(View.INVISIBLE);
                    btnListToggle.setImageResource(R.drawable.btn_userlist_categoryfold);
                }else {
                    isFriendListOpen = true;
                    friendListView.setVisibility(View.VISIBLE);
                    btnListToggle.setImageResource(R.drawable.btn_userlist_categoryfold);
                }
        }
    }

    @Override
    public void onUserRemove(int position) {
        String userId = selectedFriendsAdapter.getItemUserId(position);
        String userNm = selectedFriendsAdapter.getItemUserNm(position);
        for(FriendUser i : friendList){
            if (i.getId().equals(userId)) {
                i.setIschecked();
                break;
            }
        }
        selectedFriendList.remove(position);
        selectedFriendsAdapter.notifyDataSetChanged();
        snsFriendListAdapter.notifyDataSetChanged();
        setUiTransform(false, userNm);
    }


    public void setFindViewById() {
        inputSnsFriendSearchImg = (ImageView) mRootView.findViewById(R.id.input_sns_friend_search_img);
        btnClearFriendSearch = (ImageView) mRootView.findViewById(R.id.btn_clear_friend_search);
        imageViewFriendListTitle = (ImageView) mRootView.findViewById(R.id.image_view_friend_list_title);
        btnListToggle = (ImageView) mRootView.findViewById(R.id.btn_list_toggle);
        sns_friend_search_empty_img = (ImageView) mRootView.findViewById(R.id.sns_friend_search_empty_img);
        friend_no_result_img = (ImageView) mRootView.findViewById(R.id.friend_no_result_img);

        snsFriendListTitle = (LinearLayout) mRootView.findViewById(R.id.sns_friend_list_title);
        selectFriendLayout = (LinearLayout)mRootView.findViewById(R.id.select_friend_layout);
        searchMode = (LinearLayout) mRootView.findViewById(R.id.search_mode);
        snsFriendListLayer = (LinearLayout) mRootView.findViewById(R.id.sns_friend_list_layer);
        snsFriendSearchEmptyLayout = (LinearLayout) mRootView.findViewById(R.id.sns_friend_list_layer);
        friendNoResult = (LinearLayout) mRootView.findViewById(R.id.friend_no_result);
        friendSearchLayout = (LinearLayout) mRootView.findViewById(R.id.friend_search_layout);

        inputSnsFriendSearch = (EditText) mRootView.findViewById(R.id.input_sns_friend_search);

        snsFriendSearchResultTitle = (TextView) mRootView.findViewById(R.id.sns_friend_search_result_title);
        textViewFriendListTitle = (TextView) mRootView.findViewById(R.id.text_view_friend_list_title);
        selectUsersMoreViewBtn = (TextView) mRootView.findViewById(R.id.select_users_more_view_btn);
        sns_friend_search_empty_text = (TextView) mRootView.findViewById(R.id.sns_friend_search_empty_text);
        friend_no_result_text = (TextView) mRootView.findViewById(R.id.friend_no_result_text);

        btnNextDepth = (Button) mRootView.findViewById(R.id.btn_next_depth);
        friendListView = (ListView) mRootView.findViewById(R.id.friend_list);
        selectedUsersList = (RecyclerView) mRootView.findViewById(R.id.selected_users_list);

        searchUnderLine = (View) mRootView.findViewById(R.id.search_under_line);

        friendListView.setAdapter(snsFriendListAdapter);
        selectedUsersList.setHasFixedSize(true);
        selectedUsersList.setLayoutManager(mLayoutManager);
        selectedUsersList.setAdapter(selectedFriendsAdapter);

        btnClearFriendSearch.setOnClickListener(this);
        btnNextDepth.setOnClickListener(this);
        selectUsersMoreViewBtn.setOnClickListener(this);
        snsFriendListTitle.setOnClickListener(this);
    }


    private void thisFragmentInit() {
        mDbOpenHelper = new DbOpenHelper(getActivity());
        mDbOpenHelper.open(DataBases.CreateDB._TABLENAME);

        dencityHeight = AndroidUtils.getPxFromDp(getContext(),(float)40);
        dencityMargin = AndroidUtils.getPxFromDp(getContext(),(float)10);

        friendList = mDbOpenHelper.getAllRow();
        snsFriendListAdapter = new FriendListAdapter(getContext(), friendList);

        selectedFriendsAdapter = new SelectFriendRecyclerAdapter(getActivity(), selectedFriendList);
        mLayoutManager = new GridLayoutManager(getActivity(), (int)(AndroidUtils.getPxFromDp(getContext(),(float)280))){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
            @Override
            public int getSpanSize(int position) {
                return getSpanNum(selectedFriendsAdapter.getItemUserNm(position));
            }
        });
    }


    private void thisFragmentUiInit() {
        if(prefManager.getSnsType().equals("0")){
            imageViewFriendListTitle.setImageResource(R.drawable.ico_userlist_title_facebook);
            textViewFriendListTitle.setText(getResources().getString(R.string.invite_fb_list));
        }else if(prefManager.getSnsType().equals("1")){
            imageViewFriendListTitle.setImageResource(R.drawable.ico_userlist_title_google);
            textViewFriendListTitle.setText(getResources().getString(R.string.invite_google_list));
        }

        if (friendList.size() == 0) {
            friendNoResult.setVisibility(View.VISIBLE);
            friendSearchLayout.setVisibility(View.GONE);
            friend_no_result_img.setImageResource(R.drawable.thumb_list_friendslist);
            friend_no_result_text.setText(getResources().getString(R.string.invite_guide));
        } else {
            friendNoResult.setVisibility(View.GONE);
            friendSearchLayout.setVisibility(View.VISIBLE);
            friend_no_result_img.setImageResource(R.drawable.thumb_userlist_search);
            friend_no_result_text.setText(getResources().getString(R.string.global_search_noresult));
        }

    }


    private void setUiTransform(boolean isCheck, String data) {
        if (isCheck) {
            spanCount += getSpanNum(data);
        } else {
             spanCount -= getSpanNum(data);
        }

        if (selectedFriendList.size() !=0 ) {
            selectFriendLayout.setVisibility(View.VISIBLE);
        } else {
            selectFriendLayout.setVisibility(View.GONE);
            spanCount = 0;
        }
        Log.d("selectedFriend Count", Integer.toString(selectedFriendsAdapter.getItemCount()));

        if (selectedFriendsAdapter.getItemCount() <= getRowCount()) {
            selectUsersMoreViewBtn.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dencityHeight);
            selectedUsersList.setLayoutParams(params);
            isSelectFriendOpen = false;

        } else if(spanCount > mLayoutManager.getSpanCount()) {
            selectUsersMoreViewBtn.setVisibility(View.VISIBLE);
            if (isSelectFriendOpen) {
                selectUsersMoreViewBtn.setText(getResources().getString(R.string.invite_seemore_fold));
            } else {
                selectUsersMoreViewBtn.setText(String.format(getResources().getString(R.string.invite_seemore),Integer.toString(selectedFriendList.size() - getRowCount())));
            }
        }
    }


    public int getRowCount() {
        int spanSum = 0;
        int rowCnt = 0;
        for (int i=0; i<selectedFriendList.size();i++) {
            spanSum += getSpanNum(selectedFriendsAdapter.getItemUserNm(i));
            if (spanSum < mLayoutManager.getSpanCount())
                rowCnt++;
            else
                break;
        }
        return rowCnt;
    }
    public int getSpanNum(String name){

        View view = getActivity().getLayoutInflater().inflate(R.layout.row_select_user_chips,null,false);
        ((TextView) view.findViewById(R.id.select_usernm)).setText(name);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d("viewRect",Integer.toString(view.getMeasuredWidth()));
        int width = view.getMeasuredWidth() + dencityMargin;

        return width;
    }


    private void sendInvite(String roomId, String roomTitle, String receiveUserId, String userNo, String userNm) {

        String inviteDataform = getResources().getString(R.string.history_recv_HC003_IV001);
        String msg = String.format(inviteDataform, userNm, roomTitle);

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
                        initclear();
                        JSONArray inviteUserArr = response.getJSONArray("absence");
                        RoomActivity.activity.addOtherUserListHandler(inviteUserArr);
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

    private void initclear() {

        selectedFriendsAdapter.notifyDataSetChanged();
        snsFriendListAdapter.notifyDataSetChanged();
        selectFriendLayout.setVisibility(View.GONE);
        selectUsersMoreViewBtn.setVisibility(View.GONE);

    }


    // 친구 검색 adapter
    public class FriendListAdapter extends BaseAdapter {

        private  Context mContext;

        private ArrayList<FriendUser> list;

        public FriendListAdapter(Context context, ArrayList<FriendUser> list) {
            this.mContext = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public FriendUser getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rows_friend_list, null);
            }

            FriendUser item = getItem(position);

            String userId = item.getId();
            String userNm = item.getUserNm();
            String userThumbnail = item.getUserThumbnail();

            TextView friendNmTextView = (TextView) convertView.findViewById(R.id.friend_name);
            ImageView friendThumbnailView = (ImageView) convertView.findViewById(R.id.friend_thumbnail_icon);

            Glide.with(getActivity()).load(userThumbnail).transform(new CircleTransformTemp(getActivity())).into(friendThumbnailView);
//            Picasso.with(getContext()).load(userThumbnail).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(friendThumbnailView);  // 썸네일 이미지 로드
            friendNmTextView.setText(userNm);


            if(list.get(position).IsChecked()) {
                //Log.d("SNSFriendList : ", list.get(position).getUserNm() + "is selected");
                convertView.setBackgroundColor(Color.parseColor("#ebeced"));
                friendNmTextView.setPaintFlags(friendNmTextView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            } else {
                //Log.d("SNSFriendList : ", list.get(position).getUserNm() + "is not selected");
                convertView.setBackgroundColor(Color.parseColor("#ffffff"));
                friendNmTextView.setPaintFlags(friendNmTextView.getPaintFlags() &~ Paint.FAKE_BOLD_TEXT_FLAG);
            }


            return convertView;
        }

        public void toggleChecked(int position) {
            list.get(position).setIschecked();
            notifyDataSetChanged();
        }

    }
}