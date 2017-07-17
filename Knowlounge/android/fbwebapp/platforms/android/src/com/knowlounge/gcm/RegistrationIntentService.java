package com.knowlounge.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;

import java.io.IOException;

/**
 * Created by Minsu on 2016-02-16.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistIntentService";

    private WenotePreferenceManager prefManager;

    public RegistrationIntentService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefManager = WenotePreferenceManager.getInstance(getApplicationContext());

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GcmRegistStatePreference.REGISTRATION_PROCESSING));
        String token = null;
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String svrFlag = getString(R.string.svr_flag);
                String senderId = getString(getResources().getIdentifier("gcm_sender_id_" + svrFlag, "string", getPackageName()));
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                token = instanceID.getToken(senderId, scope, null);
                Log.d(TAG, "GCM Push token : " + token);
                prefManager.setDeviceToken(token);
            }
        } catch(IOException e) {
            String errMsg = e.getMessage();
            Log.d(TAG, "onHandleIntent" + errMsg);
            if(errMsg.indexOf("SERVICE_NOT_AVAILABLE") > -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_common_error), Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
        }

        Intent completeIntent = new Intent(GcmRegistStatePreference.REGISTRATION_COMPLETE);
        completeIntent.putExtra("registrationId", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(completeIntent);
    }
}
