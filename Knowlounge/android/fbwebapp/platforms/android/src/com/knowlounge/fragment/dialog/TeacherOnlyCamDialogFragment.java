package com.knowlounge.fragment.dialog;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.CommonEvent;
import com.knowlounge.util.AndroidUtils;

/**
 * Created by we160303 on 2016-10-28.
 */

public class TeacherOnlyCamDialogFragment extends DialogFragment {
    View rootView;
    private TextView positiveBtn;
    private TextView negativeBtn;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_teacher_only_cam_dialog, container);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        positiveBtn = (TextView) rootView.findViewById(R.id.positive_btn);
        negativeBtn = (TextView) rootView.findViewById(R.id.negative_btn);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.get().post(new CommonEvent(CommonEvent.TEACHER_ONLY_CAM, true));
                getDialog().dismiss();
            }
        });

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.get().post(new CommonEvent(CommonEvent.TEACHER_ONLY_CAM, false));
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;
        getDialog().getWindow().setLayout((int)(AndroidUtils.getPxFromDp(getActivity(),290)), ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
