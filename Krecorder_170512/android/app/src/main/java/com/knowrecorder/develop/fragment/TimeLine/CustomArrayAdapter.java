package com.knowrecorder.develop.fragment.TimeLine;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.knowrecorder.R;

/** An array adapter that knows how to render views when given CustomData classes */
public class CustomArrayAdapter extends ArrayAdapter<CustomData> {
    private LayoutInflater mInflater;
    private Context mContext;

    public CustomArrayAdapter(Context context, CustomData[] values) {
        super(context, R.layout.custom_data_view, values);
        mContext = context;
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            // Inflate the view since it does not exist
            convertView = mInflater.inflate(R.layout.custom_data_view, parent, false);

            // Create and save off the holder in the tag so we get quick access to inner fields
            // This must be done for performance reasons
            holder = new Holder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        if(position == 0){
            holder.textView.setPadding(getMargin(),0,0,0);
        }else if(position == getCount()-1){
            holder.textView.setPadding(0,0,getMargin(),0);
        }

        // Populate the text
        holder.textView.setText(getItem(position).getText());

        // Set the color
        convertView.setBackgroundColor(getItem(position).getBackgroundColor());

        return convertView;
    }
    private int getMargin(){
        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int resolutionWidth = metrics.widthPixels;
        return resolutionWidth/2;
    }
    /** View holder for the views we need access to */
    private static class Holder {
        public TextView textView;
    }
}