package com.knowrecorder.develop.fragment.LeftMenu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.Utils.TimeConverter;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.phone.rxevent.PlayVideo;
import com.knowrecorder.rxjava.EventBus;
import com.knowrecorder.rxjava.RxEventFactory;
import com.knowrecorder.transform.CircleBitmapTransform;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-11-30.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteItemRow>{

    private ArrayList<NoteInformation> itemsList;
    private Context mContext;

    public NoteAdapter(Context mContext, ArrayList<NoteInformation> itemsList) {
        this.itemsList = itemsList;
        this.mContext = mContext;
    }

    @Override
    public NoteItemRow onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rb_note_item, null);
        return new NoteItemRow(v);
    }

    @Override
    public void onBindViewHolder(NoteItemRow holder, int position) {

        Log.d("NoteAdapter", " onBindViewHolder");

        final NoteInformation data = itemsList.get(position);

        holder.noteTitle.setText(data.getTitle());
        holder.playTime.setText(TimeConverter.convertSecondsToMmSs(data.getRunTime()));
        holder.noteCreateDate.setText(data.getCreateDate());

        Glide.with(mContext).load(data.getThumbNailPath()).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.noteThumbnail);

        if(position == 0)
            holder.rootView.setPadding((int)PixelUtil.getInstance().convertDpToPixel(30), (int)PixelUtil.getInstance().convertDpToPixel(60), 0, 0);
        else if(position == itemsList.size()-1)
            holder.rootView.setPadding((int)PixelUtil.getInstance().convertDpToPixel(30), (int)PixelUtil.getInstance().convertDpToPixel(30), 0, (int)PixelUtil.getInstance().convertDpToPixel(60));
        else
            holder.rootView.setPadding((int)PixelUtil.getInstance().convertDpToPixel(30), (int)PixelUtil.getInstance().convertDpToPixel(30), 0, 0);

        holder.deleteNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxEventFactory.get().post(new EventType(EventType.DEL_NOTE, data.getNoteName()));
            }
        });

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxEventFactory.get().post(new EventType(EventType.SET_NOTE, data.getNoteName()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class NoteItemRow extends RecyclerView.ViewHolder{
        final View rootView;
        final ImageView noteThumbnail;
        final TextView playTime;
        final TextView noteTitle;
        final ImageView deleteNote;
        final TextView noteCreateDate;

        public NoteItemRow(View itemView) {
            super(itemView);
            this.rootView = itemView;
            this.noteThumbnail = (ImageView) rootView.findViewById(R.id.note_thumbnail);
            this.playTime = (TextView) rootView.findViewById(R.id.note_run_time);
            this.noteTitle = (TextView) rootView.findViewById(R.id.note_title);
            this.deleteNote = (ImageView) rootView.findViewById(R.id.btn_delete_note);
            this.noteCreateDate = (TextView) rootView.findViewById(R.id.note_create_date);
        }
    }
}
