package com.knowlounge.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.model.ChatUser;

import java.util.ArrayList;

/**
 * Created by Minsu on 2016-03-15.
 */
public class ChatUserRecyclerAdapter extends RecyclerView.Adapter<ChatUserRecyclerAdapter.ViewHolder> {

    private static String TAG = "ChatListAdapter";
    private ArrayList<ChatUser> mChatUserDataSet;
    private Context context;
    private Fragment parent;
    private static ChatUserListener mCallback = null;
    private static int selectedPosition = 0;

    public interface ChatUserListener {
        public void onWisperSelected(String userNo, String thumnail, String userNm, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView chat_user_thumb;
        public TextView chat_user_name;

        public ViewHolder(View view) {
            super(view);
            chat_user_thumb = (ImageView) view.findViewById(R.id.chat_user_thumb);
            chat_user_name = (TextView) view.findViewById(R.id.chat_user_name);
        }

    }

    public ChatUserRecyclerAdapter(ArrayList<ChatUser> roomDataSet, Fragment parentFragment) {

        mChatUserDataSet = roomDataSet;
        parent = parentFragment;
        context = parent.getContext();
        mCallback = (ChatUserListener)parentFragment;
    }


    @Override
    public ChatUserRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_list_rows, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder");
        Log.d(TAG, Boolean.toString(mChatUserDataSet.get(position).isSelected()));
        final String userThumbnail = mChatUserDataSet.get(position).getThumbnail();
        final String userNm = mChatUserDataSet.get(position).getUserNm();


        Uri thumbnailUri = Uri.parse(userThumbnail);
        if(position == 0)
//            Picasso.with(context).load(R.drawable.img_userlist_defaultgroup01).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(holder.chat_user_thumb);

            Glide.with(context).load(R.drawable.img_userlist_defaultgroup01).diskCacheStrategy(DiskCacheStrategy.ALL)
                .bitmapTransform(new CircleTransformTemp(context))
                .into(holder.chat_user_thumb);

        else
//            Picasso.with(context).load(thumbnailUri).error(R.drawable.img_userlist_default01).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(holder.chat_user_thumb);
            Glide.with(context).load(thumbnailUri).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(context.getResources().getIdentifier("img_userlist_default01", "drawable", context.getPackageName()))
                    .bitmapTransform(new CircleTransformTemp(context))
                    .into(holder.chat_user_thumb);


//        if(position == 0){
//            if(mChatUserDataSet.get(position).isSelected()) {
//                Picasso.with(context).load(R.drawable.img_userlist_defaultgroup01).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_SELECT_THUMB)).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.chat_user_thumb);
//            }else{
//                Picasso.with(context).load(R.drawable.img_userlist_defaultgroup01).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.chat_user_thumb);
//            }
//        }else{
//            if(mChatUserDataSet.get(position).isSelected()) {
//                Picasso.with(context).load(thumbnailUri).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_SELECT_THUMB)).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.chat_user_thumb);
//            }else{
//                Picasso.with(context).load(thumbnailUri).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.chat_user_thumb);
//            }
//        }

        holder.chat_user_name.setText(userNm);

        holder.chat_user_thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onWisperSelected(mChatUserDataSet.get(position).getUserNo(), userThumbnail, mChatUserDataSet.get(position).getUserNm(),position);
            }
        });

        // TODO 디폴트 썸네일 예외처리
    }

    @Override
    public int getItemCount() {
        return mChatUserDataSet.size();
    }
    public static void setSelectUser(int position)
    {
        selectedPosition = position;
    }

}
