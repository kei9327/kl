package com.knowlounge.fragment.poll;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by Mansu on 2017-02-09.
 *  - 판서형 질문을 받았을 때 띄워주는 DialogFragment
 *  - 질문 내용과 질문 이미지가 포함되어 있음.
 *  - 다른 사람의 보드에서 창이 띄워질 때는 "내 보드로 이동하기" 버튼으로 설정되고, 내 보드에서 창이 띄워질 때는 "답변하기" 버튼으로 설정됨.
 */
public class DrawingPollNotifyFragment extends DialogFragment {

    private final String TAG = "DrawingPollNotify";

    View rootView;
    private Context context;

    private TextView txtPollTitle, btnMoveBoard;
    private ImageView pollImagePreview;

    public static DrawingPollNotifyFragment _instance;

    public WenotePreferenceManager prefManager;

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        this.context = context;
        prefManager = WenotePreferenceManager.getInstance(context);
    }


    @Override
    public void onStart() {
        // 폰에서는 풀스크린으로 구성하기..
        if (KnowloungeApplication.isPhone){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            getDialog().getWindow().setLayout((int) (600 * prefManager.getDensity()), (int) (540 * prefManager.getDensity()));
        }
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final Bundle param = getArguments();

        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_drawing_poll_notify, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setCancelable(false);

        txtPollTitle = (TextView) rootView.findViewById(R.id.txt_poll_title);
        btnMoveBoard = (TextView) rootView.findViewById(R.id.btn_move_board);
        pollImagePreview = (ImageView) rootView.findViewById(R.id.poll_image_preview);

        String pollTitle = param.getString("title");
        String pollImage = param.getString("url");

        final String roomCode = param.getString("code");
        final String pollNo = param.getString("pollno");
        final String timeLimit = param.getString("timelimit");
        final String isCountdown = param.getString("iscountdown");
        final boolean isMove = param.getBoolean("ismove");
        final String imageMap = param.getString("image");

        txtPollTitle.setText(pollTitle);
        Glide.with(context)
                .load(pollImage)
                .asBitmap()
                .error(context.getResources().getIdentifier("thumbnail_01", "drawable", context.getPackageName()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(pollImagePreview);

        if (isMove) {
            btnMoveBoard.setText(getString(R.string.canvas_navi_sub));
            btnMoveBoard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("pollno", pollNo);
                    bundle.putString("timelimit", timeLimit);
                    bundle.putString("iscountdown", isCountdown);
                    bundle.putString("image", imageMap);

                    RoomActivity.activity.moveRoom(roomCode, bundle);
                }
            });
        } else {
            btnMoveBoard.setText(getString(R.string.answer_response_title));
            btnMoveBoard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });
        }

        return rootView;
    }
}
