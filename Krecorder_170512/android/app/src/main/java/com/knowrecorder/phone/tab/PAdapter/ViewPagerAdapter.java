package com.knowrecorder.phone.tab.PAdapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowrecorder.R;
import com.knowrecorder.phone.tab.model.DeepLink;
//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-12-01.
 */

public class ViewPagerAdapter  extends PagerAdapter{

    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<DeepLink> list;


    public ViewPagerAdapter(Context context, LayoutInflater inflater, ArrayList<DeepLink> list) {
        this.mContext = context;
        this.inflater = inflater;
        this.list = list;
    }

    @Override
    public int getCount() {
        return (null != list ? list.size() : 0);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        DeepLink data = list.get(position);
        View view = null;
        view = inflater.inflate(R.layout.p_viewpager_childview, null);
        ImageView img = (ImageView) view.findViewById(R.id.img_viewpager_childimage);

        if(data.getVideoId().isEmpty()){
            Glide.with(mContext).load(data.getDefaultImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(img);
        }
        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }
}
