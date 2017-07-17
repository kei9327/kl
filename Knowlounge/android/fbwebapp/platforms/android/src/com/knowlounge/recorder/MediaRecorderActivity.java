package com.knowlounge.recorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.manager.WenotePreferenceManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mansu on 2016-10-25.
 * 안드로이드 스크린 레코딩 테스트를 위해 만든 엑티비티
 */

@TargetApi(21)
public class MediaRecorderActivity extends AppCompatActivity {

    private static final String TAG = "MediaRecorderActivity";
    private static final int SCREEN_RECORDER_REQUEST_CODE= 1000;

    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 1080;    // 720
    private static final int DISPLAY_HEIGHT = 1920;   // 1280

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;

    private boolean recordStarting = false;

    private static final int[] defaultResolution = {720, 1280};

    private static final int LD_240P = 350 * 1000;

    private static final int LD_360P = 700 * 1000;
    private static final int SD_480P = 1200 * 1000;
    private static final int HD_720P = 2500 * 1000;
    private static final int HD_1280P = 5000 * 1000;
    public WenotePreferenceManager prefManager;


    // MediaProjection 기능 활성화 여부 값
    public boolean recordEnabled = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = WenotePreferenceManager.getInstance(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult / requestCode : " + requestCode + ", resultCode : " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == SCREEN_RECORDER_REQUEST_CODE) {
//            if (resultCode != RESULT_OK) {  // 사용자가 권한을 허용해주지 않았습니다.
//                Log.d(TAG, "Screen Cast Permission Denied");
//                Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
//                //mToggleButton.setChecked(false);
//                RoomActivity.activity.recorderSwitch.setChecked(false);
//                return;
//            } else {
//                // 사용자가 권한을 허용해주었기에 mediaProjection을 사용할 수 있는 권한이 생기게 됩니다.
//                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//                if (mMediaProjection != null) {
//                    mMediaProjectionCallback = new MediaProjectionCallback();
//                    mMediaProjection.registerCallback(mMediaProjectionCallback, null);
//                    mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                            mMediaRecorder.getSurface(), null, null);
//                }
//                mMediaRecorder.start();
//                Log.d(TAG, "mMediaRecorder.start()");
//                //onToggleScreenShare(true);
//            }
//        }
    }

    public CompoundButton.OnCheckedChangeListener setOnRecorderSwitchListener() {
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onStartRecord(isChecked);
            }
        };
        return listener;
    }

    public void onStartRecord(boolean isCheck) {
        // 퍼미션 체크..
        if (ContextCompat.checkSelfPermission(MediaRecorderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(MediaRecorderActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MediaRecorderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MediaRecorderActivity.this, Manifest.permission.RECORD_AUDIO)) {
                mToggleButton.setChecked(false);
                Snackbar.make(findViewById(android.R.id.content), "Please enable Microphone and Storage permissions.", Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MediaRecorderActivity.this,
                                        new String[]{Manifest.permission
                                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                        REQUEST_PERMISSIONS);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(MediaRecorderActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
            }
        } else {   // 퍼미션을 다 만족하면..
            Log.d(TAG, "onStartRecord - Permission passed");
            onToggleScreenShare(isCheck);
        }
    }


    public void onToggleScreenShare(boolean isCheck) {
        Log.d(TAG, "onToggleScreenShare");
        if (isCheck) {
            initRecorder();
            shareScreen();
        } else {
            Log.d(TAG, "Stopping Recording");
            if(mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder = null;
            }
            stopScreenSharing();
        }
    }



    private void initRecorder() {
        Log.d(TAG, "initRecorder");
        try {
            String savePath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/record";

            File dir = new File(savePath);
            if(!dir.exists())
                dir.mkdirs();

            String roomTitle = prefManager.getRoomTitle();
            String fileName = roomTitle + "_" + String.valueOf(System.currentTimeMillis()) + ".mp4";
            //Visualizer audio = new Visualizer(0);
            if(mMediaRecorder == null) mMediaRecorder = new MediaRecorder();


            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(savePath + "/" + fileName);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            mMediaRecorder.setVideoEncodingBitRate(HD_720P);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation * 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            Log.d(TAG, "mMediaRecorder.prepare()");
//            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//            mMediaRecorder.setProfile(profile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void shareScreen() {
        Log.d(TAG, "shareScreen");
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), SCREEN_RECORDER_REQUEST_CODE);
            return;
        }
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
        mMediaRecorder.start();
        Log.d(TAG, "mMediaRecorder.start()");
    }


    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                //mMediaRecorder.release();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if(mMediaRecorder != null) {
            //mMediaRecorder.release(); //If used: mMediaRecorder object cannot be reused again
        }
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.d(TAG, "MediaProjection Stopped");

        Toast.makeText(getApplicationContext(), "녹화가 완료되었습니다.", Toast.LENGTH_SHORT).show();
    }
}
