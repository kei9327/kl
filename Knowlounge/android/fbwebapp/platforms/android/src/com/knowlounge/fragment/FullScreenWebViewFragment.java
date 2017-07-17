package com.knowlounge.fragment;

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
import android.widget.TextView;

import com.knowlounge.R;


/**
 * Created by Minsu on 2016-04-07.
 */
public class FullScreenWebViewFragment extends DialogFragment {

    private TextView fullScreenWebviewTitle;
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
        ((ImageView)rootView.findViewById(R.id.btn_mainleft_help_contract_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        if(getArguments().getString("type").equals("C")){
            fullScreenWebviewTitle.setText(getResources().getString(R.string.help_contact));
        }else if(getArguments().getString("type").equals("T")){
            fullScreenWebviewTitle.setText(getResources().getString(R.string.setting_inform_service));
        }else if(getArguments().getString("type").equals("P")){
            fullScreenWebviewTitle.setText(getResources().getString(R.string.setting_inform_privacy));
        }
        webView = (WebView) rootView.findViewById(R.id.contract_web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(getArguments().getString("uri"));
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