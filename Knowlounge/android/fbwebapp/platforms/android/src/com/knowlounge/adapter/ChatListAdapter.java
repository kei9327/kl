package com.knowlounge.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.Chat;
import com.knowlounge.util.AndroidUtils;

import java.util.ArrayList;

/**
 * Created by Minsu on 2016-01-14.
 */
public class ChatListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Chat> list;
    private ChatViewHolder viewHolder;
    private WenotePreferenceManager prefManager;
    public ChatListAdapter(Context context, ArrayList<Chat> list) {
        super();
        this.context = context;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
        prefManager = WenotePreferenceManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Chat getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(getClass().getSimpleName(), "getView");

        Chat chatting = getItem(position);

        String chatType  = chatting.getType();  // 0 - 전체 채팅, 1 - 귓속말 필요!
        String chatMode  = chatting.getMode();  // 0 - 보낸 메세지, 1 - 받은 메세지
        String userNm    = chatting.getUserNm();  //필요
        String thumbnail = chatting.getThumbnail();   //필요
        String cDatetime = chatting.getCdatetime();                                //필요
        String chatMsg   = chatting.getContent();                                   //필요


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chat_list_rows, parent, false);
        }
        viewHolder = new ChatViewHolder();
        convertView = convertChatView(convertView);

        if(position == list.size()-1){
            convertView.setPadding(0, 0, 0, AndroidUtils.getPxFromDp(context,(float)10));
        }else{
            convertView.setPadding(0, 0, 0, 0);
        }

        viewHolder.chat_datetime.setText(convertChatDisplay(cDatetime));
        viewHolder.chat_content.setText(chatMsg);
//        if(thumbnail.length() != 0) {
//            Glide.with(context).load(thumbnail).transform(new CircleTransformTemp(context)).into(viewHolder.chat_user_thumb);
//        }else{
//            Glide.with(context).load(prefManager.getUserThumbnail()).transform(new CircleTransformTemp(context)).into(viewHolder.chat_user_thumb);
//        }

        if(TextUtils.isEmpty(thumbnail)) {
//            int idx = (int)(Math.random() * 3) + 1
            if (list.get(position).getSender().equals(prefManager.getUserNo())) {
                if (!"".equals(prefManager.getUserThumbnail()))
                    Glide.with(context).load(prefManager.getUserThumbnail()).transform(new CircleTransformTemp(context)).into(viewHolder.chat_user_thumb);
                else
                    Glide.with(context).load(R.drawable.img_userlist_default01).transform(new CircleTransformTemp(context)).into(viewHolder.chat_user_thumb);
            } else {
                Glide.with(context).load(R.drawable.img_userlist_default01).transform(new CircleTransformTemp(context)).into(viewHolder.chat_user_thumb);
            }
        } else {
            Glide.with(context).load(thumbnail).transform(new CircleTransformTemp(context)).into(viewHolder.chat_user_thumb);
        }

        if(userNm.length() != 0){
            viewHolder.chat_user_name.setText(userNm);
        }else{
            viewHolder.chat_user_name.setText(prefManager.getUserNm());
        }

        //귓속말
        if(chatType.equals("1") ) {
            viewHolder.chat_wisper_title.setVisibility(View.VISIBLE);
            viewHolder.chat_wisper_img.setVisibility(View.VISIBLE);
            if (chatMode.equals("0")) {
                viewHolder.chat_wisper_title.setText("To.");
            } else {
                viewHolder.chat_wisper_title.setText("From.");
            }
        }else{
            viewHolder.chat_wisper_title.setVisibility(View.GONE);
            viewHolder.chat_wisper_img.setVisibility(View.GONE);
        }

        return convertView;
    }

    private View convertChatView(View convertView){

        ((LinearLayout)convertView.findViewById(R.id.chat_view_area)).removeAllViews();
        ((LinearLayout)convertView.findViewById(R.id.chat_view_area)).addView(inflater.inflate(R.layout.chat_list_rows_temp,null));
        viewHolder.chat_user_thumb = (ImageView) convertView.findViewById(R.id.chat_user_thumb);
        viewHolder.chat_wisper_img = (ImageView) convertView.findViewById(R.id.chat_wisper_img);
        viewHolder.chat_wisper_title = (TextView) convertView.findViewById(R.id.chat_wisper_title);
        viewHolder.chat_user_name =  (TextView) convertView.findViewById(R.id.chat_user_name);
        viewHolder.chat_datetime =  (TextView) convertView.findViewById(R.id.chat_datetime);
        viewHolder.chat_content = (TextView) convertView.findViewById(R.id.chat_content);

        return convertView;
    }

    private String convertChatDisplay(String dateTime){

        int hour = Integer.parseInt(dateTime.substring(8, 10));
        int minute = Integer.parseInt(dateTime.substring(10, dateTime.length()));
        String amPmTxt = hour > 11 ? "PM" : "AM";

        hour = hour % 12;
        hour = hour == 0 ? 12 : hour;

        return amPmTxt + " " + (hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour) ) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute));
    }
    public class ChatViewHolder{

        ImageView chat_user_thumb = null;
        ImageView chat_wisper_img = null;
        TextView chat_wisper_title = null;
        TextView chat_user_name = null;
        TextView chat_datetime = null;
        TextView chat_content = null;
    }
}
