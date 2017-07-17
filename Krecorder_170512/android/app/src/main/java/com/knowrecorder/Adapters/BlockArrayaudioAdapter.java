//package com.knowrecorder.Adapters;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.LinearLayout;
//
//import com.knowrecorder.RxEvent.RecordPacketDelet;
//import com.knowrecorder.Timeline.BlockData;
//import com.knowrecorder.Timeline.TimelineBlock;
//import com.knowrecorder.Utils.PixelUtil;
//import com.knowrecorder.rxjava.EventBus;
//
//import java.util.ArrayList;
//
///**
// * Created by we160303 on 2016-12-22.
// */
//
//public class BlockArrayaudioAdapter extends ArrayAdapter<BlockData> {
//    private Context context;
//    private int centerX;
//    private ArrayList<BlockData> blocks;
//
//    public BlockArrayaudioAdapter(Context context, ArrayList<BlockData> blocks, int centerX) {
//        super(context, 0, blocks);
//        this.context = context;
//        this.centerX = centerX;
//        this.blocks = blocks;
//    }
//
//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent) {
//        BlockArrayaudioAdapter.Holder holder;
//
//        if (blocks.size() > 0) {
//
//            if (convertView == null) {
//                convertView = new LinearLayout(context);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                convertView.setLayoutParams(params);
//                TimelineBlock block = new TimelineBlock(context);
//                holder = new BlockArrayaudioAdapter.Holder();
//                holder.timelineBlock = block;
//                if (getItem(position).getWidth() > 0) {
//                    holder.timelineBlock.drawBlock(getItem(position).getWidth(), getItem(position).getColor(), getItem(position).getType());
//                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(getItem(position).getWidth(), (int) PixelUtil.getInstance().convertDpToPixel(30));
//                    holder.timelineBlock.setLayoutParams(params2);
//                    ((ViewGroup) convertView).addView(holder.timelineBlock);
//                }
//                convertView.setTag(holder);
//            } else {
//                holder = (BlockArrayaudioAdapter.Holder) convertView.getTag();
//                if (getItem(position).getWidth() > 0) {
//                    holder.timelineBlock.drawBlock(getItem(position).getWidth(), getItem(position).getColor(), getItem(position).getType());
//                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(getItem(position).getWidth(), (int) PixelUtil.getInstance().convertDpToPixel(30));
//                    holder.timelineBlock.setLayoutParams(params2);
//                }
//            }
//
//            if (blocks.size() == 1) {
//                convertView.setPadding(getItem(position).getGap() + centerX, 0, centerX, 0);
//            } else {
//                if (position == 0) { // 처음 패킷일 때
//                    convertView.setPadding(getItem(position).getGap() + centerX, 0, 0, 0);
//                } else if (position == getCount() - 1) { // 마지막 패킷일 때
//                    convertView.setPadding(getItem(position).getGap(), 0, centerX, 0);
//                } else {
//                    convertView.setPadding(getItem(position).getGap(), 0, 0, 0);
//                }
//            }
//
//            if (getItem(position).getRightMargin() > 0) {
//                ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(holder.timelineBlock.getLayoutParams());
//                marginParams.setMargins(0, 0, getItem(position).getRightMargin(), 0);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);
//                holder.timelineBlock.setLayoutParams(layoutParams);
//            }
//
//            return convertView;
//        } else {
//            return null;
//        }
//    }
//
//    private static class Holder {
//        public TimelineBlock timelineBlock;
//    }
//}
