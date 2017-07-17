package com.knowlounge.fragment.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by Minsu on 2016-06-16.
 */
public class TeacherCallDialogFragment extends DialogFragment {

    private String roomCode;
    private String teacherName;
    private TeacherCallDialogListener mCallback;
    private WenotePreferenceManager prefManager;

    public interface TeacherCallDialogListener {
        void onMoveTeacherRoom(String roomCode);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (TeacherCallDialogListener)context;
        prefManager = WenotePreferenceManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teacher_call_dialog, container);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        roomCode = getArguments().getString("roomcode");
        teacherName = getArguments().getString("teachernm");

        String msg = String.format(getResources().getString(R.string.canvas_noti_call), teacherName);
        ((TextView) rootView.findViewById(R.id.extend_request_content)).setText(msg);

        rootView.findViewById(R.id.btn_call_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onMoveTeacherRoom(roomCode);
            }
        });

        rootView.findViewById(R.id.btn_call_holdback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;
        getDialog().getWindow().setLayout((int)(290*prefManager.getDensity()), ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
