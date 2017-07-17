/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.knowlounge.multipage;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.multipage.data.AbstractDataProvider;
import com.knowlounge.multipage.data.MultiPageDataProvider;
import com.knowlounge.multipage.utils.DrawableUtils;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.MultiPageEvent;

class DraggableGridItemAdapter
        extends RecyclerView.Adapter<DraggableGridItemAdapter.MyViewHolder>
        implements DraggableItemAdapter<DraggableGridItemAdapter.MyViewHolder> {
    private static final String TAG = "MyDraggableItemAdapter";
    private int mItemMoveMode = RecyclerViewDragDropManager.ITEM_MOVE_MODE_DEFAULT;

    private static final int ITEM_VIEW_TYPE_SECTION_FOOTER = MultiPageDataProvider.ITEM_VIEW_TYPE_SECTION_FOOTER;
    private static final int ITEM_VIEW_TYPE_SECTION_ITEM = MultiPageDataProvider.ITEM_VIEW_TYPE_SECTION_ITEM;

    // NOTE: Make accessible with short name
    private interface Draggable extends DraggableItemConstants {
    }

    private AbstractDataProvider mProvider;
    private Context mContext;
    private int currentPositon = 0;
    private boolean dragStart = false;

    public static class MyViewHolder extends AbstractDraggableItemViewHolder {
        public View rootView;
        public FrameLayout mContainer;
        public ImageView mThumbnail;
        public ImageView mEventBtn;
        public TextView mNumber;

        public MyViewHolder(View v) {
            super(v);
            rootView  = v;
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mThumbnail = (ImageView) v.findViewById(R.id.multi_page_thumbnail) ;
//            mDragHandle = v.findViewById(R.id.drag_handle);
            mEventBtn = (ImageView) v.findViewById(R.id.multi_page_event_btn);
            mNumber = (TextView) v.findViewById(R.id.multi_page_number);
        }
    }

    public DraggableGridItemAdapter(Context context, @Nullable AbstractDataProvider dataProvider) {

        mProvider = dataProvider;
        mContext = context;
        // DraggableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    public void setItemMoveMode(int itemMoveMode) {
        mItemMoveMode = itemMoveMode;
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return mProvider.getItem(position).getViewType();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View v;
        switch (viewType) {
            case ITEM_VIEW_TYPE_SECTION_FOOTER:
                v = inflater.inflate(R.layout.multi_page_footer, parent, false);
                break;
            case ITEM_VIEW_TYPE_SECTION_ITEM:
                v = inflater.inflate(R.layout.multi_page_item, parent, false);
                break;
            default:
                throw new IllegalStateException("Unexpected viewType (= " + viewType + ")");
        }

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_SECTION_FOOTER :
                onBindSectionFooterViewHolder(holder, position);
                break;
            case ITEM_VIEW_TYPE_SECTION_ITEM :
                onBindSectionItemViewHolder(holder, position);
                break;
        }
    }

    private void onBindSectionFooterViewHolder(MyViewHolder holder, int position) {
        final AbstractDataProvider.Data item = mProvider.getItem(position);

        // set text
        holder.mEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "AddBtn 눌림", Toast.LENGTH_SHORT).show();
                RoomActivity.activity.addCanvasPage();
            }
        });
    }

    private void onBindSectionItemViewHolder(MyViewHolder holder, int position) {
        Log.d(TAG, "onBindSectionItemViewHolder / position : " + position);
        final AbstractDataProvider.Data item = mProvider.getItem(position);

        String svrFlag = mContext.getResources().getString(R.string.svr_flag);
        String svrHost = mContext.getResources().getString(mContext.getResources().getIdentifier("svr_host_" + svrFlag, "string", mContext.getPackageName()));

        String url = svrHost + "/data/fb/room/" + RoomActivity.activity.getRoomId().substring(0, 3) + "/" + RoomActivity.activity.getRoomId() + "_page/" + item.getPageId() + ".jpg";
        Uri thumbnailUri = Uri.parse(url);

        if (RoomActivity.activity.getCurrentPageId().equals(item.getPageId())) {
            holder.mEventBtn.setVisibility(View.GONE);
        } else {
            holder.mEventBtn.setVisibility(View.VISIBLE);
        }

        if(KnowloungeApplication.isPhone) {
            holder.mNumber.setText(position + 1 + "");
        }

        holder.mEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "DeleteBtn 눌림", Toast.LENGTH_SHORT).show();
                RoomActivity.activity.removeCanvasPage(item.getPageId());
            }
        });

        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.changeCanvasPage(item.getPageId());
            }
        });

        if(!dragStart) {
            Glide.with(mContext)
                    .load(thumbnailUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.mThumbnail);
        }


        final int dragState = holder.getDragStateFlags();

        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
            Log.d(TAG, "true");
            int bgResId;

            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = getCheckSelectedPage(item, position);
                DrawableUtils.clearState(holder.mContainer.getForeground());
            } else if ((dragState & Draggable.STATE_FLAG_DRAGGING) != 0) {
                bgResId = getCheckSelectedPage(item, position);
            } else {
                bgResId = getCheckSelectedPage(item, position);
            }
            holder.mContainer.setBackgroundResource(bgResId);
        } else {
            Log.d(TAG, "set Multipage Checked UI");
            holder.mContainer.setBackgroundResource(getCheckSelectedPage(item, position));
        }
    }

    private int getCheckSelectedPage(AbstractDataProvider.Data item, int position){
        if (RoomActivity.activity.getCurrentPageId().equals(item.getPageId())) {

            Log.d(TAG, "selected position : " + position + "," + "pageID : " + item.getPageId() + "viewType : " + item.getViewType());

            currentPositon = position;
            EventBus.get().post(new MultiPageEvent(MultiPageEvent.CURRENT_PAGE, ""));

            return R.drawable.bg_border_multipage_selector;
        } else {
            Log.d(TAG, "normal position : " + position + "," + "pageID : " + item.getPageId() + "viewType : " + item.getViewType());
            return R.drawable.bg_border_multipage_default;
        }
    }

    @Override
    public int getItemCount() {
        return mProvider.getCount();
    }

    public int getCurrentPositon(){
        return currentPositon;
    }

    public void setDragStart(boolean drag){
        this.dragStart = drag;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        dragStart = false;
        if (fromPosition == toPosition) {
            return;
        }

        if (mItemMoveMode == RecyclerViewDragDropManager.ITEM_MOVE_MODE_DEFAULT) {
            mProvider.moveItem(fromPosition, toPosition);

            RoomActivity.activity.orderCanvasPage(makeOrderList(),mProvider.getItem(toPosition).getPageId());
            notifyItemMoved(fromPosition, toPosition);
        } else {
            mProvider.swapItem(fromPosition, toPosition);
            notifyDataSetChanged();
        }
    }

    private String makeOrderList() {
        String list = "";
        for(int i = 0; i < mProvider.getCount()-1 ; i++)
            list += mProvider.getItem(i).getPageId() + "|" + (i+1) + ",";
        return list.substring(0, list.length()-1);
    }

    @Override
    public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        Log.d(TAG,"Drag Start");
        // return false if the item is a section header
        if (holder.getItemViewType() != ITEM_VIEW_TYPE_SECTION_ITEM) {
            return false;
        }

        dragStart = true;
        return true;
    }


    @Override
    public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
        // no drag-sortable range specified
        final int start = findFirstSectionItem(position);
        final int end = findLastSectionItem(position);

        Log.d(TAG,"onGetItemDraggableRange Start : " + start + " End : " + end);

        return new ItemDraggableRange(start, end);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    private int findFirstSectionItem(int position) {
        AbstractDataProvider.Data item = mProvider.getItem(position);

        if (item.isSectionFooter()) {
            throw new IllegalStateException("section item is expected");
        }

        while (position > 0) {
            AbstractDataProvider.Data prevItem = mProvider.getItem(position - 1);

            if (prevItem.isSectionFooter()) {
                break;
            }

            position -= 1;
        }

        return position;
    }

    private int findLastSectionItem(int position) {
        AbstractDataProvider.Data item = mProvider.getItem(position);

        if (item.isSectionFooter()) {
            throw new IllegalStateException("section item is expected");
        }

        final int lastIndex = getItemCount() - 1;

        while (position < lastIndex) {
            AbstractDataProvider.Data nextItem = mProvider.getItem(position + 1);

            if (nextItem.isSectionFooter()) {
                break;
            }

            position += 1;
        }

        return position;
    }

    public void setProvider(AbstractDataProvider provider) {
        mProvider = provider;
    }
}
