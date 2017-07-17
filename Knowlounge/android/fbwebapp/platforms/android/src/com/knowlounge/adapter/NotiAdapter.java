package com.knowlounge.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.fragment.dialog.ExtendReqDialogFragment;
import com.knowlounge.fragment.dialog.RoomPasswdDialogFragment;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.NoticeData;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-05-30.
 */
public class NotiAdapter extends BaseAdapter {

    private final String TAG = "NotiAdapter";

    private boolean edittingMode = false;
    private boolean selectedAll = false;
    private Context mContext;
    private ArrayList<NoticeData> list;
    LayoutInflater inflater;
    private WenotePreferenceManager prefManager;
    private Fragment parent;
    private boolean isAC001 = false;


    public NotiAdapter(Context context, Fragment parentFragment , ArrayList<NoticeData> list){
        this.mContext = context;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
        this.prefManager = WenotePreferenceManager.getInstance(context);
        this.parent = parentFragment;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) { return list.get(position); }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.klounge_notice_row,parent,false);
        }
        LinearLayout notiRowLayout = (LinearLayout) convertView.findViewById(R.id.noti_row_layout);
        final ImageView deleteCheckBtn =(ImageView) convertView.findViewById(R.id.delete_check_btn);
        ImageView noticeThumbnail = (ImageView) convertView.findViewById(R.id.notice_thumbnail);
        TextView noticeText = (TextView) convertView.findViewById(R.id.notice_text);
        TextView noticeDate = (TextView) convertView.findViewById(R.id.notice_date);
        TextView noticeDirect  = (TextView) convertView.findViewById(R.id.notice_direct);

        View extraMargine = (View) convertView.findViewById(R.id.extra_margin);

        noticeText.setText(getText(position));
        noticeDate.setText(AndroidUtils.transformDate(mContext,list.get(position).getDateTime()));

        convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        notiRowLayout.setBackgroundResource(R.drawable.bg_under_gray_background_white);

        if (edittingMode) {
            deleteCheckBtn.setVisibility(View.VISIBLE);
            extraMargine.setVisibility(View.GONE);
            noticeDirect.setVisibility(View.GONE);

            if (list.get(position).isChecked()) {
                deleteCheckBtn.setImageResource(R.drawable.btn_checkbox_on);
                convertView.setBackgroundColor(Color.parseColor("#ebeced"));
                notiRowLayout.setBackgroundResource(R.drawable.bg_under_gray_background_ebeced);
            } else {
                deleteCheckBtn.setImageResource(R.drawable.btn_checkbox);
                convertView.setBackgroundColor(Color.parseColor("#ffffff"));
                notiRowLayout.setBackgroundResource(R.drawable.bg_under_gray_background_white);
            }

        } else {
            deleteCheckBtn.setVisibility(View.GONE);
            extraMargine.setVisibility(View.VISIBLE);
            if (!list.get(position).getSenderName().equals(list.get(position).getUserName())) {  // recv
                if (list.get(position).getRoomSeqNo() != null) {
                    if(list.get(position).getHistoryType().equals("IV001") || list.get(position).getHistoryType().equals("IV002") || list.get(position).getHistoryType().equals("IV003"))
                    noticeDirect.setVisibility(View.VISIBLE);
                } else {
                    noticeDirect.setVisibility(View.GONE);
                }
            } else {
                if (list.get(position).getRoomSeqNo() != null) {   //send
                    if( list.get(position).getHistoryType().equals("IV003"))
                        noticeDirect.setVisibility(View.VISIBLE);
                } else {
                    noticeDirect.setVisibility(View.GONE);
                }
            }
        }

        noticeDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String roomId = list.get(position).getRoomId();
                    String passwd = "";
                    String tokenStr = "roomid=" + roomId + "&passwd=" + passwd;

                    AESUtil aesUtilObj = null;
                    aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
                    String encryptToken = aesUtilObj.encrypt(tokenStr);

                    MainActivity._instance.enterRoom(list.get(position).getRoomSeqNo());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (list.get(position).getSenderThumbNail() != null && !list.get(position).getSenderThumbNail().equals("")) {
            if (isAC001)
                Glide.with(mContext).load(R.drawable.ic_launcher).transform(new CircleTransformTemp(mContext)).into(noticeThumbnail);
            else {
                Glide.with(mContext).load(list.get(position).getSenderThumbNail()).error(R.drawable.img_userlist_default01).transform(new CircleTransformTemp(mContext)).into(noticeThumbnail);
//            Picasso.with(mContext).load(list.get(position).getSenderThumbNail()).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(noticeThumbnail);  // 썸네일 이미지 로드
            }

        }

        Log.d("Notice_item_check", list.get(position).isChecked()+"");
        return convertView;
    }

    public void isAllSelectedMode(boolean isAllSelected){
        for(NoticeData data:list){
            data.setChecked(isAllSelected);
        }
        notifyDataSetChanged();
    }

    private void enterRoom(final int position, String tokenStr) {

        final RequestParams params = new RequestParams();
        params.put("token", tokenStr);

        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();
        RestClient.postWithCookie("room/check.json", masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int result = response.getInt("result");
                    if(result == 0) {
                        String roomCode = list.get(position).getRoomSeqNo();
                        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                        Intent mainIntent = new Intent(mContext, RoomActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.putExtra("roomurl", roomUrl);
                        MainActivity._instance.startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM);

                    } else if(result == -201) {
                        // Invalid room
                    } else if(result == -102) {
                        // Incorrect password
                        FragmentManager fm = parent.getFragmentManager();
                        RoomPasswdDialogFragment dialogFragment = new RoomPasswdDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("mode", "roomcode");
                        args.putString("roomcode", list.get(position).getRoomSeqNo());
                        dialogFragment.setArguments(args);
                        dialogFragment.show(fm, "room_passwd");
                    } else if(result == -207) {
                        // room count limit (over 3)
//                        FragmentManager fm = parent.getFragmentManager();
//                        StarPayNotiDialogFragment dialogFragment = new StarPayNotiDialogFragment();
//                        dialogFragment.show(fm, "pay_star_noti");
//
                        String roomId = response.getJSONObject("map").getString("roomid");
                        Bundle args = new Bundle();
                        args.putString("roomid", roomId);
                        args.putString("code", list.get(position).getRoomSeqNo());
                        args.putString("masterno", response.getJSONObject("map").getString("masterno"));

                        ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                        dialogFragment.setArguments(args);
                        dialogFragment.show(parent.getFragmentManager(), "extend_user_limit");
                    } else if(result == -208) {
                        // room count limit (over 25)
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.global_popup_full), Toast.LENGTH_SHORT).show();
                    } else if (result == -8001) {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.login_join_invalidcode), Toast.LENGTH_SHORT).show();
                    }

                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Create room onFailure " + statusCode);
            }
        });
    }

    private String getText(int position) {
        String resourceId = "history";
        String sender = list.get(position).getSenderName();
        String user = list.get(position).getUserName();
        String category = list.get(position).getCategory();
        String historyType = list.get(position).getHistoryType();
        String resultText ="";

        if (sender.equals(user)) {
            resourceId +="_send";
            if(historyType.equals("IV001") && list.get(position).getCnt()>1)
                resourceId += "_all";
        } else {
            resourceId += "_recv";
        }
        resourceId += "_"+category+"_"+historyType;
        Log.d(TAG, "resourceId : " + resourceId);
        resultText = mContext.getResources().getString(mContext.getResources().getIdentifier(resourceId,"string",mContext.getPackageName()));
        resultText = setParameter(position,resultText, historyType);

        Log.d("text", resultText);

        return resultText;
    }

    private String setParameter(int position, String data, String historyType) {
        isAC001 = false;
        String extraNm = list.get(position).getExtraNm();
        String senderNm = list.get(position).getSenderName();
        String userNm = list.get(position).getUserName();
        String noticeTitle = list.get(position).getNoticeTitle();
        int cnt = list.get(position).getCnt();

        if(historyType.equals("AC001")){
            isAC001 = true;
            return String.format(data,userNm);
        }else if(historyType.equals("IV002")){
            return String.format(data,noticeTitle, senderNm);
        }else if(historyType.equals("IV001")){
            return String.format(data,senderNm, noticeTitle, extraNm, Integer.toString(cnt) );
        }else if(historyType.equals("ST001")){
            return String.format(data,cnt);
        }else if(historyType.equals("ST002")){
            return String.format(data,senderNm, cnt);
        }else if(historyType.equals("IV003")){
            return String.format(data,noticeTitle);
        }
        return "";
    }

    public void toggleEditMode(){
        if(edittingMode)
            edittingMode = false;
        else
            edittingMode = true;
        notifyDataSetChanged();
    }

    public boolean isEdittingMode(){ return this.edittingMode;}

    public void selectedAll(){
        if(selectedAll){
            selectedAll = false;
        }else{
            selectedAll = true;
        }
        notifyDataSetChanged();
    }

    public boolean isSelectedAll(){ return this.selectedAll;}

    public boolean setSelectedAll(boolean selectedAll){
        this.selectedAll = selectedAll;
        return this.selectedAll;
    }

    public void setChecked(int position){
        list.get(position).toggleCheck();
        Log.d("remove_list",list.get(position).getHistoryNo()+"");
    }

    public String getSelectedItem() {
        String result = "";
        for(NoticeData data : list){
            if(data.isChecked()){
                result += data.getHistoryNo() +",";
            }
        }
        Log.d("NoticeAdater","SelectedItem result : "+result);
        return result.length() !=0 ? result.substring(0,result.length()-1) : "";
    }

    public int getSelectedCount(){
        int count = 0;

        for(NoticeData data:list){
            if(data.isChecked())
                count++;
        }

        return count;
    }
}
