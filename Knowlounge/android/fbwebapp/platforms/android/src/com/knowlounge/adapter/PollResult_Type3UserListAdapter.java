package com.knowlounge.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.fragment.poll.QuestionResultFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by changha on 2016-04-04.
 */
public class PollResult_Type3UserListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<QuestionResultFragment.PollResultType3User> list;
    private HashMap<Integer, Boolean> mCheckedMap = new HashMap<>();


    public PollResult_Type3UserListAdapter(Context context, ArrayList<QuestionResultFragment.PollResultType3User> list){
        this.mContext = context;
        this.list = list;

        for (int i=0; i<list.size();i++){
            mCheckedMap.put(i, false);
        }
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
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String itemUserNm = list.get(position).getUserNm();
        String itemTitle = list.get(position).getUserItemNm();
        String itemUserThumbnail = list.get(position).getUserThumbnail();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.poll_result_type3_group, null);
        }

        final LinearLayout pollResultType3Layout = (LinearLayout) convertView.findViewById(R.id.poll_result_type3_layout);
        final LinearLayout pollResultType3UnderlineLayout = (LinearLayout) convertView.findViewById(R.id.poll_result_type3_underline_layout);

        ImageView pollResultType3Thumbnail = (ImageView) convertView.findViewById(R.id.poll_result_type3_thumbnail);
        TextView pollResultType3Usernm = (TextView) convertView.findViewById(R.id.poll_result_type3_usernm);
        TextView pollResultType3Answer = (TextView) convertView.findViewById(R.id.poll_result_type3_answer);
        final TextView pollResultType3DetailText = (TextView) convertView.findViewById(R.id.poll_result_type3_detail_text);
        ImageView foldBtn = (ImageView) convertView.findViewById(R.id.poll_result_type3_on_off);

        View divider = (View) convertView.findViewById(R.id.list_divider);

        Glide.with(mContext).load(itemUserThumbnail).error(R.drawable.img_userlist_default01).bitmapTransform(new CircleTransformTemp(mContext)).into(pollResultType3Thumbnail);
//        Picasso.with(mContext).load(thumbnail).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(pollResultType3Thumbnail);  // 썸네일 이미지 로드
        pollResultType3Usernm.setText(!itemUserNm.equals("") ? itemUserNm : mContext.getResources().getString(R.string.userlist_guest));
        pollResultType3Answer.setText(itemTitle);
        pollResultType3DetailText.setText(itemTitle);

        if (mCheckedMap.get(position)) {
//            pollResultType3Layout.setBackgroundColor(Color.parseColor("#f5f5f5"));
//            pollResultType3UnderlineLayout.setBackgroundColor(Color.parseColor("#00000000"));
            pollResultType3DetailText.setVisibility(View.VISIBLE);
            foldBtn.setImageResource(R.drawable.btn_poll_fold_on);
        } else {
            pollResultType3DetailText.setVisibility(View.GONE);
            foldBtn.setImageResource(R.drawable.btn_poll_fold);

            if (position == list.size()-1) {
//                pollResultType3Layout.setBackgroundResource(R.drawable.bg_under_gray_background_white);
//                pollResultType3UnderlineLayout.setBackgroundColor(Color.parseColor("#00000000"));
                divider.setVisibility(View.GONE);
            } else {
//                pollResultType3Layout.setBackgroundColor(Color.parseColor("#ffffff"));
//                pollResultType3UnderlineLayout.setBackgroundResource(R.drawable.bg_under_gray_background_white);
                divider.setVisibility(View.VISIBLE);
            }
        }


        pollResultType3Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckedMap.put(position,!mCheckedMap.get(position));
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
