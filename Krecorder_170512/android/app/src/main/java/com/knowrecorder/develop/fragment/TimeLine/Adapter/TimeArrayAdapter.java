package com.knowrecorder.develop.fragment.TimeLine.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.knowrecorder.R;
import com.knowrecorder.develop.fragment.TimeLine.Model.TimelineTimeBlock;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by we160303 on 2017-03-13.
 */

public class TimeArrayAdapter extends ArrayAdapter<TimelineTimeBlock> {

    private String TAG = "TimeArrayAdapter";
    private Context mContext;
    private LayoutInflater inflater = null;

    public TimeArrayAdapter(Context context, ArrayList<TimelineTimeBlock> blocks) {
        super(context, 0, blocks);
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public TimelineTimeBlock getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View line;
        TextView second;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.rb_timeline_time_item, parent, false);
            line = (View) convertView.findViewById(R.id.line);
            second = (TextView) convertView.findViewById(R.id.second);
            convertView.setTag(new ViewHolder(line, second));
        }else{
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            line = viewHolder.line;
            second = viewHolder.second;
        }
        TimelineTimeBlock block = getItem(position);

        convertView.setPadding(block.getLeftGap(), 0, block.getRightGap(), 0);
        second.setText(block.getTime());

        return convertView;
    }

    private  static class ViewHolder {
        public final View line;
        public final TextView second;
        public ViewHolder(View line, TextView second){
            this.line = line;
            this.second = second;
        }
    }

    public String getTAG(){ return TAG;}
}
