package com.knowlounge.gcm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.NotificationHandlerActivity;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.AndroidContext;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AndroidUtils;

import java.util.List;
import java.util.Random;

/**
 * Created by Minsu on 2016-02-16.
 */
public class GCMIntentService extends GcmListenerService {

    private static final String TAG = "GcmIntentService";
    private WenotePreferenceManager prefManager;



    @Override
    public void onMessageReceived(String from, Bundle data) {
        prefManager = WenotePreferenceManager.getInstance(this);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        boolean isAwake = isAwake();
        boolean pushAllowFlag = prefManager.getNotification_message();
        if(!pushAllowFlag) return;   // 푸시 수신 비허용 상태이면 리턴..

        // 푸시에서 데이터 추출..
        String title    = data.getString("title") == null ? "" : data.getString("title");
        String message  = data.getString("message") == null ? "" : data.getString("message");
        String roomCode = data.getString("code") == null ? "" : data.getString("code");
        String category = data.getString("category") == null ? "" : data.getString("category");
        int notId = Integer.parseInt(data.getString("notId") == null ? "0" : data.getString("notId"));

        int requestCode = new Random().nextInt();

        Log.d(TAG, "<onMessageReceived / Knowlounge> arrived notification signal data : " + data.toString());
        Log.d(TAG, "<onMessageReceived / Knowlounge> roomCode : " + roomCode);

        Intent startAppIntent = new Intent(this, NotificationHandlerActivity.class);
        startAppIntent.putExtra("code", roomCode);
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

//        Intent moveRoomIntent = new Intent(RoomActivity.activity, RoomSwitchActivity.class);
//        moveRoomIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        moveRoomIntent.putExtra("roomurl", KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode);

        Intent enterRoomIntent = new Intent(getApplicationContext(), RoomActivity.class);
        enterRoomIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        enterRoomIntent.putExtra("roomurl", KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode);

        Intent noActionIntent = new Intent();

        PendingIntent pendingIntent;

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notify_list);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{1000, 1000});

        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
        ComponentName topActivity = Info.get(0).topActivity;
        String topactivityname = topActivity.getShortClassName();
        Log.d(TAG, topactivityname);

        /*
            푸시 정책
            - 앱이 실행중이면 상태바의 푸시는 수신받지 않도록 해야 함
            -
         */
        switch (category) {

            case GlobalCode.NOTIFICATION_INVITE :
//                if (!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
//                    if(AndroidContext.instance().isRunning(RoomActivity.class)) {
//                        pendingIntent = PendingIntent.getActivity(this, requestCode, moveRoomIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
//                    } else {
//                        pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
//                    }
//                    notificationBuilder.setContentIntent(pendingIntent);
//                    notificationManager.notify(notId, notificationBuilder.build());
//                } else {
//                    sendNotification(data);
//                }

                if (topactivityname.equals(".RoomActivity")) {
                    Log.d(TAG, "RoomActivity running..");
                    pendingIntent = PendingIntent.getActivity(this, requestCode, noActionIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                } else if(topactivityname.equals(".MainActivity")) {
                    Log.d(TAG, "MainActivity running..");
                    pendingIntent = PendingIntent.getActivity(this, requestCode, enterRoomIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                } else {
                    pendingIntent = PendingIntent.getActivity(this, requestCode, startAppIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                }
                notificationBuilder.setContentIntent(pendingIntent);
                notificationManager.notify(notId, notificationBuilder.build());
                sendNotification(data);
                break;

            case GlobalCode.NOTIFICATION_REQUEST_JOIN :
                prefManager.updateReqjoinNotiBadgeCount(Integer.parseInt(roomCode)+"");

                if (!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
                    if (AndroidContext.instance().isRunning(RoomActivity.class)) {
                        pendingIntent = PendingIntent.getActivity(this, requestCode, noActionIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                    } else {
                        pendingIntent = PendingIntent.getActivity(this, requestCode, startAppIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                    }
                    notificationBuilder.setContentIntent(pendingIntent);
                    notificationManager.notify(notId, notificationBuilder.build());  // 상태바에 푸시 노티 띄우기
                } else {
                    sendNotification(data);
                }



//                if (AndroidContext.instance().isRunning(RoomActivity.class)) {
//                    pendingIntent = PendingIntent.getActivity(this, requestCode, noActionIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
//                } else {
//                    pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
//                }
//                notificationBuilder.setContentIntent(pendingIntent);
//
//                if (topactivityname.equals(".RoomActivity") && topactivityname.equals(".MainActivity")) {
//                    sendNotification(data);
//                } else {
//                    notificationManager.notify(notId, notificationBuilder.build());  // 상태바에 푸시 노티 띄우기
//                }
                break;

            case GlobalCode.NOTIFICATION_EXTEND_ROOM :
                if (!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
                    if (AndroidContext.instance().isRunning(RoomActivity.class)) {
                        pendingIntent = PendingIntent.getActivity(this, requestCode, noActionIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                    } else {
                        pendingIntent = PendingIntent.getActivity(this, requestCode, startAppIntent, PendingIntent.FLAG_ONE_SHOT);  // 일회성의 PendingIntent를 생성..
                    }
                    notificationBuilder.setContentIntent(pendingIntent);
                    notificationManager.notify(notId, notificationBuilder.build());
                } else {
                    sendNotification(data);
                }
                break;

            default :
                notificationManager.notify(notId, notificationBuilder.build());
        }

        // 스크린 OFF 상태일 때, 스크린을 ON 시켜주고 WakeLock 객체는 10초후 자동으로 release 시킴..
        if(!isAwake) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock screenWakeLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, "notification");
            screenWakeLock.acquire(10000);   // 10초후에 자동으로 release..
        }

        AndroidUtils.setBadge(getApplicationContext(), prefManager.updateNotiBadgeCount());

//        if(category != null) {
//            if (prefManager.getNotification_message()) {
//                Log.d(TAG, "Push category : " + category);
//
//                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notify_list);
//
//                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                        .setLargeIcon(bm)
//                        .setSmallIcon(R.drawable.ic_stat_notify)
//                        .setContentTitle(title)
//                        .setContentText(message)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setVibrate(new long[]{1000, 1000});
//
//                if (category.equals("invite")) {   // 수업 초대
//                    if (!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
//                        notificationBuilder.setContentIntent(pendingIntent);
//                        notificationManager.notify(notId, notificationBuilder.build());
//                    } else {
//                        sendNotification(data);
//                    }
//                } else if (category.equals("reqjoin")) {   // 수업 참여요청
//                    prefManager.updateReqjoinNotiBadgeCount(roomCode);
//                    if (!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
//                        notificationBuilder.setContentIntent(pendingIntent);
//                        notificationManager.notify(notId, notificationBuilder.build());
//                    } else {
//                        sendNotification(data);
//                    }
//                } else if (category.equals("extendroom")) {  // 수업 참여제한 해제 알림
//                    if (!AndroidUtils.isKnowloungeRunningCheck(getApplicationContext())) {
//                        notificationBuilder.setContentIntent(pendingIntent);
//                        notificationManager.notify(notId, notificationBuilder.build());
//                    } else {
//                        sendNotification(data);
//                    }
//                } else {
//                    notificationManager.notify(notId, notificationBuilder.build());
//                }
//                AndroidUtils.setBadge(getApplicationContext(), prefManager.updateNotiBadgeCount());
//            }
//        }
    }

//    public boolean isRunningProcess() {
//
//        boolean isRunning = false;
//
//        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//
//        String topActivityname = am.getRunningAppProcesses().get(0).processName;
//
//        if(topActivityname.equals(getPackageName()))
//        {
//            isRunning = true;
//        }
//        return isRunning;
//    }

//    private  boolean isKnowloungeRunningCheck(){
//        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//        String topactivityname;
//
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
//            topactivityname = am.getRunningAppProcesses().get(0).processName;
//        }else{
//            List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
//            ComponentName topActivity = Info.get(0).topActivity;
//            topactivityname = topActivity.getPackageName();
//        }
//
//        if(topactivityname.equals(getPackageName()))
//            return true;
//        else
//            return false;
//    }


    private void sendNotification(Bundle extras) {
        Intent pushReceiveIntent = new Intent(GcmRegistStatePreference.PUSH_RECEIVED);
        pushReceiveIntent.putExtra("push", extras);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushReceiveIntent);
    }


    private boolean isAwake() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isScreenOn();
        } else {
            return pm.isInteractive();
        }
    }
}
