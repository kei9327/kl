package com.knowrecorder.develop.fragment.Dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.knowrecorder.R;
import com.knowrecorder.Utils.E;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.develop.audio.AudioPlayer;
import com.knowrecorder.develop.event.ExportSelectedDialogEvent;
import com.knowrecorder.rxjava.RxEventFactory;

/**
 * Created by we160303 on 2017-03-20.
 */

public class ExportDialog extends DialogFragment implements View.OnClickListener {

    private View rootView;
    private ImageButton btnClose;
    private TextView btnOpenCourse;
    private TextView btnGallary;
    private TextView btnYoutube;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.rb_dialog_export, container, false);
        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBindViewId();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;

        int dialogWidth = (int) PixelUtil.getInstance().convertDpToPixel(400);
        int dialogHeight = ViewPager.LayoutParams.WRAP_CONTENT;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_close :
                break;
            case R.id.btn_opencourse :
                if(AudioPlayer.getInstance(getActivity()).prevGetDuration() != 0)
                    RxEventFactory.get().post(new ExportSelectedDialogEvent(ExportSelectedDialogEvent.EXPORT_OPENCOURSE));
                else
                    Toast.makeText(getActivity(), "녹화 분량이 없음....", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_gallery :
                Toast.makeText(getActivity(), "준비중입니다.", Toast.LENGTH_SHORT).show();
                return;
//                RxEventFactory.get().post(new ExportSelectedDialogEvent(ExportSelectedDialogEvent.EXPORT_GALLARY));
//                break;
            case R.id.btn_youtube :
                Toast.makeText(getActivity(), "준비중입니다.", Toast.LENGTH_SHORT).show();
                return;
//                RxEventFactory.get().post(new ExportSelectedDialogEvent(ExportSelectedDialogEvent.EXPORT_YOUTUBE));
//                break;
        }
        dismiss();
    }

    private void setBindViewId() {
        btnClose = (ImageButton) rootView.findViewById(R.id.btn_close);
        btnOpenCourse = (TextView) rootView.findViewById(R.id.btn_opencourse);
        btnGallary = (TextView) rootView.findViewById(R.id.btn_gallery);
        btnYoutube = (TextView) rootView.findViewById(R.id.btn_youtube);

        btnClose.setOnClickListener(this);
        btnOpenCourse.setOnClickListener(this);
        btnGallary.setAlpha(E.ALPHA.INACTIVE);
        //btnGallary.setOnClickListener(this);
        btnYoutube.setAlpha(E.ALPHA.INACTIVE);
        //btnYoutube.setOnClickListener(this);

    }


}
