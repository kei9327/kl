package com.knowlounge.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.squareup.picasso.Picasso;
import com.knowlounge.CircleTransform;
import com.knowlounge.model.ChatUser;

import java.util.ArrayList;

/**
 * Created by Minsu on 2016-01-14.
 */
public class ChatUserListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<ChatUser> list;

    public ChatUserListAdapter(Context context, ArrayList<ChatUser> list) {
        super();
        this.context = context;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ChatUser getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Log.d(getClass().getSimpleName(), "getView");
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chat_user_list_rows, parent, false);
        }

        ChatUser user = getItem(position);
        String userId = user.getUserId();
        String userNm = user.getUserNm();
        String userNo = user.getUserNo();
        String thumbnail = user.getThumbnail();

        TextView writerText = (TextView) convertView.findViewById(R.id.chat_user_name);
        ImageView thumbnailIcon = (ImageView) convertView.findViewById(R.id.chat_user_thumb);

        writerText.setText(userNm);
        if(thumbnail != null && !thumbnail.equals("")){
            Picasso.with(context).load(thumbnail).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(thumbnailIcon);  // 썸네일 이미지 로드
        }

        return convertView;
    }
}
