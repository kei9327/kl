package com.knowrecorder.Utils;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionChecker {

    private String[] mMediaPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
    };

    public boolean check(Activity activity, int requestCode)
    {
        List<String> deniedPermission = new ArrayList<>();
        for(String strPermission : mMediaPermissions){
            int permissionCheck = ContextCompat.checkSelfPermission(activity, strPermission);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, strPermission)) {
                    //사용자가 취소한 권한, 취소하지말라는 안내.
                    deniedPermission.add(strPermission);
                } else {
                    //처음부터 받지 않은 권한
                    deniedPermission.add(strPermission);
                }
            }
        }
        if(!deniedPermission.isEmpty()) {
            String[] requestArray = deniedPermission.toArray(new String[deniedPermission.size()]);
            ActivityCompat.requestPermissions(activity, requestArray, requestCode);
        }
        if(deniedPermission.size() == 0) {
            return true;
        }else{
            return false;
        }
    }
}
