package com.knowlounge.youtube.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.sqllite.SearchKeyword;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-10-12.
 */

public class SearchKeywordAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private ArrayList<SearchKeyword> list;

    public SearchKeywordAdapter(Context context, ArrayList<SearchKeyword> list){
        this.mContext = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_search_keyword_item,parent,false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder itemHolder = (ViewHolder)holder;

        itemHolder.keyword.setText(list.get(position).getKeyword());
        itemHolder.keyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        itemHolder.removeKeywordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo Remove Keyword
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getKeyword(int position){ return list.get(position).getKeyword() ; }

    public class ViewHolder  extends RecyclerView.ViewHolder {

        public View rootView;

        public ImageView removeKeywordBtn;
        public TextView keyword;

        public ViewHolder(View view) {
            super(view);
            rootView = view;
            removeKeywordBtn = (ImageView) view.findViewById(R.id.remove_keyword_btn);
            keyword = (TextView) view.findViewById(R.id.keyword);
        }
    }

}
