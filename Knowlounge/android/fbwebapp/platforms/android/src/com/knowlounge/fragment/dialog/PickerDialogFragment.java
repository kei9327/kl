package com.knowlounge.fragment.dialog;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.NumberPicker;

import com.knowlounge.R;

/**
 * Created by we160303 on 2016-04-20.
 */
public class PickerDialogFragment extends AppCompatDialogFragment {
    View rootView;
    NumberPicker picker;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        rootView = inflater.inflate(R.layout.piker_dialog, container);

        picker = (NumberPicker) rootView.findViewById(R.id.picker);
        picker.setMaxValue(1);
        picker.setMinValue(0);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setDisplayedValues(new String[]{"전체공개", "비공개"});

        return rootView ;
    }
}
