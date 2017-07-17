package com.knowlounge;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.knowlounge.fragment.NoticeFragment;

/**
 * Created by we160303 on 2016-05-19.
 */
public class NoticeTabletActivity extends FragmentActivity{

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.klounge_notice_container);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        NoticeFragment noticeFragment = new NoticeFragment();
        fragmentTransaction.replace(R.id.notice_container, noticeFragment);
        fragmentTransaction.commit();
    }
}
