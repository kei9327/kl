package com.knowrecorder.phone.tab.setting;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowrecorder.R;

/**
 * Created by we160303 on 2016-12-05.
 */

public class PhoneSettingWebViewActivity extends DialogFragment {
    private TextView fullScreenWebviewTitle;
    private LinearLayout progressBar;
    private WebView webView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("FullScreenWebView", "onCrateView");
        View rootView = inflater.inflate(R.layout.fragment_full_screen_webview, null);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getDialog().setCancelable(false);

        fullScreenWebviewTitle = (TextView) rootView.findViewById(R.id.full_screen_webview_title);
        progressBar = (LinearLayout) rootView.findViewById(R.id.progress_bar);
        ((ImageView)rootView.findViewById(R.id.full_screen_webview_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        if(getArguments().getString("type").equals("I")){
            fullScreenWebviewTitle.setText(getActivity().getResources().getString(R.string.s_question));
        }else if(getArguments().getString("type").equals("T")){
            fullScreenWebviewTitle.setText(getActivity().getResources().getString(R.string.s_terms));
        }else if(getArguments().getString("type").equals("P")){
            fullScreenWebviewTitle.setText(getActivity().getResources().getString(R.string.s_privacy));
        }
        webView = (WebView) rootView.findViewById(R.id.contract_web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(getArguments().getString("uri"));

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

}
