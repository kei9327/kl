package com.knowlounge.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.CircleTransform;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.NoticeData;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-05-30.
 */
public class RoomNoticeAdapter extends BaseAdapter {

    private final String URL_HISTORY_UPDATE = "history/update.json";

    private Context mContext;
    private ArrayList<NoticeData> list;
    private String type;
    private LayoutInflater inflater;
    private WenotePreferenceManager prefManager;

    public interface SetRoomNotiAdapterListener {
        void updateData();
    }
    public static SetRoomNotiAdapterListener mCallback;
    public static void setRoomNotiAdapterListener(SetRoomNotiAdapterListener listener) {mCallback = listener;}

    public RoomNoticeAdapter(Context context, Activity activity, ArrayList<NoticeData> list, String type){
        this.mContext = context;
        this.list = list;
        this.type = type;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.prefManager = WenotePreferenceManager.getInstance(context);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.klounge_notice_room_row, parent, false);
        }

        ImageView roomNoticeThumbnail = (ImageView) convertView.findViewById(R.id.room_notice_thumbnail);
        TextView roomNoticeText = (TextView) convertView.findViewById(R.id.room_notice_text);
        TextView roomNoticeDate = (TextView) convertView.findViewById(R.id.room_notice_date);

        if(list.get(position).getSenderThumbNail() != null && !list.get(position).getSenderThumbNail().equals("")){
            Picasso.with(mContext).load(list.get(position).getSenderThumbNail()).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(roomNoticeThumbnail);  // 썸네일 이미지 로드
        }
        SpannableStringBuilder sb = getText(position);
        if(sb != null)
            roomNoticeText.setText(sb);
        roomNoticeDate.setText(AndroidUtils.transformDate(mContext,list.get(position).getDateTime()));

        return convertView;
    }

    private void updateStatus(int historyNo,String status) {

        RequestParams params = new RequestParams();
        params.put("historyno",Integer.toString(historyNo));
        params.put("status",status);

        String masterCookie = prefManager.getUserCookie();
        String checkSum = prefManager.getChecksumCookie();

        RestClient.postWithCookie(URL_HISTORY_UPDATE, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("RoomNotice","response : "+ response.toString());
                    int apiResult = response.getInt("result");
                    if(apiResult == 0) {
                        mCallback.updateData();
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        });
    }

    private SpannableStringBuilder getText(int position) {
        String resourceId = "history";
        String category = list.get(position).getCategory();
        String historyType = list.get(position).getHistoryType();
        String resultText ="";

        if(type.equals("request")){
            resourceId += "_recv";
        }else {
            resourceId += "_send";
        }
        resourceId += "_"+category+"_"+historyType;
        resultText = mContext.getResources().getString(mContext.getResources().getIdentifier(resourceId, "string", mContext.getPackageName()));
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
