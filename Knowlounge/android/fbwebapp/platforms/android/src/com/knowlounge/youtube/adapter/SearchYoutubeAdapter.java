package com.knowlounge.youtube.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.R;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.youtube.model.YouTubeModel;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-10-12.
 */

public class SearchYoutubeAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private ArrayList<YouTubeModel> list;

    public SearchYoutubeAdapter(Context context, ArrayList<YouTubeModel> list){
        this.mContext = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_list_item,parent,false);
        ViewHolder holder = new ViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder itemHolder = (ViewHolder)holder;

        String thumbnail = list.get(position).getUrl();
        String title = list.get(position).getTitle();
        String channelTitle = list.get(position).getChannelTitle();

        Uri thumbnailUri = Uri.parse(thumbnail);
        Glide.with(mContext)
                .load(thumbnailUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(itemHolder.videoThumbnail);
        itemHolder.videoTitle.setText(title);
        itemHolder.videoChannelTitle.setText(channelTitle);

        if(position == list.size()-1){
            itemHolder.rootView.setPadding(AndroidUtils.getPxFromDp(mContext,20), AndroidUtils.getPxFromDp(mContext,20), AndroidUtils.getPxFromDp(mContext,20), AndroidUtils.getPxFromDp(mContext,20));
        }else{
            itemHolder.rootView.setPadding(AndroidUtils.getPxFromDp(mContext,20),AndroidUtils.getPxFromDp(mContext,20),AndroidUtils.getPxFromDp(mContext,20),0);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getVideoId(int position){ return list.get(position).getVideoId() ; }
    public String getVIdeoTitle(int position){ return list.get(position).getTitle() ; }

    public class ViewHolder  extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView videoThumbnail;
        public TextView videoTitle;
        public TextView videoChannelTitle;

        public ViewHolder(View view) {
            super(view);
            rootView = view;
            videoThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            videoTitle = (TextView) view.findViewById(R.id.title);
            videoChannelTitle = (TextView) view.findViewById(R.id.chanel_title);
        }
    }

}
