package com.knowrecorder.develop.fragment.Dialog;

import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;

/**
 * Created by we160303 on 2017-02-27.
 */

public class HelpDialog extends DialogFragment {
    View rootView;
    ImageView btnClose;
    WebView webView;
    private Tracker mTracker;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.rb_dialog_help, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        KnowRecorderApplication application = (KnowRecorderApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        setBindViewId();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSfwFvJ0ThoI4xobo2hkV-BuZrqKEDeJgAK6E0BO1A5u26HTxw/viewform");

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }


    private void setBindViewId() {
        btnClose = (ImageView) rootView.findViewById(R.id.btn_help_close);
        webView = (WebView) rootView.findViewById(R.id.web_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("HelpActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;

        int dialogWidth = ViewGroup.LayoutParams.MATCH_PARENT;
        int dialogHeight = ViewGroup.LayoutParams.MATCH_PARENT;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

    }
}
