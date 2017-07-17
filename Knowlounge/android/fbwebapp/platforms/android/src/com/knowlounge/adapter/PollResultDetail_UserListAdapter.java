package com.knowlounge.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.model.PollResultDetailUser;

import java.util.ArrayList;

/**
 * Created by Minsu on 2016-01-12.
 */
public class PollResultDetail_UserListAdapter extends BaseAdapter {
    private final String TAG = "PollListAdapter";

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<PollResultDetailUser> list;

    public PollResultDetail_UserListAdapter(Context context, ArrayList<PollResultDetailUser> list) {

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
    public PollResultDetailUser getItem(int position) {
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
            convertView = inflater.inflate(R.layout.poll_result_detail_row, parent, false);
        }
        ImageView resultDetailThumbnail = (ImageView)convertView.findViewById(R.id.result_detail_thumbnail);
        TextView result_detail_user_name = (TextView) convertView.findViewById(R.id.result_detail_user_name);

        Glide.with(context).load(list.get(position).getThumbnail()).error(R.drawable.img_userlist_default01).bitmapTransform(new CircleTransformTemp(context)).into(resultDetailThumbnail);
//        Picasso.with(context).load(list.get(position).getThumbnail()).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(resultDetailThumbnail);  // 썸네일 이미지 로드
        result_detail_user_name.setText(!list.get(position).getUser_name().equals("") ? list.get(position).getUser_name() : context.getResources().getString(R.string.userlist_guest));

        if(position == list.size()-1){
            result_detail_user_name.setBackgroundResource(R.drawable.bg_under_while_background_while);
        }else{
            result_detail_user_name.setBackgroundResource(R.drawable.bg_under_gray_background_white);
        }
        return convertView;
    }


}

