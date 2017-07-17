package com.knowlounge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.knowlounge.fragment.RoomLeftmenuFragment;
import com.knowlounge.fragment.StarShopFragment;
import com.knowlounge.inapp.InAppRootDispatcher;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Minsu on 2016-03-24.
 */
public class RoomLeftNavActivity extends AppCompatActivity implements StarShopFragment.InAppBillingListener {

    private final String TAG = "RoomLeftNavActivity";

    public static DrawerLayout mDrawerLayout;
    public static LinearLayout mDrawerContent;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private WenotePreferenceManager prefManager;

    private InAppRootDispatcher mDispatcher;

    private InAppRootDispatcher.InAppListener inAppListener = new InAppRootDispatcher.InAppListener() {
        @Override
        public void onFinished() {
            // 실제 구매가 완료가 되었을 때, UI 또는 기타 처리는 여기에서 하면 됩니다
            getStarBalance();
        }
    };

    private boolean hasStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_activity_left_nav_drawer);

        Intent roomIntent = getIntent();
        hasStar = roomIntent.hasExtra("star");
        final String roomCode = roomIntent.getStringExtra("roomcode");
        final String roomTitle = roomIntent.getStringExtra("roomtitle");


        //InApp 처리 관련 위임
        mDispatcher = new InAppRootDispatcher(this);
        mDispatcher.setListener(inAppListener);
        mDispatcher.onCreate();


        mDrawerLayout = (DrawerLayout)findViewById(R.id.room_left_nav_drawer);
        mDrawerContent = (LinearLayout)findViewById(R.id.room_left_drawer_panel);

        prefManager = WenotePreferenceManager.getInstance(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.activity_name, R.string.activity_name) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "onDrawerOpened");
                mDrawerLayout.openDrawer(mDrawerContent);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "onDrawerClosed");
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
            }
        };

        mDrawerLayout.setDrawerListener(toggle);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerLayout.openDrawer(Gravity.LEFT);


        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(hasStar){
            Bundle argument = new Bundle();
            argument.putString("through","notstar");
            StarShopFragment starShopFragment = new StarShopFragment();
            starShopFragment.setArguments(argument);
            fragmentTransaction.replace(R.id.roomleft_container, starShopFragment );
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else {
            Bundle argument = new Bundle();
            argument.putString("roomcode", roomCode);
            argument.putString("roomtitle", roomTitle);

            RoomLeftmenuFragment roomLeftMenuFragment = new RoomLeftmenuFragment();
            roomLeftMenuFragment.setArguments(argument);
            fragmentTransaction.replace(R.id.roomleft_container, roomLeftMenuFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        //앱 실행 상태 체크
        prefManager.setAppOnOff(false);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mDispatcher.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStarShopItemClicked(String productId) {
        mDispatcher.start(productId);
    }

    private void getStarBalance() {
        String url = "user/currency?userAccessToken=" + CommonUtils.urlEncode(prefManager.getSiAccessToken());
        RestClient.getSiPlatform(url, false, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int starCount = response.getJSONObject("balance").getJSONObject("currency").getJSONObject("knowlounge").getInt("value");
                    int savedStarCount = prefManager.getUserStarBalance();
                    if(starCount != savedStarCount) {
                        prefManager.setUserStarBalance(starCount);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "SI platform getBalance onFailure - " + statusCode + ", " + responseString);
            }

        });
    }
}
