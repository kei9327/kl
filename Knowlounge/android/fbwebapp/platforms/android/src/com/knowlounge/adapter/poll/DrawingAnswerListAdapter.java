package com.knowlounge.adapter.poll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.fragment.poll.QuestionResultFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by changha on 2016-04-04.
 */
public class DrawingAnswerListAdapter extends BaseAdapter {

    private static final String TAG = "DrawingAnswerAdapter";

    private Context mContext;
    private ArrayList<QuestionResultFragment.QuestionAnswerUser> list;
    private HashMap<Integer, Boolean> mCheckedMap = new HashMap<>();


    public DrawingAnswerListAdapter(Context context, ArrayList<QuestionResultFragment.QuestionAnswerUser> list){
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
        String pollNo = list.get(position).getPollNo();
        final String pollUserNo = list.get(position).getPollUserNo();
        final String pollFileNo = list.get(position).getPollFileNo();
        String itemUserNm = list.get(position).getUserNm();
        final String answerData = list.get(position).getAnswerData();
        String itemUserThumbnail = list.get(position).getUserThumbnail();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.poll_result_drawing_parent, null);
        }

        final LinearLayout pollResultType3Layout = (LinearLayout) convertView.findViewById(R.id.layout_drawing_result);
        final LinearLayout pollResultType3UnderlineLayout = (LinearLayout) convertView.findViewById(R.id.layout_drawing_result_body);

        ImageView imgUserThumb = (ImageView) convertView.findViewById(R.id.img_user_thumbnail);
        TextView txtUserNm = (TextView) convertView.findViewById(R.id.txt_usernm);

        LinearLayout layerDrawingAnswer = (LinearLayout)convertView.findViewById(R.id.layer_drawing_answer);
        ImageView imgDrawingResult = (ImageView) convertView.findViewById(R.id.img_drawing_answer);
        ImageView foldBtn = (ImageView) convertView.findViewById(R.id.btn_fold);

        View divider = (View) convertView.findViewById(R.id.list_divider);

        // 유저 썸네일 출력
        Glide.with(mContext)
                .load(itemUserThumbnail)
                .error(R.drawable.img_userlist_default01)
                .bitmapTransform(new CircleTransformTemp(mContext))
                .into(imgUserThumb);

        // 판서 답안 이미지 바이너리 출력
        Glide.with(mContext)
                .load(answerData)
                .asBitmap()
                .error(mContext.getResources().getIdentifier("thumbnail_01", "drawable", mContext.getPackageName()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imgDrawingResult);

        // 유저 이름 출력
        txtUserNm.setText(!itemUserNm.equals("") ? itemUserNm : mContext.getResources().getString(R.string.userlist_guest));

        if (mCheckedMap.get(position)) {
            layerDrawingAnswer.setVisibility(View.VISIBLE);
            foldBtn.setImageResource(R.drawable.btn_poll_fold_on);
        } else {
            layerDrawingAnswer.setVisibility(View.GONE);
            foldBtn.setImageResource(R.drawable.btn_poll_fold);

            if (position == list.size()-1) {
                divider.setVisibility(View.GONE);
            } else {
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

        imgDrawingResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : 판서 답변 공유 기능 추가
//                Toast.makeText(mContext, "답변을 공유합니다.", Toast.LENGTH_SHORT).show();
//                CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
//                webView.sendJavascript("PollCtrl.Action.Master.Question.shareDrawAnswer('" + pollFileNo + "', '" + answerData + "', '" + pollUserNo + "')");
            }
        });

        return convertView;
    }
}
