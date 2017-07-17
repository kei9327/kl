package com.knowrecorder.phone.tab.PAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.phone.rxevent.PlayVideo;
import com.knowrecorder.phone.tab.model.VideoData;
import com.knowrecorder.rxjava.EventBus;
import com.knowrecorder.transform.CircleBitmapTransform;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-11-30.
 */

public class SubjectListAdapter extends RecyclerView.Adapter<SubjectListAdapter.VideoItemRow>{

    private ArrayList<VideoData> itemsList;
    private Context mContext;

    public SubjectListAdapter(Context mContext, ArrayList<VideoData> itemsList) {
        this.itemsList = itemsList;
        this.mContext = mContext;
    }

    @Override
    public VideoItemRow onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.p_video_list_card, null);

        return new VideoItemRow(v);
    }

    @Override
    public void onBindViewHolder(VideoItemRow holder, int position) {

        final VideoData data = itemsList.get(position);
        holder.userName.setText(data.getAuthor());
        holder.videoTitle.setText(data.getTitle());
        holder.playTime.setText(data.getPlaytime());
        holder.viewCount.setText(data.getHit());

        Glide.with(mContext).load(data.getThumbnailUrl() + "?api_key=" + ServerInfo.API_KEY).into(holder.videoThumbnail);
        Glide.with(mContext).load(data.getUserThumbnail()).bitmapTransform(new CircleBitmapTransform(mContext)).into(holder.userThumbnail);

        if(position == 0)
            holder.rootView.setPadding((int)PixelUtil.getInstance().convertDpToPixel(12), (int)PixelUtil.getInstance().convertDpToPixel(24), (int)PixelUtil.getInstance().convertDpToPixel(12), 0);
        else if(position == itemsList.size()-1)
            holder.rootView.setPadding((int)PixelUtil.getInstance().convertDpToPixel(12), (int)PixelUtil.getInstance().convertDpToPixel(12), (int)PixelUtil.getInstance().convertDpToPixel(12), (int)PixelUtil.getInstance().convertDpToPixel(24));
        else
            holder.rootView.setPadding((int)PixelUtil.getInstance().convertDpToPixel(12), (int)PixelUtil.getInstance().convertDpToPixel(12), (int)PixelUtil.getInstance().convertDpToPixel(12), 0);


        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new PlayVideo(data.getId(), data.getTitle()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class VideoItemRow extends RecyclerView.ViewHolder{
        final View rootView;
        final ImageView videoThumbnail;
        final TextView playTime;
        final ImageView userThumbnail;
        final TextView videoTitle;
        final TextView userName;
        final TextView viewCount;

        public VideoItemRow(View itemView) {
            super(itemView);
            this.rootView = itemView;
            this.videoThumbnail = (ImageView) rootView.findViewById(R.id.p_op_video_thumbnail);
            this.playTime = (TextView) rootView.findViewById(R.id.p_op_play_time);
            this.userThumbnail = (ImageView) rootView.findViewById(R.id.p_op_user_thumbnail);
            this.videoTitle = (TextView) rootView.findViewById(R.id.p_op_video_title);
            this.userName = (TextView) rootView.findViewById(R.id.p_op_user_name);
            this.viewCount = (TextView) rootView.findViewById(R.id.p_op_view_count);

        }
    }
}
