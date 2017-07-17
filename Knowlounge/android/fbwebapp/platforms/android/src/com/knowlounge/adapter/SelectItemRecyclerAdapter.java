package com.knowlounge.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-04-27.
 */
public class SelectItemRecyclerAdapter extends RecyclerView.Adapter<SelectItemRecyclerAdapter.ViewHolder> {

    private ArrayList<String> list;
    private Context mContext;
    private int type;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // ViewHolder
        public TextView itemNm;
        public ViewHolder(View view){
            super(view);
            itemNm = (TextView) view.findViewById(R.id.item_nm);
        }
    }

    public SelectItemRecyclerAdapter(Context context, ArrayList<String> list, int type) {
        if (list == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.mContext = context;
        this.list = list;
        this.type = type;
    }

    @Override
    public SelectItemRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chips, viewGroup, false);
        if(type == GlobalConst.TYPE_STUDENT_START){
            view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_student_chips));
        }else{
            view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_teacher_chips));
        }
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String itemNm = list.get(position);

        holder.itemNm.setText(itemNm);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getItemNm(int position){return list.get(position);}

}