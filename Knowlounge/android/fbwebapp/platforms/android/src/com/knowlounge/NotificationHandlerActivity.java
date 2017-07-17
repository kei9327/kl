package com.knowlounge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.knowlounge.login.SplashIntroActivity;

/**
 * Created by Minsu on 2016-02-19.
 */
public class NotificationHandlerActivity extends Activity {

    private final String TAG = "NotificationActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        Intent intent = getIntent();
        String roomCode = intent.getStringExtra("code");
//        String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
        Log.d(TAG, "roomCode : " + roomCode);

        Intent mainIntent = new Intent(NotificationHandlerActivity.this, SplashIntroActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.putExtra("code", roomCode);
        startActivity(mainIntent);
        finish();
    }
}
