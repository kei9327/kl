package com.knowlounge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.model.ProfileMultiSelectItem;

import java.util.ArrayList;

/**
 * Created by Minsu on 2016-01-12.
 */
public class ProfileMultiSelectAdapter extends BaseAdapter {
    private final String TAG = "ProfileMultiAdapter";

    private Context mContext;
    private LayoutInflater inflater;
    private  ArrayList<ProfileMultiSelectItem> list;

    public ProfileMultiSelectAdapter(Context context, ArrayList<ProfileMultiSelectItem> list) {
        this.mContext = context;
        this.list = list;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null ) {
            view = inflater.inflate(R.layout.rows_profile_edit_multi, parent, false);
        }

        TextView rowText = (TextView) view.findViewById(R.id.row_text);
        rowText.setText(list.get(position).getItemNm());

        ImageView rowCheck = (ImageView) view.findViewById(R.id.row_check);
        Boolean checked = list.get(position).isChecked();
        if (checked!=null) {
            if (checked) {
                rowCheck.setImageResource(R.drawable.btn_checkbox_on);
                view.setBackgroundColor(Color.parseColor("#FFF5F5F5"));
            } else {
                rowCheck.setImageResource(R.drawable.btn_checkbox);
                view.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            }
        }
        return view;
    }

    public void toggleChecked(int position) {
        list.get(position).toggleCheck();
        notifyDataSetChanged();
    }

//    public boolean isSelected() {
//        return mCheckedMap.isEmpty();
//    }

    public String getResult(String type){
        String result="";

        for(int i=0; i < getCount(); i++){
            if(list.get(i).isChecked()) {
                if(type.equals("code"))
                    result += list.get(i).getItemCode() + ",";
                else
                    result += list.get(i).getItemNm() +",";
            }
        }
        result = result.length()!=0 ? result.substring(0, result.length() - 1) : "";
        return result;
    }
}

