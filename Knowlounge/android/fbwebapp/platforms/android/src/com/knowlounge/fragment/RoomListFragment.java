package com.knowlounge.fragment;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.Room;

import java.util.ArrayList;

/**
 * Created by Minsu on 2016-08-24.
 */
public class RoomListFragment extends Fragment {

    private final String TAG = "RoomListFragment";

    public WenotePreferenceManager prefManager;
    public String svrFlag;
    public String svrHost;
    public boolean isTablet = false;
    public Context rootContext;

    public SwipeRefreshLayout mSwipeRefreshLayout;

    public final String CLASS_VIEW_ROWS_PHONE_BASIC = "2";
    public final String CLASS_VIEW_ROWS_PHONE = "3";
    public final String CLASS_VIEW_ROWS_TABLET_BASIC = "3";
    public final String CLASS_VIEW_ROWS_TABLET = "9";

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);

        rootContext = context;
        prefManager = WenotePreferenceManager.getInstance(context);
        svrFlag = getResources().getString(R.string.svr_flag);
        svrHost = getResources().getString(getResources().getIdentifier("svr_host_" + svrFlag, "string", getActivity().getPackageName()));
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET)
            isTablet = true;
    }


    public void renderRoomList(JsonArray roomList, ArrayList<Room> roomListDataSet, boolean isMyRoom, int classType, @Nullable String moreFlag) {
        for(JsonElement room : roomList) {
            JsonObject obj = room.getAsJsonObject();

            String seqNo = obj.get("seqno").getAsString();
            String roomId = obj.get("roomid").getAsString();
            String roomTitle = obj.get("title").getAsString();
            int roomCnt = obj.get("readcnt").getAsInt();
            String userNm = obj.get("usernm").getAsString();
            String userThumb = obj.has("thumbnail") ? obj.get("thumbnail").getAsString() : "";
            String userLimitCnt = obj.get("user_limit_cnt").getAsString();
            String passwdFlag = obj.has("passwd") ? obj.get("passwd").getAsString() : "";

            String roomThumbnail = svrHost + "/data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_01.jpg";

            roomListDataSet.add(new Room(seqNo, roomId, roomTitle, roomThumbnail, roomCnt, userNm, userThumb, userLimitCnt, isMyRoom, classType, moreFlag, passwdFlag));
        }
    }



}
