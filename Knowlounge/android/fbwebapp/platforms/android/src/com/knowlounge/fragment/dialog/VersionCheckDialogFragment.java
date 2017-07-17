package com.knowlounge.fragment.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by Minsu on 2016-05-30.
 */
public class VersionCheckDialogFragment extends DialogFragment{

    private static String TAG = "VersionCheckDialogFragment";
    private WenotePreferenceManager prefManager;
    private View rootView;
    private String type = "";
    private String msg;

    private TextView version_msg;
    private Button version_ok_btn, version_cancel_btn;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
        type = getArguments().getString("type");
        msg = getArguments().getString("msg");
    }


    @Override
    public void onStart() {
        super.onStart();

        if(getDialog() == null)
            return;

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getDialog().getWindow().getAttributes();

        if (type.equals("T")) {
            params.width = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? ViewGroup.LayoutParams.MATCH_PARENT : (int)(500 * prefManager.getDensity());
            params.height = prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE ? ViewGroup.LayoutParams.MATCH_PARENT : (int)(600 * prefManager.getDensity());
        } else if (type.equals("N")) {
            params.width = (int)(290 * prefManager.getDensity());
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if (type.equals("M")) {
            params.width = (int)(290 * prefManager.getDensity());
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        getDialog().getWindow().setLayout(params.width, params.height);
        getDialog().setCancelable(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_version_chk_dialog, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);  // remove dialog title
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  // remove dialog background

        setFindViewById();
        setUI();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        version_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("T")) {
                    getDialog().dismiss();
                } else if (type.equals("N")) {
                    //PlayStoreIntent
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName())));

                } else if (type.equals("M")) {
                    //PlayStoreIntent
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName())));
                }
            }
        });

        version_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    private void setFindViewById() {
        version_msg = (TextView) rootView.findViewById(R.id.version_msg);
        version_ok_btn = (Button) rootView.findViewById(R.id.version_ok_btn);
        version_cancel_btn = (Button) rootView.findViewById(R.id.version_cancel_btn);
    }

    private void setUI() {

        if(msg.equals("")){
            version_msg.setText(getResources().getString(R.string.request_update));
        }else{
            version_msg.setText(msg);
        }

        if(type.equals("T")){
            version_cancel_btn.setVisibility(View.GONE);
            version_ok_btn.setVisibility(View.VISIBLE);
        }else if(type.equals("N")){
            version_cancel_btn.setVisibility(View.VISIBLE);
            version_ok_btn.setVisibility(View.VISIBLE);
        }else if(type.equals("M")){
            version_cancel_btn.setVisibility(View.GONE);
            version_ok_btn.setVisibility(View.VISIBLE);
        }
    }
}
