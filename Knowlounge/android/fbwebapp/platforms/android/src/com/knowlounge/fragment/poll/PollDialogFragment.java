package com.knowlounge.fragment.poll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by Mansu on 2016-12-05.
 */

public class PollDialogFragment extends Fragment {

    public WenotePreferenceManager prefManager;
    public View rootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        prefManager = WenotePreferenceManager.getInstance(context);
    }


    public void typeImgChecked(ImageView check, ImageView noncheck, @Nullable ImageView noncheck2, @Nullable ImageView noncheck3) {
        check.setImageResource(R.drawable.btn_checkbox_on);
        noncheck.setImageResource(R.drawable.btn_checkbox);
        if(noncheck2 != null)
            noncheck2.setImageResource(R.drawable.btn_checkbox);
        if(noncheck3 != null)
            noncheck3.setImageResource(R.drawable.btn_checkbox);
    }


    public void typeTextChecked(TextView check, TextView noncheck, @Nullable TextView noncheck2, @Nullable TextView noncheck3) {
        check.setTextColor(Color.parseColor("#5a5a5a"));
        check.setTypeface(null, Typeface.BOLD);

        noncheck.setTextColor(Color.parseColor("#969696"));
        noncheck.setTypeface(null, Typeface.NORMAL);
        if(noncheck2 != null) {
            noncheck2.setTextColor(Color.parseColor("#969696"));
            noncheck2.setTypeface(null, Typeface.NORMAL);
        }
        if(noncheck3 != null) {
            noncheck3.setTextColor(Color.parseColor("#969696"));
            noncheck3.setTypeface(null, Typeface.NORMAL);
        }
    }
}
