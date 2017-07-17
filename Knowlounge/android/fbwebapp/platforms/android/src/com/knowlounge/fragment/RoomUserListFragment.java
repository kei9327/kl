package com.knowlounge.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.apprtc.KlgePeerNode;
import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.model.RoomUser;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.OtherUser;
import com.knowlounge.util.RestClient;
import com.knowlounge.util.RoomUtils;
import com.knowlounge.view.room.RoomUserPresenter;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by Minsu on 2015-12-15.
 */
public class RoomUserListFragment extends Fragment implements RoomActivity.notiChangeData, KlgePeerWatcher.RtcListListener {

    private final String TAG = "RoomUserListFragment";

    // 상태를 표현하는 상수
    private final int CREATE = 0;
    private final int NOMAL = 1;
    private final int GUEST = 2;

    // 3점 메뉴 타입
    private final int ZERO = 3;
    private final int ONE = 4;
    private final int TWO = 5;
    private final int THREE = 6;
    private final int FOUR = 7;
    private final int FIVE = 8;
    private final int SIX = 9;

    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView recyclerView;

    private boolean isMyClass = false;

    public static RoomUserListFragment getInstance() { return new RoomUserListFragment();}

    private View rootView;
    private WenotePreferenceManager prefManager;

    private RoomUserPresenter mRoomUserPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            RoomActivity.setOnNotiChangeData(this);
            RoomActivity.activity.getPeerWatcher().addRtcListListener(this);

            prefManager = WenotePreferenceManager.getInstance(getActivity());
            isMyClass = RoomActivity.activity.getTeacherFlag();
            mRoomUserPresenter = ((RoomActivity) context).getRoomUserPresenter();

        } catch(ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mRoomUserList = mRoomUserPresenter.getRoomUserList();
        ((RoomActivity)getActivity()).getPeerWatcher();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - " + getActivity().getClass().getSimpleName());
        rootView = inflater.inflate(R.layout.user_list_fragment, container, false);

        sectionAdapter = new SectionedRecyclerViewAdapter();

        sectionAdapter.addSection("user_list", new UserListDefaultSection("나의 프로필", RoomActivity.myInfoList, RoomActivity.otherUserList, true, 1));
        sectionAdapter.addSection("class_user_list", new ClassUserSection(getResources().getString(R.string.userlist_class), RoomActivity.classUserList));
        sectionAdapter.addSection("other_user_list", new UserListDefaultSection(getResources().getString(R.string.userlist_notattended), RoomActivity.myInfoList, RoomActivity.otherUserList, false, 0));

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sectionAdapter);
        sectionAdapter.notifyDataSetChanged();

        checkMyroom(isMyClass);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "onDestroy");
        super.onDestroy();

        for(RoomUser classUser : RoomActivity.classUserList) {
            classUser.setIsExpanded(false);
        }
    }

    @Override
    public void onNotiChangeData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sectionAdapter.notifyDataSetChanged();
            }
        });
    }


    /*
     * -----------------------------------------------------
     * --<implements : RtcListListener>
     */
    @Override
    public void onPeerList() {
        Log.d(TAG, "<onPeerList / ZICO> 새로운 rtcList를 받았습니다.");

        RoomActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sectionAdapter.notifyDataSetChanged();
            }
        });

    }


    class UserListDefaultSection extends StatelessSection {
        private String title;
        private List<RoomUser> userList;
        private List<OtherUser> otherUserList;
        private boolean expanded = true;
        private boolean isUserList;
        private View layout;
        private PopupWindow popup;
        private int sectionNum;

        public UserListDefaultSection(String title, List<RoomUser> userList, List<OtherUser> otherUserList, boolean isUserList, int sectionNum) {
            super(R.layout.user_list_default_header, R.layout.user_list_default_item);
            this.title = title;

            if(isUserList)
                this.userList = userList;
            else
                this.otherUserList = otherUserList;

            this.isUserList = isUserList;
            this.sectionNum = sectionNum;
        }

        @Override
        public int getContentItemsTotal() {
            return expanded ? (isUserList ? userList.size() : otherUserList.size()) : 0;
        }

        //header
        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new UserListDefaultHeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            final UserListDefaultHeaderViewHolder headerHolder = (UserListDefaultHeaderViewHolder) holder;
            headerHolder.user_list_title.setText(title);

//            if (sectionNum == 1) {
//                headerHolder.userListTitleLayout.setBackgroundResource(R.drawable.user_list_title_style_section_first);
//            }else{
//                headerHolder.userListTitleLayout.setBackgroundResource(R.drawable.user_list_title_style);
//            }

            if(!isUserList && otherUserList.size()==0 && expanded)
                headerHolder.other_user_list_empty.setVisibility(View.VISIBLE);
            else
                headerHolder.other_user_list_empty.setVisibility(View.GONE);

            headerHolder.btn_user_list_fold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    headerHolder.btn_user_list_fold.setImageResource(
                            expanded ? R.drawable.btn_userlist_categoryfold : R.drawable.btn_userlist_categoryfold
                    );
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        }

        //item
        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new UserListDefaultItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final UserListDefaultItemViewHolder itemHolder = (UserListDefaultItemViewHolder) holder;
            final RoomUser user = userList.get(position);
            if(isUserList) {
                if (RoomActivity.activity.isVideoControl()) {
                    if (RoomActivity.activity.isTeacher()) {
                        KlgePeerNode peerNode = ((RoomActivity) getActivity()).getPeerWatcher().getPeerNode(user.getUserNo());
                        if (peerNode != null) {
                            itemHolder.mVideoContrlBtn.setVisibility(View.VISIBLE);
                            final boolean isUserCaller = peerNode.getPeer().isCaller();
                            if (isUserCaller) {
                                itemHolder.mVideoContrlBtn.setImageResource(R.drawable.btn_userlist_cam_on);
                            } else {
                                itemHolder.mVideoContrlBtn.setImageResource(R.drawable.btn_userlist_cam);
                            }
                        }
                    } else {
                        KlgePeerNode peerNode = ((RoomActivity) getActivity()).getPeerWatcher().getPeerNode(user.getUserNo());
                        if (peerNode != null) {
                            itemHolder.mVideoContrlBtn.setVisibility(View.VISIBLE);
                            final boolean isUserCaller = peerNode.getPeer().isCaller();
                            if (isUserCaller) {
                                itemHolder.mVideoContrlBtn.setImageResource(R.drawable.btn_userlist_cam_on);
                            } else {
                                itemHolder.mVideoContrlBtn.setImageResource(R.drawable.btn_userlist_cam);
                            }

                            itemHolder.mVideoContrlBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (RoomActivity.activity.isTeacher()) {
                                        if (isUserCaller) {
                                            // connect 날리기
                                            RoomActivity.activity.denyVideoPermission(user.getUserNo());
                                        } else {
                                            // connect 날리기
                                            RoomActivity.activity.allowVideoPermission(user.getUserNo());
                                        }
                                    } else {
                                        if (isUserCaller) {

                                        } else {
                                            // connect 날리기
                                            RoomActivity.activity.requestVideoPermission(user.getUserNo());
                                        }
                                    }
                                }
                            });
                        }
                    }
                } else {
                    itemHolder.mVideoContrlBtn.setVisibility(View.GONE);
                }

                itemHolder.btn_userlist_option.setVisibility(View.VISIBLE);
                itemHolder.btn_retry_invite.setVisibility(View.GONE);
                userListFlow(itemHolder, userList.get(position));

                if (userList.size()-1 == position)
                    itemHolder.user_list_default_under_line.setVisibility(View.INVISIBLE);
                else
                    itemHolder.user_list_default_under_line.setVisibility(View.VISIBLE);

            } else {
                itemHolder.btn_userlist_option.setVisibility(View.GONE);
                itemHolder.btn_retry_invite.setVisibility(View.VISIBLE);
                otherUserListFlow(itemHolder, otherUserList.get(position));
            }
        }


        //UserList Flow - start
        private void userListFlow(final UserListDefaultItemViewHolder itemHolder, final RoomUser user) {

            setItemContent(itemHolder, user);
            final int myState = getMyState();
            final int type = getMenuType(myState, user);

            if(type == ZERO)
                itemHolder.btn_userlist_option.setVisibility(View.GONE);

            itemHolder.btn_userlist_option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (type != ZERO) {
                        displayPopupWindow(v, type, user);
                    }
                }
            });
        }

        //내 상태 구하는 메소드
        public int getMyState() {
            if(RoomActivity.activity.getGuestFlag())
                return GUEST;

            if(RoomActivity.activity.getCreatorFlag())
                return CREATE;

            return NOMAL;
        }

        //상황별로 메뉴 타입 구하는 메소드
        public int getMenuType(int myState, RoomUser user) {
            boolean hasAuth = RoomActivity.activity.getMasterFlag();
            boolean isThisRowMe = RoomActivity.activity.getUserNo().equals(user.getUserNo()) ? true : false;

            if(myState == GUEST)    // 내 상태가 게스트 인가?
                return ZERO;

            if(myState == CREATE){  // 내 상태가 개설자 인가?
                if(hasAuth){  // 개설자 이면서 권한을 가지고 있는가?
                    if(isThisRowMe) //나인가?
                        return ZERO;   //개설자 -> 권한O -> 나
                    else {
                        if (user.isGuest())
                            return TWO;   //개설자 -> 권한 o -> 다른사람(게스트O)
                        else
                            return ONE;   //개설자 -> 권한O -> 다른사람(게스트X)
                    }
                }else{
                    if(isThisRowMe){ //나인가?
                        return FIVE;  //개설자 -> 권한X -> 나
                    }else{
                        if(user.isMaster())
                            return TWO; //개설자 -> 권한 X  -> 다른사람 -> 다른사람 권한O
                        else
                            return ONE; //개설자  -> 권한 X -> 다른사람 -> 다른사람 권한X
                    }
                }
            }else if(myState == NOMAL){  // 내 상태가 일반 인가?
                if(hasAuth) {   //일반 이면서 권한을 가지고 있는가?
                    if(isThisRowMe)//나인가?
                        return ZERO;   //일반 -> 권한O -> 나
                    else
                        return FOUR; //일반 -> 권한O -> 다른사람
                }else{
                    if(isThisRowMe) //나인가?
                        return THREE; //일반 -> 권한X -> 나
                    else
                        return FOUR; //일반 -> 권한X -> 다른사람
                }
            }

            return ZERO;
        }

        //UserList item 이름, 마스터색, thumbnail등 셋팅 하는 메소드
        private void setItemContent(UserListDefaultItemViewHolder holder, RoomUser user) {

            boolean thisRowMaster = user.isMaster();
            String thisRowThumbNail = user.getThumbnail();
            boolean thisRowGuest = user.isGuest();
            String thisRowUserType = user.getUserType();

            //이름 넣고
            holder.row_title.setText(user.getUserNm());

            // 게스트일 경우 디폴트 썸네일 이미지 출력
            if (thisRowGuest) {
                Glide.with(getContext())
                        .load(R.drawable.img_userlist_default01)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .bitmapTransform(new CircleTransformTemp(getContext()))
                        .into(holder.user_thumbnail_view);
            } else {  // 게스트가 아닐 경우..
                Glide.with(getContext())
                        .load(thisRowThumbNail)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.img_userlist_default01)
                        .bitmapTransform(new CircleTransformTemp(getContext()))
                        .into(holder.user_thumbnail_view);

                if(thisRowUserType.equals("2"))
                    Glide.with(getContext()).load(R.drawable.ico_userlist_teacher).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.user_type_thumbnail);
                else
                    Glide.clear(holder.user_type_thumbnail);
            }

            //배경색 넣고
            if (thisRowMaster) {
                holder.rootView.setBackgroundColor(Color.parseColor("#F0FAFC"));
                holder.user_roll_text.setText(getResources().getString(R.string.userlist_presenter));
            } else {
                holder.rootView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                holder.user_roll_text.setText("");
            }
        }


        //UserList Option Button 메뉴 메소드
        private void displayPopupWindow(View anchorView, int type, RoomUser user){
            popup = new PopupWindow(getActivity());
            layout = getActivity().getLayoutInflater().inflate(R.layout.user_list_sub_menu, null);
            popup.setContentView(layout);

            // Set content width and height
            popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            // Closes the popup window when touch outside of it - when looses focus
            popup.setOutsideTouchable(true);
            popup.setFocusable(true);
            // Show anchored to button
            popup.setBackgroundDrawable(new BitmapDrawable());
            popup.showAsDropDown(anchorView);

            setTypeView( type, user);
        }


        //Option 메뉴 타입별로 이벤트 다르게 주기
        private void setTypeView(final int type, final RoomUser user){
            LinearLayout user_list_submenu_ban = (LinearLayout) layout.findViewById(R.id.user_list_submenu_ban);
            LinearLayout user_list_submenu_auth = (LinearLayout) layout.findViewById(R.id.user_list_submenu_auth);
            LinearLayout user_list_submenu_chat = (LinearLayout) layout.findViewById(R.id.user_list_submenu_chat);

            ImageView user_list_submenu_auth_img = (ImageView) layout.findViewById(R.id.user_list_submenu_auth_img);
            TextView user_list_submenu_auth_txt = (TextView) layout.findViewById(R.id.user_list_submenu_auth_txt);

            switch (type) {
                case ONE :  //강퇴, 권한주기, 1:1채팅
                    user_list_submenu_ban.setVisibility(View.VISIBLE);
                    user_list_submenu_auth.setVisibility(View.VISIBLE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);

                    user_list_submenu_auth_img.setImageResource(R.drawable.ico_userlist_send_authority);
                    user_list_submenu_auth_txt.setText(getResources().getString(R.string.userlist_auth_set));
                    user_list_submenu_auth.setOnClickListener(RoomActivity.activity.setAuthChangeEvent(popup,"send_authority", user.getUserId()));

                    break;
                case TWO :  // 강퇴 , 1:1 채팅
                    user_list_submenu_ban.setVisibility(View.VISIBLE);
                    user_list_submenu_auth.setVisibility(View.GONE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);
                    break;
                case THREE : //권한회수, 1:1채팅
                    user_list_submenu_ban.setVisibility(View.GONE);
                    user_list_submenu_auth.setVisibility(View.VISIBLE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);

                    user_list_submenu_auth_img.setImageResource(R.drawable.ico_userlist_get_authority);
                    user_list_submenu_auth_txt.setText(getResources().getString(R.string.userlist_auth_get));
                    user_list_submenu_auth.setOnClickListener(RoomActivity.activity.setAuthChangeEvent(popup,"get_authority", user.getUserId()));
                    break;
                case FOUR : // 1:1 채팅
                    user_list_submenu_ban.setVisibility(View.GONE);
                    user_list_submenu_auth.setVisibility(View.GONE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);
                    break;
                case FIVE : // 권한회수;
                    user_list_submenu_ban.setVisibility(View.GONE);
                    user_list_submenu_auth.setVisibility(View.VISIBLE);
                    user_list_submenu_chat.setVisibility(View.GONE);

                    user_list_submenu_auth_img.setImageResource(R.drawable.ico_userlist_get_authority);
                    user_list_submenu_auth_txt.setText(getResources().getString(R.string.userlist_auth_get));
                    user_list_submenu_auth.setOnClickListener(RoomActivity.activity.setAuthChangeEvent(popup,"get_authority", user.getUserId()));
                    break;
            }

            user_list_submenu_ban.setOnClickListener(RoomActivity.activity.setDeportUserEvent(popup, user.getUserNo(), user.getUserId(), user.getUserNm()));
            user_list_submenu_chat.setOnClickListener(RoomActivity.activity.setChattingWisperEvent(popup, user.getUserId()));
        }

        //UserList Flow - end

        private void otherUserListFlow(UserListDefaultItemViewHolder itemHolder, final OtherUser otherUser) {
            String userNm = otherUser.getUserNm();
            String thumbNail = otherUser.getThumbnail();
            String userType = otherUser.getUserType();

            itemHolder.row_title.setText(userNm);

            Glide.with(getContext()).load(thumbNail).diskCacheStrategy(DiskCacheStrategy.SOURCE).bitmapTransform(new CircleTransformTemp(getContext())).into(itemHolder.user_thumbnail_view);

            if (userType.equals("2"))
                Glide.with(getContext()).load(R.drawable.ico_userlist_teacher).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(itemHolder.user_type_thumbnail);
            else
                Glide.clear(itemHolder.user_type_thumbnail);

            itemHolder.btn_retry_invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
                    builder.setMessage(getResources().getString(R.string.global_popup_reinvite)).setCancelable(true)
                            .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                    String roomId = RoomActivity.activity.getRoomId();
                                    String roomTitle = RoomActivity.activity.getRoomTitle();
                                    String userNo = RoomActivity.activity.getUserNo();
                                    String userNm = RoomActivity.activity.getUserNm();

                                    String receiverUserNo = otherUser.getUserNo();
                                    String receiverUserId = otherUser.getUserId();

                                    sendInvite(getContext(), roomId, roomTitle, receiverUserNo, receiverUserId, userNo, userNm);
                                }
                            })
                            .setNegativeButton(getContext().getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
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

        }

        private void sendInvite(final Context context, String roomId, String roomTitle, String receiveUserNo, String receiveUserId, String userNo, String userNm) {

            WenotePreferenceManager prefManager = WenotePreferenceManager.getInstance(context);

            String inviteDataform = context.getResources().getString(R.string.history_recv_HC003_IV001);
            String msg = String.format(inviteDataform, userNm, roomTitle) ;

            RequestParams params = new RequestParams();
            params.put("roomid", roomId);
            params.put("title", roomTitle.replace("'", "\\'"));
            params.put("msg", msg);
            params.put("userno", receiveUserNo);
            params.put("userid", receiveUserId);
            params.put("guserno", userNo);
            params.put("gusernm", userNm);

            String masterCookie = prefManager.getUserCookie();
            String checksumCookie = prefManager.getChecksumCookie();

            if (TextUtils.isEmpty(masterCookie) || TextUtils.isEmpty(checksumCookie)) {
                // TODO : 예외처리
            }

            RestClient.postWithCookie("invite/send.json", masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "send message success.. response : " + response.toString());
                    try {
                        String resultCode = response.getString("result");
                        if ("0".equals(resultCode)) {
                            Toast.makeText(context, context.getString(R.string.toast_invite_send), Toast.LENGTH_SHORT).show();
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

    }


    // 클래스 유저 리스트
    class ClassUserSection extends StatelessSection {
        String title;
        List<RoomUser> list;
        boolean expanded = true;
        private String svrHost;

        private View layout;
        private PopupWindow popup;

        public ClassUserSection(String title, List<RoomUser> list) {
            super(R.layout.user_list_class_header, R.layout.user_list_class_footer, R.layout.user_list_class_item);
            this.title = title;
            this.list = list;
            String svrFlag = getContext().getString(R.string.svr_flag);
            svrHost = getContext().getResources().getString(getContext().getResources().getIdentifier("svr_host_" + svrFlag, "string", getContext().getPackageName()));
        }

        @Override
        public int getContentItemsTotal() {
            return expanded ? list.size() : 0;
        }


        /*
         *----------------------------------------------------------------------------------------------
         *--<Header>
         */
        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new UserListClassHeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            final UserListClassHeaderViewHolder headerHolder = (UserListClassHeaderViewHolder) holder;

            headerHolder.user_list_title.setText(title);
            headerHolder.class_user_list_cnt.setText(RoomActivity.classUserList.size()+"");
            headerHolder.other_user_list_cnt.setText(RoomActivity.otherUserList.size()+"");
            headerHolder.btn_user_list_fold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    headerHolder.btn_user_list_fold.setImageResource(
                            expanded ? R.drawable.btn_userlist_categoryfold : R.drawable.btn_userlist_categoryfold
                    );

                    if(prefManager.getCurrentTeacherUserNo().equals(prefManager.getUserNo())){
                        setHasFooter(expanded);
                    }
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        }


        /*
         *----------------------------------------------------------------------------------------------
         *--<Footer>
         */
        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return new UserListClassFooterViewHolder(view);
        }

        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            final UserListClassFooterViewHolder footerHolder = (UserListClassFooterViewHolder ) holder;

            if (RoomActivity.classUserList.size() == 0) {
                footerHolder.btn_call_student.setBackgroundColor(Color.parseColor("#b3f3c3"));
                footerHolder.btn_call_student.setClickable(false);
            } else {
                footerHolder.btn_call_student.setBackgroundColor(getResources().getColor(R.color.app_base_color));
                footerHolder.btn_call_student.setClickable(true);

            }
            footerHolder.btn_call_student.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RoomActivity.activity.callAllStudent();
                }
            });
        }


        /*
         *----------------------------------------------------------------------------------------------
         *--<Body>
         */
        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new UserListClassItemViewHolder(view);
        }


        /**
         * 클래스 유저 목록의 각 참여자 항목을 렌더링하는 메서드이다.
         * @param holder
         * @param position
         */
        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final UserListClassItemViewHolder itemHolder = (UserListClassItemViewHolder) holder;
            final RoomUser user = list.get(position);
            final boolean thisRowTeacher = prefManager.getCurrentTeacherUserNo().equals(user.getUserNo());
            final boolean thumbNailExpanded = user.getIsExpanded();

            final ImageView subRoomThumb = ((UserListClassItemViewHolder) holder).sub_room_thumbnail;
            final String userRoomId = user.getUserRoomId();
            final String userRoomCode = user.getUserRoomCode();

            Log.d(TAG, "<onBindItemViewHolder> userRoomId : " + userRoomId + ", userRoomSeqNo : " + userRoomCode);
//            if( list.size()-1 == position && !hasFooter() )
//                itemHolder.user_list_class_under_line.setVisibility(View.INVISIBLE);
//            else
//                itemHolder.user_list_class_under_line.setVisibility(View.VISIBLE);

            setItemContent(itemHolder, user, thisRowTeacher,thumbNailExpanded);
            isExpendedView(itemHolder, thumbNailExpanded);

            final int myState = getMyState();
            final int type = getMenuType(myState, user);

            if (RoomActivity.activity.isVideoSeparate()) {

            } else {

            }

            if (RoomActivity.activity.isVideoControl()) {
                if (RoomActivity.activity.isTeacher()) {
                    if (user.isConnectedRoomSeparate()) {
                        itemHolder.mVideoContrlBtn.setVisibility(View.GONE);
                    } else {
                        itemHolder.mVideoContrlBtn.setVisibility(View.VISIBLE);
                        KlgePeerNode peerNode = ((RoomActivity) getActivity()).getPeerWatcher().getPeerNode(user.getUserNo());
                        if (peerNode != null) {
                            final boolean isUserCaller = peerNode.getPeer().isCaller();
                            if (isUserCaller) {
                                itemHolder.mVideoContrlBtn.setImageResource(R.drawable.btn_userlist_cam_on);
                            } else {
                                itemHolder.mVideoContrlBtn.setImageResource(R.drawable.btn_userlist_cam);
                            }

                            itemHolder.mVideoContrlBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (RoomActivity.activity.isTeacher()) {
                                        if (isUserCaller) {
                                            // connect 날리기
                                            RoomActivity.activity.denyVideoPermission(user.getUserNo());
                                        } else {
                                            // connect 날리기
                                            RoomActivity.activity.allowVideoPermission(user.getUserNo());
                                        }
                                    } else {
                                        if (isUserCaller) {

                                        } else {
                                            // connect 날리기
                                            RoomActivity.activity.requestVideoPermission(user.getUserNo());
                                        }
                                    }
                                }
                            });
                        }
                    }
                } else {
                    itemHolder.mVideoContrlBtn.setVisibility(View.GONE);
                }
            } else {
                itemHolder.mVideoContrlBtn.setVisibility(View.GONE);
            }

            // 유저 옵션 메뉴 버튼의 클릭 이벤트
            itemHolder.mUserListOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (type != ZERO) {
                        showExtendMenu(v, type, user);
                    }
                }
            });

            // 서브룸 메뉴 확장시켜 보여주는 버튼의 클릭 이벤트
            itemHolder.mExpandSubroomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(user.getUserRoomId()) || TextUtils.isEmpty(user.getUserRoomCode())) {
                        Toast.makeText(getContext(), "생성된 서브룸이 아직 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (list.get(position).getIsExpanded()) {
                        itemHolder.mExpandSubroomBtn.setImageResource(R.drawable.btn_userlist_preview);
                        itemHolder.expanding_layout.setVisibility(View.GONE);
//                        if(thisRowTeacher)
//                            itemHolder.rootView.setBackgroundColor(Color.parseColor("#ffffff"));
//                        else
//                            itemHolder.rootView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    } else {
                        itemHolder.mExpandSubroomBtn.setImageResource(R.drawable.btn_userlist_preview_on);
                        itemHolder.expanding_layout.setVisibility(View.VISIBLE);
//                        if(thisRowTeacher)
//                            itemHolder.rootView.setBackgroundColor(Color.parseColor("#f0fafc"));
//                        else
//                            itemHolder.rootView.setBackgroundColor(Color.parseColor("#f0fafc"));
                    }
                    list.get(position).setIsExpanded(!list.get(position).getIsExpanded());
                    isExpendedView(itemHolder, list.get(position).getIsExpanded());
                }
            });

            // 서브룸으로 이동하는 버튼 이벤트..
            itemHolder.btn_rightmenu_preview_shortcuts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(user.getUserRoomId()) || TextUtils.isEmpty(user.getUserRoomCode())) {

                        String parentRoomId = RoomActivity.activity.getRoomId();
                        String deviceId = prefManager.getDeviceId();
                        String master = prefManager.getUserCookie();
                        String checksum = prefManager.getChecksumCookie();
                        RoomUtils.moveMyRoom(parentRoomId, deviceId, master, checksum);
                        ((RoomActivity) getActivity()).moveMyRoom(null);
                    } else {
                        String roomCode = user.getUserRoomCode();
                        ((RoomActivity) getActivity()).moveRoom(roomCode, null);
                        //RoomUtils.moveRoom(roomCode);
                    }
                }
            });

            // 서브 룸의 썸네일을 갱신하는 버튼의 이벤트 정의..
            itemHolder.btn_rightmenu_preview_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String roomThumbnail = (!TextUtils.isEmpty(userRoomId) || !TextUtils.isEmpty(userRoomCode)) ?
                            svrHost + "data/fb/room/" + userRoomId.substring(0, 3) + "/" + userRoomId + "_01.jpg" : "";
                    Log.d(TAG, "Subroom thumbnail refresh / roomThumbnail : " + roomThumbnail);
                    int seqNoNum = Integer.parseInt(user.getUserRoomCode());
                    Glide.clear(subRoomThumb);
                    Glide.with(getContext())
                            .load(roomThumbnail)
                            .error(getContext().getResources().getIdentifier("thumbnail_0" + ((seqNoNum % 4) + 1), "drawable", getContext().getPackageName()))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            //.transform(new RoundedSquareTransform(getContext()))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(subRoomThumb);
                }
            });
        }


        //ClassUserList item 이름, 마스터색, thumbnail등 셋팅 하는 메소드
        private void setItemContent(UserListClassItemViewHolder holder, RoomUser user, boolean thisRowTeacher, boolean expand) {

            String userThumb = user.getThumbnail();
            String userType = user.getUserType();

            final String roomId = user.getUserRoomId();
            final String seqNo = user.getUserRoomCode();
            String roomThumbnail = (!TextUtils.isEmpty(roomId) || !TextUtils.isEmpty(seqNo)) ? svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg" : "";

            String userNo = user.getUserNo();
            String userId = user.getUserId();
            String userNm = user.getUserNm();
            String connectedRoomId = user.getConnectedRoomId();
            String connectedRoomTitle = user.getConnectedRoomTitle();

            String parentRoomId = RoomActivity.activity.getParentRoomId();
            Log.d(TAG, "<setItemContent> userNm : " + userNm + ", parentRoomId : " + parentRoomId + ", connectedRoomId : " + connectedRoomId + ", connectedRoomTitle : " + connectedRoomTitle);

            //이름 넣고
            holder.row_title.setText(userNm);
            if (!TextUtils.isEmpty(connectedRoomId) && !parentRoomId.equals(connectedRoomId)) {
                holder.mUserRoomTitle.setText(user.getConnectedRoomTitle());
                holder.rootView.setBackgroundColor(Color.parseColor("#f0fafc"));
                holder.mExpandSubroomBtn.setVisibility(View.VISIBLE);
                holder.mUserListOptions.setVisibility(View.GONE);
            } else {
                holder.rootView.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.mUserRoomTitle.setVisibility(View.GONE);
                holder.mExpandSubroomBtn.setVisibility(View.GONE);
                holder.mUserListOptions.setVisibility(View.VISIBLE);
            }

            if (userId.equals(userNo)) {
                // 게스트 일 경우..
                Glide.with(getContext())
                    .load(R.drawable.img_userlist_default01)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new CircleTransformTemp(getContext()))
                    .into(holder.user_thumbnail_view);
            } else {
                // 게스트가 아니면
                Glide.with(getContext())
                    .load(userThumb)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new CircleTransformTemp(getContext()))
                    .error(getContext().getResources().getIdentifier("img_userlist_default0" + (TextUtils.equals(seqNo, "") ? 0 : Integer.parseInt(seqNo)%2+1), "drawable", getContext().getPackageName()))
                    .into(holder.user_thumbnail_view);



                if (!TextUtils.isEmpty(connectedRoomId) && !parentRoomId.equals(connectedRoomId)) {
                    Glide.with(getContext())
                        .load(R.drawable.ico_userlist_board_move)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(holder.user_type_thumbnail);
                } else {
                    if (userType.equals("2"))
                        Glide.with(getContext())
                            .load(R.drawable.ico_userlist_teacher)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(holder.user_type_thumbnail);
                    else
                        Glide.clear(holder.user_type_thumbnail);
                }
            }
            // 룸 썸네일 처리
            if (!seqNo.equals("")) {
                int seqNoNum = Integer.parseInt(seqNo);
                Log.d(TAG, "<setItemContent> roomThumbnail : " + roomThumbnail);
                Glide.with(getContext())
                    .load(roomThumbnail)
                    .error(getContext().getResources().getIdentifier("thumbnail_0" + ((seqNoNum % 4) + 1), "drawable", getContext().getPackageName()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    //.transform(new RoundedSquareTransform(getContext()))
                    .into(holder.sub_room_thumbnail);
            } else {

            }

            // 배경색 넣고
//            if (thisRowTeacher) {
//                holder.rootView.setBackgroundColor(Color.parseColor("#F3FAF5"));
//                holder.txt_class_user_roll.setText(getResources().getString(R.string.userlist_teacher));
//            } else {
//                if (expand)
//                    holder.rootView.setBackgroundColor(Color.parseColor("#F2F5F8"));
//                else
//                    holder.rootView.setBackgroundColor(Color.parseColor("#FFFFFF"));
//                holder.txt_class_user_roll.setText("");
//            }

        }
        //ClassUserList item 이 펼쳐 졌는지 아닌지 판별
        private void isExpendedView(UserListClassItemViewHolder holder, boolean expand){
            if(expand){
                holder.btn_rightmenu_preview_refresh.setVisibility(View.VISIBLE);
                holder.btn_rightmenu_preview_shortcuts.setVisibility(View.VISIBLE);
            }else{
                holder.btn_rightmenu_preview_refresh.setVisibility(View.GONE);
                holder.btn_rightmenu_preview_shortcuts.setVisibility(View.GONE);
            }
        }


        //UserList Option Button 메뉴 메소드
        private void showExtendMenu(View anchorView, int type, RoomUser user){
            popup = new PopupWindow(getActivity());
            layout = getActivity().getLayoutInflater().inflate(R.layout.user_list_sub_menu, null);
            popup.setContentView(layout);

            // Set content width and height
            popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            // Closes the popup window when touch outside of it - when looses focus
            popup.setOutsideTouchable(true);
            popup.setFocusable(true);
            // Show anchored to button
            popup.setBackgroundDrawable(new BitmapDrawable());
            popup.showAsDropDown(anchorView);

            setTypeView(type, user);
        }


        //Option 메뉴 타입별로 이벤트 다르게 주기
        private void setTypeView(final int type, final RoomUser user){
            LinearLayout user_list_submenu_ban = (LinearLayout) layout.findViewById(R.id.user_list_submenu_ban);
            LinearLayout user_list_submenu_auth = (LinearLayout) layout.findViewById(R.id.user_list_submenu_auth);
            LinearLayout user_list_submenu_chat = (LinearLayout) layout.findViewById(R.id.user_list_submenu_chat);

            ImageView user_list_submenu_auth_img = (ImageView) layout.findViewById(R.id.user_list_submenu_auth_img);
            TextView user_list_submenu_auth_txt = (TextView) layout.findViewById(R.id.user_list_submenu_auth_txt);

            switch (type) {
                case ONE :  //강퇴, 권한주기, 1:1채팅
                    user_list_submenu_ban.setVisibility(View.VISIBLE);
                    user_list_submenu_auth.setVisibility(View.VISIBLE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);

                    user_list_submenu_auth_img.setImageResource(R.drawable.ico_userlist_send_authority);
                    user_list_submenu_auth_txt.setText(getResources().getString(R.string.userlist_auth_set));
                    user_list_submenu_auth.setOnClickListener(RoomActivity.activity.setAuthChangeEvent(popup,"send_authority", user.getUserId()));

                    break;
                case TWO :  // 강퇴 , 1:1 채팅
                    user_list_submenu_ban.setVisibility(View.VISIBLE);
                    user_list_submenu_auth.setVisibility(View.GONE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);
                    break;
                case THREE : //권한회수, 1:1채팅
                    user_list_submenu_ban.setVisibility(View.GONE);
                    user_list_submenu_auth.setVisibility(View.VISIBLE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);

                    user_list_submenu_auth_img.setImageResource(R.drawable.ico_userlist_get_authority);
                    user_list_submenu_auth_txt.setText(getResources().getString(R.string.userlist_auth_get));
                    user_list_submenu_auth.setOnClickListener(RoomActivity.activity.setAuthChangeEvent(popup,"get_authority", user.getUserId()));
                    break;
                case FOUR : // 1:1 채팅
                    user_list_submenu_ban.setVisibility(View.GONE);
                    user_list_submenu_auth.setVisibility(View.GONE);
                    user_list_submenu_chat.setVisibility(View.VISIBLE);
                    break;
                case FIVE : // 권한회수;
                    user_list_submenu_ban.setVisibility(View.GONE);
                    user_list_submenu_auth.setVisibility(View.VISIBLE);
                    user_list_submenu_chat.setVisibility(View.GONE);

                    user_list_submenu_auth_img.setImageResource(R.drawable.ico_userlist_get_authority);
                    user_list_submenu_auth_txt.setText(getResources().getString(R.string.userlist_auth_get));
                    user_list_submenu_auth.setOnClickListener(RoomActivity.activity.setAuthChangeEvent(popup,"get_authority", user.getUserId()));
                    break;
            }

            user_list_submenu_ban.setOnClickListener(RoomActivity.activity.setDeportUserEvent(popup, user.getUserNo(), user.getUserId(), user.getUserNm()));
            user_list_submenu_chat.setOnClickListener(RoomActivity.activity.setChattingWisperEvent(popup, user.getUserId()));
        }


        //내 상태 구하는 메소드
        public int getMyState() {
            if(RoomActivity.activity.getGuestFlag())
                return GUEST;

            if(RoomActivity.activity.getCreatorFlag())
                return CREATE;

            return NOMAL;
        }

        //상황별로 메뉴 타입 구하는 메소드
        public int getMenuType(int myState, RoomUser user) {
            boolean hasAuth = RoomActivity.activity.getMasterFlag();
            boolean isThisRowMe = RoomActivity.activity.getUserNo().equals(user.getUserNo()) ? true : false;

            if (myState == GUEST)    // 내 상태가 게스트 인가?
                return ZERO;

            if (myState == CREATE) {  // 내 상태가 개설자 인가?
                if (hasAuth) {  // 개설자 이면서 권한을 가지고 있는가?
                    if(isThisRowMe) //나인가?
                        return ZERO;   //개설자 -> 권한O -> 나
                    else {
                        if (user.isGuest())
                            return TWO;   //개설자 -> 권한 o -> 다른사람(게스트O)
                        else
                            return ONE;   //개설자 -> 권한O -> 다른사람(게스트X)
                    }
                } else {
                    if (isThisRowMe) { //나인가?
                        return FIVE;  //개설자 -> 권한X -> 나
                    } else {
                        if (user.isMaster())
                            return TWO; //개설자 -> 권한 X  -> 다른사람 -> 다른사람 권한O
                        else
                            return ONE; //개설자  -> 권한 X -> 다른사람 -> 다른사람 권한X
                    }
                }
            } else if(myState == NOMAL) {  // 내 상태가 일반 인가?
                if (hasAuth) {   //일반 이면서 권한을 가지고 있는가?
                    if (isThisRowMe)    //나인가?
                        return ZERO;   //일반 -> 권한O -> 나
                    else
                        return FOUR; //일반 -> 권한O -> 다른사람
                }else{
                    if(isThisRowMe) //나인가?
                        return THREE; //일반 -> 권한X -> 나
                    else
                        return FOUR; //일반 -> 권한X -> 다른사람
                }
            }

            return ZERO;
        }
    }


    //UserListDefault 부분
    class UserListDefaultHeaderViewHolder extends RecyclerView.ViewHolder {
        private final View rootVIew;
        private final LinearLayout userListTitleLayout;
        private final TextView user_list_title;
        private final ImageView btn_user_list_fold;
        private final TextView other_user_list_empty;


        public UserListDefaultHeaderViewHolder(View itemView) {
            super(itemView);
            rootVIew = itemView;
            userListTitleLayout = (LinearLayout) itemView.findViewById(R.id.user_list_title_layout);
            user_list_title = (TextView) itemView.findViewById(R.id.user_list_title);
            btn_user_list_fold = (ImageView) itemView.findViewById(R.id.btn_user_list_fold);
            other_user_list_empty = (TextView) itemView.findViewById(R.id.other_user_list_empty);
        }
    }

    class UserListDefaultItemViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final View user_list_default_under_line;

        private final TextView row_title;
        private final TextView user_roll_text;
        private final TextView btn_retry_invite;

        private final ImageView mVideoContrlBtn;

        private final ImageView btn_userlist_option;
        private final ImageView user_thumbnail_view;
        private final ImageView user_type_thumbnail;

        public UserListDefaultItemViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            row_title = (TextView) itemView.findViewById(R.id.row_title);
            user_roll_text = (TextView) itemView.findViewById(R.id.user_roll_text);
            btn_retry_invite = (TextView) itemView.findViewById(R.id.btn_retry_invite);

            mVideoContrlBtn = (ImageView) itemView.findViewById(R.id.btn_video);

            user_thumbnail_view = (ImageView) itemView.findViewById(R.id.user_thumbnail_view);
            user_type_thumbnail = (ImageView) itemView.findViewById(R.id.user_type_thumbnail);
            btn_userlist_option = (ImageView) itemView.findViewById(R.id.btn_userlist_option);

            user_list_default_under_line = (View) itemView.findViewById(R.id.user_list_default_under_line);
        }
    }


    //UserListClass 부분
    class UserListClassHeaderViewHolder extends RecyclerView.ViewHolder {

        private final View rootVIew;
        private final TextView user_list_title;
        private final ImageView btn_user_list_fold;
        private final TextView class_user_list_cnt;
        private final TextView other_user_list_cnt;

        public UserListClassHeaderViewHolder(View itemView) {
            super(itemView);
            rootVIew = itemView;
            user_list_title = (TextView) itemView.findViewById(R.id.user_list_title);
            btn_user_list_fold = (ImageView) itemView.findViewById(R.id.btn_user_list_fold);

            class_user_list_cnt = (TextView) itemView.findViewById(R.id.class_user_list_cnt);
            other_user_list_cnt = (TextView) itemView.findViewById(R.id.other_user_list_cnt);
        }
    }


    class UserListClassItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView row_title;
        private final TextView mUserRoomTitle;
        private final TextView txt_class_user_roll;

        private final ImageView mVideoContrlBtn;
        private final ImageView mUserListOptions;
        private final ImageView mExpandSubroomBtn;

        private final ImageView user_thumbnail_view;
        private final ImageView user_type_thumbnail;
        //private final ImageView row_auth_icon;
        private final ImageView btn_rightmenu_preview_refresh;
        private final ImageView btn_rightmenu_preview_shortcuts;

        private final RelativeLayout expanding_layout;
        private final ImageView sub_room_thumbnail;

//        private final View user_list_class_under_line;

        public UserListClassItemViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            row_title = (TextView) itemView.findViewById(R.id.row_title);
            mUserRoomTitle = (TextView) itemView.findViewById(R.id.user_room_title);
            txt_class_user_roll = (TextView) itemView.findViewById(R.id.txt_class_user_roll);

            mVideoContrlBtn = (ImageView) itemView.findViewById(R.id.btn_control_cam);
            mUserListOptions = (ImageView) itemView.findViewById(R.id.btn_3dot_options);
            mExpandSubroomBtn = (ImageView) itemView.findViewById(R.id.btn_expand_subroom);

            user_thumbnail_view = (ImageView) itemView.findViewById(R.id.user_thumbnail_view);
            user_type_thumbnail = (ImageView) itemView.findViewById(R.id.user_type_thumbnail);
            //row_auth_icon = (ImageView) itemView.findViewById(R.id.row_auth_icon);
            btn_rightmenu_preview_refresh = (ImageView) itemView.findViewById(R.id.btn_rightmenu_preview_refresh);
            btn_rightmenu_preview_shortcuts = (ImageView) itemView.findViewById(R.id.btn_rightmenu_preview_shortcuts);
            expanding_layout = (RelativeLayout) itemView.findViewById(R.id.expanding_layout);
            sub_room_thumbnail = (ImageView) itemView.findViewById(R.id.sub_room_thumbnail);
//            user_list_class_under_line = (View) itemView.findViewById(R.id.user_list_class_under_line);
        }
    }


    class UserListClassFooterViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final Button btn_call_student;

        public UserListClassFooterViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            btn_call_student = (Button) itemView.findViewById(R.id.btn_call_student);
        }
    }



    private void checkMyroom(boolean isMyClass){
        if(RoomActivity.activity.getGuestFlag()) {
            sectionAdapter.getSection("class_user_list").setHasFooter(false);
            return;
        }

        if (isMyClass) {
            sectionAdapter.getSection("class_user_list").setHasFooter(true);
        } else {
            sectionAdapter.getSection("class_user_list").setHasFooter(false);
        }
    }

}