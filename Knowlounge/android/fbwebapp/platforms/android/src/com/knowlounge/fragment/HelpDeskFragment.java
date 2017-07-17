package com.knowlounge.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;

public class HelpDeskFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "HelpDeskFragment";

    private View rootView;

    ImageView btn_mainleft_help_back;
    LinearLayout mainleftHelpContactFacebook, mainleftHelpContactReport, mainleftHelpAccountPassword,
            mainleftHelpGuideInvite, mainleftHelpGuideApply, mainleftHelpGuideCanvas, mainleftHelpGuideYoutube;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private WenotePreferenceManager prefManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        prefManager = WenotePreferenceManager.getInstance(activity);
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_help_desk, container, false);
        setFindViewById();

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btn_mainleft_help_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getFragmentManager().popBackStack();
                MainActivity._instance.popFragmentStack();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    private void setFindViewById() {

        btn_mainleft_help_back = (ImageView)rootView.findViewById(R.id.btn_mainleft_help_back);

        mainleftHelpContactFacebook = (LinearLayout)rootView.findViewById(R.id.mainleft_help_contact_facebook);
        mainleftHelpContactReport = (LinearLayout)rootView.findViewById(R.id.mainleft_help_contact_report);
//        mainleftHelpAccountPassword = (LinearLayout)rootView.findViewById(R.id.mainleft_help_account_password);
        mainleftHelpGuideInvite = (LinearLayout)rootView.findViewById(R.id.mainleft_help_guide_invite);
        mainleftHelpGuideApply = (LinearLayout)rootView.findViewById(R.id.mainleft_help_guide_apply);
        mainleftHelpGuideCanvas = (LinearLayout)rootView.findViewById(R.id.mainleft_help_guide_canvas);
        mainleftHelpGuideYoutube = (LinearLayout)rootView.findViewById(R.id.mainleft_help_guide_youtube);

        mainleftHelpContactFacebook.setOnClickListener(this);
        mainleftHelpContactReport.setOnClickListener(this);
//        mainleftHelpAccountPassword.setOnClickListener(this);
        mainleftHelpGuideInvite.setOnClickListener(this);
        mainleftHelpGuideApply.setOnClickListener(this);
        mainleftHelpGuideCanvas.setOnClickListener(this);
        mainleftHelpGuideYoutube.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Uri uri;
        Intent it;
        switch (v.getId()) {
            case R.id.mainleft_help_contact_facebook :
                uri = Uri.parse("https://www.facebook.com/knowlounge");
                it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;
            case R.id.mainleft_help_contact_report :
                Bundle argument = new Bundle();
                argument.putString("type","C");
                argument.putString("uri","https://docs.google.com/forms/d/16ZUUuHdCwSdLP3n35xgthYW8Fz44yYLteEMr-nm6fzk/viewform");
                FullScreenWebViewFragment fragment = new FullScreenWebViewFragment();
                fragment.setArguments(argument);
                fragment.show(getFragmentManager(), "contact");
                break;
//            case R.id.mainleft_help_account_password :
//                break;
            case R.id.mainleft_help_guide_invite :
//                Bundle argument = new Bundle();
//                argument.putString("uri","rtsp://v4.cache3.c.youtube.com/CjYLENy73wIaLQlW_ji2apr6AxMYDSANFEIJbXYtZ29vZ2xlSARSBXdhdGNoYOr_86Xm06e5UAw=/0/0/0/video.3gp");
//                HelpVideoDialogFragment dialogFragment = new HelpVideoDialogFragment();
//                dialogFragment.setArguments(argument);
//                dialogFragment.show(fragmentManager, "video");
                uri = Uri.parse(GlobalConst.MOVIE_INVITE);
                it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;
            case R.id.mainleft_help_guide_apply :
                uri = Uri.parse(GlobalConst.MOVIE_APPLY);
                it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;
            case R.id.mainleft_help_guide_canvas :
                uri = Uri.parse(GlobalConst.MOVIE_CANVAS);
                it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;
            case R.id.mainleft_help_guide_youtube :
                uri = Uri.parse(GlobalConst.MOVIE_CHANNEL);
                it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;


        }
    }
}