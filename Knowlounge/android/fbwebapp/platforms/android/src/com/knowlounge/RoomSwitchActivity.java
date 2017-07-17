package com.knowlounge;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.knowlounge.apprtc.KlgeConnection;
import com.knowlounge.apprtc.KlgePeerWatcher;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.view.room.RoomActivity;
import com.wescan.alo.rtc.RtcChatSession;

/**
 * Created by Minsu on 2016-05-24.
 */
public class RoomSwitchActivity extends RtcSupportActivity {

    public static final String TAG = "RoomSwitchActivity";
    private WenotePreferenceManager prefManager;
    private View mCustomActionBar;
    private ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        prefManager = WenotePreferenceManager.getInstance(this);

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE :
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_PORTRAIT :
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
        }

        /*
        if (prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE) {
            switch (prefManager.getOrientation()){
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); break;
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE :
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT :
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT); break;
            }
        } else {
            switch (prefManager.getOrientation()) {
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE :
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT :
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); break;
            }
        }*/

        // 레이아웃 설정
        setContentView(R.layout.dummy_main_activity);
        Glide.with(this).load(R.drawable.gif_multi_board).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into((ImageView)findViewById(R.id.room_loading_img));
        findViewById(R.id.loading_layer).setBackgroundColor(Color.parseColor("#DDFFFFFF"));


//        Toolbar roomToolbar = (Toolbar) findViewById(R.id.room_toolbar);
//        setSupportActionBar(roomToolbar);

//        if(prefManager.getDeviceType() == GlobalConst.DEVICE_TABLET) {
//            mCustomActionBar = getLayoutInflater().inflate(R.layout.actionbar_room_custom, null, false);
//
//            actionBar = getSupportActionBar();
//            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
//
//            actionBar.setCustomView(mCustomActionBar, params);
//            actionBar.setDisplayShowCustomEnabled(true);
//            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//
//            // Actionbar home button 설정
//            actionBar.setDefaultDisplayHomeAsUpEnabled(false);
//            actionBar.setHomeButtonEnabled(false);
//            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.setDisplayShowHomeEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(false);  // Actionbar title 설정
//            actionBar.setDisplayUseLogoEnabled(false);  // Actionbar logo 설정
//
//            Toolbar parent = (Toolbar) mCustomActionBar.getParent();
//            parent.setContentInsetsAbsolute(0, 0);
//            parent.setPadding(0, 0, 0, 0);
//
//            //actionBar.hide();
//        }else{
//
//            mCustomActionBar = getLayoutInflater().inflate(R.layout.actionbar_room_custom_phone, null, false);
//
//            actionBar = getSupportActionBar();
//            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
//
//            actionBar.setCustomView(mCustomActionBar, params);
//            actionBar.setDisplayShowCustomEnabled(true);
//            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//
//            // Actionbar home button 설정
//            actionBar.setDefaultDisplayHomeAsUpEnabled(false);
//            actionBar.setHomeButtonEnabled(false);
//            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.setDisplayShowHomeEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(false);  // Actionbar title 설정
//            actionBar.setDisplayUseLogoEnabled(false);  // Actionbar logo 설정
//
//            Toolbar parent = (Toolbar) mCustomActionBar.getParent();
//            parent.setContentInsetsAbsolute(0, 0);
//            parent.setPadding(0, 0, 0, 0);
//        }

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("roomurl")) {
            final RoomSpec roomSpec = intent.getParcelableExtra("arguments");
            final String roomCode = intent.hasExtra("roomcode") ? intent.getStringExtra("roomcode") : "";
            final String roomUrl = intent.getStringExtra("roomurl");
            final Bundle extraParam = intent.hasExtra("extra") ? intent.getBundleExtra("extra") : null;   // 판서 폴을 받아서 이동할 때 참조하는 값
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent moveResponseIntent = new Intent(RoomSwitchActivity.this, RoomActivity.class);
                    moveResponseIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    moveResponseIntent.putExtra("roomurl", roomUrl);
                    moveResponseIntent.putExtra("mode", GlobalConst.MOVE_ROOM_MODE);
                    moveResponseIntent.putExtra("extra", extraParam);
                    moveResponseIntent.putExtra("arguments", roomSpec);
                    startActivity(moveResponseIntent);

                    overridePendingTransition(0,0) ;
                    finish();
                }
            }, 2000);
        }
    }

    @Override
    public KlgePeerWatcher getPeerWatcher() {
        return null;
    }

    @Override
    protected RtcChatSession newChat(KlgeConnection connection) {
        return null;
    }
}
