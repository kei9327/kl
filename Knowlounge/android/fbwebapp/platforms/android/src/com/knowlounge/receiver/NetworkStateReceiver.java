package com.knowlounge.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.knowlounge.KnowloungeApplication;

/**
 * Created by Minsu on 2016-05-26.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public final String TAG = "NetWorkStateReceiver";
    public final static int WIFI_STATE_DISABLED = 0x00;
    public final static int WIFI_STATE_DISABLING = WIFI_STATE_DISABLED + 1;
    public final static int WIFI_STATE_ENABLED = WIFI_STATE_DISABLING + 1;
    public final static int WIFI_STATE_ENABLING = WIFI_STATE_ENABLED + 1;
    public final static int WIFI_STATE_UNKNOWN = WIFI_STATE_ENABLING + 1;
    public final static int NETWORK_STATE_CONNECTED = WIFI_STATE_UNKNOWN + 1;
    public final static int NETWORK_STATE_CONNECTING = NETWORK_STATE_CONNECTED + 1;
    public final static int NETWORK_STATE_DISCONNECTED = NETWORK_STATE_CONNECTING + 1;
    public final static int NETWORK_STATE_DISCONNECTING = NETWORK_STATE_DISCONNECTED + 1;
    public final static int NETWORK_STATE_SUSPENDED = NETWORK_STATE_DISCONNECTING + 1;
    public final static int NETWORK_STATE_UNKNOWN = NETWORK_STATE_SUSPENDED + 1;
    public final static int NETWORK_ERROR = -1;

    public final static int NETWORK_CONNECTED = 1000;
    public final static int NETWORK_DISCONNECTED = -1000;

    private ConnectivityManager connectivityManager;
    private WifiManager mWifiManager = null;

    private NetworkInfo mobile = null;
    private NetworkInfo wifi = null;
    private OnChangeNetworkStatusListener mCallback = null;

    public interface OnChangeNetworkStatusListener {
        void onChange(int status);
    }

    public NetworkStateReceiver(){

    }

    public NetworkStateReceiver(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    public void setOnChangeNetworkStatusListener(OnChangeNetworkStatusListener listener) {
        mCallback = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(mCallback == null) {
            return;
        }

        String IntentAction = intent.getAction();
        Log.i(TAG, IntentAction);

        if (IntentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION) || IntentAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int iResult = getConnectivityStatus(context);
            Log.i(TAG, "iResult == " + iResult);
            if (iResult == 2 || iResult == 1) {
                KnowloungeApplication.isNetworkConnected = true;
                mCallback.onChange(NETWORK_CONNECTED);
            } else if (iResult == 3) {
                KnowloungeApplication.isNetworkConnected = false;
                mCallback.onChange(NETWORK_DISCONNECTED);
            } else {

            }
        } else
            return;
    }

    public  int getConnectivityStatus(Context context)
    {
        int disconnectedCount = 0;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (null != wifiNetwork || null != mobileNetwork)
        {
            if(null != wifiNetwork)
            {
                if ( wifiNetwork.getState() == NetworkInfo.State.CONNECTED )
                {
                    return 1;
                }
                else if ( wifiNetwork.getState() == NetworkInfo.State.CONNECTING)
                {

                    Log.d(TAG,"Wifi CONNECTING");
                }
                else if ( wifiNetwork.getState() == NetworkInfo.State.DISCONNECTED)
                {
                    Log.d(TAG,"Wifi DISCONNECTED");
                }
                else if ( wifiNetwork.getState() == NetworkInfo.State.DISCONNECTING)
                {

                    Log.d(TAG,"Wifi DISCONNECTING");
                }
                else if ( wifiNetwork.getState() == NetworkInfo.State.SUSPENDED)
                {

                    Log.d(TAG,"Wifi SUSPENDED");
                }
                else if ( wifiNetwork.getState() == NetworkInfo.State.UNKNOWN)
                {

                    Log.d(TAG,"Wifi UNKNOWN");
                }
            }
            if(null != mobileNetwork) {

                if ( mobileNetwork.getState() == NetworkInfo.State.CONNECTED)
                {
                    Log.d(TAG,"Mobile CONNECTED");
                    return 2;
                }
                else if ( mobileNetwork.getState() == NetworkInfo.State.CONNECTING)
                {

                    Log.d(TAG,"Mobile CONNECTING");
                }
                else if ( mobileNetwork.getState() == NetworkInfo.State.DISCONNECTED)
                {

                    Log.d(TAG,"Mobile DISCONNECTED");
                }
                else if ( mobileNetwork.getState() == NetworkInfo.State.DISCONNECTING)
                {

                    Log.d(TAG,"Mobile DISCONNECTING");
                }
                else if ( mobileNetwork.getState() == NetworkInfo.State.SUSPENDED)
                {

                    Log.d(TAG,"Mobile SUSPENDED");
                }
                else if ( mobileNetwork.getState() == NetworkInfo.State.UNKNOWN)
                {

                    Log.d(TAG,"Mobile UNKNOWN");
                }
            }
        }
        return 3;
    }
}
