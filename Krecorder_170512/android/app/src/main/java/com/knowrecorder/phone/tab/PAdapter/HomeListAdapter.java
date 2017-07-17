package com.knowrecorder.phone.tab.PAdapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.knowrecorder.R;
import com.knowrecorder.phone.PhoneOpencourseActivity;
import com.knowrecorder.phone.rxevent.SelectTab;
import com.knowrecorder.phone.tab.model.SectionList;
import com.knowrecorder.rxjava.EventBus;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-11-30.
 */

public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.ItemRowHolder>{

    private Context mContext;
    private ArrayList<SectionList> list;

    public HomeListAdapter(Context context, ArrayList<SectionList> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v  = LayoutInflater.from(parent.getContext()).inflate(R.layout.p_home_list_row, null);
        ItemRowHolder mh = new ItemRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemRowHolder holder, final int position) {
        String sectionName =  list.get(position).getSectionTitle();
        ArrayList sectionItem = list.get(position).getSectionItemList();

        if(position == list.size()-1){
            holder.homeListDivider.setVisibility(View.GONE);
        }else{
            holder.homeListDivider.setVisibility(View.VISIBLE);
        }

        holder.sectionTitle.setText(sectionName);

        SectionListAdapter itemListAdapter = new SectionListAdapter(sectionItem, mContext);
        holder.horizontalList.setHasFixedSize(true);
        holder.horizontalList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.horizontalList.setAdapter(itemListAdapter);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectTab event = new SelectTab();
                event.setTab(PhoneOpencourseActivity.SUBJECT_TAB);
                event.setSubject(list.get(position).getSectionCategory());
                EventBus.getInstance().post(event);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != list ? list.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder{
        final View rootView;
        final TextView sectionTitle;
        final TextView moreBtn;
        final RecyclerView horizontalList;
        final View homeListDivider;


        public ItemRowHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            this.sectionTitle = (TextView) rootView.findViewById(R.id.p_opencourse_section_title);
            this.moreBtn = (TextView) rootView.findViewById(R.id.p_opencourse_more_btn);
            this.horizontalList = (RecyclerView) rootView.findViewById(R.id.p_opencourse_horizontal_list);
            this.homeListDivider = (View) rootView.findViewById(R.id.p_opencourse_home_list_diviter);
        }
    }
}
