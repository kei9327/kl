package com.knowrecorder.phone.tab.PAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.R;
import com.knowrecorder.phone.rxevent.PlayVideo;
import com.knowrecorder.phone.tab.model.VideoData;
import com.knowrecorder.rxjava.EventBus;
//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-11-30.
 */

public class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.ItemRowHolder>{

    private ArrayList<VideoData> itemsList;
    private Context mContext;

    public SectionListAdapter(ArrayList<VideoData> itemsList, Context mContext) {
        this.itemsList = itemsList;
        this.mContext = mContext;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.p_home_list_card, null);
        ItemRowHolder mh = new ItemRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemRowHolder holder, int position) {
        final VideoData data = itemsList.get(position);

        Glide.with(mContext).load(data.getThumbnailUrl()+"?api_key=" + ServerInfo.API_KEY)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.itemThumbnail);
        holder.itemTitle.setText(data.getTitle());

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

    public class ItemRowHolder extends RecyclerView.ViewHolder{
        final View rootView;
        final ImageView itemThumbnail;
        final TextView itemTitle;

        public ItemRowHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            this.itemThumbnail = (ImageView) rootView.findViewById(R.id.p_opencourse_item_thumbnail);
            this.itemTitle = (TextView) rootView.findViewById(R.id.p_opencourse_item_title);
        }
    }
}
