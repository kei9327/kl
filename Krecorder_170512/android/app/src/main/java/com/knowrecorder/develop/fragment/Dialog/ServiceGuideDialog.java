package com.knowrecorder.develop.fragment.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;


/**
 * Created by we160303 on 2017-02-27.
 */

public class ServiceGuideDialog extends DialogFragment {

    View rootView;

    ViewPager sgViewPager;
    ImageView textImage;
    TextView sgSentence;
    ImageView guidePage1;
    ImageView guidePage2;
    ImageView guidePage3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.rb_dialog_service_guid, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setBindViewId();

//        Glide.with(getActivity()).load(R.drawable.img_main_popup_01_tablet).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(textImage);
        sgViewPager.setAdapter(new ViewPagerAdapter(getActivity()));
        sgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                guidePage1.setImageResource(R.drawable.ico_pagecontrol);
                guidePage2.setImageResource(R.drawable.ico_pagecontrol);
                guidePage3.setImageResource(R.drawable.ico_pagecontrol);

                switch (position) {
                    case 0:
                        rootView.setBackgroundColor(getResources().getColor(R.color.guidePage1));
                        guidePage1.setImageResource(R.drawable.ico_pagecontrol_s);
                        break;

                    case 1:
                        rootView.setBackgroundColor(getResources().getColor(R.color.guidePage2));
                        guidePage2.setImageResource(R.drawable.ico_pagecontrol_s);
                        break;

                    case 2:
                        rootView.setBackgroundColor(getResources().getColor(R.color.guidePage3));
                        guidePage3.setImageResource(R.drawable.ico_pagecontrol_s);
                        break;
                }

                setServiceGuideText(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;

        int dialogWidth = (int)PixelUtil.getInstance().convertDpToPixel(600);
        int dialogHeight = (int)PixelUtil.getInstance().convertDpToPixel(530);

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);


    }

    private void setBindViewId() {
        sgViewPager = (ViewPager) rootView.findViewById(R.id.sg_view_pager);
        sgSentence = (TextView) rootView.findViewById(R.id.sg_sentence);
        guidePage1 = (ImageView) rootView.findViewById(R.id.sg_page_1);
        guidePage2 = (ImageView) rootView.findViewById(R.id.sg_page_2);
        guidePage3 = (ImageView) rootView.findViewById(R.id.sg_page_3);

    }

    private void setServiceGuideText(int position) {
        String guideText = "";
        switch (position){
            case 0 :
                if(!KnowRecorderApplication.isPhone) {
                    guideText = "";
                }else{

                }
                break;
            case 1 :
                if(!KnowRecorderApplication.isPhone) {
                    guideText = "";
                }else{

                }
                break;
            case 2 :
                if(!KnowRecorderApplication.isPhone) {
                    guideText = "";
                }else{

                }
                break;
        }
        sgSentence.setText(guideText);
    }


    class ViewPagerAdapter extends PagerAdapter{

        int[] guideGIF = new int[] { R.drawable.img_main_popup_01_tablet, R.drawable.img_main_popup_02_tablet, R.drawable.img_main_popup_03_tablet};
        Context mContext;

        public ViewPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return guideGIF.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView imageView = new ImageView(mContext);
            Glide.with(mContext).load(guideGIF[position]).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);

            ((ViewPager) container).addView(imageView, 0);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ImageView) object);
        }
    }
}
