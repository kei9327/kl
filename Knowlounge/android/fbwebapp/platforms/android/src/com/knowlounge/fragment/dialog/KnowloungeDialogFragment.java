package com.knowlounge.fragment.dialog;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.knowlounge.R;

import org.json.JSONObject;

/**
 * Created by Mansu on 2016-12-23.
 */

public class KnowloungeDialogFragment extends DialogFragment {

    private View rootView;
    private String dialogMode;
    private String dialogContent;

    private TextView textViewContent;
    private Button btnConfirm, btnCancel;

    private final String DIALOG_MODE_ALERT = "alert";
    private final String DIALOG_MODE_CONFIRM = "confirm";

    private View.OnClickListener confirmBtnListener;

    public static KnowloungeDialogFragment newInstance(String dialogMode, String dialogContent) {
        KnowloungeDialogFragment fragment = new KnowloungeDialogFragment();

        Bundle args = new Bundle();
        args.putString("mode", dialogMode);
        args.putString("content", dialogContent);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialogMode = getArguments().getString("mode");
        dialogContent = getArguments().getString("content");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_knowlounge_dialog, container);

        textViewContent = (TextView) rootView.findViewById(R.id.dialog_content);
        btnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
        btnConfirm = (Button) rootView.findViewById(R.id.btn_confirm);

        textViewContent.setText(dialogContent);

        if (TextUtils.equals(dialogMode, DIALOG_MODE_ALERT)) {
            btnCancel.setVisibility(View.GONE);
        } else if (TextUtils.equals(dialogMode, DIALOG_MODE_CONFIRM)) {
            btnCancel.setVisibility(View.VISIBLE);
        }

        if (confirmBtnListener != null) {
            btnConfirm.setOnClickListener(confirmBtnListener);
        }

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);  // remove dialog title
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  // remove dialog background

        return rootView;
    }

    public void setOnConfirmClickListener(View.OnClickListener listener) {
        this.confirmBtnListener = listener;
    }

}
