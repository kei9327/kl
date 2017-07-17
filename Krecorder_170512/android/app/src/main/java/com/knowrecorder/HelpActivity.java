package com.knowrecorder;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class HelpActivity extends AppCompatActivity {

    private WebView webView;
    private Tracker mTracker;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KnowRecorderApplication application = (KnowRecorderApplication) getApplication();
        mTracker = application.getDefaultTracker();

        setContentView(R.layout.activity_help);

        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSfwFvJ0ThoI4xobo2hkV-BuZrqKEDeJgAK6E0BO1A5u26HTxw/viewform");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("HelpActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}