//package com.knowrecorder.Adapters;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.knowrecorder.Timeline.TimeTextBlock;
//import com.knowrecorder.Utils.PixelUtil;
//
//import java.util.ArrayList;
//
//public class BlockTimeAdapter extends ArrayAdapter<TimeTextBlock> {
//    private Context context;
//    private int centerX;
//    private ArrayList<TimeTextBlock> times;
//    private final static int SECONDS_DEFAULT_LENGTH = 200;
//
//    public BlockTimeAdapter(Context context, ArrayList<TimeTextBlock> times, int centerX) {
//        super(context, 0, times);
//        this.context = context;
//        this.centerX = centerX;
//        this.times = times;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Holder holder;
//
//        if (times.size() > 0) {
//
//            if (convertView == null) {
//                convertView = new LinearLayout(context);
//
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                convertView.setLayoutParams(params);
//
//                TextView textView = new TextView(context);
//                textView.setTextColor(Color.WHITE);
//                textView.setText(convertSecondsToHMmSs(getItem(position).getTime()));
//                LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                textView.setLayoutParams(tvParams);
//                ((ViewGroup) convertView).addView(textView);
//
//                holder = new Holder();
//                holder.textView = textView;
//
//                convertView.setTag(holder);
//            } else {
//                holder = (Holder) convertView.getTag();
//                holder.textView.setText(convertSecondsToHMmSs(getItem(position).getTime()));
//                LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                holder.textView.setLayoutParams(tvParams);
//            }
//
//            if (times.size() == 1) {
//                convertView.setPadding(centerX - (int) PixelUtil.getInstance().convertDpToPixel(17), 0, centerX + (int) PixelUtil.getInstance().convertDpToPixel(17), 0);
//            } else {
//                if (position == 0) { // 처음 패킷일 때
//                    convertView.setPadding(centerX - (int) PixelUtil.getInstance().convertDpToPixel(17), 0, 0, 0);
//                } else if (position == getCount() - 1) { // 마지막 패킷일 때
//                    convertView.setPadding((int) PixelUtil.getInstance().convertDpToPixel(SECONDS_DEFAULT_LENGTH) - (int) PixelUtil.getInstance().convertDpToPixel(35), 0, centerX + (int) PixelUtil.getInstance().convertDpToPixel(17), 0);
//                } else {
//                    convertView.setPadding((int) PixelUtil.getInstance().convertDpToPixel(SECONDS_DEFAULT_LENGTH) - (int) PixelUtil.getInstance().convertDpToPixel(35), 0, 0, 0);
//                }
//            }
//
//            if (getItem(position).getRightMargin() > 0) {
//                ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(holder.textView.getLayoutParams());
//                marginParams.setMargins(0, 0, getItem(position).getRightMargin(), 0);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);
//                holder.textView.setLayoutParams(layoutParams);
//            }
//
//            return convertView;
//        } else {
//            return null;
//        }
//    }
//
//    private static class Holder {
//        public TextView textView;
//    }
//
//    private String convertSecondsToHMmSs(long millis) {
//        long s = (millis / 1000) % 60;
//        long m = (millis / (1000 * 60)) % 60;
//        return String.format("%02d:%02d", m, s);
//    }
//}
