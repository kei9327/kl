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
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.model.CommentItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Minsu on 2016-01-12.
 */
public class CommentListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<CommentItem> list;
    WenotePreferenceManager prefManager;

    public CommentListAdapter(Context context, ArrayList<CommentItem> list) {
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
    public CommentItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.comment_list_rows_temp, parent, false);
        }

        CommentItem item = getItem(position);

        String userId = item.getUserId();
        String userNm = item.getUserNm();
        String regDatetime = item.getRegDatetime();
        String contents = item.getContents();
        String displayCdateTime = convertDisplayDateTime(regDatetime);
        String myUserId = RoomActivity.activity.getUserId();
        final String commentNo = item.getCommentNo();

       if(position == list.size()-1){
            convertView.setPadding(0, AndroidUtils.getPxFromDp(context,(float)20), 0, AndroidUtils.getPxFromDp(context,(float)10));
        }else{
            convertView.setPadding(0, AndroidUtils.getPxFromDp(context,(float)20), 0, 0);
        }

        ImageView commentUserThumb = (ImageView) convertView.findViewById(R.id.comment_user_thumb);
        ImageView btnCommentDel = (ImageView) convertView.findViewById(R.id.btn_comment_del);
        TextView commentUserName = (TextView) convertView.findViewById(R.id.comment_user_name);
        TextView commentDatetime = (TextView) convertView.findViewById(R.id.comment_datetime);
        TextView commentContent = (TextView) convertView.findViewById(R.id.comment_content);

        commentUserName.setText(userNm);
        commentDatetime.setText(displayCdateTime);
        commentContent.setText(contents);

        if(!userId.equals(myUserId)){
            btnCommentDel.setVisibility(View.INVISIBLE);
        } else {
            btnCommentDel.setVisibility(View.VISIBLE);
        }

        btnCommentDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.openRemoveCommentDialog(commentNo, context.getResources().getString(R.string.global_popup_title), context.getResources().getString(R.string.canvas_comment_delete));
            }
        });
        Glide.with(context).load(item.getThumbnail()).transform(new CircleTransformTemp(context)).into(commentUserThumb);  //썸네일 이미지 로딩

        return convertView;
    }

    private String convertDisplayDateTime(String dateTime){

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date a = AndroidUtils.getDateFormat(dateTime);
        Date b = new Date(a.getTime() + prefManager.getGlobalTimeTurm());
        dateTime = format.format(b);

        return dateTime;
    }
}
