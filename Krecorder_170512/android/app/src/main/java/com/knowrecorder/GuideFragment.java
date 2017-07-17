package com.knowrecorder;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GuideFragment extends Fragment {

    private int pageNumber;

    public static GuideFragment create(int pageNumber) {
        GuideFragment fragment = new GuideFragment();
        Bundle args = new Bundle();
        args.putInt("page", pageNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pageNumber = getArguments().getInt("page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.layout_viewpager, container, false);
        ImageView guideImage = (ImageView) rootView.findViewById(R.id.img_guide2);

        Drawable drawable = null;
        switch (pageNumber) {
            case 0:
                drawable = KnowRecorderApplication.isPhone ? getResources().getDrawable(R.drawable.img_serviceguide_ph_01) : getResources().getDrawable(R.drawable.img_service_guide_01);
                break;

            case 1:
                drawable = KnowRecorderApplication.isPhone ? getResources().getDrawable(R.drawable.img_serviceguide_ph_02) : getResources().getDrawable(R.drawable.img_service_guide_02);
                break;

            case 2:
                drawable = KnowRecorderApplication.isPhone ? getResources().getDrawable(R.drawable.img_serviceguide_ph_03) : getResources().getDrawable(R.drawable.img_service_guide_03);
                break;
        }

        guideImage.setImageDrawable(drawable);
        rootView.setTag(pageNumber);

        return rootView;
    }
}
