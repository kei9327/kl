package com.knowrecorder.phone.tab;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.knowrecorder.R;
import com.knowrecorder.phone.PhoneOpencourseActivity;
import com.knowrecorder.phone.rxevent.SelectTab;
import com.knowrecorder.rxjava.EventBus;

/**
 * Created by we160303 on 2016-11-29.
 */

public class GroupFragment extends Fragment {

    private View rootView;
    private ImageView toggleBtn;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.p_fragment_my_school, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggleBtn = (ImageView) rootView.findViewById(R.id.p_op_group_toggle_btn);
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectTab event = new SelectTab();

                event.setTab(PhoneOpencourseActivity.SUBJECT_TAB);
                event.setSubject(PhoneOpencourseActivity.ALL);

                EventBus.getInstance().post(event);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
