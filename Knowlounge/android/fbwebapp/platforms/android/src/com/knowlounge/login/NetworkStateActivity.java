package com.knowlounge.login;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.knowlounge.base.BaseActivity;
import com.knowlounge.receiver.NetworkStateReceiver;
import com.knowlounge.util.logger.AppLog;

/**
 * Created by we160303 on 2016-07-20.
 */
public abstract class NetworkStateActivity extends BaseActivity {

    private NetworkStateReceiver networkStateReceiver;
    private int currentNetworkStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkStateReceiver = new NetworkStateReceiver(this);  // 데이터 네트워크 체크 (WiFi / LTE)

        NetworkStateReceiver.OnChangeNetworkStatusListener networkChangeListener = new NetworkStateReceiver.OnChangeNetworkStatusListener() {
            @Override
            public void onChange(int status) {
                AppLog.d(AppLog.TAG, "onChange fire.. / status=" + status);
                currentNetworkStatus = status;
            }
        };
        networkStateReceiver.setOnChangeNetworkStatusListener(networkChangeListener); // 네트워크 상태 체인지 리스너 등록

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }



    protected int getCurrentNetworkStatus() {
        return currentNetworkStatus;
    }


}
