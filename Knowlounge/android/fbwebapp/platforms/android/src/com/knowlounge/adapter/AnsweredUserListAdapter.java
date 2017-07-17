package com.knowlounge.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.model.ClassUser;
import com.knowlounge.model.PollAnsweredUser;

import java.util.ArrayList;

/**
 * Created by Mansu on 2016-12-15.
 */

public class AnsweredUserListAdapter extends BaseAdapter {
    private final String TAG = "AnsweredUserListAdapter";
    Context context;
    ArrayList<PollAnsweredUser> list;

    private LayoutInflater mInflater;

    public class ViewHolder {
        public ImageView userThumbDeemed;
        public ImageView userThumb;
        public ImageView icoCheck;
    }

    public AnsweredUserListAdapter(Context context, ArrayList<PollAnsweredUser> list) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.list = list;


    }

    public ArrayList<PollAnsweredUser> getList() {
        return list;
    }

    public void setList(ArrayList<PollAnsweredUser> list) {
        this.list = list;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.d(TAG, "getView");
        View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_answerd_user, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.userThumbDeemed = (ImageView) view.findViewById(R.id.user_thumbnail_deemed);
            viewHolder.userThumb = (ImageView) view.findViewById(R.id.user_thumbnail_view);
            viewHolder.icoCheck = (ImageView) view.findViewById(R.id.ico_checked);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        PollAnsweredUser user = list.get(position);

        if (!user.isAnswered()) {
            viewHolder.userThumbDeemed.setVisibility(View.VISIBLE);
//            Glide.clear(viewHolder.icoCheck);
        } else {
            viewHolder.userThumbDeemed.setVisibility(View.GONE);
            Glide.with(context).load(R.drawable.btn_poll_userlist_check).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(viewHolder.icoCheck);
        }

        String thumbnail = user.getUserThumb();
        //if (hasImage(viewHolder.userThumb)) {
        if (viewHolder.userThumb.getDrawable() == null) {
            Glide.with(context)
                    .load(thumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(context.getResources().getIdentifier("img_userlist_default01", "drawable", context.getPackageName()))
                    .bitmapTransform(new CircleTransformTemp(context))
                    .into(viewHolder.userThumb);
        }
        return view;
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }
}