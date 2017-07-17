package com.knowrecorder.develop.fragment.Dialog;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;

/**
 * Created by we160303 on 2017-02-27.
 */

public class InformationDialog extends DialogFragment {
    View rootView;
    TextView btnServiceAgreement, btnPrivacy, versionText;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.rb_dialog_infomation, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBindViewId();

        btnServiceAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        try {
            versionText.setText(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void setBindViewId() {
        btnServiceAgreement = (TextView) rootView.findViewById(R.id.btn_service_agreement);
        btnPrivacy = (TextView) rootView.findViewById(R.id.btn_privacy);
        versionText = (TextView) rootView.findViewById(R.id.version_text);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;

        int dialogWidth = (int) PixelUtil.getInstance().convertDpToPixel(500);
        int dialogHeight = (int)PixelUtil.getInstance().convertDpToPixel(266);

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

    }
}
