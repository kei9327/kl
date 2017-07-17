package com.knowrecorder.develop.fragment.TimeLine.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.develop.fragment.TimeLine.Model.TImelineAudioBlock;
import com.knowrecorder.develop.fragment.TimeLine.Model.TimelinePacketBlock;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by we160303 on 2017-03-14.
 */

public class AudioArrayAdapter extends ArrayAdapter<TImelineAudioBlock> {

    private String TAG = "AudioArrayAdapter";
    private Context mContext;
    private LayoutInflater inflater = null;

    public AudioArrayAdapter(Context context, ArrayList<TImelineAudioBlock> blocks) {
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
    public TImelineAudioBlock getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView packet;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.rb_timeline_packet_item, parent, false);
            packet = (TextView) convertView.findViewById(R.id.packet);
            convertView.setTag(new ViewHolder(packet));

        }else{
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            packet = viewHolder.packet;
        }
        TImelineAudioBlock block = getItem(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(block.getWidth(),(int) PixelUtil.getInstance().convertDpToPixel(30));
        convertView.setPadding(block.getLeftGap(), 0, block.getRightGap(), 0);
        packet.setLayoutParams(params);
        packet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_trackbar_sound, 0, 0, 0);



        return convertView;
    }

    private  static class ViewHolder {
        public final TextView packet;
        public ViewHolder(TextView packet){
            this.packet = packet;
        }
    }
    public String getTAG(){ return TAG;}
}
