package com.knowlounge.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.adapter.RoomLeftDrawerListAdapter;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.ExpandableMenu;
import com.knowlounge.model.ExpandableMenuItemList;
import com.knowlounge.util.AndroidUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by we160303 on 2016-06-08.
 */
public class RoomLeftmenuFragment extends Fragment implements WenotePreferenceManager.OnPreferenceChangeListener {
    private final String TAG = "RoomLeftNavActivity";

    private static int currentMenu=-1;
    View rootView, header;

    @BindView(R.id.btn_leftmenu_back) ImageView btnLeftmenuBack;

    ImageView btnSetting, leftmenuRoomTitleEdit;
    EditText leftmenuRoomTitle;
    LinearLayout roomCodeCopy;
    TextView textViewMasterUserNm, roomCodeText;
    ExpandableListView drawerMainListview;

    private ArrayList<ExpandableMenu> group_list;
    private HashMap<ExpandableMenu, ArrayList<ExpandableMenuItemList>> group_list_item;
    public ArrayList<ExpandableMenuItemList> group_list_item_arr;

    private RoomLeftDrawerListAdapter mAdapter;
    private int lastClickedPosition = -1;

    private WenotePreferenceManager prefManager;

    @Override
    public void onPreferenceChanged(SharedPreferences preferences, String key) {
        Log.d(TAG, "onPreferenceChanged");
        if (key.equals("roomtitle")) {
            //leftmenuRoomTitle.setText(prefManager.getRoomTitle());
            if(RoomActivity.activity.getCreatorFlag())
                leftmenuRoomTitle.setText(prefManager.getRoomTitle());
            else
                ((TextView)rootView.findViewById(R.id.leftmenu_room_title_no_editable)).setText(prefManager.getRoomTitle());
        }
    }

    public interface SetLeftDrawerListener {
        void setRoomTitleFromLeftDrawer(String roomTitleParam);
        void onPenTool();
        void onEraserTool();
        void onShapeTool();
        void onTextTool();
        void onPhotoTool();
        void onImageTool();
        void onPdfTool();
        void onUndoTool();
        void onRedoTool();
        void onDeleteAll();
        void onMemoTool();
        void onHandMode();
        void onLaserTool();
        void onZoomIn();
        void onZoomOut();
        void onPoll();
        void onPollTmpList();
        void onPollCompletedList();
        void onUserList();
        void onUserVideo();
        void onChatting();
        void onComment();
        void onInvite();
        void onBookmark(boolean bookmarkFlag);
        void onShare();
        void onSaveCanvas();
        void onExitRoom();

        void onRoomSetting();
    }

    public SetLeftDrawerListener mCallback;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
        prefManager.registerOnPreferenceChangeListener(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_room_left_nav, container,false);

//        final String roomCode = getArguments().getString("roomcode");
//        final String roomTitle = getArguments().getString("roomtitle");

        final String roomCode = RoomActivity.activity.getRoomCode();
        final String roomTitle = RoomActivity.activity.getRoomTitle();

        mCallback = (SetLeftDrawerListener) RoomActivity.activity;
        header = getActivity().getLayoutInflater().inflate(R.layout.fragment_room_left_nav_header, null, false);

        ButterKnife.bind(this, header);

        Glide.with(getActivity())
                .load(AndroidUtils.changeSizeThumbnail(prefManager.getUserThumbnail(), 200))
                .error(getResources().getDrawable(R.drawable.img_userlist_default01))
                .transform(new CircleTransformTemp(getActivity())).into(((ImageView) header.findViewById(R.id.room_left_user_thumb)));

        setFindViewID();
        drawerMainListview.addHeaderView(header);

        // 진행중인 유저명 업데이트 - 2016.06.15 삭제
        /*
        String currentMasterNm = prefManager.getCurrentMasterNm();
        if(!TextUtils.isEmpty(currentMasterNm)) {
            textViewMasterUserNm.setText(prefManager.getCurrentMasterNm() + getResources().getString(R.string.canvas_authority));
        } else {
            textViewMasterUserNm.setText("진행자가 없습니다.");
        }*/

        roomCodeText.setText(roomCode);

        // 환경설정 버튼 이벤트 정의..
        header.findViewById(R.id.btn_leftmenu_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean creatorFlag = RoomActivity.activity.getCreatorFlag();
                if (!creatorFlag) {
//                    Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_deny), Toast.LENGTH_SHORT).show();
                } else {

                    leftmenuRoomTitle.setClickable(false);
                    leftmenuRoomTitle.setFocusable(false);
                    leftmenuRoomTitleEdit.setVisibility(View.VISIBLE);

                    setSoftKeywordVisible(leftmenuRoomTitle, false);
                    mCallback.onRoomSetting();

                }
            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (currentMenu != -1) {
            drawerMainListview.expandGroup(currentMenu, true);
            lastClickedPosition = currentMenu;
            mAdapter.setIndex(currentMenu);
        }

        leftmenuRoomTitle.setFocusable(false);
        leftmenuRoomTitle.setClickable(false);

        leftmenuRoomTitle.setImeOptions(EditorInfo.IME_ACTION_DONE);
        leftmenuRoomTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String roomTitleStr = leftmenuRoomTitle.getText().toString();
                    if (TextUtils.isEmpty(roomTitleStr)) {
                        Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.canvas_edittitle_hint), Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    leftmenuRoomTitle.setClickable(false);
                    leftmenuRoomTitle.setFocusable(false);
                    leftmenuRoomTitleEdit.setVisibility(View.VISIBLE);

                    setSoftKeywordVisible(leftmenuRoomTitle, false);
                    mCallback.setRoomTitleFromLeftDrawer(roomTitleStr);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        prefManager.unregisterOnPreferenceChangeListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }


    // 룸 좌측 네비게이션 드로어 초기화
    public void initializeLeftMenuUI(final String roomTitle) {
        Log.d(TAG, "initializeLeftMenuUI : " + roomTitle);


        // 개설자에게만 룸 타이틀 편집 버튼, 환경설정 버튼 보이도록 수정
        if (RoomActivity.activity.getCreatorFlag()) {
            leftmenuRoomTitleEdit.setVisibility(View.VISIBLE);
            leftmenuRoomTitle.setVisibility(View.VISIBLE);
            leftmenuRoomTitle.setClickable(true);
            leftmenuRoomTitle.setFocusable(true);
            btnSetting.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.leftmenu_room_title_no_editable).setVisibility(View.GONE);
        } else {
            leftmenuRoomTitleEdit.setVisibility(View.GONE);
            leftmenuRoomTitle.setVisibility(View.GONE);
            leftmenuRoomTitle.setClickable(false);
            leftmenuRoomTitle.setFocusable(false);
            btnSetting.setVisibility(View.GONE);
            rootView.findViewById(R.id.leftmenu_room_title_no_editable).setVisibility(View.VISIBLE);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(RoomActivity.activity.getCreatorFlag())
                    leftmenuRoomTitle.setText(roomTitle);
                else
                    ((TextView)rootView.findViewById(R.id.leftmenu_room_title_no_editable)).setText(roomTitle);
            }
        });

        initExpandableMenu();

    }


    private void setFindViewID() {
        //btnLeftmenuBack = (ImageView) header.findViewById(R.id.btn_leftmenu_back);
        btnSetting = (ImageView) header.findViewById(R.id.btn_leftmenu_setting);
        leftmenuRoomTitleEdit = (ImageView) header.findViewById(R.id.leftmenu_room_title_edit);
        leftmenuRoomTitle = (EditText) header.findViewById(R.id.leftmenu_room_title);
        roomCodeCopy = (LinearLayout) header.findViewById(R.id.room_code_copy);
        roomCodeText = (TextView) header.findViewById(R.id.room_code_text);

        drawerMainListview = (ExpandableListView)rootView.findViewById(R.id.drawer_main_listview);

        btnLeftmenuBack.setOnClickListener(clickListener);
        btnSetting.setOnClickListener(clickListener);
        leftmenuRoomTitleEdit.setOnClickListener(clickListener);
        roomCodeCopy.setOnClickListener(clickListener);
    }


    private void initExpandableMenu() {
        makeLeftGroupAndChild();

        mAdapter = new RoomLeftDrawerListAdapter(getActivity(), group_list, group_list_item);
        drawerMainListview.setAdapter(mAdapter);

        drawerMainListview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                TextView tx = (TextView) v.findViewById(R.id.main_group_text);

                if (drawerMainListview.isGroupExpanded(groupPosition)) {
                    drawerMainListview.collapseGroup(groupPosition);
                    tx.setTextColor(Color.parseColor("#505050"));
                    mAdapter.setIndex(-1);
                    lastClickedPosition = -1;
                    currentMenu = -1;
                } else {
                    drawerMainListview.expandGroup(groupPosition, true);
//                    adapter.selected_group(groupPosition);
                    mAdapter.setIndex(groupPosition);
                    if (lastClickedPosition != -1) {
                        drawerMainListview.collapseGroup(lastClickedPosition);
                    }
                    lastClickedPosition = groupPosition;
                    currentMenu = groupPosition;
                }

                if (groupPosition == 4) {
                    if (RoomActivity.activity.getCreatorFlag()) {
                        mCallback.onRoomSetting();
                        lastClickedPosition = -1;
                    } else {  // 일반 유저에게 수업 정보 메뉴 기능을 접근할 수 없도록 예외처리..
                        RoomActivity.activity.closeLeftNavDrawer();
                        mCallback.onExitRoom();
                        lastClickedPosition = -1;
                    }
                }

                if (groupPosition == 5) {
                    RoomActivity.activity.closeLeftNavDrawer();
                    mCallback.onExitRoom();
                    lastClickedPosition = -1;
                }

                return true;
            }
        });

        drawerMainListview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView tx = (TextView) v.findViewById(R.id.main_group_item_text);
                String itemName = tx.getText().toString();
                Log.d(TAG, "child item clicked.. " + itemName);

                if (groupPosition == 0) {  // Drawing..
                    switch (childPosition) {
                        case 0:
                            // 펜 툴
                            mCallback.onPenTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 1:
                            // 지우개 툴
                            mCallback.onEraserTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 2:
                            // 도형 툴
                            mCallback.onShapeTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 3:
                            // 텍스트 툴
                            mCallback.onTextTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 4:
                            //메모 툴
                            mCallback.onMemoTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 5:
                            // 이미지 업로드
                            mCallback.onPhotoTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 6:
                            // 이미지 업로드
                            mCallback.onImageTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 7:
                            // PDF 업로드
                            mCallback.onPdfTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            //finish();
                            break;
                        case 11:
                            // Todo 1차 크랄우드 빠짐
                            Toast.makeText(getActivity().getApplicationContext(), "준비 중입니다.", Toast.LENGTH_SHORT).show();
                            //mCallback.onPdfTool();
                            //mDrawerLayout.closeDrawers();
                            //finish();
                            break;
                        case 8:
                            // Undo
                            mCallback.onUndoTool();
                            //mDrawerLayout.closeDrawers();
                            //finish();
                            break;
                        case 9:
                            // Redo
                            mCallback.onRedoTool();
                            //mDrawerLayout.closeDrawers();
                            //finish();
                            break;
                        case 10:
                            //모두 지우기
                            mCallback.onDeleteAll();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        default:
                            break;
                    }

                } else if (groupPosition == 1) {
                    switch (childPosition) {
                        case 0:
                            // 선택 모드
                            mCallback.onHandMode();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 1:
                            // 레이저 포인터 툴
                            mCallback.onLaserTool();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 2:
                            // 확대
                            mCallback.onZoomIn();
                            //mDrawerLayout.closeDrawers();
                            break;
                        case 3:
                            // 축소
                            mCallback.onZoomOut();
                            //mDrawerLayout.closeDrawers();
                            break;
                        case 4:
                            // 폴 만들기
                            mCallback.onPoll();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 5:
                            // 폴 템플릿 리스트
                            mCallback.onPollTmpList();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 6:
                            // 완료된 폴 리스트
                            mCallback.onPollCompletedList();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        default:
                            break;
                    }

                } else if (groupPosition == 2) {
                    switch (childPosition) {
                        case 0:
                            // 참여자 목록

                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            mCallback.onUserList();
//                            Toast.makeText(getContext(), getResources().getString(R.string.toast_soon), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            // 비디오
                            mCallback.onUserVideo();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 2:
                            // 대화(채팅)
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            mCallback.onChatting();
//                            Toast.makeText(getContext(), getResources().getString(R.string.toast_soon), Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            // 코멘트
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            mCallback.onComment();
//                            Toast.makeText(getContext(), getResources().getString(R.string.toast_soon), Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            // 초대하기
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawers();
                            RoomActivity.activity.closeLeftNavDrawer();
                            mCallback.onInvite();
//                            Toast.makeText(getContext(), getResources().getString(R.string.toast_soon), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                } else if (groupPosition == 3) {
                    switch (childPosition) {
                        case 0:
                            // 북마크 - SwitchCompat으로 동작함..
                            break;
                        case 1:
                            // Import
                            mCallback.onSaveCanvas();
                            //RoomLeftNavActivity.mDrawerLayout.closeDrawer(RoomLeftNavActivity.mDrawerContent);
                            RoomActivity.activity.closeLeftNavDrawer();
                            break;
                        case 2:
                            // URL 복사
                            ClipboardManager clipBoard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            clipBoard.setText(roomCodeText.getText().toString());
                            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_urlcopy), Toast.LENGTH_SHORT).show();
//                            Toast.makeText(getActivity().getApplicationContext(), "준비 중입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            // 공유하기
                            if (RoomActivity.activity.getGuestFlag()) {
                                Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                            } else {
                                mCallback.onShare();
                            }
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
    }



    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.btn_leftmenu_back :
//                    RoomLeftNavActivity.mDrawerLayout.closeDrawer(RoomLeftNavActivity.mDrawerContent);
                    RoomActivity.activity.closeLeftNavDrawer();
                    break;
                case R.id.btn_leftmenu_setting :
                    break;
                case R.id.leftmenu_room_title_edit :
                    setTitleFocus_click();
                    break;
                case R.id.room_code_copy :
                    //TODO 룸코드 클립보드에 복사
                    ClipboardManager clipBoard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipBoard.setText(roomCodeText.getText().toString());
                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_urlcopy), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private void makeLeftGroupAndChild() {
        String[] groupTitleArr = getResources().getStringArray(R.array.group_title);
        TypedArray iconResArr = getResources().obtainTypedArray(R.array.group_img);
        String[] menuTags = getResources().getStringArray(R.array.menu_tags);
        String[] hasSubListArr = getResources().getStringArray(R.array.has_sub_list);


        TypedArray group_title_string_array = getResources().obtainTypedArray(R.array.group_list_title);
        String[] group_item_arr;
        group_list = new ArrayList<ExpandableMenu>();
        group_list_item = new HashMap<ExpandableMenu, ArrayList<ExpandableMenuItemList>>();

        for (int i=0; i<groupTitleArr.length; i++) {
            if(!RoomActivity.activity.getCreatorFlag() && i == 4) {
                // 일반 참여자에게는 수업 정보 메뉴를 노출시키지 않기 위한 예외처리..
                continue;
            }
            String itemName    = groupTitleArr[i];
            String itemTagName = menuTags[i];
            int iconResourceId = iconResArr.getResourceId(i,-1);
            boolean hasSubList = Boolean.parseBoolean(hasSubListArr[i]);

            group_item_arr = getResources().getStringArray(group_title_string_array.getResourceId(i,-1));
            group_list_item_arr = new ArrayList<ExpandableMenuItemList>();
            for (String item : group_item_arr) {
                group_list_item_arr.add(new ExpandableMenuItemList(item));
            }

            ExpandableMenu temp_data = new ExpandableMenu(itemName, iconResourceId, itemTagName, hasSubList);
            group_list.add(temp_data);
            group_list_item.put(temp_data, group_list_item_arr);
        }
        iconResArr.recycle();
        group_title_string_array.recycle();
    }


    public void setSoftKeywordVisible(final View v, boolean flag) {
        if (flag) {
            v.post(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, 0);
                }
            });
        } else {
            v.post(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            });
        }
    }


    private void setTitleFocus_click() {
        leftmenuRoomTitleEdit.setVisibility(View.GONE);
        leftmenuRoomTitle.setTextIsSelectable(true);
        leftmenuRoomTitle.setFocusableInTouchMode(true);
        leftmenuRoomTitle.setClickable(true);
        leftmenuRoomTitle.setFocusable(true);
        leftmenuRoomTitle.requestFocus();

        setSoftKeywordVisible(leftmenuRoomTitle, true);
    }


    public static void clearCurrentPosition(){
        currentMenu = -1;
    }



}
